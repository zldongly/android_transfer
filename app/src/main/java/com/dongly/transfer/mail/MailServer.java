package com.dongly.transfer.mail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailServer {
    private String host;
    private int port;

    private String email;
    private String password;

    private static MailServer instance = new MailServer(); // 单例

    private MailServer() {
    }

    public static MailServer getInstance() {
        return instance;
    }

    public MailServer setHost(String host) {
        this.host = host;
        return this;
    }

    public MailServer setPort(int port) {
        this.port = port;
        return this;
    }

    public MailServer setEmail(String email) {
        this.email = email;
        return this;
    }

    public MailServer setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public String toString() {
        return "MailServer{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    private Properties getProperties() {
        Properties p = new Properties();
        //p.put("mail.pop.host", this.host);
        p.put("mail.smtp.host", this.host);
        p.put("mail.smtp.port", this.port);
        p.put("mail.smtp.auth", "true");//设置为true 才被允许，默认false

        return p;
    }

    public boolean sendMail(final MailInfo info) {
        Properties pro = getProperties();
        // 根据邮件会话属性和密码验证器构造一个发送邮件的session
        Session sendMailSession = Session.getDefaultInstance(pro, new MailAuthenticator(email, password));

        try {
            // 根据session创建一个邮件消息
            Message mailMessage = new MimeMessage(sendMailSession);
            // 创建邮件发送者地址
            Address from = new InternetAddress(email);
            // 设置邮件消息的发送者
            mailMessage.setFrom(from);
            // 创建邮件的接收者地址，并设置到邮件消息中
            Address to = new InternetAddress(info.getReceiveMail());
            mailMessage.setRecipient(Message.RecipientType.TO, to);
            // 设置邮件消息的主题
            mailMessage.setSubject(info.getSubject());
            // 设置邮件消息发送的时间
            mailMessage.setSentDate(new Date());

            // 设置邮件消息的主要内容
            mailMessage.setText(info.getContent());
            // 发送邮件
            Transport.send(mailMessage);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
