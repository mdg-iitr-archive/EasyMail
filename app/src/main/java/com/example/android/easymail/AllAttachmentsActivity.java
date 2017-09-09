package com.example.android.easymail;

import android.app.ProgressDialog;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.android.easymail.adapters.AttachmentAdapter;
import com.example.android.easymail.interfaces.AttachmentClickListener;
import com.example.android.easymail.models.HashTable;
import com.example.android.easymail.utils.AttachmentItem;
import com.example.android.easymail.utils.AttachmentListItem;
import com.example.android.easymail.utils.HeaderItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;

public class AllAttachmentsActivity extends AppCompatActivity implements AttachmentClickListener{

    private RecyclerView attachmentsRecyclerView;
    private List<Message> messageList;
    private HashTable table;
    private List<AttachmentListItem> attachmentListItems;
    private AttachmentAdapter attachmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_attachments);

        initViews();
        new LoadAttachments().execute();
    }

    private void initViews() {
        table = new HashTable(2000);
        messageList = new ArrayList<>();
        attachmentListItems = new ArrayList<>();
        attachmentsRecyclerView = (RecyclerView) findViewById(R.id.all_attachments_recycler);
        attachmentsRecyclerView.setLayoutManager(new LinearLayoutManager(AllAttachmentsActivity.this));
    }

    @Override
    public void onDownloadClicked(Part part) {
        new DownloadAttachment(part).execute();
    }

    @Override
    public void onAttachmentClicked(Message message) {

    }

    private class LoadAttachments extends AsyncTask<Void, Void, Void>{

        private Store store;
        private Folder folder;
        private ProgressDialog dialog;

        public LoadAttachments() {
            dialog = new ProgressDialog(AllAttachmentsActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Retrieving Attachments...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            Session session = Session.getDefaultInstance(props, null);
            try {
                store = session.getStore("imaps");
                store.connect("imap.googlemail.com", "harshit.bansalec@gmail.com", "harshit1206");

                folder = store.getFolder("inbox"); // This does work for other email account

                if (!folder.isOpen()) folder.open(Folder.READ_WRITE);
                SearchTerm sender = new FromTerm(new InternetAddress("madanlal7z585@gmail.com"));
                javax.mail.Message[] messages = folder.getMessages();
                FetchProfile profile = new FetchProfile();
                profile.add(FetchProfile.Item.ENVELOPE);
                profile.add(FetchProfile.Item.CONTENT_INFO);
                folder.fetch(messages, profile);
                Collections.reverse(Arrays.asList(messages));
                int index = 0;
                String day = messages[0].getReceivedDate().toString().split(" ")[2];
                String month = messages[0].getReceivedDate().toString().split(" ")[1];
                String year = messages[0].getReceivedDate().toString().split(" ")[5];

                for (Message message : messages) {
                    String d = message.getContentType();
                    if (d.contains("multipart")) {
                        // this message may contain attachment
                        Multipart multiPart = (Multipart) message.getContent();
                        for (int i = 0; i < multiPart.getCount(); i++) {
                            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
                            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                                if (index == 0) {
                                    day = message.getReceivedDate().toString().split(" ")[2];
                                    month = message.getReceivedDate().toString().split(" ")[1];
                                    year = message.getReceivedDate().toString().split(" ")[5];
                                    attachmentListItems.add(new HeaderItem
                                            (day + " " + month + " " + year));
                                    index = 1;
                                }
                                String messageDay = message.getReceivedDate().toString().split(" ")[2];
                                String messageMonth = message.getReceivedDate().toString().split(" ")[1];
                                String messageYear = message.getReceivedDate().toString().split(" ")[5];
                                if (day.equals(messageDay) && month.equals(messageMonth) && year.equals(messageYear)) {
                                    attachmentListItems.add(new AttachmentItem(message));
                                } else {
                                    attachmentListItems.add(new HeaderItem
                                            (messageDay + " " + messageMonth + " " + messageYear));
                                    attachmentListItems.add(new AttachmentItem(message));
                                    day = messageDay;
                                    month = messageMonth;
                                    year = messageYear;
                                }
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            try{
                folder.close(false);
                store.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            attachmentAdapter = new AttachmentAdapter
                    (AllAttachmentsActivity.this, attachmentListItems, AllAttachmentsActivity.this);
            attachmentsRecyclerView.setAdapter(attachmentAdapter);
        }
    }

    private class DownloadAttachment extends AsyncTask<Void, Void, Void>{

        private ProgressDialog dialog;
        private Part p;

        public DownloadAttachment(Part p) {
            dialog = new ProgressDialog(AllAttachmentsActivity.this);
            this.p = p;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Downloading Attachment...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            writeToExternalStorage(p);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            Toast.makeText(AllAttachmentsActivity.this, "Downloaded!!", Toast.LENGTH_SHORT).show();
        }
    }

    /* write to external storage if space is available */
    public void writeToExternalStorage(Part p) {

        Store store;
        Folder folder;
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(props, null);
        try {
            store = session.getStore("imaps");
            store.connect("imap.googlemail.com", "harshit.bansalec@gmail.com", "harshit1206");

            folder = store.getFolder("inbox"); // This does work for other email account
            if (!folder.isOpen()) folder.open(Folder.READ_WRITE);
            String fileName = p.getFileName();

            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            String dbPath = path + "/" + fileName;
            File file = new File(dbPath);
            // Get the directory for the user's public pictures directory.
            // File file = new File(Environment.getExternalStoragePublicDirectory
            // (Environment.DIRECTORY_DOWNLOADS), fileName);
            if (isExternalStorageWritable()) {
                InputStream input;
                input = p.getInputStream();
                FileOutputStream output = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int byteRead;
                while ((byteRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, byteRead);
                }
                output.flush();
                output.close();
                folder.close(false);
                store.close();
            }
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
