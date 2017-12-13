package com.example.android.easymail;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.easymail.adapters.MessageAttachmentsAdapter;
import com.example.android.easymail.interfaces.MessageAttachmentClickListener;
import com.example.android.easymail.models.Attachment;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;

public class DisplayMessageActivity extends AppCompatActivity implements MessageAttachmentClickListener{

    private Message message;
    private TextView subjectTextView, senderNameInitialTextView, senderTextView, receiverTextView, dateTextView, plainTextView;
    private String subject, senderNameInitial, sender, receiver, date, plainText, htmlText;
    private WebView webView;
    private RecyclerView attachmentsRecyclerView;
    private List<Attachment> messageAttachmentsList;
    private List<Part> parts;
    private Folder folder;
    private Store store;
    private MessageAttachmentsAdapter messageAttachmentsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        initViews();
        new GetMessage((Long) getIntent().getExtras().get("message_uid")).execute();
    }

    private void initViews() {
        messageAttachmentsList = new ArrayList<>();
        parts = new ArrayList<>();
        subjectTextView = (TextView) findViewById(R.id.txt_subject);
        senderNameInitialTextView = (TextView) findViewById(R.id.txt_sender_name_initial);
        senderTextView = (TextView) findViewById(R.id.txt_sender_name);
        receiverTextView = (TextView) findViewById(R.id.txt_receiver_name);
        dateTextView = (TextView) findViewById(R.id.txt_date);
        plainTextView = (TextView) findViewById(R.id.txt_plain_message);
        webView = (WebView) findViewById(R.id.message_web_view);
        attachmentsRecyclerView = (RecyclerView) findViewById(R.id.message_attachments_recycler);
        attachmentsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    @Override
    public void onAttachmentClicked(int position) {
        String fileName = messageAttachmentsList.get(position).getFileName();
        Part part = parts.get(position);
        writeToExternalStorage(fileName, part);
    }

    private void writeToExternalStorage(String fileName, Part p) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String dbPath = path + "/" + fileName;
        File file = new File(dbPath);
        if (isExternalStorageWritable()) {
            try {
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
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            new CloseFolder().execute();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private class GetMessage extends AsyncTask<Void, Void, Void> {

        private Long messageUid;
        private ProgressDialog dialog;

        public GetMessage(Long messageUid) {
            this.messageUid = messageUid;
            dialog =  new ProgressDialog(DisplayMessageActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Retrieving Message...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            getMessage();
            return null;
        }

        private void getMessage() {
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            Session session = Session.getDefaultInstance(props, null);
            try {
                store = session.getStore("imaps");
                // TODO: insert your own email and password
                store.connect("imap.googlemail.com", "your_email", "your_password");
                folder = store.getFolder("inbox"); // This does work for other email account
                UIDFolder uidFolder = (UIDFolder) folder;
                if (!folder.isOpen()) folder.open(Folder.READ_WRITE);
                message = uidFolder.getMessageByUID(messageUid);
                writePart(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            setTextViews();
            setAdapter();
        }
    }

    private void setAdapter() {
        messageAttachmentsAdapter = new MessageAttachmentsAdapter
                (DisplayMessageActivity.this, messageAttachmentsList, DisplayMessageActivity.this);
        attachmentsRecyclerView.setAdapter(messageAttachmentsAdapter);
    }

    private void setTextViews() {
        subjectTextView.setText(subject);
        senderNameInitialTextView.setText(senderNameInitial);
        senderNameInitialTextView.setVisibility(View.VISIBLE);
        senderTextView.setText(sender);
        receiverTextView.setVisibility(View.VISIBLE);
        dateTextView.setText(date);
        if (htmlText != null) webView.loadData(htmlText, "text/html", null);
        else plainTextView.setText(plainText);
    }

    /**
     * This method checks for content-type
     * based on which, it processes and
     * fetches the content of the message
     */
    void writePart(Part p) throws Exception {
        if (p instanceof Message)
            writeEnvelope((Message) p);
        Log.i("CONTENT-TYPE: ", p.getContentType());
        //check if the content is plain text
        if (p.isMimeType("text/html")){
            htmlText = (String) p.getContent();
        }
        //check if the content is html text
        else if (p.isMimeType("text/plain")){
            plainText = (String) p.getContent();
        }
        //check if the content is a multipart message
        else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++)
                writePart(mp.getBodyPart(i));
        }
        //check if the content is a nested message
        else if (p.isMimeType("message/rfc822")) {
            writePart((Part) p.getContent());
        }
        // check if part is an attachment
        else if (Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition())) {
            messageAttachmentsList.add(new Attachment(p.getContentType(), p.getFileName()));
            parts.add(p);
        }
        //check if the content is an inline image
        else if (p.isMimeType("image/jpeg")) {
            System.out.println("--------> image/jpeg");
            Object o = p.getContent();
            InputStream x = (InputStream) o;
            FileOutputStream f2 = new FileOutputStream("/tmp/image.jpg");
            // Construct the required byte array
            byte[] bArray = new byte[x.available()];
            System.out.println("x.length = " + x.available());
            int byteRead;
            while ((byteRead = x.read(bArray)) != -1){
                f2.write(bArray, 0, byteRead);
            }
            f2.close();
        }
        else if (p.getContentType().contains("image/")) {
            System.out.println("content type" + p.getContentType());
            File f = new File("image" + new Date().getTime() + ".jpg");
            DataOutputStream output = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(f)));
            com.sun.mail.util.BASE64DecoderStream test =
                    (com.sun.mail.util.BASE64DecoderStream) p
                            .getContent();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = test.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
    }

    public void writeEnvelope(final Message m) throws Exception {
        final  Address[] a;
        // SUBJECT
        if (m.getSubject() != null)
            subject = m.getSubject();
        // FROM
        if ((a = m.getFrom()) != null) {
            senderNameInitial = ((InternetAddress) a[0]).getPersonal().substring(0, 1).toUpperCase();
            sender = ((InternetAddress) a[0]).getPersonal();
        }
        // DATE
        if (m.getReceivedDate() != null){
            date = m.getReceivedDate().toString();
        }
        // TO
        /*
        if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
            for (int j = 0; j < a.length; j++)
                Log.i("TO: ", a[j].toString());
        }
        */
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
}
