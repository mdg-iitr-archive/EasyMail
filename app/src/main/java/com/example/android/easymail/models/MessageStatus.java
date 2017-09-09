package com.example.android.easymail.models;

import com.google.api.services.gmail.Gmail;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Harshit Bansal on 7/10/2017.
 */

public class MessageStatus extends RealmObject {

    @PrimaryKey
    private String id;
    private int isStored;

    public MessageStatus(){

    }

    public MessageStatus(String id, int isStored) {
        this.id = id;
        this.isStored = isStored;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIsStored() {
        return isStored;
    }

    public void setIsStored(int isStored) {
        this.isStored = isStored;
    }
}
