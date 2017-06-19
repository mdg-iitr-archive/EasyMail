package com.example.android.easymail.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Harshit Bansal on 5/20/2017.
 */

public class MessagePart extends RealmObject {

    private String mimeType;
    private RealmList<MessageHeader> headers;
    private MessageBody body;
    private String partId;
    private String fileName;

    public MessagePart() {
    }

    public MessagePart(String mimeType, RealmList<MessageHeader> headers, MessageBody body, String partId, String fileName) {
        this.mimeType = mimeType;
        this.headers = headers;
        this.body = body;
        this.partId = partId;
        this.fileName = fileName;
    }

    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public String getMimeType() {

        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public RealmList<MessageHeader> getHeaders() {

        return headers;
    }

    public void setHeaders(RealmList<MessageHeader> headers) {
        this.headers = headers;
    }

    public String getFileName() {

        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public MessageBody getBody() {

        return body;
    }

    public void setBody(MessageBody body) {
        this.body = body;
    }
}
