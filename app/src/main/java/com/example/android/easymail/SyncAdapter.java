package com.example.android.easymail;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.easymail.interactor.ResponseInteractorImpl;
import com.example.android.easymail.models.CurrentDayMessageSendersList;
import com.example.android.easymail.models.CurrentDayMessageSendersRealmList;
import com.example.android.easymail.models.HashTable;
import com.example.android.easymail.models.Message;
import com.example.android.easymail.models.MessageBody;
import com.example.android.easymail.models.MessageHeader;
import com.example.android.easymail.models.MessagePart;
import com.example.android.easymail.models.MessagePayload;
import com.example.android.easymail.models.RealmString;
import com.example.android.easymail.presenter.ResponsePresenterImpl;
import com.example.android.easymail.utils.Constants;
import com.example.android.easymail.view.ResponseActivityView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.MessagePartHeader;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;

/**
 * Created by Harshit Bansal on 6/23/2017.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    AccountManager accountManager;
    Context context;
    private static final int HASH_TABLE_SIZE = 100;
    public HashTable hashTable = new HashTable(HASH_TABLE_SIZE);
    private List<CurrentDayMessageSendersRealmList> currentDayMessageSendersRealmList = new ArrayList<>();
    private List<Message> customCurrentDayMessages = new ArrayList<>();
    Realm realm;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        this.context = context;
    }

    @Override
    public void onPerformSync(final Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        Log.i("sync", account.toString());
        Realm.init(context);
        RealmConfiguration configuration = new RealmConfiguration.Builder().build();
        realm = Realm.getInstance(configuration);

        accountManager = AccountManager.get(context);
        String deserializedAuthState = null;
        try {
            deserializedAuthState = accountManager.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE_FULL_ACCESS, true);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        AuthState state = null;
        try {
            state = AuthState.jsonDeserialize(deserializedAuthState);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        AuthorizationService service = new AuthorizationService(context);

        // obtain the fresh access token from the auth state
        state.performActionWithFreshTokens(service, new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                if (ex == null) {
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
                        } else {
                            break;
                        }
                    }

                    if (currentDayMessages.size() == 0) {

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
                                for (com.google.api.services.gmail.model.Message message : list) {
                                    Message modifiedMessage = new Message();
                                    modifiedMessage.setId(message.getId());
                                    modifiedMessage.setThreadId(message.getThreadId());
                                    RealmList<RealmString> stringList = new RealmList<>();
                                    for (String labelId : message.getLabelIds()) {
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
                                    if (message.getPayload().getParts() != null) {
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
                                    modifiedMessage.setCustomListName(null);
                                    modifiedMessage.setCustomListDetails(null);
                                    modifiedList.add(modifiedMessage);
                                    customCurrentDayMessages.add(modifiedMessage);
                                }
                                currentDayMessageSendersRealmList.add(new CurrentDayMessageSendersRealmList(hashTable.keys[i], modifiedList));
                            }
                        }
                        realm.beginTransaction();
                        realm.copyToRealmOrUpdate(currentDayMessageSendersRealmList);
                        realm.copyToRealmOrUpdate(customCurrentDayMessages);
                        realm.commitTransaction();
                    }
                }
            }
        });

    }
}

