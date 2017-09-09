package com.example.android.easymail.utils;

/**
 * Created by Harshit Bansal on 7/21/2017.
 */

public abstract class AttachmentListItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ATTACHMENT = 1;

    abstract public int getType();
}
