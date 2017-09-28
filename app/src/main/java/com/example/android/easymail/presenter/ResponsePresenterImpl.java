package com.example.android.easymail.presenter;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.android.easymail.R;
import com.example.android.easymail.interactor.ResponseInteractor;
import com.example.android.easymail.models.CurrentDayMessageSendersList;
import com.example.android.easymail.models.CurrentDayMessageSendersRealmList;
import com.example.android.easymail.view.ResponseActivityView;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Harshit Bansal on 6/14/2017.
 */

public class ResponsePresenterImpl implements ResponsePresenter, ResponseInteractor.PresenterCallback {

    private ResponseActivityView responseActivityView;
    private Context context;
    private Application application;
    private ResponseInteractor responseInteractor;
    private LinearLayout currentLinearLayout;

    public ResponsePresenterImpl(ResponseActivityView responseActivityView, ResponseInteractor responseInteractor, Context context, Application application){

        this.responseActivityView = responseActivityView;
        this.responseInteractor = responseInteractor;
        this.context = context;
        this.application = application;
    }

    @Override
    public void onCreate(){
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void getOfflineMessages() {
        responseInteractor.getRealmSavedMessages(this, context);

    }

    @Override
    public void performTokenRequest(AuthorizationResponse response, String isAutoSignedInToken) {

        String[] scopes = responseInteractor.getScopes();
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                application, Arrays.asList(scopes)).
                setBackOff(new ExponentialBackOff());
        AuthorizationService service = new AuthorizationService(context);
        responseInteractor.performMesssageRequestTask(this, isAutoSignedInToken, response, service);
    }

    @Override
    public void onMessagesReceived() {
        responseActivityView.hideDialog();
    }

    @Override
    public void onRealmMessagesListFormed(int count) {

        responseInteractor.formMessagesGridView(this, count);
    }

    @Override
    public void setCredential(String acsessToken) {
        responseActivityView.getCredential(acsessToken);
    }

    @Override
    public void addDayLinearLayout(int linearLayoutId) {
        responseActivityView.appendLinearLayout(linearLayoutId);
    }

    @Override
    public void writeAuthState(AuthState state) {
        SharedPreferences authPrefs = context.getSharedPreferences(context.getResources().getString(R.string.AuthSharedPref), MODE_PRIVATE);
        authPrefs.edit()
                .putString(context.getResources().getString(R.string.StateJson), state.jsonSerializeString())
                .apply();
    }

    @Override
    public void formLinearLayout(int linearLayoutId) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout l1 = new LinearLayout(context);
        l1.setLayoutParams(params);
        l1.setOrientation(LinearLayout.HORIZONTAL);
        l1.setId(linearLayoutId);
        currentLinearLayout = l1;
    }

    @Override
    public void formRecyclerView(List<CurrentDayMessageSendersRealmList> list, int day, int i, int j, int recyclerViewId) {
        RecyclerView m1 = new RecyclerView(context);
        m1.setId(recyclerViewId);
        m1.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        m1.setLayoutManager(new LinearLayoutManager(context));
        currentLinearLayout.addView(m1);
        responseActivityView.formRecyclerView(list, day, i, j, m1);
    }

    @Override
    public void addLinearLayout() {
        responseActivityView.addLinearLayoutToDisplay(currentLinearLayout);
    }

    @Override
    public void onZeroMessagesReceived() {
        responseActivityView.showZeroMessagesReceivedToast();
        responseActivityView.hideDialog();
    }

    @Override
    public void autoSignedInTokenReceived() {
        responseActivityView.showAutoSignedInDialog();
    }

    @Override
    public void onMakeTokenRequest() {
        responseActivityView.showTokenRequestDialog();
    }

    @Override
    public void onAuthorizationFailed() {
        responseActivityView.showAuthorizationFailedToast();
        responseActivityView.hideDialog();
    }

    @Override
    public void onExchangeSuccedded() {
        responseActivityView.showExchangeSucceddedToast();
    }

    @Override
    public void onExchangeFailed() {
        responseActivityView.showExchangeFailedToast();
        responseActivityView.hideDialog();
    }
}
