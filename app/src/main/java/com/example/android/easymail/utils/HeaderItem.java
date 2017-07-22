package com.example.android.easymail.utils;

/**
 * Created by Harshit Bansal on 7/21/2017.
 */

public class HeaderItem extends AttachmentListItem {

    private String date;

    public HeaderItem(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public int getType() {
        return 0;
    }
}
