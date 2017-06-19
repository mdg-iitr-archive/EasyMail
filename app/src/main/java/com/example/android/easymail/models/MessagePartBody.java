package com.example.android.easymail.models;

import io.realm.RealmObject;

/**
 * Created by Harshit Bansal on 6/18/2017.
 */

public class MessagePartBody extends RealmObject {

    private int size;

    public MessagePartBody() {
    }

    public MessagePartBody(int size){
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
