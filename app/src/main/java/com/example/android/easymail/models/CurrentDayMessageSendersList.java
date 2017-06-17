package com.example.android.easymail.models;

import com.bignerdranch.expandablerecyclerview.model.Parent;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.model.Message;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Harshit Bansal on 6/7/2017.
 */

public class CurrentDayMessageSendersList extends RealmObject implements Parent<Message> {

    @PrimaryKey
    public String sender;
    private List<Message> senderCurrentDayMessageList;

    public CurrentDayMessageSendersList(String sender, List<Message> senderCurrentDayMessageList){

        this.sender = sender;
        this.senderCurrentDayMessageList = senderCurrentDayMessageList;
    }

    public List<Message> getSenderCurrentDayMessageList() {

        return senderCurrentDayMessageList;
    }

    public void setSenderCurrentDayMessageList(List<Message> senderCurrentDayMessageList) {
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
        return senderCurrentDayMessageList;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
