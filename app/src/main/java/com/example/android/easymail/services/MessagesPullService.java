package com.example.android.easymail.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.android.easymail.utils.Constants;

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
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MessagesPullService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Get an instance of realm
        // Start time only for debugging
        long startTime = System.nanoTime();
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder().build();
        realm = Realm.getInstance(configuration);
        // Would be used later when we enable
        // xoauth2 authentication via javamail
        String accessToken = (String) intent.getExtras().get("token");
        try {
            //create properties field
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");

            Session session = Session.getDefaultInstance(props, null);

            Store store = session.getStore("imaps");
            // TODO: insert your own email and password
            store.connect("imap.googlemail.com", "your_email", "your_password");

            Folder folder = store.getFolder("inbox"); // This does work for other email account

            if(!folder.isOpen())
                folder.open(Folder.READ_WRITE);
            SearchTerm sender = new FromTerm(new InternetAddress("moodle@brijeshkumar.com"));
            javax.mail.Message[] messages = folder.getMessages();
            System.out.println("No of Messages : " + folder.getMessageCount());
            System.out.println("No of Unread Messages : " + folder.getUnreadMessageCount());
            System.out.println(messages.length);
            FetchProfile profile = new FetchProfile();
            profile.add(FetchProfile.Item.ENVELOPE);
            folder.fetch(messages,profile);
            map = new HashMap<>();
            addressMap = new HashMap<>();
            for (int i=0; i < messages.length;i++)
            {
                // Create a new message using MimeMessage copy constructor
                javax.mail.Message cmsg = messages[i];
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
                // Display log messages only for debugging.
                Log.i("messages", Integer.toString(i) + " " + email);
                Log.i("star", "***********************************");
            }
            // The following variable is used only for debugging
            long endTime = System.nanoTime();
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


