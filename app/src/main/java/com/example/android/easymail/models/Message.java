package com.example.android.easymail.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Harshit Bansal on 5/20/2017.
 */

public class Message {

    @SerializedName("id")
    public int id;
    @SerializedName("threadId")
    public int threadId;
    @SerializedName("snippet")
    public String snippet;
    @SerializedName("labelIds")
    public ArrayList<String> labelIds;
    @SerializedName("payload")
    public MessagePayload payload;

    public Message(int id, int threadId, String snippet, ArrayList<String> labelIds, MessagePayload payload) {

        this.id = id;
        this.threadId = threadId;
        this.snippet = snippet;
        this.labelIds = labelIds;
        this.payload = payload;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
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

    public ArrayList<String> getLabelIds() {

        return labelIds;
    }

    public void setLabelIds(ArrayList<String> labelIds) {
        this.labelIds = labelIds;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
