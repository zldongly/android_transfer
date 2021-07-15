package com.dongly.transfer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dongly.transfer.mail.MailInfo;
import com.dongly.transfer.mail.MailServer;

import java.io.FileOutputStream;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String mailAddr;
    private String password;

    private String TAG = "MainActivity: ";

    //private Button btnSetting;
    private EditText etEmail;
    private EditText etPassword;

    private MailServer mailServer;
    private long maxSmsId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.findViewById(R.id.btn_setting).setOnClickListener(this);

        etEmail = this.findViewById(R.id.et_email);
        etPassword = this.findViewById(R.id.et_password);

        checkPermission();  //检查权限
        registerObserver();  //注册监听接受短信

        load();
        Log.i(TAG, String.format("email: %s, password: %s", mailAddr, password));
        etEmail.setText(this.mailAddr);
        etPassword.setText(this.password);

        mailServer = MailServer
                .getInstance()
                .setHost("smtp.163.com")
                .setPort(25)
                .setEmail(mailAddr)
                .setPassword(password);

        // 先查一下最新的一条sms
        Sms sms = readSmsFirst();
        if (sms != null) {
            maxSmsId = sms.getId();
            Log.i(TAG, "onCreate: " + sms.toString());
        }

    }

    //短信到来时 调用 onChange 函数
    private ContentObserver newMmsContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            // 读取sms

            Sms sms = readSmsFirst();
            if (sms != null && sms.getId() > maxSmsId) {
                maxSmsId = sms.getId();
                MailInfo mail = sms.toMail().setReceiveMail(mailAddr);
                sendMail(mail);
            }
        }
    };

    // 注册监听短信
    private void registerObserver() {
        unregisterObserver();
        getContentResolver().
                registerContentObserver(Uri.parse("content://sms"), true,
                        newMmsContentObserver);
    }

    // 注销监听短信
    private synchronized void unregisterObserver() {
        try {
            if (newMmsContentObserver != null) {
                getContentResolver().unregisterContentObserver(newMmsContentObserver);
            }
        } catch (Exception e) {
            Log.e(TAG, "unregisterObserver fail");
        }
    }

    //sd 卡权限检查
    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "没有权限进行申请");
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 100);
        } else {
            Log.i(TAG, "已经有权限");
        }
    }

    //sd 卡权限申请
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "用户同意权限申请");
            } else {
                Log.i(TAG, "用户不同意权限申请");
            }
        }
    }

    @Override
    protected void onDestroy() {
        //取消注册
        unregisterObserver();
        super.onDestroy();
    }

    // button
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_setting:
                String email = etEmail.getText().toString();
                String smtpPassword = etPassword.getText().toString();
                if (!email.equals("")) {
                    this.mailAddr = email;
                }
                if (!smtpPassword.equals("")) {
                    this.password = smtpPassword;
                }
                //Log.i(TAG, String.format("mail: %s, password: %s", this.mailAddr, this.password));

                save(this.mailAddr, this.password);

                mailServer
                        .setEmail(this.mailAddr)
                        .setPassword(this.password);
            default:
                break;
        }

    }

    private Sms readSmsFirst() {
        String[] select = {"_id", "address", "person", "date", "body"};
        Cursor csr = getContentResolver().query(Uri.parse("content://sms"), select,
                "type = 1", null, null);
        //null, null, null);

        if (csr == null || !csr.moveToFirst()) {
            return null;
        }

        Sms sms = new Sms();
        sms.setId(csr.getString(0));
        sms.setSender(csr.getString(1));
        sms.setPerson(csr.getString(2));
        sms.setTime(csr.getString(3));
        sms.setContent(csr.getString(4));

        csr.close();

        return sms;
    }

    private void sendMail(String subject, String content) {
        MailInfo info = new MailInfo()
                .setReceiveMail(mailAddr). // 目标邮箱
                setSubject(subject).
                        setContent(content);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mailServer.sendMail(info);
            }
        }).start();
    }

    private void sendMail(MailInfo info) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                mailServer.sendMail(info);
            }
        }).start();
    }

    //存数据
    private void save(String email, String password) {
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.commit();
    }

    // 读数据
    private void load() {
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        this.mailAddr = pref.getString("email", "");
        this.password = pref.getString("password", "");
    }

}

/*
_id 一个自增字段，从1开始
thread_id 序号，同一发信人的id相同
address 发件人手机号码
person 联系人列表里的序号，陌生人为null
date 发件日期
protocol 协议，分为： 0 SMS_RPOTO, 1 MMS_PROTO
read 是否阅读 0未读， 1已读
status 状态 -1接收，0 complete, 64 pending, 128 failed
type ALL = 0;INBOX = 1;SENT = 2;DRAFT = 3;OUTBOX = 4;FAILED = 5; QUEUED = 6;
body 短信内容
service_center 短信服务中心号码编号。如+8613800755500
subject 短信的主题
 */