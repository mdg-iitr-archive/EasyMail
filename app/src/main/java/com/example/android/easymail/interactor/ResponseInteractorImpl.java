package com.example.android.easymail.interactor;

import com.example.android.easymail.ResponseActivity;
import com.example.android.easymail.models.CurrentDayMessageSendersList;
import com.example.android.easymail.models.HashTable;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

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
import android.os.Handler;


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
    private int i, j , k , recyclerViewId;
    List<CurrentDayMessageSendersList> list;

    @Override
    public void performMesssageRequestTask(final ResponseInteractor.PresenterCallback callback, String accessToken, final AuthorizationResponse response, AuthorizationService service){

        GoogleCredential googleCredential = new GoogleCredential().setAccessToken(accessToken);
        if (accessToken != null){
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
            callback.autoSignedInTokenReceived();
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
                                callback.onMakeTokenRequest();
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
                ArrayList<Message> currentDayMessages = new ArrayList<>();

                ListMessagesResponse listMessagesResponse = null;
                try {
                    listMessagesResponse = service.users().messages().list(user).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (Message message : listMessagesResponse.getMessages())
                    messagesId.add(message.getId());

                for (String messageId : messagesId) {
                    Message message = null;
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
                    } else {
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
                    for (Message message : currentDayMessages) {
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

                            List<Message> list = hashTable.vals.get(i);
                            currentDayMessageSendersList.add(new CurrentDayMessageSendersList(hashTable.keys[i], hashTable.vals.get(i)));
                        }
                    }
                    formMessagesGridView(callback, currentDayMessageSendersList.size());
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
