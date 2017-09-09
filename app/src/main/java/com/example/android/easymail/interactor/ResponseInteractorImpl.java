package com.example.android.easymail.interactor;

import com.example.android.easymail.ResponseActivity;
import com.example.android.easymail.models.CurrentDayMessageSendersList;
import com.example.android.easymail.models.CurrentDayMessageSendersRealmList;
import com.example.android.easymail.models.HashTable;
import com.example.android.easymail.models.Message;
import com.example.android.easymail.models.MessageBody;
import com.example.android.easymail.models.MessageHeader;
import com.example.android.easymail.models.MessagePart;
import com.example.android.easymail.models.MessagePayload;
import com.example.android.easymail.models.RealmString;
import com.example.android.easymail.models.Sender;
import com.example.android.easymail.services.WeeklyMessagesService;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.StringUtils;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;


import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import retrofit2.Callback;


/**
 * Created by Harshit Bansal on 6/14/2017.
 */

public class ResponseInteractorImpl extends ResultReceiver implements ResponseInteractor{

    public static final String[] scopes = {GmailScopes.GMAIL_READONLY};
    private Exception lastError = null;
    private static final int HASH_TABLE_SIZE =  100;
    public HashTable hashTable = new HashTable(HASH_TABLE_SIZE);
    private Handler handler = new Handler();
    private List<Message> customCurrentDayMessages = new ArrayList<>();
    private List<CurrentDayMessageSendersList> currentDayMessageSendersList = new ArrayList<>();
    private List<CurrentDayMessageSendersRealmList> currentDayMessageSendersRealmList = new ArrayList<>();
    private int i, j , k , recyclerViewId;
    private List<CurrentDayMessageSendersRealmList> list;
    ResponseInteractor.PresenterCallback callback;
    private int lastCount = 0;
    Realm realm;
    Context context;

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public ResponseInteractorImpl(Handler handler) {
        super(handler);
    }

    @Override
    public void getRealmSavedMessages(ResponseInteractor.PresenterCallback callback, Context context) {

        this.context = context;

        Realm.init(context);
        RealmConfiguration configuration = new RealmConfiguration.Builder().build();
        realm = Realm.getInstance(configuration);
        this.callback = callback;
        // classifyMessages(0, 0, 7);
    }

