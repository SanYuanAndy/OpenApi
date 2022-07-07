package com.openapi.comm.mail;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.openapi.comm.utils.LogUtil;
import com.sun.mail.imap.IMAPFolder;

import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

public class MailBox {
    private static final String TAG = MailBox.class.getSimpleName();
    private Store mStore = null;
    private Folder mFolder = null;
    private Handler mWorkHandler = null;
    private static final int CMD_CONNECT = 1000;
    private static final int CMD_FETCH = 1001;

    public MailBox() {
        HandlerThread thread = new HandlerThread("MaiBox");
        thread.start();
        mWorkHandler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case CMD_CONNECT:
                        connect();
                        break;
                    case CMD_FETCH:
                        fetch();
                        break;
                }
            }
        };

        mWorkHandler.sendEmptyMessage(CMD_CONNECT);
    }

    private Store open() {
        Store store = null;
        try {
            Properties props = System.getProperties();
            String server = "pop.qq.com";
            boolean enableSSL = true;
            int port = enableSSL ? 995 : 110;
            props.put("mail.pop3.host", server);
            props.put("mail.pop3.ssl.enable", enableSSL);
            props.put("mail.pop3.port", port);
            Session session = Session.getDefaultInstance(props, null);
            store = session.getStore("pop3");
        } catch (Exception e) {

        } finally {
            return store;
        }

    }

    private Store open2() {
        Store store = null;
        try {
            Properties props = System.getProperties();
            String server = "imap.qq.com";
            boolean enableSSL = true;
            int port = enableSSL ? 993 : 143;

            props.put("mail.imap.host", server);
            props.put("mail.imap.ssl.enable", enableSSL);
            props.put("mail.imap.port", port);
            Session session = Session.getDefaultInstance(props, null);
            store = session.getStore("imap");
        } catch (Exception e) {

        } finally {
            return store;
        }

    }

    private void connect() {
        try {
            String userName = "765828478";
            String password = "wgqpnvikrnfobfah";
            final Store store = open2();

            store.addConnectionListener(new ConnectionListener() {
                @Override
                public void opened(ConnectionEvent e) {
                    LogUtil.e(TAG, "opened:" + e);
                }

                @Override
                public void disconnected(ConnectionEvent e) {
                    LogUtil.e(TAG, "disconnected:" + e);
                }

                @Override
                public void closed(ConnectionEvent e) {
                    LogUtil.e(TAG, "closed:" + e);
                }
            });

            store.connect(userName, password);
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);
            folder.addMessageCountListener(new MessageCountAdapter() {
                @Override
                public void messagesAdded(MessageCountEvent e) {
                    LogUtil.e(TAG, "messagesAdded:" + e);
                }
            });
            mStore = store;
            mFolder = folder;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ((IMAPFolder) mFolder).idle();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            fetch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetch() {
        Store store = mStore;
        if (store == null) {
            return;
        }

        try {
            Message messages[] = mFolder.getMessages();
            if (messages.length > 0) {
                Log.d(TAG, "Messages's length: " + messages.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

