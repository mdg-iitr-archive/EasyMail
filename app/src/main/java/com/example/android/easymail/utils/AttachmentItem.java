package com.example.android.easymail.utils;

import javax.mail.Message;

/**
 * Created by Harshit Bansal on 7/21/2017.
 */

public class AttachmentItem extends AttachmentListItem {

    private Message message;

    public AttachmentItem(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public int getType() {
        return 1;
    }
}
