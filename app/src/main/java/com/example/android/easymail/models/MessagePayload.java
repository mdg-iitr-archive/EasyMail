package com.example.android.easymail.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Harshit Bansal on 5/20/2017.
 */

public class MessagePayload extends RealmObject {

    private String mimeType;
    private RealmList<MessageHeader> headers;
    private RealmList<MessagePart> parts;
    private MessagePartBody body;
    private String fileName;

    public MessagePayload() {
    }

    public MessagePayload(String mimeType, RealmList<MessageHeader> headers, RealmList<MessagePart> parts, MessagePartBody body, String fileName) {
        this.mimeType = mimeType;
        this.headers = headers;
        this.parts = parts;
        this.body = body;
        this.fileName = fileName;
    }

    public RealmList<MessagePart> getParts() {
        return parts;
    }

    public void setParts(RealmList<MessagePart> parts) {
        this.parts = parts;
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

    public MessagePartBody getBody() {

        return body;
    }

    public void setBody(MessagePartBody body) {
        this.body = body;
    }
}
