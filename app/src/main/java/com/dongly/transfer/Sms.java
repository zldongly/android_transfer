package com.dongly.transfer;

import com.dongly.transfer.mail.MailInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Sms {
    private long id;
    private String content; // 内容
    private String time; // 时间
    private String sender; // 发送人
    private String person; // 发送人

    public Sms() {
    }

    public Sms setId(String id) {
        this.id = Long.parseLong(id);
        return this;
    }

    public long getId() {
        return id;
    }

    public Sms setContent(String content) {
        this.content = content;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Sms setTime(String time) {
        long l = Long.parseLong(time);
        Date date = new Date(l);
        this.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        //this.time = time;
        return this;
    }

    public String getTime() {
        return time;
    }

    public Sms setSender(String sender) {
        this.sender = sender;
        return this;
    }

    public String getSender() {
        return sender;
    }

    public Sms setPerson(String person) {
        this.person = person;
        return this;
    }

    public String getPerson() {
        return person;
    }

    public MailInfo toMail() {
        String content  = String.format("from: %s\ntime: %s\ncontent:\n%s",
                this.sender, this.time, this.content);

        MailInfo m = new MailInfo();

        return m.setSubject("短信通知")
                .setContent(content);
    }

    @Override
    public String toString() {
        return "Sms{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", time='" + time + '\'' +
                ", sender='" + sender + '\'' +
                ", person='" + person + '\'' +
                '}';
    }
}
