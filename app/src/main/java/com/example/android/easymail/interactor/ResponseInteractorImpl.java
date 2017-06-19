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
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
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
import java.util.List;

import android.app.Notification;
import android.content.Context;
import android.os.Handler;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;


/**
 * Created by Harshit Bansal on 6/14/2017.
 */

public class ResponseInteractorImpl implements ResponseInteractor{

    public static final String[] scopes = {GmailScopes.GMAIL_READONLY};
    private Exception lastError = null;
    private static final int HASH_TABLE_SIZE =  100;
    public HashTable hashTable = new HashTable(HASH_TABLE_SIZE);
    private Handler handler = new Handler();
    private List<CurrentDayMessageSendersList> currentDayMessageSendersList = new ArrayList<>();
    private List<CurrentDayMessageSendersRealmList> currentDayMessageSendersRealmList = new ArrayList<>();
    private int i, j , k , recyclerViewId;
    List<CurrentDayMessageSendersList> list;
    Realm realm;

    @Override
    public void getRealmSavedMessages(ResponseInteractor.PresenterCallback callback, Context context) {

        Realm.init(context);
        RealmConfiguration configuration = new RealmConfiguration.Builder().build();
        realm = Realm.getInstance(configuration);

        RealmResults<CurrentDayMessageSendersRealmList> results = realm.where(CurrentDayMessageSendersRealmList.class).findAll();
        currentDayMessageSendersRealmList =  realm.copyFromRealm(results);
        for (CurrentDayMessageSendersRealmList list : currentDayMessageSendersRealmList) {
            String sender = list.getSender();
            List<Message> messageList = list.getSenderCurrentDayMessageList();
            currentDayMessageSendersList.add(new CurrentDayMessageSendersList(sender, messageList));
        }
        formMessagesGridView(callback, currentDayMessageSendersList.size());
    }

    @Override
    public void performMesssageRequestTask(final ResponseInteractor.PresenterCallback callback, String accessToken, final AuthorizationResponse response, AuthorizationService service){

        GoogleCredential googleCredential = new GoogleCredential().setAccessToken(accessToken);
        if (accessToken != null){
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
            //callback.autoSignedInTokenReceived();
            startBackgroundTask(callback, credential);
        }
        if (response != null){
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
                                startBackgroundTask(callback, credential);
                            }
                            else {
                                //exchange failed\\
                                callback.onExchangeFailed();
                                AuthState state = new AuthState(response, null, ex);
                                callback.writeAuthState(state);
                            }
                        }
                    }
            );
        }
        else {
            //authorization failed\\
            callback.onAuthorizationFailed();
        }
    }

    @Override
    public String[] getScopes() {
        return scopes;
    }

    private void startBackgroundTask(final ResponseInteractor.PresenterCallback callback, GoogleCredential credential) {

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        final com.google.api.services.gmail.Gmail service = new com.google.api.services.gmail.Gmail.Builder(
                transport, jsonFactory, credential
        ).setApplicationName("Gmail Api").build();

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
                                hashTable.insert(sender, message);
                                break;
                            }
                        }
                        index++;
                    }
                    for (int i = 0; i < HASH_TABLE_SIZE; i++) {
                        if (hashTable.keys[i] != null) {

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
                                modifiedList.add(modifiedMessage);
                            }
                            currentDayMessageSendersRealmList.add(new CurrentDayMessageSendersRealmList(hashTable.keys[i], modifiedList));
                            /*
                            CurrentDayMessageSendersRealmList a = new CurrentDayMessageSendersRealmList();
                            a.setSender(hashTable.keys[i]);
                            a.setSenderCurrentDayMessageList(new RealmList<Message>((Message[]) modifiedList.toArray()));
                            currentDayMessageSendersRealmList.add(a);
                            */

                        }
                    }
                    RealmConfiguration configuration = new RealmConfiguration.Builder().build();
                    realm = Realm.getInstance(configuration);
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(currentDayMessageSendersRealmList);
                    realm.commitTransaction();
                    formMessagesGridView(callback, currentDayMessageSendersRealmList.size());
                }
            }
        });
        thread.start();
    }

    public void formMessagesGridView(final ResponseInteractor.PresenterCallback callback, int count) {

        int numberOfRows = (int) Math.ceil((float)count / 4);
        for (i = 0, k = 0; i < numberOfRows; i++) {

            int linearLayoutId = Integer.parseInt("1" + Integer.toString(i+1));
            callback.formLinearLayout(linearLayoutId);
            for (j = 0; j < 4 && k < count; j++,k++){
                list = new ArrayList<>();
                list.add(currentDayMessageSendersList.get(k));
                recyclerViewId = Integer.parseInt("2" + Integer.toString(i + 1) + Integer.toString(j + 1));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.formRecyclerView(list, i, j, recyclerViewId);
                    }
                });

            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.addLinearLayout();
                }
            });
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onMessagesReceived();
            }
        });
    }
}
