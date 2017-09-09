package com.example.android.easymail;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.android.easymail.adapters.CustomListMessagesAdapter;
import com.example.android.easymail.models.Message;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class CustomListMessagesActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private RecyclerView customListMessagesRecyclerView;
    private CustomListMessagesAdapter customListMessagesAdapter;
    private Realm realm;
    private List<Message> customListMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_list_messages);

        String listName = (String) getIntent().getExtras().get("list_name");
        configureActionBar(listName);
        getMessagelistFromRealm(listName);
        initViews();
        setAdapter();
    }

    private void configureActionBar(String listName) {
        actionBar = getSupportActionBar();
        actionBar.setTitle(listName);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setAdapter() {
        customListMessagesRecyclerView.setAdapter(customListMessagesAdapter);
    }

    private void initViews() {
        customListMessagesRecyclerView = (RecyclerView) findViewById(R.id.custom_list_messages_recycler);
        customListMessagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        customListMessagesAdapter = new CustomListMessagesAdapter(this, customListMessages);
    }

    private void getMessagelistFromRealm(String listName){
        RealmConfiguration configuration = new RealmConfiguration.Builder().build();
        realm = Realm.getInstance(configuration);
        RealmResults<Message> results = realm.where(Message.class).equalTo("customListName", listName).findAll();
        customListMessages = realm.copyFromRealm(results);
    }
}
