package com.openapi.comm.mail;

import android.content.Context;
import android.text.TextUtils;

import com.openapi.comm.utils.ZipUtils;

import java.io.File;
import java.util.List;
import java.util.Properties;

public class Mail {
    private String serverHost;
    private String serverPort;
    private String senderUserName;
    private String password;
    private boolean auth;
    private String from;
    private List<String> to;
    private String subject;
    private String content;
    private List<String> attachFileNames;
    private String attachPassword;
    private String attachName;

    public Properties getProperties() {
        Properties p = new Properties();
        p.put("mail.smtp.host", serverHost);
        p.put("mail.smtp.port", serverPort);
        p.put("mail.smtp.auth", auth ? "true" : "false");
        return p;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getSenderUserName() {
        return senderUserName;
    }

    public void setSenderUserName(String senderUserName) {
        this.senderUserName = senderUserName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getAttachFileNames() {
        return attachFileNames;
    }

    public void setAttachFileNames(List<String> attachFileNames) {
        this.attachFileNames = attachFileNames;
    }

    public String getAttachPassword() {
        return attachPassword;
    }

    public void setAttachPassword(String attachPassword) {
        this.attachPassword = attachPassword;
    }

    public String getAttachName() {
        return attachName;
    }

    public void setAttachName(String attachName) {
        this.attachName = attachName;
    }

    public boolean send(Context context) {
        check(context);
        return MailSender.send(this);
    }

    private void check(Context context) {
        if (!TextUtils.isEmpty(attachPassword)) {
            if (context != null) {
                encrypt(context.getCacheDir().getPath(), attachPassword);
            }
        }
    }

    private void encrypt(String zipTmpDir, String password) {
        if (attachFileNames != null && !attachFileNames.isEmpty()) {
            if (TextUtils.isEmpty(attachName)) {
                attachName = new File(attachFileNames.get(0)).getName();
            }
            File tmpZipFile = new File(zipTmpDir,  attachName + ".zip");
            boolean ret = ZipUtils.zip(attachFileNames, tmpZipFile.getPath(), password);
            if (ret) {
                attachFileNames.clear();
                attachFileNames.add(tmpZipFile.getPath());
            }
        }
    }

}
