package com.example.android.easymail.models;

import java.util.ArrayList;

/**
 * Created by Harshit Bansal on 5/20/2017.
 */

public class MessagePart {

    public String mimeType;
    public ArrayList<MessageHeader> headers;
    public MessageBody body;
    public String partId;
    public String fileName;

    public MessagePart(String mimeType, ArrayList<MessageHeader> headers, MessageBody body, String partId, String fileName) {
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
