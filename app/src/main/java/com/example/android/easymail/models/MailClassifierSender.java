package com.example.android.easymail.models;

/**
 * Created by Harshit Bansal on 6/28/2017.
 */

public class MailClassifierSender {

    private String name;
    private String count;

    public MailClassifierSender(String name, String count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
