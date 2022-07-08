package com.openapi.comm.mail;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.openapi.comm.utils.LogUtil;
import com.openapi.comm.utils.NetTools;
import com.sun.mail.imap.IMAPFolder;

import java.util.Properties;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.InternetAddress;

public class MailBox {
    private static MailBox sInstance = new MailBox();
    private static final String TAG = MailBox.class.getSimpleName();
    private Store mStore = null;
    private Folder mFolder = null;
    private Handler mWorkHandler = null;
    private Handler mIdleHandler = null;
    private static final int CMD_CONNECT = 1000;
    private static final int CMD_FETCH = 1001;
    private static final int CMD_RECONNECT = 1002;
    private static final int CMD_NET_STATE_CHANGED = 1010;

    private static final int CMD_IDLE = 1100;
    private MailBoxConf mMailBoxConf;
    private boolean mNetConnected;
    private INewMailListener mNewMailListener;


    private MailBox() {

    }

    public static MailBox getBox() {
        return sInstance;
    }

    public static interface INewMailListener {
        void onNewMailReceived(String subject, String sender);
    }

    public void init(Context context, MailBoxConf conf, INewMailListener listener) {
        if (conf == null) {
            return;
        }

        if (isInit()) {
            return;
        }

        mMailBoxConf = conf;
        mNewMailListener = listener;
        HandlerThread thread = new HandlerThread("MaiBox");
        thread.start();
        mWorkHandler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                handleCmdMsg(msg);
            }
        };
        HandlerThread idleThread = new HandlerThread("Idle");
        idleThread.start();
        mIdleHandler = new Handler(idleThread.getLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case CMD_IDLE:
                        idle();
                        break;
                }
            }
        };

        NetTools.getInstance().init(context);
        mNetConnected = NetTools.isConnected(context);
        NetTools.getInstance().addListener(new NetTools.INetListener() {
            @Override
            public void onChanged(boolean isConnected, int type) {
                android.os.Message message = android.os.Message.obtain();
                message.what = CMD_NET_STATE_CHANGED;
                message.arg1 = isConnected ? 1 : 0;
                message.arg2 = type;
                mWorkHandler.sendMessageDelayed(message, 500);
            }
        });

        mWorkHandler.sendEmptyMessage(CMD_CONNECT);
    }

    public void fetchAll() {
        if (!isInit()) {
            return;
        }
        mWorkHandler.sendEmptyMessageDelayed(CMD_FETCH, 0);
    }

    private void handleCmdMsg(android.os.Message msg) {
        if (msg == null) {
            return;
        }

        switch (msg.what) {
            case CMD_CONNECT:
                connect();
                break;
            case CMD_FETCH:
                fetch();
                break;
            case CMD_RECONNECT:
                reConnect();
                break;
            case CMD_NET_STATE_CHANGED:
                onNetChanged(msg.arg1 == 1, msg.arg2);
                break;
        }
    }

    private boolean isInit() {
        return mMailBoxConf != null;
    }

    private void connect() {
        if (!mNetConnected) {
            LogUtil.e(TAG, "network is disconnected");
            return;
        }

        try {
            LogUtil.d(TAG, "connect mail box...");
            Properties props = System.getProperties();
            props.put("mail.imap.host", mMailBoxConf.serverHost);
            props.put("mail.imap.ssl.enable", mMailBoxConf.ssl);
            props.put("mail.imap.port", mMailBoxConf.serverPort);
            Session session = Session.getDefaultInstance(props, null);

            Store store = session.getStore("imap");
            store.connect(mMailBoxConf.userName, mMailBoxConf.password);
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            folder.addMessageCountListener(new MessageCountAdapter() {
                @Override
                public void messagesAdded(MessageCountEvent e) {
                    LogUtil.e(TAG, "messagesAdded:" + e);
                    onNewMailReceive(e);
                }
            });
            mStore = store;
            mFolder = folder;
            startIdle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startIdle() {
        mIdleHandler.removeMessages(CMD_IDLE);
        mIdleHandler.sendEmptyMessageDelayed(CMD_IDLE, 0);
    }

    private void idle() {
        while (true) {
            Folder folder = mFolder;
            if (folder == null) {
                    break;
            }
            LogUtil.e(TAG, "idle...");
            try {
                ((IMAPFolder) folder).idle();
            } catch (Exception e) {
                e.printStackTrace();
                onError(e);
                break;
            }
            LogUtil.e(TAG, "idle end");
        }

    }

    private void onError(Exception e) {
        if (e instanceof FolderClosedException) {
            mWorkHandler.sendEmptyMessageDelayed(CMD_RECONNECT, 3000);
        }
    }

    private void onNetChanged(boolean isConnected, int type) {
        boolean oldConnected = mNetConnected;
        mNetConnected = isConnected;
        if (oldConnected != isConnected && isConnected) {
            connect();
        }
    }

    private void onNewMailReceive(MessageCountEvent event) {
        Message[] messages = event.getMessages();
        if (messages == null) {
            return;
        }
        LogUtil.d(TAG, "onNewMailReceive begin");
        for (Message message : messages) {
            try {
                String subject = message.getSubject();
                InternetAddress address = (InternetAddress) message.getFrom()[0];
                INewMailListener listener = mNewMailListener;
                if (listener != null) {
                    listener.onNewMailReceived(subject, address.getAddress());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LogUtil.d(TAG, "onNewMailReceive end");
    }

    private void reConnect() {
        reset();
        connect();
    }

    private void reset() {
        Folder folder = mFolder;
        mFolder = null;
        if (folder != null) {
            if (folder.isOpen()) {
                try {
                    folder.close(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Store store = mStore;
        mStore = null;
        if (store != null) {
            if (store.isConnected()) {
                try {
                    store.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void fetch() {
        LogUtil.e(TAG, "fetch...");
        Store store = mStore;
        if (store == null) {
            return;
        }

        try {
            Message messages[] = mFolder.getMessages();
            if (messages != null) {
                Log.d(TAG, "Messages length: " + messages.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

