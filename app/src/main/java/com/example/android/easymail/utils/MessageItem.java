package com.example.android.easymail.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.android.easymail.models.Message;

/**
 * Created by harshit on 10/12/17.
 */

public class MessageItem extends SenderEmailListItem {

    private String id;
    private Long internalDate;
    private String subject, snippet;

    public MessageItem() {
    }

    public MessageItem(String id, Long internalDate, String subject, String snippet) {
        this.id = id;
        this.internalDate = internalDate;
        this.subject = subject;
        this.snippet = snippet;
    }

    @Override
    public int getType() {
        return TYPE_MESSAGE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getInternalDate() {
        return internalDate;
    }

    public void setInternalDate(Long internalDate) {
        this.internalDate = internalDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public static final Parcelable.Creator<MessageItem> CREATOR
            = new Parcelable.Creator<MessageItem>() {
        public MessageItem createFromParcel(Parcel in) {
            return new MessageItem(in);
        }

        public MessageItem[] newArray(int size) {
            return new MessageItem[size];
        }
    };

    private MessageItem(Parcel in) {
        id = in.readString();
        internalDate = in.readLong();
        subject = in.readString();
        snippet = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeLong(internalDate);
        parcel.writeString(subject);
        parcel.writeString(snippet);
    }
}
