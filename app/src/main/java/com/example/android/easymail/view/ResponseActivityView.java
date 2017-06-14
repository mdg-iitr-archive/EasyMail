package com.example.android.easymail.view;

import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

import com.example.android.easymail.models.CurrentDayMessageSendersList;

import java.util.List;

/**
 * Created by Harshit Bansal on 6/14/2017.
 */

public interface ResponseActivityView {

    void showAutoSignedInDialog();
    void showTokenRequestDialog();
    void showExchangeSucceddedToast();
    void showExchangeFailedToast();
    void showAuthorizationFailedToast();
    void formRecyclerView(List<CurrentDayMessageSendersList> list, int i, int j, RecyclerView recyclerView);
    void addLinearLayoutToDisplay(LinearLayout currentLinearLayout);
    void showZeroMessagesReceivedToast();
}
