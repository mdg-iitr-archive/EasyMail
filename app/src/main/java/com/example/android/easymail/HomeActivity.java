package com.example.android.easymail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.android.easymail.adapters.EmailAdapter;
import com.example.android.easymail.interfaces.EndlessScrollListener;
import com.example.android.easymail.models.CurrentDayMessageSendersRealmList;
import com.example.android.easymail.models.Message;
import com.example.android.easymail.services.EmailPullService;
import com.example.android.easymail.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView emailRecyclerView;
    private String accessToken;
    private Realm realm;
    private EmailAdapter emailAdapter;
    List<CurrentDayMessageSendersRealmList> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViews();
        initRealm();
        initRecycler();
        accessToken = (String) getIntent().getExtras().get("token");
        // display the offline messages first
        int offlineMessagesSize = getOfflineMessages();
        if (offlineMessagesSize == 0) {
            if (accessToken != null) {
                Intent emailPullIntent = new Intent(this, EmailPullService.class);
                emailPullIntent.putExtra("token", accessToken);
                // getPageToken() always returns null in this case
                emailPullIntent.putExtra("page_token", getPageToken());
                // getFailedEmailNumber() always returns 1000L in this case
                emailPullIntent.putExtra("failed_email_number", getFailedEmailNumber());
                startService(emailPullIntent);
            }
        }

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ((Long) intent.getExtras().get("failed_email_number") == 1000L){
                    writeFailedEmailNumber(1000L);
                    writeFailedPageToken(getPageToken());
                }else{
                    writeFailedEmailNumber((Long) intent.getExtras().get("failed_email_number"));
                }
                writePageToken((String) intent.getExtras().get("next_page_token"));
                showCurrentPageMails();
            }
        };
        // Add a intent filter with the desired action
        IntentFilter filter = new IntentFilter(Constants.BROADCAST_ACTION_EMAIL);
        // Register the receiver for the local broadcasts
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private void initRecycler() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        emailRecyclerView.setLayoutManager(layoutManager);
        emailAdapter = new EmailAdapter(this, list);
        emailRecyclerView.setAdapter(emailAdapter);
        emailRecyclerView.addOnScrollListener(new EndlessScrollListener((LinearLayoutManager) layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Intent emailPullIntent = new Intent(HomeActivity.this, EmailPullService.class);
                emailPullIntent.putExtra("token", accessToken);
                if (getFailedEmailNumber() == 1000L) {
                    emailPullIntent.putExtra("page_token", getPageToken());
                }else{
                    emailPullIntent.putExtra("page_token", getFailedPageToken());
                }
                emailPullIntent.putExtra("failed_email_number", getFailedEmailNumber());
                startService(emailPullIntent);
            }
        });
    }

    /**
     * initialise views of the activity
     */
    private void initViews() {
        emailRecyclerView = (RecyclerView) findViewById(R.id.email_recycler);
    }

    private void initRealm() {
        realm = Realm.getDefaultInstance();
    }

    /**
     * TODO: needs work for repeated filters
     */
    private void showCurrentPageMails() {
        RealmResults<Message> response = realm.where(Message.class).equalTo("pageToken", getPageToken()).findAll();
        List<Message> messages =  realm.copyFromRealm(response);
        for (Message message : messages){
            RealmList<Message> messageList = new RealmList<>();
            messageList.add(message);
            CurrentDayMessageSendersRealmList currentDayMessageSendersRealmList =
                    new CurrentDayMessageSendersRealmList(message.getSender(), messageList);
            list.add(currentDayMessageSendersRealmList);
        }
        emailAdapter.setParentList(list);
        emailAdapter.notifyParentDataSetChanged(true);
    }

    private String getPageToken() {
        SharedPreferences preferences = getSharedPreferences("NEXT_PAGE_TOKEN", MODE_PRIVATE);
        return preferences.getString("pageToken", null);
    }

    private void writePageToken(String pageToken){
        SharedPreferences preferences = getSharedPreferences("NEXT_PAGE_TOKEN", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("pageToken", pageToken);
        editor.apply();
    }

    private String getFailedPageToken() {
        SharedPreferences preferences = getSharedPreferences("NEXT_PAGE_TOKEN", MODE_PRIVATE);
        return preferences.getString("failedPageToken", null);
    }

    private void writeFailedPageToken(String pageToken){
        SharedPreferences preferences = getSharedPreferences("NEXT_PAGE_TOKEN", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("failedPageToken", pageToken);
        editor.apply();
    }

    private void writeFailedEmailNumber(Long failedEmailNumber) {
        SharedPreferences preferences = getSharedPreferences("NEXT_PAGE_TOKEN", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("failedEmailNumber", failedEmailNumber);
        editor.apply();
    }

    private Long getFailedEmailNumber() {
        SharedPreferences preferences = getSharedPreferences("NEXT_PAGE_TOKEN", MODE_PRIVATE);
        return preferences.getLong("failedEmailNumber", 1000L);
    }

    private int getOfflineMessages() {
        RealmResults<Message> response = realm.where(Message.class).findAll();
        List<Message> messages =  realm.copyFromRealm(response);
        for (Message message : messages){
            RealmList<Message> messageList = new RealmList<>();
            messageList.add(message);
            CurrentDayMessageSendersRealmList currentDayMessageSendersRealmList =
                    new CurrentDayMessageSendersRealmList(message.getSender(), messageList);
            list.add(currentDayMessageSendersRealmList);
        }
        emailAdapter.setParentList(list);
        emailAdapter.notifyParentDataSetChanged(true);
        return list.size();
    }
}
