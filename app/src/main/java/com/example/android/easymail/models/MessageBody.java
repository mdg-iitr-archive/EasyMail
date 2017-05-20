package com.example.android.easymail.models;

import java.util.HashMap;

/**
 * Created by Harshit Bansal on 5/20/2017.
 */

public class MessageBody{

    public HashMap<String, String> data;
    public HashMap<String, Integer> size;

    public MessageBody(HashMap<String, String> data, HashMap<String, Integer> size) {
        this.data = data;
        this.size = size;
    }

    public HashMap<String, String> getData() {
        return data;
    }

    public HashMap<String, Integer> getSize() {
        return size;
    }

    public void setSize(HashMap<String, Integer> size) {
        this.size = size;
    }

    public void setData(HashMap<String, String> data) {

        this.data = data;
    }
}
