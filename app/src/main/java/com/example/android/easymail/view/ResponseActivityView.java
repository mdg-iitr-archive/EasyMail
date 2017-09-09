package com.example.android.easymail.view;

import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

import com.example.android.easymail.models.CurrentDayMessageSendersList;
import com.example.android.easymail.models.CurrentDayMessageSendersRealmList;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

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
    void formRecyclerView(List<CurrentDayMessageSendersRealmList> list, int day, int i, int j, RecyclerView recyclerView);
    void addLinearLayoutToDisplay(LinearLayout currentLinearLayout);
    void showZeroMessagesReceivedToast();
    void hideDialog();
    void getCredential(String accessToken);
    void appendLinearLayout(int linearLayoutId);
}
