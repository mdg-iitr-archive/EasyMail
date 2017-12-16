package com.example.android.easymail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.android.easymail.adapters.EmailAdapter;
import com.example.android.easymail.interfaces.EmailLongClickListener;
import com.example.android.easymail.interfaces.EndlessScrollListener;
import com.example.android.easymail.interfaces.SenderEmailItemClickListener;
import com.example.android.easymail.models.CurrentDayMessageSendersRealmList;
import com.example.android.easymail.models.Message;
import com.example.android.easymail.services.EmailPullService;
import com.example.android.easymail.utils.Constants;
import com.example.android.easymail.utils.DateItem;
import com.example.android.easymail.utils.LoadMoreItem;
import com.example.android.easymail.utils.MessageItem;
import com.example.android.easymail.utils.ProgressItem;
import com.example.android.easymail.utils.SenderEmail;
import com.example.android.easymail.utils.SenderEmailListItem;
import com.example.android.easymail.utils.SenderListItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class HomeActivity extends AppCompatActivity implements SenderEmailItemClickListener, EmailLongClickListener, NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView rightNavigationView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView emailRecyclerView;
    private String accessToken;
    private Realm realm;
    private EmailAdapter emailAdapter;
    private MessageItem messageItem;
    List<SenderListItem> list = new ArrayList<>();

    private Long offlineEmailDate;
    private boolean isSync = false;
    private boolean isFirstTimeLaunch = false;
    private boolean isProgressLayoutAdded = false;

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
                // first time launch
                isSync = false;
                isFirstTimeLaunch = true;
                Intent emailPullIntent = new Intent(this, EmailPullService.class);
                emailPullIntent.putExtra("isSync", false);
                emailPullIntent.putExtra("token", accessToken);
                emailPullIntent.putExtra("isPresentDay", false);
                // getDate() always returns present date in this case
                emailPullIntent.putExtra("date", getDate());
                // getFailedEmailNumber() always returns 1000L in this case
                emailPullIntent.putExtra("failed_email_number", getFailedEmailNumber());
                startService(emailPullIntent);
            }
        }else{
            syncMails();
        }

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isProgressLayoutAdded){
                    isProgressLayoutAdded = false;
                    list.remove(list.size() - 1);
                    emailAdapter.notifyParentDataSetChanged(true);
                }
                if (isSync) {
                    writeSyncFailedEmailNumber((Long) intent.getExtras().get("failed_email_number"));
                    writeMostRecentMessageDate((Long) intent.getExtras().get("date"));
                    showSyncMails((Parcelable[]) intent.getExtras().get("parcelables"));
                }else {
                    writeFailedEmailNumber((Long) intent.getExtras().get("failed_email_number"));
                    writeDate((Long) intent.getExtras().get("date"));
                    showCurrentPageMails();
                }
            }
        };
        // Add a intent filter with the desired action
        IntentFilter filter = new IntentFilter(Constants.BROADCAST_ACTION_EMAIL);
        // Register the receiver for the local broadcasts
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    private void syncMails() {
        isSync = true;
        while (!Objects.equals(getMidnightDate(), getMidnightDate(getMostRecentMessageDate()))){
            Intent emailPullIntent = new Intent(this, EmailPullService.class);
            emailPullIntent.putExtra("isSync", true);
            emailPullIntent.putExtra("token", accessToken);
            emailPullIntent.putExtra("isPresentDay", false);
            // getDate() always returns present date in this case
            emailPullIntent.putExtra("date", getMostRecentMessageDate());
            // getFailedEmailNumber() always returns 1000L in this case
            emailPullIntent.putExtra("failed_email_number", getSyncFailedEmailNumber());
            startService(emailPullIntent);
        }
        Intent emailPullIntent = new Intent(this, EmailPullService.class);
        emailPullIntent.putExtra("isSync", true);
        emailPullIntent.putExtra("isPresentDay", true);
        emailPullIntent.putExtra("token", accessToken);
        // getDate() always returns present date in this case
        emailPullIntent.putExtra("date", getMostRecentMessageDate());
        // getFailedEmailNumber() always returns 1000L in this case
        emailPullIntent.putExtra("failed_email_number", getSyncFailedEmailNumber());
        startService(emailPullIntent);
    }

    private Long getMidnightDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        TimeZone timeZone = TimeZone.getDefault();
        calendar.setTimeZone(timeZone);
        return calendar.getTimeInMillis()/1000;
    }

    private Long getMidnightDate(Long mostRecentMessageDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mostRecentMessageDate * 1000);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        TimeZone timeZone = TimeZone.getDefault();
        calendar.setTimeZone(timeZone);
        return calendar.getTimeInMillis()/1000;
    }

    private void initRecycler() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        emailRecyclerView.setLayoutManager(layoutManager);
        emailAdapter = new EmailAdapter(this, list, this, this);
        emailRecyclerView.setAdapter(emailAdapter);
        emailRecyclerView.addOnScrollListener(new EndlessScrollListener((LinearLayoutManager) layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                isProgressLayoutAdded = true;
                list.add(new ProgressItem());
                emailAdapter.notifyParentDataSetChanged(true);
                Intent emailPullIntent = new Intent(HomeActivity.this, EmailPullService.class);
                emailPullIntent.putExtra("token", accessToken);
                emailPullIntent.putExtra("date", getDate());
                emailPullIntent.putExtra("failed_email_number", getFailedEmailNumber());
                startService(emailPullIntent);
            }
        });
    }

    /**
     * initialise views of the activity
     */
    private void initViews() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        rightNavigationView = (NavigationView) findViewById(R.id.right_drawer);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        emailRecyclerView = (RecyclerView) findViewById(R.id.email_recycler);

        /*
        * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
        * performs a swipe-to-refresh gesture.
        */
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        syncMails();
                    }
                }
        );

        //set the listeners for the left and right navigation views
        rightNavigationView.setNavigationItemSelectedListener(this);

        //restrict the swiping of the right drawer
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);

        //restrict the swiping of the right drawer on closing of left or right drawer
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }
            @Override
            public void onDrawerOpened(View drawerView) {
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
            }
            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    private void initRealm() {
        realm = Realm.getDefaultInstance();
    }

    private void showSyncMails(Parcelable[] parcelables) {
        if (getSyncFailedEmailNumber() == 1000L){
            list.add(0, new DateItem(formDateFromTimeStamp(getMostRecentMessageDate())));
        }
        for (Parcelable parcelable : parcelables){
            list.add(1, (SenderEmail)parcelable);
        }
    }

    /**
     * TODO: needs work for repeated filters
     */
    private void showCurrentPageMails() {
        Long date = getDate();
        if (getFailedEmailNumber() == 1000L){
            date = date + 86400;
            list.add(new DateItem(formDateFromTimeStamp(date)));
        }

        RealmResults<Message> response = realm.where(Message.class).equalTo("date", date).findAll();
        List<Message> messages =  realm.copyFromRealm(response);
        if (isFirstTimeLaunch){
            writeMostRecentMessageDate(messages.get(0).getInternalDate());
        }
        for (Message message : messages){
            List<SenderEmailListItem> messageList = new ArrayList<>();
            String subject = null;
            for (int i = 0; i < message.getPayload().getHeaders().size(); i++) {
                String check = message.getPayload().getHeaders().get(i).getName();
                String value = message.getPayload().getHeaders().get(i).getValue();
                switch (check) {
                    case "Subject":
                        subject = value;
                        break;
                }
            }
            messageList.add(new MessageItem
                    (message.getId(), message.getInternalDate(), subject, message.getSnippet()));
            messageList.add(new LoadMoreItem());
            SenderEmail senderEmailList = new SenderEmail(message.getSender(), messageList);
            list.add(senderEmailList);
        }
        emailAdapter.setParentList(list);
        emailAdapter.notifyParentDataSetChanged(true);
    }

    private void writeMostRecentMessageDate(Long internalDate) {
        SharedPreferences preferences = getSharedPreferences("NEXT_DATE", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("mostRecentMessageDate", internalDate);
        editor.apply();
    }

    private Long getMostRecentMessageDate() {
        SharedPreferences preferences = getSharedPreferences("NEXT_DATE", MODE_PRIVATE);
        return preferences.getLong("mostRecentMessageDate", getCurrentTime());
    }

    private String formDateFromTimeStamp(Long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.setTimeInMillis(date * 1000);
        return (String) DateFormat.format("dd-MM-yyyy", cal);
    }

    private Long getDate() {
        SharedPreferences preferences = getSharedPreferences("NEXT_DATE", MODE_PRIVATE);
        return preferences.getLong("date", getCurrentTime());
    }

    private long getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        TimeZone timeZone = TimeZone.getDefault();
        calendar.setTimeZone(timeZone);
        return calendar.getTimeInMillis()/1000;
    }

    private void writeDate(Long date){
        SharedPreferences preferences = getSharedPreferences("NEXT_DATE", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("date", date);
        editor.apply();
    }

    private void writeFailedEmailNumber(Long failedEmailNumber) {
        SharedPreferences preferences = getSharedPreferences("NEXT_DATE", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("failedEmailNumber", failedEmailNumber);
        editor.apply();
    }

    private Long getFailedEmailNumber() {
        SharedPreferences preferences = getSharedPreferences("NEXT_DATE", MODE_PRIVATE);
        return preferences.getLong("failedEmailNumber", 1000L);
    }

    private void writeSyncFailedEmailNumber(Long failedEmailNumber) {
        SharedPreferences preferences = getSharedPreferences("NEXT_DATE", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("syncFailedEmailNumber", failedEmailNumber);
        editor.apply();
    }

    private Long getSyncFailedEmailNumber() {
        SharedPreferences preferences = getSharedPreferences("NEXT_DATE", MODE_PRIVATE);
        return preferences.getLong("syncFailedEmailNumber", 1000L);
    }

    private int getOfflineMessages() {
        RealmResults<Message> response = realm.where(Message.class).findAll();
        List<Message> messages =  realm.copyFromRealm(response);
        if (messages.size() != 0) {
            offlineEmailDate = messages.get(0).getDate();
            list.add(new DateItem(formDateFromTimeStamp(offlineEmailDate)));
            for (Message message : messages) {
                if (!Objects.equals(message.getDate(), offlineEmailDate)) {
                    offlineEmailDate = message.getDate();
                    list.add(new DateItem(formDateFromTimeStamp(offlineEmailDate)));
                }
                List<SenderEmailListItem> messageList = new ArrayList<>();
                String subject = null;
                for (int i = 0; i < message.getPayload().getHeaders().size(); i++) {
                    String check = message.getPayload().getHeaders().get(i).getName();
                    String value = message.getPayload().getHeaders().get(i).getValue();
                    switch (check) {
                        case "Subject":
                            subject = value;
                            break;
                    }
                }
                messageList.add(new MessageItem
                        (message.getId(), message.getInternalDate(), subject, message.getSnippet()));
                messageList.add(new LoadMoreItem());
                SenderEmail senderEmailList = new SenderEmail(message.getSender(), messageList);
                list.add(senderEmailList);
            }
        }
        emailAdapter.setParentList(list);
        emailAdapter.notifyParentDataSetChanged(true);
        return list.size();
    }

    @Override
    public void onEmailLongClicked(SenderEmailListItem item) {
        messageItem = (MessageItem) item;
        drawerLayout.openDrawer(GravityCompat.END);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
    }

    @Override
    public void onSenderItemClicked(SenderEmailListItem senderEmailListItem) {
        if (senderEmailListItem.getType() == SenderEmailListItem.TYPE_LOAD_MORE){

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent editMessageIntent = new Intent(HomeActivity.this, EditMessageActivity.class);
        switch(item.getItemId()) {
            case R.id.right_nav_to_do:
                editMessageIntent.putExtra("listName", "To-Do");
                editMessageIntent.putExtra("messageId", messageItem.getId());
                startActivity(editMessageIntent);
                break;
            case R.id.right_nav_follow_up:
                editMessageIntent.putExtra("listName", "Follow Up");
                editMessageIntent.putExtra("messageId", messageItem.getId());
                startActivity(editMessageIntent);
                break;
            case R.id.right_nav_launch_events:
                editMessageIntent.putExtra("listName", "Launch Events");
                editMessageIntent.putExtra("messageId", messageItem.getId());
                startActivity(editMessageIntent);
                break;
            case R.id.right_nav_business_events:
                editMessageIntent.putExtra("listName", "Business Events");
                editMessageIntent.putExtra("messageId", messageItem.getId());
                startActivity(editMessageIntent);
                break;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.swipe_refresh_menu, menu);
        return true;
    }

    /*
    * Listen for option item selections so that we receive a notification
    * when the user requests a refresh by selecting the refresh action bar item.
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Check if user triggered a refresh:
        if (item.getItemId() == R.id.menu_refresh){
            // Signal SwipeRefreshLayout to start the progress indicator
            swipeRefreshLayout.setRefreshing(true);

            // Start the refresh background task.
            // This method calls setRefreshing(false) when it's finished.
            syncMails();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

