package com.example.android.easymail.utils;

/**
 * Created by harshit on 10/12/17.
 */

public abstract class SenderEmailListItem {

    public static final int TYPE_MESSAGE = 2;
    public static final int TYPE_LOAD_MORE = 3;

    abstract public int getType();
}
