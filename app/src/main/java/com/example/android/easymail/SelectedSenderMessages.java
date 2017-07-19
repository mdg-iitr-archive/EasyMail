package com.example.android.easymail;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.EventLogTags;
import android.util.Log;

import com.example.android.easymail.adapters.SelectedSenderMessagesAdapter;
import com.example.android.easymail.interfaces.SelectedSenderMessageClickListener;
import com.sun.mail.imap.IMAPFolder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;


public class SelectedSenderMessages extends AppCompatActivity implements SelectedSenderMessageClickListener{


    private String address;
    private List<MimeMessage> mimeMessageList;
    private Message[] messages;
    private RecyclerView senderMessagesRecyclerView;
    private SelectedSenderMessagesAdapter selectedSenderMessagesAdapter;

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
        senderMessagesRecyclerView.setLayoutManager(new LinearLayoutManager(SelectedSenderMessages.this));
        selectedSenderMessagesAdapter = new SelectedSenderMessagesAdapter
                (SelectedSenderMessages.this, new ArrayList<>(Arrays.asList(messages)), this);
    }

    private void setRecyclerAdapter() {
        senderMessagesRecyclerView.setAdapter(selectedSenderMessagesAdapter);
    }

    public int efficientGetContents(IMAPFolder inbox, Message[] messages) throws MessagingException {

        FetchProfile profile = new FetchProfile();
        profile.add(FetchProfile.Item.ENVELOPE);
        // profile.add("Sender");
        inbox.fetch(messages, profile);
        int nbMessages = messages.length;
        /*
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
    public void onSenderMessageClicked(Message message) {

    }

    public class RequestSenderMessages extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(SelectedSenderMessages.this);
            dialog.setMessage("Retrieving Messages...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void[] params) {

            try {
                Properties props = System.getProperties();
                props.setProperty("mail.store.protocol", "imaps");

                Session session = Session.getDefaultInstance(props, null);

                Store store = session.getStore("imaps");
                store.connect("imap.googlemail.com", "harshit.bansalec@gmail.com", "harshit1206");

                //IMAPFolder folder = (IMAPFolder) store.getFolder("inbox"); // This doesn't work for other email account
                IMAPFolder folder = (IMAPFolder) store.getFolder("inbox"); // This does work for other email account
                //folder = (IMAPFolder) store.getFolder("inbox");

                if (!folder.isOpen())
                    folder.open(Folder.READ_WRITE);

                SearchTerm sender = new FromTerm(new InternetAddress(address));
                messages = folder.search(sender);
                System.out.println("No of Messages : " + folder.getMessageCount());
                System.out.println("No of Unread Messages : " + folder.getUnreadMessageCount());
                System.out.println(messages.length);
                efficientGetContents(folder, messages);
                folder.close(false);
                store.close();
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

    /*
   * This method checks for content-type
   * based on which, it processes and
   * fetches the content of the message
   */
    void writePart(Part p) throws Exception {

        if (p instanceof Message)
            //Call method writeEnvelope
            writeEnvelope((Message) p);

        Log.i("Checking content type", "**********************************");
        Log.i("CONTENT-TYPE: ", p.getContentType());

        //check if the content is plain text
        if (p.isMimeType("text/plain")) {
            Log.i("Mime Type: ", "This is plain text");
            Log.i(" ", "---------------------------");
            String content = (String) p.getContent();
            Log.i("Content", (String) p.getContent());
        }
        else if (p.isMimeType("text/html")){
            Log.i("Mime Type: ", "This is html text");
            Log.i(" ", "---------------------------");
            String content = (String) p.getContent();
            Log.i("Content", (String) p.getContent());
        }
        //check if the content has attachment
        else if (p.isMimeType("multipart/*")) {
            Log.i("Mime Type", "This is a Multipart");
            Log.i(" ", "---------------------------");
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++)
                writePart(mp.getBodyPart(i));
        }
        //check if the content is a nested message
        else if (p.isMimeType("message/rfc822")) {
            Log.i("Mime Type", "This is a Nested Message");
            Log.i(" ", "---------------------------");
            writePart((Part) p.getContent());
        }
        // check if part is an attachment
        else if (Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition())) {
            writeToExternalStorage(p);
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

    public static void writeEnvelope(Message m) throws Exception {
        Log.i("writeEnvelope", "This is the message envelope");
        Log.i("writeEnvelope", "---------------------------");
        Address[] a;
        // FROM
        if ((a = m.getFrom()) != null) {
            for (int j = 0; j < a.length; j++)
                Log.i("FROM: ", a[j].toString());
        }
        // TO
        if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
            for (int j = 0; j < a.length; j++)
                Log.i("TO: ", a[j].toString());
        }
        // SUBJECT
        if (m.getSubject() != null)
            Log.i("SUBJECT: ", m.getSubject());
    }

    /* write to external storage if space is available */
    public void writeToExternalStorage(Part p){

        String fileName = null;
        try {
            fileName = p.getFileName();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), fileName);
        if (isExternalStorageWritable()){
            InputStream input;
            try {
                input = p.getInputStream();
                FileOutputStream output = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int byteRead;
                while ((byteRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, byteRead);
                }
                output.close();
            }catch (IOException | MessagingException e) {
                e.printStackTrace();
            }
        }
        if (!file.mkdirs()) {
            Log.e("Attachment: ", "Directory not created");
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
