package com.openapi.comm.mail;

import java.util.List;

public class MailBoxConf {
    public String serverHost;
    public int serverPort;
    public boolean ssl;
    public String userName;
    public String password;

    public static MailBoxConf createQQImapConf(String userName, String password) {
        MailBoxConf conf = new MailBoxConf();
        conf.serverHost = "imap.qq.com";
        conf.serverPort = 993;
        conf.ssl = true;
        conf.userName = userName;
        conf.password = password;
        return conf;
    }
}
