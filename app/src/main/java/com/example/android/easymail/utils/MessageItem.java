package com.example.android.easymail.utils;

/**
 * Created by harshit on 10/12/17.
 */

public class MessageItem extends SenderEmailListItem {

    private String id;
    private Long internalDate;
    private String subject, snippet;

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
}
