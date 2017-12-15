package com.example.android.easymail.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by harshit on 15/12/17.
 */

public class ProgressItem extends SenderListItem {

    public ProgressItem() {
    }

    @Override
    public int getType() {
        return TYPE_PROGRESS;
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
