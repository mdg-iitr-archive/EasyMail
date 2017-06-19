package com.example.android.easymail.models;

import com.bignerdranch.expandablerecyclerview.model.Parent;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Harshit Bansal on 6/7/2017.
 */

public class CurrentDayMessageSendersRealmList extends RealmObject implements Parent<Message>{

    @PrimaryKey
    private String sender;
    private RealmList<Message> senderCurrentDayMessageList;

    public CurrentDayMessageSendersRealmList(String sender, RealmList<Message> senderCurrentDayMessageList){

        this.sender = sender;
        this.senderCurrentDayMessageList = senderCurrentDayMessageList;
    }

    public CurrentDayMessageSendersRealmList(){

    }

    public RealmList<Message> getSenderCurrentDayMessageList() {

        return senderCurrentDayMessageList;
    }

    public void setSenderCurrentDayMessageList(RealmList<Message> senderCurrentDayMessageList) {
        this.senderCurrentDayMessageList = senderCurrentDayMessageList;
    }

    public String getSender() {

        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public List<Message> getChildList() {
        return null;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
