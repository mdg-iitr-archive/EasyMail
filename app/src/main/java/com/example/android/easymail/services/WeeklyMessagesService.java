package com.example.android.easymail.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.example.android.easymail.models.HashTable;
import com.example.android.easymail.models.MessageBody;
import com.example.android.easymail.models.MessageHeader;
import com.example.android.easymail.models.MessagePart;
import com.example.android.easymail.models.MessagePayload;
import com.example.android.easymail.models.MessageStatus;
import com.example.android.easymail.models.RealmString;
import com.example.android.easymail.models.Sender;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.StringUtils;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by Harshit Bansal on 7/4/2017.
 */

public class WeeklyMessagesService extends IntentService {

    List<Sender> senders = new ArrayList<>();

    public WeeklyMessagesService() {
        super("");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WeeklyMessagesService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Realm.init(this);
        Realm realm = Realm.getInstance(new RealmConfiguration.Builder().build());

        String accessToken = (String) intent.getExtras().get("token");
        String mostRecentMessageId = (String) intent.getExtras().get("most_recent_id");
        ResultReceiver rec = intent.getParcelableExtra("receiverTag");

        RealmResults<MessageStatus> statusResults = realm.where(MessageStatus.class).findAll();
        List<MessageStatus> day1 = realm.copyFromRealm(statusResults);

        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        final com.google.api.services.gmail.Gmail service = new com.google.api.services.gmail.Gmail.Builder(
                transport, jsonFactory, credential
        ).setApplicationName("Gmail Api").build();
        String user = "harshit.bansalec@gmail.com";

        ListMessagesResponse listMessagesResponse = null;

        if (mostRecentMessageId == null) {

            boolean isCompleted = false;
            // the below task is only to update the mostRecentMessageId
            try {
                mostRecentMessageId = service.users().messages().list(user).execute().getMessages().get(0).getId();
                // save mostRecentMessageId into shared preferences
                Bundle bundle = new Bundle();
                bundle.putString("most_recent_message_id", mostRecentMessageId);
                rec.send(1, bundle);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String nextPageToken = null;

            while (!isCompleted) {
                try {
                    listMessagesResponse = service.users().messages().list(user).setPageToken(nextPageToken).execute();
                    nextPageToken = listMessagesResponse.getNextPageToken();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < listMessagesResponse.getMessages().size(); i++) {

                    day1.add(new MessageStatus(listMessagesResponse.getMessages().get(i).getId(), 0));

                    if (i == listMessagesResponse.getMessages().size() - 1) {
                        try {
                            String pageLastId = listMessagesResponse.getMessages().get(i).getId();
                            Message message = service.users().messages().get(user, pageLastId).execute();
                            String date = message.getPayload().getHeaders().get(1).getValue().split(",")[1];
                            if (date != null) {
                                String[] words = date.split("\\s");

                                SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
                                String day = dayFormat.format(Calendar.getInstance().getTime());

                                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
                                String month = monthFormat.format(Calendar.getInstance().getTime());

                                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
                                String year = yearFormat.format(Calendar.getInstance().getTime());
                                Calendar currentDate = GregorianCalendar.getInstance();
                                currentDate.set(Integer.parseInt(year), getMonthNo(month), Integer.parseInt(day), 0, 0);
                                Calendar lastIdDate = GregorianCalendar.getInstance();
                                lastIdDate.set(Integer.parseInt(words[3]), getMonthNo(words[2]), Integer.parseInt(words[1]), 0, 0);
                                Log.e("time", String.valueOf(currentDate.getTime()));
                                Log.e("time", String.valueOf(lastIdDate.getTime()));
                                lastIdDate.add(Calendar.DATE, 7);
                                Log.e("time", String.valueOf(lastIdDate.getTime()));
                                if (!lastIdDate.after(currentDate)) {
                                    isCompleted = true;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            try {
                listMessagesResponse = service.users().messages().list(user).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            int i = 0;
            while (!mostRecentMessageId.equals(listMessagesResponse.getMessages().get(i).getId())) {
                // add to the list till the two become equal updating the most recent message id
                day1.add(new MessageStatus(listMessagesResponse.getMessages().get(i).getId(), 0));
                i = i + 1;
            }
        }

        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
        String day = dayFormat.format(Calendar.getInstance().getTime());

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        String month = monthFormat.format(Calendar.getInstance().getTime());

        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        String year = yearFormat.format(Calendar.getInstance().getTime());

        Calendar currentDate = Calendar.getInstance();
        currentDate.set(Integer.parseInt(year), getMonthNo(month), Integer.parseInt(day), 0, 0);

        Calendar previousDate = Calendar.getInstance();
        previousDate.set(Integer.parseInt(year), getMonthNo(month), Integer.parseInt(day), 0, 0);

        for (int i = 0; i < day1.size(); i++) {

            if (day1.get(i).getIsStored() == 0) {

                String sender = null;
                // download the message and check the date difference.
                String id = day1.get(i).getId();
                Message message = null;

                try {
                    message = service.users().messages().get(user, id).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (message != null) {

                    String date = null;
                    try {
                        for (MessagePartHeader header : message.getPayload().getHeaders()) {
                            if (header.getName().equals("Date"))
                                date = header.getValue().split(",")[1];
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (date != null) {
                        String[] words = date.split("\\s");
                        // check if date is older then a week from the present date
                        // if so break the for loop and delete all the ids obtained till
                        // then.
                        Calendar messageDate = Calendar.getInstance();
                        int monthNo = getMonthNo(words[2]);
                        messageDate.set(Integer.parseInt(words[3]), monthNo, Integer.parseInt(words[1]), 0, 0);
                        int daysBetween = (int) calendarDaysBetween(messageDate, previousDate);
                        Calendar sevenDaysLater = Calendar.getInstance();
                        sevenDaysLater.setTimeInMillis(messageDate.getTimeInMillis());
                        Log.i("messageDate", String.valueOf(messageDate));
                        Log.i("sevenDaysLater", String.valueOf(sevenDaysLater));
                        sevenDaysLater.add(Calendar.DATE, 7);

                        if (!sevenDaysLater.after(currentDate)) {
                            // clear the entire sublist after this date
                            day1.subList(i, day1.size()).clear();
                            // make a callback to the activity that the
                            // messages of the end day of the week have
                            // been fetched.
                            Bundle bundle = new Bundle();
                            bundle.putInt("days_between", 7);
                            rec.send(0, bundle);
                            break;
                        }

                        if (daysBetween >= 1) {
                            previousDate.setTimeInMillis(messageDate.getTimeInMillis());
                            Bundle bundle = new Bundle();
                            bundle.putInt("days_between", (int) calendarDaysBetween(messageDate, currentDate));
                            rec.send(0, bundle);
                        }

                        com.example.android.easymail.models.Message modifiedMessage = new com.example.android.easymail.models.Message();
                        modifiedMessage.setId(message.getId());
                        modifiedMessage.setThreadId(message.getThreadId());
                        RealmList<RealmString> stringList = new RealmList<>();
                        for (String labelId : message.getLabelIds()) {
                            stringList.add(new RealmString(labelId));
                        }
                        modifiedMessage.setLabelIds(stringList);
                        modifiedMessage.setSnippet(message.getSnippet());

                        String mimeType = message.getPayload().getMimeType();
                        RealmList<MessageHeader> headers = new RealmList<>();
                        for (MessagePartHeader header : message.getPayload().getHeaders()) {
                            String name = header.getName();
                            if (name.equals("From")) {
                                sender = header.getValue();
                                modifiedMessage.setSender(sender);
                                senders.add(new Sender(sender));
                            }
                            headers.add(new MessageHeader(header.getName(), header.getValue()));
                        }
                        RealmList<MessagePart> parts = new RealmList<>();
                        if (message.getPayload().getParts() != null) {
                            for (com.google.api.services.gmail.model.MessagePart part : message.getPayload().getParts()) {
                                String partMimeType = part.getMimeType();
                                MessageBody body = new MessageBody(part.getBody().getData(),
                                        part.getBody().getSize());
                                RealmList<MessageHeader> partHeader = new RealmList<>();
                                for (MessagePartHeader pHeader : part.getHeaders()) {
                                    partHeader.add(new MessageHeader(pHeader.getName(), pHeader.getValue()));
                                }
                                String partId = part.getPartId();
                                String filename = part.getFilename();
                                parts.add(new MessagePart(partMimeType, partHeader, body, partId, filename));
                                if (filename != null && filename.length() > 0) {
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

                        day1.get(i).setIsStored(1);
                        realm.beginTransaction();
                        realm.copyToRealmOrUpdate(modifiedMessage);
                        realm.copyToRealmOrUpdate(day1.get(i));
                        realm.copyToRealmOrUpdate(new Sender(sender));
                        realm.commitTransaction();
                    }
                }
            }
        }
    }

    /**
     * Compute the number of calendar days between two Calendar objects.
     * The desired value is the number of days of the month between the
     * two Calendars, not the number of milliseconds' worth of days.
     *
     * @param startCal The earlier calendar
     * @param endCal   The later calendar
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
        long ans = TimeUnit.MILLISECONDS.toDays(
                Math.abs(end.getTimeInMillis() - start.getTimeInMillis()));
        return ans;
    }

    public int getMonthNo(String month) {
        switch (month) {
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
