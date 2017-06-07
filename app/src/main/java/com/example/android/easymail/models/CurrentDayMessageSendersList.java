package com.example.android.easymail.models;

import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.model.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harshit Bansal on 6/7/2017.
 */

public class CurrentDayMessageSendersList {

    public String sender;
    public List<Message> senderCurrentDayMessageList;

    public CurrentDayMessageSendersList(String sender, List<Message> senderCurrentDayMessageList){

        senderCurrentDayMessageList = new ArrayList<>();
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
}
