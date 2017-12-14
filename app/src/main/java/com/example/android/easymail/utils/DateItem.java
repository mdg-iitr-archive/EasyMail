package com.example.android.easymail.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by harshit on 14/12/17.
 */

public class DateItem extends SenderListItem {

    private String date;

    public DateItem(String date) {
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
        return TYPE_DATE;
    }

    @Override
    public List<SenderEmailListItem> getChildList() {
        return new ArrayList<>();
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
