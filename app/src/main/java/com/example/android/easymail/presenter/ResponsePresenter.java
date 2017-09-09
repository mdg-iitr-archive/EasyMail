package com.example.android.easymail.presenter;

import android.app.Application;

import net.openid.appauth.AuthorizationResponse;

/**
 * Created by Harshit Bansal on 6/14/2017.
 */

public interface ResponsePresenter {

    void onCreate();
    void onPause();
    void onResume();
    void onDestroy();
    void getOfflineMessages();
    void performTokenRequest(AuthorizationResponse response, String isAutoSignedInToken);
}
