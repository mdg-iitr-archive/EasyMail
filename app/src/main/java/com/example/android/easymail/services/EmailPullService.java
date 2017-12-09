package com.example.android.easymail.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.example.android.easymail.R;
import com.example.android.easymail.models.MessageHeader;
import com.example.android.easymail.models.MessagePayload;
import com.example.android.easymail.models.RealmString;
import com.example.android.easymail.utils.Constants;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by harshit on 15/10/17.
 */

public class EmailPullService extends IntentService {

    private Realm realm;
    private Long totalCount;
    private Long count = 0L;

    public EmailPullService() {
        super("");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        realm = Realm.getDefaultInstance();
        String accessToken = (String) intent.getExtras().get("token");
        String pageToken = (String) intent.getExtras().get("page_token");
        Long failedEmailNumber = (Long) intent.getExtras().get("failed_email_number");
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        final com.google.api.services.gmail.Gmail service = new com.google.api.services.gmail.Gmail.Builder(
                transport, jsonFactory, credential
        ).setApplicationName("Gmail Api").build();
        String user = "harshit.bansalec@gmail.com";
        String nextPageToken = null;
        try {
            List<String> labelIds = new ArrayList<>();
            labelIds.add(Constants.UPDATES_LABEL);
            ListMessagesResponse listMessagesResponse =
                    service.users().messages().list(user).setLabelIds(labelIds)
                            .setMaxResults(50L).setPageToken(pageToken).execute();
            nextPageToken = listMessagesResponse.getNextPageToken();
            totalCount = listMessagesResponse.getResultSizeEstimate() - 2;
            if (failedEmailNumber != 1000L){
                totalCount = totalCount - failedEmailNumber;
            }
            List<Message> messages = listMessagesResponse.getMessages();
            long i = failedEmailNumber;
            if (failedEmailNumber == 1000L){
                i = 0;
            }
            for (; i <  messages.size(); i++){
                Message message = messages.get((int)i);
                parseGmailMessage(service, user, nextPageToken, message.getId(), pageToken);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String status;
        failedEmailNumber = 1000L;
        if (Objects.equals(count, totalCount)) status = "success";
        else{
            status = "failure";
            failedEmailNumber = count;
        }
        Intent localIntent =
                new Intent(Constants.BROADCAST_ACTION_EMAIL).
                        putExtra(Constants.EXTENDED_DATA_STATUS, status).
                        putExtra("next_page_token", nextPageToken).
                        putExtra("failed_email_number", failedEmailNumber);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void parseGmailMessage(Gmail service, String user, String nextPageToken, String messageId, String pageToken) {
        String sender;
        Message message = null;
        try {
            message = service.users().messages().get(user, messageId)
                    .setFormat(Constants.METADATA_STRING)
                    .setMetadataHeaders(Arrays.asList(getResources().getStringArray(R.array.metadata_headers_list)))
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (message != null) {
            com.example.android.easymail.models.Message modifiedMessage = new com.example.android.easymail.models.Message();
            modifiedMessage.setId(message.getId());
            modifiedMessage.setThreadId(message.getThreadId());
            modifiedMessage.setPageToken(nextPageToken);
            RealmList<RealmString> stringList = new RealmList<>();
            for (String labelId : message.getLabelIds()) {
                stringList.add(new RealmString(labelId));
            }
            modifiedMessage.setLabelIds(stringList);
            modifiedMessage.setSnippet(message.getSnippet());
            modifiedMessage.setInternalDate(message.getInternalDate());
            String mimeType = message.getPayload().getMimeType();
            RealmList<MessageHeader> headers = new RealmList<>();
            for (MessagePartHeader header : message.getPayload().getHeaders()) {
                String name = header.getName();
                if (name.equals("From")) {
                    sender = header.getValue();
                    modifiedMessage.setSender(sender);
                }
                headers.add(new MessageHeader(header.getName(), header.getValue()));
            }
            modifiedMessage.setPayload(new MessagePayload(mimeType, headers, null, null , null));
            modifiedMessage.setCustomListName(null);
            modifiedMessage.setCustomListDetails(null);
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(modifiedMessage);
            realm.commitTransaction();
            count = count + 1;
        }
    }
}
