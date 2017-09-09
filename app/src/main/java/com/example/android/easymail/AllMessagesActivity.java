package com.example.android.easymail;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.android.easymail.adapters.MailClassifierAdapter;
import com.example.android.easymail.interfaces.MailClassifierSenderClickListener;
import com.example.android.easymail.models.MailClassifierSender;
import com.example.android.easymail.services.MessagesPullService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AllMessagesActivity extends AppCompatActivity implements MailClassifierSenderClickListener {

    private ProgressDialog dialog;
    private HashMap<String, Integer> map;
    private HashMap<String, String> addressMap;
    private MailClassifierAdapter mailClassifierAdapter;
    private RecyclerView sendersRecyclerView;
    private List<MailClassifierSender> sendersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_messages);
        initViews();
        dialog.show();

        String token = (String) getIntent().getExtras().get("token");
        Intent serviceIntent = new Intent(this, MessagesPullService.class);
        serviceIntent.putExtra("token", token);
        startService(serviceIntent);
        // Add a broadcast receiver to handle the intent
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String status = (String) intent.getExtras().get(Constants.EXTENDED_DATA_STATUS);
                if (status.equals("completed")){

                    // update the user interface
                    if (dialog.isShowing())
                        dialog.dismiss();
                    map = (HashMap<String, Integer>) intent.getExtras().get("hashMap");
                    addressMap = (HashMap<String, String>) intent.getExtras().get("addressMap");
                    Iterator countIterator = map.entrySet().iterator();
                    int count = 0;
                    while (countIterator.hasNext()){
                        Map.Entry countNext = (Map.Entry) countIterator.next();
                        sendersList.add(new MailClassifierSender((String) countNext.getKey(), Integer.toString((int) countNext.getValue())));
                        count = count + (int) countNext.getValue();
                    }
                    Collections.sort(sendersList, new MessageCountComparator());
                    mailClassifierAdapter = new MailClassifierAdapter(AllMessagesActivity.this, sendersList, AllMessagesActivity.this);
                    sendersRecyclerView.setAdapter(mailClassifierAdapter);
                }
            }
        };

        // Add a intent filter with the desired action
        IntentFilter filter = new IntentFilter(Constants.BROADCAST_ACTION);

        // Register the receiver for the local broadcasts
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

    }

    private void initViews() {

        map = new HashMap<>();
        sendersList = new ArrayList<>();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Retrieving Messages...");
        sendersRecyclerView = (RecyclerView) findViewById(R.id.mail_classifier_recycler);
        sendersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onSenderClicked(String sender) {

        String address = addressMap.get(sender);
        Intent selectedSenderMessagesIntent = new Intent(AllMessagesActivity.this, SelectedSenderMessages.class);
        selectedSenderMessagesIntent.putExtra("address", address);
        startActivity(selectedSenderMessagesIntent);
    }
}

