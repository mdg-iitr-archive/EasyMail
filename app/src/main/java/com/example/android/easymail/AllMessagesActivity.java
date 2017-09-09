package com.example.android.easymail;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.example.android.easymail.adapters.MailClassifierAdapter;
import com.example.android.easymail.interfaces.MailClassifierSenderClickListener;
import com.example.android.easymail.interfaces.MailClassifierSenderLongClickListener;
import com.example.android.easymail.models.MailClassifierSender;
import com.example.android.easymail.services.MessagesPullService;
import com.example.android.easymail.utils.Constants;
import com.example.android.easymail.utils.MessageCountComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;

public class AllMessagesActivity extends AppCompatActivity implements MailClassifierSenderClickListener,
        MailClassifierSenderLongClickListener {

    private ActionMode mActionMode;
    private ProgressDialog dialog;
    private HashMap<String, Integer> map;
    private HashMap<String, String> addressMap;
    private MailClassifierAdapter mailClassifierAdapter;
    private RecyclerView sendersRecyclerView;
    private List<MailClassifierSender> sendersList;
    private List<MailClassifierSender> selectedSendersList;
    private boolean isMultiSelect;

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
                    mailClassifierAdapter = new MailClassifierAdapter
                            (AllMessagesActivity.this, sendersList, AllMessagesActivity.this, AllMessagesActivity.this);
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
        selectedSendersList = new ArrayList<>();
        isMultiSelect = false;
        dialog = new ProgressDialog(this);
        dialog.setMessage("Retrieving Messages...");
        sendersRecyclerView = (RecyclerView) findViewById(R.id.mail_classifier_recycler);
        sendersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onSenderClicked(int position, String sender) {

        if (isMultiSelect) multiSelect(position);
        else {
            String address = addressMap.get(sender);
            Intent selectedSenderMessagesIntent = new Intent(AllMessagesActivity.this, SelectedSenderMessagesActivity.class);
            selectedSenderMessagesIntent.putExtra("address", address);
            startActivity(selectedSenderMessagesIntent);
        }
    }

    private void multiSelect(int position) {
        if (mActionMode != null){
            if (selectedSendersList.contains(sendersList.get(position))) selectedSendersList.remove(sendersList.get(position));
            else selectedSendersList.add(sendersList.get(position));
            if (selectedSendersList.size() > 1) mActionMode.setTitle(selectedSendersList.size() + " items selected");
            else if (selectedSendersList.size() > 0) mActionMode.setTitle(selectedSendersList.size() + " item selected");
            else mActionMode.finish();
            refreshAdapter();
        }
    }

    public void refreshAdapter() {
        mailClassifierAdapter.selectedSendersList = selectedSendersList;
        mailClassifierAdapter.sendersList = sendersList;
        mailClassifierAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSenderLongClicked(int position, String sender) {
        if (!isMultiSelect){
            isMultiSelect = true;
            if (mActionMode == null) {
                mActionMode = startActionMode(mActionModeCallback);
            }
        }
        multiSelect(position);
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.sender_multi_select_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()){
                case R.id.sender_multi_select_delete:
                    new ActOnMessages("delete").execute();
                    break;
                case R.id.sender_multi_select_archive:
                    new ActOnMessages("archive").execute();
                    break;
                case R.id.sender_multi_select_move_to:
                    new ActOnMessages("moveTo").execute();
                    break;
                case R.id.sender_multi_select_mark_read:
                    new ActOnMessages("read").execute();
                    break;
                case R.id.sender_multi_select_spam:
                    new ActOnMessages("spam").execute();
                    break;
                case R.id.sender_multi_select_star:
                    new ActOnMessages("star").execute();
                    break;
                case R.id.sender_multi_select_important:
                    new ActOnMessages("important").execute();
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            selectedSendersList = new ArrayList<>();
            refreshAdapter();
        }
    };

    private class ActOnMessages extends AsyncTask<Void, Void, Void>{

        private String action;
        private Store store;
        private Folder folder;
        private ProgressDialog dialog;

        public ActOnMessages(String action) {
            this.action = action;
            dialog = new ProgressDialog(AllMessagesActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Deleting Messages...");
            // dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            Session session = Session.getDefaultInstance(props, null);
            store = null;
            try {
                store = session.getStore("imaps");
                store.connect("imap.googlemail.com","harshit.bansalec@gmail.com", "harshit1206");
                javax.mail.Folder[] folders = store.getDefaultFolder().list("*");
                for (javax.mail.Folder folder : folders) {
                    if ((folder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0) {
                        Log.i("FOLDER" + folder.getFullName() + ": " , Integer.toString(folder.getMessageCount()));
                    }
                }
                folder = store.getFolder("inbox"); // This does work for other email account
                if(!folder.isOpen()) folder.open(Folder.READ_WRITE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (MailClassifierSender sender : selectedSendersList){
                String address = addressMap.get(sender.getName());
                try {
                    SearchTerm internetAddress = new FromTerm(new InternetAddress(address));
                    javax.mail.Message[] messages = folder.search(internetAddress);
                    performAction(messages);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        private void performAction(javax.mail.Message[] messages) {
            switch (action) {
                case "delete":
                    try {
                        folder.setFlags(messages, new Flags(Flags.Flag.DELETED), true);
                        folder.close(true);
                        store.close();
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                    break;
                case "archive":
                    try {
                        folder.setFlags(messages, new Flags(Flags.Flag.DELETED), true);
                        folder.close(false);
                        store.close();
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                    break;
                case "read":
                    try {
                        folder.setFlags(messages, new Flags(Flags.Flag.SEEN), true);
                        folder.close(false);
                        store.close();
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                    break;
                case "moveTo":
                    final int[] selectedLabelNumber = new int[]{-1};
                    final AlertDialog.Builder builder = new AlertDialog.Builder(AllMessagesActivity.this);
                    builder.setTitle("Gmail Labels:");
                    ArrayList<String> foldersNameList = new ArrayList<>();
                    ArrayAdapter<String> foldersNameAdapter = new ArrayAdapter<String>
                            (AllMessagesActivity.this, android.R.layout.select_dialog_singlechoice, foldersNameList);
                    Folder[] folders;
                    try {
                        folders = store.getDefaultFolder().list("*");
                        for (javax.mail.Folder folder : folders) {
                            if ((folder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0) {
                                foldersNameList.add(folder.getFullName());
                            }
                        }
                        builder.setAdapter(foldersNameAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectedLabelNumber[0] = which;
                            }
                        });
                        AllMessagesActivity me = AllMessagesActivity.this;
                        me.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                builder.show();
                            }
                        });
                        if (selectedLabelNumber[0] != -1){
                            Folder destinationFolder = folders[selectedLabelNumber[0]];
                            folder.copyMessages(messages, destinationFolder);
                            folder.close(false);
                            store.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "spam":
                    applyLabel(messages, "[GMAIL]/Spam");
                    break;
                case "star":
                    applyLabel(messages, "[GMAIL]/Starred");
                    break;
                case "important":
                    applyLabel(messages, "[GMAIL]/Important");
                    break;
            }
        }

        private void applyLabel(Message[] messages, String label) {
            try {
                for (javax.mail.Folder folder : store.getDefaultFolder().list("*")) {
                    if ((folder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0 && folder.getFullName().equals(label)) {
                        folder.copyMessages(messages, folder);
                        folder.close(false);
                        store.close();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mActionMode.finish();
        }
    }
}

