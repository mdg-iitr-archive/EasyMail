package com.example.android.easymail.interactor;

import android.app.Application;
import android.content.Context;

import com.example.android.easymail.models.CurrentDayMessageSendersList;
import com.example.android.easymail.models.CurrentDayMessageSendersRealmList;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;

import java.util.List;

/**
 * Created by Harshit Bansal on 6/14/2017.
 */

public interface ResponseInteractor {

    interface PresenterCallback{

        void autoSignedInTokenReceived();
        void onMakeTokenRequest();
        void onAuthorizationFailed();
        void onExchangeSuccedded();
        void onExchangeFailed();
        void writeAuthState(AuthState state);
        void formLinearLayout(int linearLayoutId);
        void formRecyclerView(List<CurrentDayMessageSendersList> list, int i, int j, int recyclerViewId);
        void addLinearLayout();
        void onZeroMessagesReceived();
        void onMessagesReceived();
    }
    void getRealmSavedMessages(ResponseInteractor.PresenterCallback callback, Context context);
    void performMesssageRequestTask(PresenterCallback callback, String accessToken, AuthorizationResponse response, AuthorizationService service);
    String[] getScopes();
}
