package com.example.android.easymail.models;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import io.realm.RealmObject;

/**
 * Created by Harshit Bansal on 5/20/2017.
 */

public class MessageBody extends RealmObject {

    private String data;
    private int size;

    public MessageBody(String data, int size) {
        this.data = data;
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setData(String data) {

        this.data = data;
    }

    public MessageBody() {
    }
}
