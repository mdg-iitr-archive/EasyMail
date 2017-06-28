package com.example.android.easymail;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.android.easymail.models.Message;
import com.example.android.easymail.models.MessageBody;
import com.example.android.easymail.models.MessageHeader;
import com.example.android.easymail.models.MessagePart;
import com.example.android.easymail.models.MessagePayload;
import com.example.android.easymail.models.RealmString;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.sun.mail.imap.IMAPFolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;

/**
 * Created by Harshit Bansal on 6/21/2017.
 */

public class MessagesPullService extends IntentService {

    private Realm realm;
    private List<ArrayList<String>> list = new ArrayList<>();

    public MessagesPullService() {
        super(" ");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    public MessagesPullService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Get an instance of realm
        long startTime = System.nanoTime();
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder().build();
        realm = Realm.getInstance(configuration);

        String accessToken = (String) intent.getExtras().get("token");
        try {

            //create properties field
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");

            Session session = Session.getDefaultInstance(props, null);

            Store store = session.getStore("imaps");
            store.connect("imap.googlemail.com","harshit.bansalec@gmail.com", "harshit1206");

            //IMAPFolder folder = (IMAPFolder) store.getFolder("inbox"); // This doesn't work for other email account
            Folder folder = store.getFolder("inbox"); // This doesn't work for other email account

            //folder = (IMAPFolder) store.getFolder("inbox"); 

            if(!folder.isOpen())
                folder.open(Folder.READ_WRITE);

            javax.mail.Message[] messages = folder.getMessages();
            System.out.println("No of Messages : " + folder.getMessageCount());
            System.out.println("No of Unread Messages : " + folder.getUnreadMessageCount());
            System.out.println(messages.length);
            FetchProfile profile = new FetchProfile();
            profile.add(FetchProfile.Item.ENVELOPE);
            profile.add("Sender");
            folder.fetch(messages,profile);
/*
            javax.mail.Message[] messages = folder.getMessages();
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add("Subject");
            folder.fetch(messages, fp);
            System.out.println("No of Messages : " + folder.getMessageCount());
            System.out.println("No of Unread Messages : " + folder.getUnreadMessageCount());
            System.out.println(messages.length);
            */
            for (int i=0; i < messages.length;i++)
            {
                // Create a new message using MimeMessage copy constructor
                javax.mail.Message cmsg = messages[i];
                //Log.i("messages", Integer.toString(i) + " " + cmsg.getFrom()[0]);
                Address[] froms = cmsg.getFrom();
                String email = froms == null ? null : ((InternetAddress) froms[0]).getPersonal();
                Log.i("messages", Integer.toString(i) + " " + email);
                Log.i("star", "*****************************************************************************");
                /*
                System.out.println("MESSAGE " + (i + 1) + ":");
                javax.mail.Message msg =  messages[i];
                //System.out.println(msg.getMessageNumber());
                //Object String;
                //System.out.println(folder.getUID(msg)
                ArrayList<String> strings = new ArrayList<>();
                strings.add(Integer.toString(msg.getMessageNumber()));
                strings.add(Arrays.toString(msg.getFrom()));
                list.add(strings);
                */
            }
            long endTime = System.nanoTime();
            int i =0;

            /*
            Properties properties = new Properties();
            Properties props = new Properties();
            props.put("mail.imap.ssl.enable", "true"); // required for Gmail
            props.put("mail.imap.sasl.enable", "true");
            props.put("mail.imap.sasl.mechanisms", "XOAUTH2");
            props.put("mail.imap.auth.login.disable", "true");
            props.put("mail.imap.auth.plain.disable", "true");
            Session session = Session.getInstance(props);
            Store store = session.getStore("imap");
            store.connect("imap.gmail.com", "harshit.bansalec@gmail.com", accessToken);
            /*
            properties.put("mail.pop3.host", "pop.gmail.com");
            properties.put("mail.pop3.port", "995");
            properties.put("mail.pop3.starttls.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);

            //create the POP3 store object and connect with the pop server
            Store store = emailSession.getStore("pop3s");

            store.connect("pop.gmail.com", "harshit.bansalec@gmail.com", "harshit1206");

            //create the folder object and open it

            ImapFolder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            // retrieve the messages from the folder in an array and print it
            javax.mail.Message[] messages = emailFolder.getMessages();
            int count = emailFolder.getMessageCount();
            int ucount = emailFolder.getNewMessageCount();
            int ncount = emailFolder.getUnreadMessageCount();
            int length = messages.length;
            System.out.println("messages.length---" + messages.length);

            for (int i = 0, n = messages.length; i < n; i++) {
                javax.mail.Message message = messages[i];
                System.out.println("---------------------------------");
                System.out.println("Email Number " + (i + 1));
                System.out.println("Subject: " + message.getSubject());
                System.out.println("From: " + message.getFrom()[0]);
                // System.out.println("Text: " + message.getContent().toString;

            }
            */
            //close the store and folder objects
            folder.close(false);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        final com.google.api.services.gmail.Gmail service = new com.google.api.services.gmail.Gmail.Builder(
                transport, jsonFactory, credential
        ).setApplicationName("Gmail Api").build();

        String user = "harshit.bansalec@gmail.com";
        List<String> messagesId = new ArrayList<String>();
        ArrayList<com.google.api.services.gmail.model.Message> currentDayMessages = new ArrayList<>();

        ListMessagesResponse listMessagesResponse = null;
        try {
            listMessagesResponse = service.users().messages().list(user).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (com.google.api.services.gmail.model.Message message : listMessagesResponse.getMessages())
            messagesId.add(message.getId());

        List<com.google.api.services.gmail.model.Message> messages = new ArrayList<>();;
        for (String messageId : messagesId) {
            com.google.api.services.gmail.model.Message message = null;
            try {
                message = service.users().messages().get(user, messageId).execute();
                messages.add(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        RealmList<Message> modifiedList = new RealmList<>();
        for (com.google.api.services.gmail.model.Message message : messages){

            Message modifiedMessage = new Message();

            //Setting of id to modified message
            modifiedMessage.setId(message.getId());

            //Setting of threadId to modified message
            modifiedMessage.setThreadId(message.getThreadId());

            //Setting of label ids to modified message
            RealmList<RealmString> stringList = new RealmList<>();
            for (String labelId : message.getLabelIds()){
                stringList.add(new RealmString(labelId));
            }
            modifiedMessage.setLabelIds(stringList);

            //Setting of snippet to modified message
            modifiedMessage.setSnippet(message.getSnippet());
/*
            //Setting of payload to modified message
            String mimeType = message.getPayload().getMimeType();
            RealmList<MessageHeader> headers = new RealmList<>();
            for (MessagePartHeader header : message.getPayload().getHeaders()) {
                headers.add(new MessageHeader(header.getName(), header.getValue()));
            }
            RealmList<MessagePart> parts = new RealmList<>();
            if (message.getPayload().getParts() != null){
                for (com.google.api.services.gmail.model.MessagePart part : message.getPayload().getParts()) {
                    String partMimeType = part.getMimeType();
                    MessageBody body = new MessageBody(part.getBody().getData(),
                            part.getBody().getSize());
                    RealmList<MessageHeader> partHeader = new RealmList<>();
                    for (MessagePartHeader header : part.getHeaders()) {
                        partHeader.add(new MessageHeader(header.getName(), header.getValue()));
                    }
                    String partId = part.getPartId();
                    String filename = part.getFilename();
                    parts.add(new MessagePart(partMimeType, partHeader,
                            body, partId, filename));
                }
            }
            String file = message.getPayload().getFilename();
            com.example.android.easymail.models.MessagePartBody partBody =
                    new com.example.android.easymail.models.MessagePartBody(message.getPayload().getBody().getSize());

            MessagePayload payload = new MessagePayload(mimeType,
                    headers,
                    parts,
                    partBody, file);
            modifiedMessage.setPayload(payload);

            //Setting of custom list name to modified message i.e presently null
            modifiedMessage.setCustomListName(null);

            //Setting of custom list details to modified message
            modifiedMessage.setCustomListDetails(null);

            modifiedList.add(modifiedMessage);
        }
    */
        realm.beginTransaction();
        //realm.copyToRealmOrUpdate(modifiedList);
        realm.commitTransaction();

        //Make a local intent for broadcasting the event
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION).
                putExtra(Constants.EXTENDED_DATA_STATUS, "completed");

        long endTime = System.nanoTime();

        //Use local broadcast manager to broadcast the intent to all the registered receivers of the application
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}


