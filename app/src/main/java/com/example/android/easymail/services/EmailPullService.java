package com.example.android.easymail.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;

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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by harshit on 15/10/17.
 */

public class EmailPullService extends IntentService {

    private Realm realm;
    private Long totalCount;
    private Long count = 0L;

    long i;

    public EmailPullService() {
        super("");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        realm = Realm.getDefaultInstance();
        Boolean isSync = (Boolean) intent.getExtras().get("isSync");
        String accessToken = (String) intent.getExtras().get("token");
        Long date = (Long) intent.getExtras().get("date");
        Long failedEmailNumber = (Long) intent.getExtras().get("failed_email_number");
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        final com.google.api.services.gmail.Gmail service = new com.google.api.services.gmail.Gmail.Builder(
                transport, jsonFactory, credential
        ).setApplicationName("Gmail Api").build();
        String user = "me";
        String queryString = formQueryString(date);
        try {
            List<String> labelIds = new ArrayList<>();
            labelIds.add(Constants.UPDATES_LABEL);
            ListMessagesResponse listMessagesResponse =
                    service.users().messages().list(user).setLabelIds(labelIds)
                            .setQ(queryString).execute();
            totalCount = listMessagesResponse.getResultSizeEstimate();

            List<Message> messages = listMessagesResponse.getMessages();
            if (isSync) Collections.reverse(messages);
            i = failedEmailNumber;
            if (failedEmailNumber == 1000L){
                i = 0;
            }
            for (; i <  messages.size(); i++){
                Message message = messages.get((int)i);
                parseGmailMessage(date, service, user, message.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String status;
        failedEmailNumber = 1000L;

        Boolean isPresentDay = (Boolean) intent.getExtras().get("isPresentDay");

        if (isPresentDay){
            status = "failure";
            if (failedEmailNumber == 1000L) failedEmailNumber = 0L;
            failedEmailNumber = failedEmailNumber + count;
        }else {
            if (Objects.equals(i, totalCount)) {
                status = "success";
                failedEmailNumber = 1000L;
                if (isSync) date = getMidnightDate(date) + 86400;
                else date = date - 86400;
            } else {
                status = "failure";
                if (failedEmailNumber == 1000L) failedEmailNumber = 0L;
                failedEmailNumber = failedEmailNumber + count;
            }
        }

        Intent localIntent =
                new Intent(Constants.BROADCAST_ACTION_EMAIL).
                        putExtra(Constants.EXTENDED_DATA_STATUS, status).
                        putExtra("date", date).
                        putExtra("failed_email_number", failedEmailNumber);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private String formQueryString(Long date) {
        String afterDate = Long.toString(date);
        String beforeDate = Long.toString(getMidnightDate(date) + 86400);
        return "after:" + afterDate + " " + "before:" + beforeDate;
    }

    private Long getMidnightDate(Long mostRecentMessageDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mostRecentMessageDate * 1000);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        TimeZone timeZone = TimeZone.getDefault();
        calendar.setTimeZone(timeZone);
        return calendar.getTimeInMillis()/1000;
    }

    private void parseGmailMessage(Long date, Gmail service, String user, String messageId) {
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
            RealmList<RealmString> stringList = new RealmList<>();
            for (String labelId : message.getLabelIds()) {
                stringList.add(new RealmString(labelId));
            }
            modifiedMessage.setLabelIds(stringList);
            modifiedMessage.setSnippet(message.getSnippet());
            modifiedMessage.setDate(date);
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
