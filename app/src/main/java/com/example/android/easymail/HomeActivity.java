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
import com.example.android.easymail.models.Message;
import com.example.android.easymail.services.EmailPullService;
import com.example.android.easymail.utils.Constants;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView emailRecyclerView;
    private String accessToken, currentPageToken;
    private Realm realm;
    private EmailAdapter emailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViews();
        initRealm();
        initRecycler();
        currentPageToken = getPageToken();
        accessToken = (String) getIntent().getExtras().get("token");
        if (accessToken != null){
            Intent emailPullIntent = new Intent(this, EmailPullService.class);
            emailPullIntent.putExtra("token", accessToken);
            emailPullIntent.putExtra("page_token", currentPageToken);
            startService(emailPullIntent);
        }

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ((Long) intent.getExtras().get("failed_email_number") == 1000L){
                    writeFailedEmailNumber(1000L);
                    writePageToken((String) intent.getExtras().get("next_page_token"));
                }else{
                    writeFailedEmailNumber((Long) intent.getExtras().get("failed_email_number"));
                    writePageToken(currentPageToken);
                }
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
        emailRecyclerView.addOnScrollListener(new EndlessScrollListener((LinearLayoutManager) layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Intent emailPullIntent = new Intent(HomeActivity.this, EmailPullService.class);
                emailPullIntent.putExtra("token", accessToken);
                emailPullIntent.putExtra("page_token", currentPageToken);
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

    private void showCurrentPageMails() {
        RealmResults<Message> response = realm.where(Message.class).equalTo("pageToken", currentPageToken).findAll();
        List<Message> messages =  realm.copyFromRealm(response);

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

    private void writeFailedEmailNumber(Long failedEmailNumber) {
        SharedPreferences preferences = getSharedPreferences("NEXT_PAGE_TOKEN", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("failedEmailNumber", failedEmailNumber);
        editor.apply();
    }
}
