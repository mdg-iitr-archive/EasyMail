package com.example.android.easymail.utils;

/**
 * Created by harshit on 10/12/17.
 */

public class MessageItem extends SenderEmailListItem {

    private Long internalDate;
    private String subject, snippet;

    public MessageItem(Long internalDate, String subject, String snippet) {
        this.internalDate = internalDate;
        this.subject = subject;
        this.snippet = snippet;
    }

    @Override
    public int getType() {
        return TYPE_MESSAGE;
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
