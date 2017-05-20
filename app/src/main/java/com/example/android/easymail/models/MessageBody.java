package com.example.android.easymail.models;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Created by Harshit Bansal on 5/20/2017.
 */

public class MessageBody{

    @SerializedName("data")
    public String data;
    @SerializedName("size")
    public int size;

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
}
