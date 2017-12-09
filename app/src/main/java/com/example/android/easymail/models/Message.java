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

public class Message extends RealmObject {

    @PrimaryKey
    private String id;
    private String pageToken;
    private String threadId;
    private String snippet;
    private Long internalDate;
    private String sender;
    private RealmList<RealmString> labelIds;
    private MessagePayload payload;
    private String customListName;
    private CustomListDetails customListDetails;

    public Long getInternalDate() {
        return internalDate;
    }

    public void setInternalDate(Long internalDate) {
        this.internalDate = internalDate;
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

    public CustomListDetails getCustomListDetails() {
        return customListDetails;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
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

    public String getPageToken() {
        return pageToken;
    }

    public void setPageToken(String pageToken) {
        this.pageToken = pageToken;
    }
}
