package com.dongly.transfer.mail;

public class MailInfo {
    private String receiveMail;

    private String subject;
    private String content;

    public MailInfo() {}

    public MailInfo(String mail) {
        this.receiveMail = mail;
    }

    public MailInfo setReceiveMail(String receiveMail) {
        this.receiveMail = receiveMail;
        return this;
    }

    public String getReceiveMail() {
        return receiveMail;
    }

    public MailInfo setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public MailInfo setContent(String content) {
        this.content = content;
        return this;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "MailInfo{" +
                "receiveMail='" + receiveMail + '\'' +
                ", subject='" + subject + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
