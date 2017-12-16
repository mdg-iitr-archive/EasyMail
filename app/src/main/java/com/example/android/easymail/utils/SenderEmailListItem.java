package com.example.android.easymail.utils;

import android.os.Parcelable;

/**
 * Created by harshit on 10/12/17.
 */

public abstract class SenderEmailListItem implements Parcelable{

    public static final int TYPE_MESSAGE = 2;
    public static final int TYPE_LOAD_MORE = 3;

    abstract public int getType();
}
