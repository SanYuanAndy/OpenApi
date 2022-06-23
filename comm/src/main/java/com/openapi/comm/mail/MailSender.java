package com.openapi.comm.mail;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

class MailSender {

    public static boolean send(Mail mail) {
        boolean ret = false;
        try {
            Transport.send(createMailMessage(mail));
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static Message createMailMessage(Mail mail) {

        MimeMessage message = null;
        Properties properties = mail.getProperties();
        try {
            Authenticator authenticator = new MailAuthenticator(mail.getSenderUserName(), mail.getPassword());
            Session sendMailSession = Session.getInstance(properties, authenticator);

            message = new MimeMessage(sendMailSession);

            Address from = new InternetAddress(mail.getFrom());
            message.setFrom(from);

            List<String> receivers = mail.getTo();
            for (String receiver : receivers) {
                Address to = new InternetAddress(receiver);
                message.addRecipient(Message.RecipientType.TO, to);
            }

            message.setSubject(mail.getSubject());

            MimeMultipart multipart = new MimeMultipart();

            MimeBodyPart text = new MimeBodyPart();
            text.setContent(mail.getContent(), "text/html;charset=UTF-8");
            multipart.addBodyPart(text);

            MimeBodyPart attach = new MimeBodyPart();
            for (String fileName : mail.getAttachFileNames()) {
                File file = new File(fileName);
                FileDataSource ds = new FileDataSource(file);
                DataHandler dh = new DataHandler(ds);
                attach.setDataHandler(dh);
                attach.setFileName(MimeUtility.encodeText(dh.getName()));
                multipart.addBodyPart(attach);
            }
            multipart.setSubType("mixed");

            message.setContent(multipart);
            message.saveChanges();

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 返回生成的邮件
        return message;
    }

    public static class MailAuthenticator extends Authenticator {
        private String mUserName;
        private String mPassword;

        public MailAuthenticator(String userName, String password) {
            mUserName = userName;
            mPassword = password;
        }


        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(mUserName, mPassword);
        }
    }
}
