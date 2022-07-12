package com.openapi.comm.mail;

import java.util.List;

public class MailBoxConf {
    public String serverHost;
    public int serverPort;
    public boolean ssl;
    public String userName;
    public String password;
    public boolean supportIdle;
    public String clientName;
    public String clientVersion;

    public static MailBoxConf createQQImapConf(String userName, String password) {
        MailBoxConf conf = new MailBoxConf();
        conf.serverHost = "imap.qq.com";
        conf.serverPort = 993;
        conf.ssl = true;
        conf.userName = userName;
        conf.password = password;
        return conf;
    }

    public static MailBoxConf createOutlookImapConf(String userName, String password) {
        MailBoxConf conf = new MailBoxConf();
        conf.serverHost = "outlook.office365.com";
        conf.serverPort = 993;
        conf.ssl = true;
        conf.userName = userName;
        conf.password = password;
        return conf;
    }

    public static MailBoxConf create163ImapConf(String userName, String password) {
        MailBoxConf conf = new MailBoxConf();
        conf.serverHost = "imap.163.com";
        conf.serverPort = 993;
        conf.ssl = true;
        conf.userName = userName;
        conf.password = password;
        return conf;
    }

    public static MailBoxConf createImapConf(Mail mail) {
        MailBoxConf conf = new MailBoxConf();
        conf.serverHost = mail.getServerHost().replace("smtp", "imap");
        conf.serverPort = 993;
        conf.ssl = true;
        conf.userName = mail.getSenderUserName();
        conf.password = mail.getPassword();
        conf.clientName = "com.openapi.mail";
        return conf;
    }
}
