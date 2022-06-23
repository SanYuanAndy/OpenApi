package com.openapi.comm.mail;

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

    public boolean send() {
        return MailSender.send(this);
    }

}
