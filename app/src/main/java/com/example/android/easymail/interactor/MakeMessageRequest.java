package com.example.android.easymail.interactor;

import android.os.AsyncTask;

import com.example.android.easymail.R;
import com.example.android.easymail.models.CurrentDayMessageSendersList;
import com.example.android.easymail.models.HashTable;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Harshit Bansal on 6/14/2017.
 */

public class MakeMessageRequest extends AsyncTask<Void, Void, List<Message>> {

    private com.google.api.services.gmail.Gmail service = null;
    private Exception lastError = null;
    private static final int HASH_TABLE_SIZE =  100;
    public HashTable hashTable;
    private List<Message> currentDayMessages;
    private List<CurrentDayMessageSendersList> currentDayMessageSendersList;

    MakeMessageRequest(GoogleCredential credential) {

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        service = new com.google.api.services.gmail.Gmail.Builder(
                transport, jsonFactory, credential
        ).setApplicationName("Gmail Api").build();
    }

    @Override
    protected List<Message> doInBackground(Void... params) {

        try {
            return getCurrentDayMessagesFromApi();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception c) {
            lastError = c;
            cancel(true);
            return null;
        }
    }

    private List<Message> getCurrentDayMessagesFromApi() throws IOException {

        String user = "harshit.bansalec@gmail.com";
        List<String> messagesId = new ArrayList<String>();
        ArrayList<Message> currentDayMessages = new ArrayList<>();

        ListMessagesResponse listMessagesResponse = service.users().messages().list(user).execute();
        for (Message message : listMessagesResponse.getMessages())
            messagesId.add(message.getId());

        for (String messageId : messagesId) {
            Message message =
                    service.users().messages().get(user, messageId).execute();

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
            } else
                break;
        }
        return currentDayMessages;
    }

    @Override
    protected void onPreExecute() {
        //progressDialog.show();
    }

    @Override
    protected void onPostExecute(List<Message> output) {

        //progressDialog.hide();
        if (output.size() == 0) {
            //responseText.setText(getResources().getString(R.string.NoMessagesResponseDisplayText));
        } else {
            currentDayMessages = output;
            List<String> senders = new ArrayList<>();
            int index = 0;
            for (Message message : output) {
                for (int i = 0; i < output.get(index).getPayload().getHeaders().size(); i++) {
                    String name = output.get(index).getPayload().getHeaders().get(i).getName();
                    if (name.equals("From")) {
                        String sender = output.get(index).getPayload().getHeaders().get(i).getValue();
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
        }
    }

    @Override
    protected void onCancelled() {
        //progressDialog.hide();
    }
}
