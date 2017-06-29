package com.example.android.easymail.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.android.easymail.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;

import io.realm.Realm;
import io.realm.RealmConfiguration;


/**
 * Created by Harshit Bansal on 6/21/2017.
 */

public class MessagesPullService extends IntentService {

    private Realm realm;
    private List<ArrayList<String>> list = new ArrayList<>();
    HashMap<String, Integer> map;
    HashMap<String, String> addressMap;

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
            Folder folder = store.getFolder("inbox"); // This does work for other email account

            //folder = (IMAPFolder) store.getFolder("inbox"); 

            if(!folder.isOpen())
                folder.open(Folder.READ_WRITE);
            SearchTerm sender = new FromTerm(new InternetAddress("moodle@brijeshkumar.com"));
            javax.mail.Message[] messages = folder.search(sender);
            System.out.println("No of Messages : " + folder.getMessageCount());
            System.out.println("No of Unread Messages : " + folder.getUnreadMessageCount());
            System.out.println(messages.length);
            FetchProfile profile = new FetchProfile();
            profile.add(FetchProfile.Item.ENVELOPE);
            //profile.add("Sender");
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
            map = new HashMap<>();
            addressMap = new HashMap<>();
            for (int i=0; i < messages.length;i++)
            {
                // Create a new message using MimeMessage copy constructor
                javax.mail.Message cmsg = messages[i];
                //Log.i("messages", Integer.toString(i) + " " + cmsg.getFrom()[0]);
                Address[] froms = cmsg.getFrom();
                String email = froms == null ? null : ((InternetAddress) froms[0]).getPersonal();
                if (email == null){
                    email =  ((InternetAddress) froms[0]).getAddress();
                }
                if (map.containsKey(email)){
                    map.put(email, map.get(email)+1);
                    addressMap.put(email, ((InternetAddress) froms[0]).getAddress());
                }
                else{
                    map.put(email, 1);
                    addressMap.put(email, ((InternetAddress) froms[0]).getAddress());
                }

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

        realm.beginTransaction();
        //realm.copyToRealmOrUpdate(modifiedList);
        realm.commitTransaction();

        //Make a local intent for broadcasting the event
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
        localIntent.putExtra(Constants.EXTENDED_DATA_STATUS, "completed");
        localIntent.putExtra("hashMap", map);
        localIntent.putExtra("addressMap", addressMap);
        long endTime = System.nanoTime();

        //Use local broadcast manager to broadcast the intent to all the registered receivers of the application
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}