    @Override
    public void performMesssageRequestTask(final ResponseInteractor.PresenterCallback callback, String accessToken, final AuthorizationResponse response, AuthorizationService service){

        GoogleCredential googleCredential = new GoogleCredential().setAccessToken(accessToken);
        if (accessToken != null){
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
            callback.setCredential(accessToken);
            //callback.autoSignedInTokenReceived();
            try {
                startBackgroundTask(callback, credential, accessToken);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            if (response != null) {
                service.performTokenRequest(
                        response.createTokenExchangeRequest(),
                        new AuthorizationService.TokenResponseCallback() {
                            @Override
                            public void onTokenRequestCompleted(TokenResponse resp, AuthorizationException ex) {
                                if (resp != null) {
                                    //exchange succeeded\\
                                    callback.onExchangeSuccedded();
                                    //callback.onMakeTokenRequest();
                                    String accessToken = resp.accessToken;
                                    AuthState state = new AuthState(response, resp, ex);
                                    callback.writeAuthState(state);
                                    GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
                                    callback.setCredential(accessToken);
                                    try {
                                        startBackgroundTask(callback, credential, accessToken);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    //exchange failed\\
                                    callback.onExchangeFailed();
                                    AuthState state = new AuthState(response, null, ex);
                                    callback.writeAuthState(state);
                                }
                            }
                        }
                );
            } else {
                //authorization failed\\
                callback.onAuthorizationFailed();
            }
        }

    }

    @Override
    public String[] getScopes() {
        return scopes;
    }

    private void startBackgroundTask(final ResponseInteractor.PresenterCallback callback, GoogleCredential credential
    , String token)
            throws InterruptedException{

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        final com.google.api.services.gmail.Gmail  service = new com.google.api.services.gmail.Gmail.Builder(
                transport, jsonFactory, credential
        ).setApplicationName("Gmail Api").build();

        Intent i = new Intent(context, WeeklyMessagesService.class);

        i.putExtra("receiverTag", this);
        i.putExtra("token", token);
        context.startService(i);
/*
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

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

                for (String messageId : messagesId) {
                    com.google.api.services.gmail.model.Message message = null;
                    try {
                        message = service.users().messages().get(user, messageId).execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String test = StringUtils.newStringUtf8(Base64.decodeBase64(message.getPayload().getParts().get(1).getBody().getData()));
                    String base64ForUrlInput = StringUtils.newStringUtf8(Base64.decodeBase64(message.getPayload().getParts().get(1).getBody().getData()));
                    int padChars = (base64ForUrlInput.length() % 4) == 0 ? 0 : (4 - (base64ForUrlInput.length() % 4));

                    String date = message.getPayload().getHeaders().get(2).getValue().split(",")[1];
                    String[] words = date.split("\\s");

                    SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
                    String day = dayFormat.format(Calendar.getInstance().getTime());

                    SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
                    String month = monthFormat.format(Calendar.getInstance().getTime());

                    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
                    String year = yearFormat.format(Calendar.getInstance().getTime());

                    if (words[1].equals(day) && words[2].equals(month) && words[3].equals(year)) {
                        currentDayMessages.add(message);
                    }
                    else {
                        break;
                    }
                }

                if (currentDayMessages.size() == 0) {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onZeroMessagesReceived();
                        }
                    });
                } else {
                    List<String> senders = new ArrayList<>();
                    int index = 0;
                    for (com.google.api.services.gmail.model.Message message : currentDayMessages) {
                        for (int i = 0; i < currentDayMessages.get(index).getPayload().getHeaders().size(); i++) {
                            String name = currentDayMessages.get(index).getPayload().getHeaders().get(i).getName();
                            if (name.equals("From")) {
                                String sender = currentDayMessages.get(index).getPayload().getHeaders().get(i).getValue();
                                // hashTable.insert(sender, message);
                                break;
                            }
                        }
                        index++;
                    }
                    for (int i = 0; i < HASH_TABLE_SIZE; i++) {
                        if (hashTable.keys[i] != null) {
/*
                            currentDayMessageSendersList.add(new CurrentDayMessageSendersList(hashTable.keys[i], hashTable.vals.get(i)));

                            List<com.google.api.services.gmail.model.Message> list = hashTable.vals.get(i);
                            RealmList<Message> modifiedList = new RealmList<>();
                            for  (com.google.api.services.gmail.model.Message message : list) {
                                Message modifiedMessage = new Message();
                                modifiedMessage.setId(message.getId());
                                modifiedMessage.setThreadId(message.getThreadId());
                                RealmList<RealmString> stringList = new RealmList<>();
                                for (String labelId : message.getLabelIds()){
                                    stringList.add(new RealmString(labelId));
                                }
                                modifiedMessage.setLabelIds(stringList);
                                // modifiedMessage.setLabelIds(new RealmList<RealmString>((RealmString[]) message.getLabelIds().toArray()));
                                modifiedMessage.setSnippet(message.getSnippet());

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
                                    if (filename != null && filename.length() > 0){
                                        String attId = part.getBody().getAttachmentId();
                                        try {
                                            MessagePartBody attachPart = service.users().messages().attachments().
                                                    get("harshit.bansalec@gmail.com", message.getId(), attId).execute();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
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
                                modifiedMessage.setCustomListName(null);
                                modifiedMessage.setCustomListDetails(null);
                                modifiedList.add(modifiedMessage);
                                customCurrentDayMessages.add(modifiedMessage);
                            }
                            currentDayMessageSendersRealmList.add(new CurrentDayMessageSendersRealmList(hashTable.keys[i], modifiedList));
                        }
                    }

                }}

            }
        });
        thread.start();
        thread.join();
*/
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(currentDayMessageSendersRealmList);
        realm.copyToRealmOrUpdate(customCurrentDayMessages);
        realm.commitTransaction();
        formMessagesGridView(callback, currentDayMessageSendersRealmList.size());
    }

    @Override
    public void formMessagesGridView(final ResponseInteractor.PresenterCallback callback, final int count) {
/*
        int numberOfRows = (int) Math.ceil((float) count / 4);
                for (i = 0, k = 0; i < numberOfRows; i++) {

                    int linearLayoutId = Integer.parseInt("1" + Integer.toString(i + 1));
                    callback.formLinearLayout(linearLayoutId);
                    for (j = 0; j < 4 && k < count; j++, k++) {
                        list = new ArrayList<>();
                        list.add(currentDayMessageSendersRealmList.get(k));
                        recyclerViewId = Integer.parseInt("2" + Integer.toString(i + 1) + Integer.toString(j + 1));
                        callback.formRecyclerView(list, i, j, recyclerViewId);
                    }
                    callback.addLinearLayout();
                }
                callback.onMessagesReceived();
*/
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);

        if (resultCode == 1) {
            String mostRecentMessageId = (String) resultData.get("most_recent_message_id");
            SharedPreferences pref = context.getSharedPreferences("MostRecentMessageId", Context.MODE_PRIVATE);
            pref.edit().putString("mostRecentMessageId", mostRecentMessageId).apply();

        } else {
            int count = (int) resultData.get("days_between");
            classifyMessages(count, lastCount, count);
            /*
            RealmResults<Sender> senderRealmResults = realm.where(Sender.class).findAll();
            List<Sender> senders = realm.copyFromRealm(senderRealmResults);
            int limit = count;

            for (int i = lastCount; i < limit; i++) {

                lastCount = count;
                /*
                int layoutCount = 1;
                int layoutMessagesCount = 0;
                int linearLayoutId = Integer.parseInt("1" + Integer.toString(i + 1) + Integer.toString(layoutCount));
                */
                List<CurrentDayMessageSendersRealmList> currentDayList = new ArrayList<>();
                /*
                callback.formLinearLayout(linearLayoutId);
                */
                /*
                for (int j = 0; j < senders.size(); j++) {

                    RealmResults<Message> realmResults = realm.where(Message.class).equalTo("sender", senders.get(j).getName()).findAll();
                    List<Message> messages = realm.copyFromRealm(realmResults);
                    RealmList<Message> list = new RealmList<>();

                    for (int k = 0; k < messages.size(); k++) {

                        String date = null;
                        for (MessageHeader header : messages.get(k).getPayload().getHeaders()){
                            if(header.getName().equals("Date"))
                                date = header.getValue().split(",")[1];
                        }String[] words = date.split("\\s");

                        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
                        String day = dayFormat.format(Calendar.getInstance().getTime());

                        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
                        String month = monthFormat.format(Calendar.getInstance().getTime());

                        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
                        String year = yearFormat.format(Calendar.getInstance().getTime());

                        Calendar currentDate = Calendar.getInstance();
                        currentDate.set(Integer.parseInt(year), getMonthNo(month), Integer.parseInt(day), 0, 0);
                        Calendar messageDate = Calendar.getInstance();
                        messageDate.set(Integer.parseInt(words[3]), getMonthNo(words[2]), Integer.parseInt(words[1]), 0, 0);

                        if (calendarDaysBetween(currentDate, messageDate) == i) {
                            list.add(messages.get(k));
                        }
                    }
                    if (list.size() != 0)
                        currentDayList.add(new CurrentDayMessageSendersRealmList(senders.get(j).getName(), list));

                    /*
                    if (layoutMessagesCount == 4) {
                        /**
                         * form a linear layout with below linear layout id
                         /
                        linearLayoutId = Integer.parseInt("1" + Integer.toString(i + 1) + layoutCount + 1);
                        callback.formLinearLayout(linearLayoutId);
                        layoutCount = layoutCount + 1;
                        layoutMessagesCount = layoutMessagesCount - 4;
                    }

                    if (list.size() != 0) {

                        List<CurrentDayMessageSendersRealmList> senderList = new ArrayList<>();
                        senderList.add(new CurrentDayMessageSendersRealmList(senders.get(j).getName(), list));
                        recyclerViewId = Integer.parseInt("2" + Integer.toString(i + 1) + Integer.toString(layoutCount) + Integer.toString(++layoutMessagesCount));
                        /**
                         * form a recycler view and append it to layout.
                         /
                        callback.formRecyclerView(senderList, i + 1, layoutCount, layoutMessagesCount, recyclerViewId);
                    }
                }
                /**
                 * finally make a linear layout and append all the above layouts to it
                 * and append this new layout to activity layout.
                 /
                for (int m = 0; m < layoutCount; m++) {
                    callback.addDayLinearLayout(Integer.parseInt("1" + Integer.toString(i + 1) + Integer.toString(m + 1)));
                }
                /
                }
                formGridView(i + 1, currentDayList);
            }
        */
        }
    }

    private void offlineMessages(){

        RealmResults<Sender> senderRealmResults = realm.where(Sender.class).findAll();
        List<Sender> senders = realm.copyFromRealm(senderRealmResults);
        for (int i = lastCount; i < 1; i++) {

            lastCount = 1;
            int layoutCount = 1;
            int layoutMessagesCount = 0;
            int linearLayoutId = Integer.parseInt("1" + Integer.toString(i + 1) + Integer.toString(layoutCount));
            callback.formLinearLayout(linearLayoutId);

            for (int j = 0; j < senders.size(); j++) {

                RealmResults<Message> realmResults = realm.where(Message.class).equalTo("sender", senders.get(j).getName()).findAll();
                List<Message> messages = realm.copyFromRealm(realmResults);
                RealmList<Message> list = new RealmList<>();

                for (int k = 0; k < messages.size(); k++) {

                    String date = messages.get(k).getPayload().getHeaders().get(2).getValue().split(",")[1];
                    String[] words = date.split("\\s");

                    SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
                    String day = dayFormat.format(Calendar.getInstance().getTime());

                    SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
                    String month = monthFormat.format(Calendar.getInstance().getTime());

                    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
                    String year = yearFormat.format(Calendar.getInstance().getTime());

                    Calendar currentDate = Calendar.getInstance();
                    currentDate.set(Integer.parseInt(year), getMonthNo(month), Integer.parseInt(day), 0, 0);
                    Calendar messageDate = Calendar.getInstance();
                    messageDate.set(Integer.parseInt(words[3]), getMonthNo(words[2]), Integer.parseInt(words[1]), 0, 0);
                    list.add(messages.get(k));
                    currentDayMessageSendersRealmList.add(new CurrentDayMessageSendersRealmList(senders.get(j).getName(), list));
                    if (calendarDaysBetween(currentDate, messageDate) == i) {
                        list.add(messages.get(k));
                        currentDayMessageSendersRealmList.add(new CurrentDayMessageSendersRealmList(senders.get(j).getName(), list));
                    }
                }
            }

            /*
                if (layoutMessagesCount == 4) {
                    /**
                     * form a linear layout with below linear layout id
                     /
                    linearLayoutId = Integer.parseInt("1" + Integer.toString(i + 1) + layoutCount + 1);
                    callback.formLinearLayout(linearLayoutId);
                    layoutCount = layoutCount + 1;
                    layoutMessagesCount = layoutMessagesCount - 4;
                }

                if (list.size() != 0) {

                    List<CurrentDayMessageSendersRealmList> senderList = new ArrayList<>();
                    senderList.add(new CurrentDayMessageSendersRealmList(senders.get(j).getName(), list));
                    recyclerViewId = Integer.parseInt("2" + Integer.toString(i + 1) + Integer.toString(layoutCount) + Integer.toString(++layoutMessagesCount));
                    /**
                     * form a recycler view and append it to layout.
                     /
                    callback.formRecyclerView(senderList, i + 1, layoutCount, layoutMessagesCount, recyclerViewId);
                }
            }
            /**
             * finally make a linear layout and append all the above layouts to it
             * and append this new layout to activity layout.
             /
            for (int m = 0; m < layoutCount; m++) {
                callback.addDayLinearLayout(Integer.parseInt("1" + Integer.toString(i + 1) + Integer.toString(m + 1)));
            }
            */
        }
    }

    private void classifyMessages(int count, int from, int to){

        RealmResults<Sender> senderRealmResults = realm.where(Sender.class).findAll();
        List<Sender> senders = realm.copyFromRealm(senderRealmResults);

        for (int i = from; i < to; i++) {

            lastCount = count;
                /*
                int layoutCount = 1;
                int layoutMessagesCount = 0;
                int linearLayoutId = Integer.parseInt("1" + Integer.toString(i + 1) + Integer.toString(layoutCount));
                */
            List<CurrentDayMessageSendersRealmList> currentDayList = new ArrayList<>();
                /*
                callback.formLinearLayout(linearLayoutId);
                */
            for (int j = 0; j < senders.size(); j++) {

                RealmResults<Message> realmResults = realm.where(Message.class).equalTo("sender", senders.get(j).getName()).findAll();
                List<Message> messages = realm.copyFromRealm(realmResults);
                RealmList<Message> list = new RealmList<>();

                for (int k = 0; k < messages.size(); k++) {

                    String date = null;
                    for (MessageHeader header : messages.get(k).getPayload().getHeaders()){
                        if(header.getName().equals("Date"))
                            date = header.getValue().split(",")[1];
                    }String[] words = date.split("\\s");

                    SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
                    String day = dayFormat.format(Calendar.getInstance().getTime());

                    SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
                    String month = monthFormat.format(Calendar.getInstance().getTime());

                    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
                    String year = yearFormat.format(Calendar.getInstance().getTime());

                    Calendar currentDate = Calendar.getInstance();
                    currentDate.set(Integer.parseInt(year), getMonthNo(month), Integer.parseInt(day), 0, 0);
                    Calendar messageDate = Calendar.getInstance();
                    messageDate.set(Integer.parseInt(words[3]), getMonthNo(words[2]), Integer.parseInt(words[1]), 0, 0);

                    if (calendarDaysBetween(currentDate, messageDate) == i) {
                        list.add(messages.get(k));
                    }
                }
                if (list.size() != 0)
                    currentDayList.add(new CurrentDayMessageSendersRealmList(senders.get(j).getName(), list));

                    /*
                    if (layoutMessagesCount == 4) {
                        /**
                         * form a linear layout with below linear layout id
                         /
                        linearLayoutId = Integer.parseInt("1" + Integer.toString(i + 1) + layoutCount + 1);
                        callback.formLinearLayout(linearLayoutId);
                        layoutCount = layoutCount + 1;
                        layoutMessagesCount = layoutMessagesCount - 4;
                    }

                    if (list.size() != 0) {

                        List<CurrentDayMessageSendersRealmList> senderList = new ArrayList<>();
                        senderList.add(new CurrentDayMessageSendersRealmList(senders.get(j).getName(), list));
                        recyclerViewId = Integer.parseInt("2" + Integer.toString(i + 1) + Integer.toString(layoutCount) + Integer.toString(++layoutMessagesCount));
                        /**
                         * form a recycler view and append it to layout.
                         /
                        callback.formRecyclerView(senderList, i + 1, layoutCount, layoutMessagesCount, recyclerViewId);
                    }
                }
                /**
                 * finally make a linear layout and append all the above layouts to it
                 * and append this new layout to activity layout.
                 /
                for (int m = 0; m < layoutCount; m++) {
                    callback.addDayLinearLayout(Integer.parseInt("1" + Integer.toString(i + 1) + Integer.toString(m + 1)));
                }
                */
            }
            formGridView(i + 1, currentDayList);
        }
    }

    private void formGridView(int day, List<CurrentDayMessageSendersRealmList> currentDayList) {

        int count = currentDayList.size();
        int numberOfRows = (int) Math.ceil((float) count / 4);
        for (i = 0, k = 0; i < numberOfRows; i++) {

            int linearLayoutId = Integer.parseInt("1" + day + Integer.toString(i + 1));
            callback.formLinearLayout(linearLayoutId);
            for (j = 0; j < 4 && k < count; j++, k++) {
                list = new ArrayList<>();
                list.add(currentDayList.get(k));
                recyclerViewId = Integer.parseInt("2" + Integer.toString(day) + Integer.toString(i + 1) + Integer.toString(j + 1));
                callback.formRecyclerView(list, day, i, j, recyclerViewId);
            }
            callback.addLinearLayout();
        }
        callback.onMessagesReceived();
    }

    /**
         * Compute the number of calendar days between two Calendar objects.
         * The desired value is the number of days of the month between the
         * two Calendars, not the number of milliseconds' worth of days.
         * @param startCal The earlier calendar
         * @param endCal The later calendar
         * @return the number of calendar days of the month between startCal and endCal
         */
    public static long calendarDaysBetween(Calendar startCal, Calendar endCal) {

        // Create copies so we don't update the original calendars.

        Calendar start = Calendar.getInstance();
        start.setTimeZone(startCal.getTimeZone());
        start.setTimeInMillis(startCal.getTimeInMillis());

        Calendar end = Calendar.getInstance();
        end.setTimeZone(endCal.getTimeZone());
        end.setTimeInMillis(endCal.getTimeInMillis());

        // Set the copies to be at midnight, but keep the day information.

        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        // At this point, each calendar is set to midnight on
        // their respective days. Now use TimeUnit.MILLISECONDS to
        // compute the number of full days between the two of them.

        return TimeUnit.MILLISECONDS.toDays(
                Math.abs(end.getTimeInMillis() - start.getTimeInMillis()));
    }

    private int getMonthNo(String month){
        switch (month){
            case "Jan":
                return 0;
            case "Feb":
                return 1;
            case "Mar":
                return 2;
            case "Apr":
                return 3;
            case "May":
                return 4;
            case "Jun":
                return 5;
            case "Jul":
                return 6;
            case "Aug":
                return 7;
            case "Sep":
                return 8;
            case "Oct":
                return 9;
            case "Nov":
                return 10;
            case "Dec":
                return 11;
        }
        return 0;
    }
}

