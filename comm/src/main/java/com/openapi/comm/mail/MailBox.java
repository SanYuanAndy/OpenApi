package com.openapi.comm.mail;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.openapi.comm.utils.CommUtils;
import com.openapi.comm.utils.LogUtil;
import com.openapi.comm.utils.NetTools;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

import java.util.HashMap;
import java.util.Map;
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
    private static final int CMD_CLOSE = 1003;
    private static final int CMD_NET_STATE_CHANGED = 1010;

    private static final int CMD_IDLE = 1100;
    private static final int CMD_NOOP = 1101;

    private static final int NOOP_DURATION_MILL = 30 * 1000;
    private static final int KEEP_ALIVE_DURATION_MILL = 3 * 60 * 1000;
    private static final int KEEP_ALIVE_DURATION_COUNT_MAX = KEEP_ALIVE_DURATION_MILL / NOOP_DURATION_MILL;

    private MailBoxConf mMailBoxConf;
    private boolean mNetConnected;
    private INewMailListener mNewMailListener;
    private boolean mOperating = false;
    private Map<String, String> mClientParams = new HashMap<>();
    private int mNoopCount = 0;


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
                    case CMD_NOOP:
                        noop();
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

        String clientName = TextUtils.isEmpty(mMailBoxConf.clientName) ?
                context.getPackageName() : mMailBoxConf.clientName;
        String clientVersion = TextUtils.isEmpty(mMailBoxConf.clientVersion) ?
                CommUtils.getVersionName(context) : mMailBoxConf.clientVersion;
        mClientParams.put("name", clientName);
        mClientParams.put("version", clientVersion);

        mWorkHandler.sendEmptyMessage(CMD_CONNECT);
    }

    public void fetchAll() {
        if (!isInit()) {
            return;
        }
        mWorkHandler.sendEmptyMessageDelayed(CMD_FETCH, 0);
    }

    public void close() {
        if (!isInit()) {
            return;
        }
        mWorkHandler.sendEmptyMessageDelayed(CMD_CLOSE, 0);
    }

    public void restart() {
        if (!isInit()) {
            return;
        }
        mWorkHandler.sendEmptyMessageDelayed(CMD_CONNECT, 0);
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
            case CMD_CLOSE:
                reset();
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
            props.put("mail.debug", "true");
            Session session = Session.getDefaultInstance(props, null);

            Store store = session.getStore("imap");
            store.connect(mMailBoxConf.userName, mMailBoxConf.password);
            mStore = store;
            Folder folder = store.getFolder("INBOX");

            ((IMAPFolder) folder).doCommand(new IMAPFolder.ProtocolCommand() {
                @Override
                public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                    protocol.id(mClientParams);
                    return null;
                }
            });

            folder.open(Folder.READ_WRITE);
            folder.addMessageCountListener(new MessageCountAdapter() {
                @Override
                public void messagesAdded(MessageCountEvent e) {
                    LogUtil.e(TAG, "messagesAdded:" + e);
                    onNewMailReceive(e);
                }
            });
            mFolder = folder;
            startIdle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startIdle() {
        LogUtil.e(TAG, "startIdle...");
        if (mMailBoxConf.supportIdle) {
            mIdleHandler.removeMessages(CMD_IDLE);
            mIdleHandler.sendEmptyMessageDelayed(CMD_IDLE, 0);
        } else {
            mIdleHandler.removeMessages(CMD_NOOP);
            mIdleHandler.sendEmptyMessageDelayed(CMD_NOOP, NOOP_DURATION_MILL);
        }
    }

    private void noop() {
        Folder folder = mFolder;
        if (folder == null) {
            return;
        }
        mIdleHandler.removeMessages(CMD_NOOP);
        mNoopCount++;
        LogUtil.e(TAG, "noop...:" + mNoopCount);
        try {
            ((IMAPFolder) folder).doCommand(new IMAPFolder.ProtocolCommand() {
                @Override
                public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                    protocol.noop();
                    return null;
                }
            });
            if (mNoopCount % KEEP_ALIVE_DURATION_COUNT_MAX == 0) {
                ((IMAPFolder) folder).doCommand(new IMAPFolder.ProtocolCommand() {
                    @Override
                    public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                        // protocol.id(mClientParams);
                        protocol.simpleCommand("CAPABILITY", null);
                        return null;
                    }
                });
            }

            mIdleHandler.sendEmptyMessageDelayed(CMD_NOOP, NOOP_DURATION_MILL);
        } catch (Exception e) {
            e.printStackTrace();
            onError(e);
        }
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
            synchronized (mIdleHandler) {
                while (mOperating) {
                    try {
                        mIdleHandler.wait(500);
                    } catch (Exception e) {

                    }
                }
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
        boolean ret = reset();
        mWorkHandler.sendEmptyMessageDelayed(CMD_CONNECT, ret ? 30*1000 : 0);
    }

    private boolean reset() {
        boolean reallyClosed = false;
        Folder folder = mFolder;
        mFolder = null;
        if (folder != null) {
            try {
                folder.close(false);
                reallyClosed = true;
            } catch (Exception e) {
                    e.printStackTrace();
            }
        }

        Store store = mStore;
        mStore = null;
        if (store != null) {
            try {
                store.close();
                reallyClosed = true;
            } catch (Exception e) {
                    e.printStackTrace();
            }
        }
        LogUtil.e(TAG, "reset :" + reallyClosed);
        return reallyClosed;
    }

    private void fetch() {
        LogUtil.e(TAG, "fetch...");
        Store store = mStore;
        if (store == null) {
            return;
        }

        mOperating = true;

        try {
            Message messages[] = mFolder.getMessages();
            if (messages != null) {
                Log.d(TAG, "Messages length: " + messages.length + ", " + mFolder.hasNewMessages());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mOperating = false;
        synchronized (mIdleHandler) {
            mIdleHandler.notifyAll();
        }
    }
}

