package com.example.android.easymail.utils;

import com.bignerdranch.expandablerecyclerview.model.Parent;
import com.example.android.easymail.models.Message;

import java.util.List;

import io.realm.RealmList;

/**
 * Created by harshit on 10/12/17.
 */

public class SenderEmail implements Parent<SenderEmailListItem> {

    private String sender;
    private List<SenderEmailListItem> senderMessageList;

    public SenderEmail(String sender, List<SenderEmailListItem> senderMessageList) {
        this.sender = sender;
        this.senderMessageList = senderMessageList;
    }

    @Override
    public List<SenderEmailListItem> getChildList() {
        return senderMessageList;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public List<SenderEmailListItem> getSenderMessageList() {
        return senderMessageList;
    }

    public void setSenderMessageList(List<SenderEmailListItem> senderMessageList) {
        this.senderMessageList = senderMessageList;
    }
}
