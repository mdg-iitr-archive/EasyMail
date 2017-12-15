package com.example.android.easymail.utils;

import com.bignerdranch.expandablerecyclerview.model.Parent;

/**
 * Created by harshit on 14/12/17.
 */

public abstract class SenderListItem implements Parent<SenderEmailListItem> {

    public static final int TYPE_SENDER = 4;
    public static final int TYPE_DATE = 5;
    public static final int TYPE_PROGRESS = 6;

    abstract public int getType();
}
