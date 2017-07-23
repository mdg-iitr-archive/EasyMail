package com.example.android.easymail.models;

/**
 * Created by Harshit Bansal on 7/23/2017.
 */

public class Attachment {

    private String contentType, fileName;

    public Attachment(String contentType, String fileName) {
        this.contentType = contentType;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
