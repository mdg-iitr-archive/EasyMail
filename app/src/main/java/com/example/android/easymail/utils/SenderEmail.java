package com.example.android.easymail.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.bignerdranch.expandablerecyclerview.model.Parent;
import com.example.android.easymail.models.Message;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

/**
 * Created by harshit on 10/12/17.
 */

public class SenderEmail extends SenderListItem implements Parcelable{

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

    @Override
    public int getType() {
        return TYPE_SENDER;
    }

    public static final Parcelable.Creator<SenderEmail> CREATOR
            = new Parcelable.Creator<SenderEmail>() {
        public SenderEmail createFromParcel(Parcel in) {
            return new SenderEmail(in);
        }

        public SenderEmail[] newArray(int size) {
            return new SenderEmail[size];
        }
    };

    private SenderEmail(Parcel in) {
        sender = in.readString();
        senderMessageList = new ArrayList<>();
        in.readList(senderMessageList, null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(sender);
        parcel.writeList(senderMessageList);
    }
}
