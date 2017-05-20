package com.example.android.easymail.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Harshit Bansal on 5/20/2017.
 */

public class MessagePayload {

    @SerializedName("mimeType")
    public String mimeType;
    @SerializedName("headers")
    public ArrayList<MessageHeader> headers;
    @SerializedName("parts")
    public ArrayList<MessagePart> parts;
    @SerializedName("body")
    public MessageBody body;
    @SerializedName("fileName")
    public String fileName;

    public MessagePayload(String mimeType, ArrayList<MessageHeader> headers, ArrayList<MessagePart> parts, MessageBody body,String fileName) {
        this.mimeType = mimeType;
        this.headers = headers;
        this.parts = parts;
        this.body = body;
        this.fileName = fileName;
    }

    public ArrayList<MessagePart> getParts() {
        return parts;
    }

    public void setParts(ArrayList<MessagePart> parts) {
        this.parts = parts;
    }

    public String getMimeType() {

        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public ArrayList<MessageHeader> getHeaders() {

        return headers;
    }

    public void setHeaders(ArrayList<MessageHeader> headers) {
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
