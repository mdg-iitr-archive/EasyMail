package com.example.android.easymail;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.android.easymail.adapters.SelectedSenderMessagesAdapter;
import com.example.android.easymail.interfaces.SelectedSenderMessageClickListener;
import com.sun.mail.imap.IMAPFolder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;


public class SelectedSenderMessagesActivity extends AppCompatActivity implements SelectedSenderMessageClickListener{


    private String address;
    private List<MimeMessage> mimeMessageList;
    private Message[] messages;
    private RecyclerView senderMessagesRecyclerView;
    private SelectedSenderMessagesAdapter selectedSenderMessagesAdapter;
    private Long messageUid;
    private Folder folder;
    private Store store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_sender_messages);

        address = (String) getIntent().getExtras().get("address");
        try {
            new RequestSenderMessages().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        senderMessagesRecyclerView = (RecyclerView) findViewById(R.id.selected_sender_mail_recycler);
        senderMessagesRecyclerView.setLayoutManager(new LinearLayoutManager(SelectedSenderMessagesActivity.this));
        selectedSenderMessagesAdapter = new SelectedSenderMessagesAdapter
                (SelectedSenderMessagesActivity.this, new ArrayList<>(Arrays.asList(messages)), this);
    }

    private void setRecyclerAdapter() {
        senderMessagesRecyclerView.setAdapter(selectedSenderMessagesAdapter);
    }

    public int efficientGetContents() throws MessagingException {

        FetchProfile profile = new FetchProfile();
        profile.add(FetchProfile.Item.ENVELOPE);
        folder.fetch(messages, profile);
        /*
        int nbMessages = messages.length;
        // total number of messages to be retrieved
        int nbMessages = messages.length;
        // to keep track of total messages retrieved
        int index = 0;
        // number of messages retrieved in one protocol command
        final int maxDoc = 5000000;
        // max size of mails permitted to be cached in one protocol command
        final long maxSize = 10000000;

        // Message limit to fetch
        int start;
        int end;

        while (index < 5) {

            start = messages[index].getMessageNumber();
            int docs = 0;
            int totalSize = 0;
            boolean noSkip = true;
            boolean noTend = true;
            // Until we reach one of the limits
            while (docs < maxDoc && totalSize < maxSize && noSkip && noTend) {

                docs = docs + 1;
                totalSize = totalSize + messages[index].getSize();
                index = index + 1;
                if (noTend = (index < nbMessages))
                    noSkip = messages[index - 1].getMessageNumber() + 1 == messages[index].getMessageNumber();
            }
            end = messages[index - 1].getMessageNumber();
            mimeMessageList = (List<MimeMessage>) inbox.doCommand(new CustomProtocolCommand(start, end));
            Log.i("Fetching contents for ", start + ":" + end);
            Log.i("Size fetched = ", Integer.toString(totalSize));
        }
        return nbMessages;
        */
        return 0;
    }

    @Override
    public void onSenderMessageClicked(int position) {
        new GetUID(messages[position]).execute();
    }

    public class RequestSenderMessages extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(SelectedSenderMessagesActivity.this);
            dialog.setMessage("Retrieving Messages...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void[] params) {
            try {
                Properties props = System.getProperties();
                props.setProperty("mail.store.protocol", "imaps");
                Session session = Session.getDefaultInstance(props, null);
                store = session.getStore("imaps");
                // TODO: insert your own email and password
                store.connect("imap.googlemail.com", "your_email", "your_password");
                folder = store.getFolder("inbox"); // This does work for other email account
                if (!folder.isOpen()) folder.open(Folder.READ_WRITE);
                SearchTerm sender = new FromTerm(new InternetAddress(address));
                messages = folder.search(sender);
                efficientGetContents();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            initViews();
            setRecyclerAdapter();
            /*
            for (int i = 0; i < mimeMessageList.size(); i++) {
                MimeMessage message = mimeMessageList.get(i);
                System.out.println("---------------------------------");
                try {
                    writePart(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            */
        }
    }

    public class CloseFolder extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            try {
                folder.close(false);
                store.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class GetUID extends AsyncTask<Void, Void, Void>{

        private Message message;

        public GetUID(Message message) {
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... params) {
            UIDFolder uidFolder = (UIDFolder) folder;
            try {
                messageUid = uidFolder.getUID(message);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent displayMessageIntent = new Intent(SelectedSenderMessagesActivity.this, DisplayMessageActivity.class);
            displayMessageIntent.putExtra("message_uid", messageUid);
            startActivity(displayMessageIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            new CloseFolder().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
