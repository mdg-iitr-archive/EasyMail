package com.example.android.easymail.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Harshit Bansal on 5/20/2017.
 */

public class Message extends RealmObject implements Parcelable{

    @PrimaryKey
    private String id;
    private String threadId;
    private String snippet;
    private RealmList<RealmString> labelIds;
    private MessagePayload payload;
    private String customListName;
    private CustomListDetails customListDetails;

    public Message(){

    }

    public Message(String id, String threadId, String snippet, RealmList<RealmString> labelIds, MessagePayload payload) {

        this.id = id;
        this.threadId = threadId;
        this.snippet = snippet;
        this.labelIds = labelIds;
        this.payload = payload;
    }

    protected Message(Parcel in) {
        id = in.readString();
        threadId = in.readString();
        snippet = in.readString();
        customListName = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getSnippet() {

        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public MessagePayload getPayload() {

        return payload;
    }

    public void setPayload(MessagePayload payload) {
        this.payload = payload;
    }

    public RealmList<RealmString> getLabelIds() {

        return labelIds;
    }

    public void setLabelIds(RealmList<RealmString> labelIds) {
        this.labelIds = labelIds;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CustomListDetails getCustomListDetails() {
        return customListDetails;
    }

    public void setCustomListDetails(CustomListDetails customListDetails) {
        this.customListDetails = customListDetails;
    }

    public String getCustomListName() {
        return customListName;
    }

    public void setCustomListName(String customListName) {
        this.customListName = customListName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(threadId);
        dest.writeString(snippet);
        dest.writeString(customListName);
    }
}
