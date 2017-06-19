package com.example.android.easymail.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Harshit Bansal on 5/20/2017.
 */

public class Message extends RealmObject {

    private String id;
    private String threadId;
    private String snippet;
    private RealmList<RealmString> labelIds;
    private MessagePayload payload;

    public Message(){

    }

    public Message(String id, String threadId, String snippet, RealmList<RealmString> labelIds, MessagePayload payload) {

        this.id = id;
        this.threadId = threadId;
        this.snippet = snippet;
        this.labelIds = labelIds;
        this.payload = payload;
    }

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
}
