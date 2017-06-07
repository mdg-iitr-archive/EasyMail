package com.example.android.easymail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.easymail.adapters.EmailTilesAdapter;
import com.example.android.easymail.models.CurrentDayMessageSendersList;
import com.example.android.easymail.models.HashTable;
import com.google.api.services.gmail.model.Message;

import com.example.android.easymail.api.MessageApi;
import com.example.android.easymail.api.NetworkingFactory;
import com.example.android.easymail.models.MessageHeader;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResponseActivity extends AppCompatActivity {

    Context context = this;
    private String accessToken;
    GoogleAccountCredential credential;
    GoogleCredential googleCredential;
    public static final String[] scopes = {GmailScopes.GMAIL_READONLY};
    private TextView responseText;
    private TextView tokenText;
    private RecyclerView emailNameInitialRecycler;
    private ProgressDialog progressDialog;
    private HashTable hashTable;
    private List<Message> currentDayMessages;
    private List<CurrentDayMessageSendersList> currentDayMessageSendersList;
    private static final int HASH_TABLE_SIZE =  100;
    private EmailTilesAdapter emailTilesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        initViews();
        final AuthorizationResponse response = AuthorizationResponse.fromIntent(getIntent());
        final AuthorizationException exception = AuthorizationException.fromIntent(getIntent());
        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(scopes)).
                setBackOff(new ExponentialBackOff());
        Bundle extras = getIntent().getExtras();
        String isAutoSignedInToken = extras.getString("is_auto_signed_in_token");
        if( isAutoSignedInToken != null ){
            performTask(isAutoSignedInToken);
        }

        if (response != null){
            //authorization succeeded\\
            Toast.makeText(context, "Authorization response completed", Toast.LENGTH_SHORT).show();
            AuthorizationService service = new AuthorizationService(context);
            service.performTokenRequest(
                    response.createTokenExchangeRequest(),
                    new AuthorizationService.TokenResponseCallback() {
                        @Override
                        public void onTokenRequestCompleted(@Nullable TokenResponse resp, @Nullable AuthorizationException ex) {
                            if (resp != null) {
                                //exchange succeeded\\
                                Toast.makeText(context, "Token Request Completed", Toast.LENGTH_SHORT).show();
                                accessToken = resp.accessToken;
                                AuthState state = new AuthState(response, resp, ex);
                                writeAuthState(state);
                                performTask(accessToken);
                            }
                            else {
                                //exchange failed\\
                                responseText.setText(response.toString());
                                Toast.makeText(context, "Token Response Failed", Toast.LENGTH_SHORT).show();
                                AuthState state = new AuthState(response, null, ex);
                                writeAuthState(state);
                            }
                        }
                    }
            );

        }
        else {
            //authorization failed\\
            Toast.makeText(context, "Authorization response failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews(){

        hashTable = new HashTable(HASH_TABLE_SIZE);
        currentDayMessages = new ArrayList<Message>();
        currentDayMessageSendersList = new ArrayList<>();
        responseText = (TextView) findViewById(R.id.txt_response);
        tokenText = (TextView) findViewById(R.id.txt_token);
        emailNameInitialRecycler = (RecyclerView) findViewById(R.id.email_name_tile_recycler);
        emailNameInitialRecycler.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        emailNameInitialRecycler.setLayoutManager(layoutManager);
    }

    public void performTask(String accessToken) {

        googleCredential = new GoogleCredential().setAccessToken(accessToken);
        progressDialog = new ProgressDialog(ResponseActivity.this);
        progressDialog.setMessage("Calling Gmail API ...");
        new MakeMessageRequestTask(googleCredential).execute();
    }

    public AuthState readAuthState() {

        SharedPreferences authPrefs = getSharedPreferences(context.getResources().getString(R.string.AuthSharedPref), MODE_PRIVATE);
        String stateJson = authPrefs.getString(context.getResources().getString(R.string.StateJson), null);

        if (stateJson != null) {
            try {
                return AuthState.jsonDeserialize(stateJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            return new AuthState();
        }
        return null;
    }

    public void writeAuthState(@NonNull AuthState state) {

        SharedPreferences authPrefs = getSharedPreferences(context.getResources().getString(R.string.AuthSharedPref), MODE_PRIVATE);
        authPrefs.edit()
                .putString(context.getResources().getString(R.string.StateJson), state.jsonSerializeString())
                .apply();
    }

    private class MakeMessageRequestTask extends AsyncTask<Void, Void, List<Message>> {

        private com.google.api.services.gmail.Gmail service = null;
        private Exception lastError = null;

        MakeMessageRequestTask(GoogleCredential credential) {

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            service = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential
            ).setApplicationName(getResources().getString(R.string.GmailApi)).build();
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

                String date = message.getPayload().getHeaders().get(2).getValue().split(",")[1] ;
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
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(List<Message> output) {

            progressDialog.hide();
            if (output.size() == 0) {
                responseText.setText(getResources().getString(R.string.NoMessagesResponseDisplayText));
            } else {
                currentDayMessages = output;
                List<String> senders = new ArrayList<>();
                int index = 0;
                for (Message message : output) {
                    for(int i=0; i<output.get(index).getPayload().getHeaders().size(); i++){
                        String name = output.get(index).getPayload().getHeaders().get(i).getName();
                        if(name.equals("From")){
                            String sender = output.get(index).getPayload().getHeaders().get(i).getValue();
                            hashTable.insert(sender, message);
                            break;
                        }
                    }
                    index++;
                }
                for(int i = 0; i < HASH_TABLE_SIZE; i++) {
                    if (hashTable.keys[i] != null) {
                        currentDayMessageSendersList.add(new CurrentDayMessageSendersList(hashTable.keys[i], hashTable.vals.get(i)));
                    }
                }
                emailTilesAdapter = new EmailTilesAdapter(ResponseActivity.this, currentDayMessageSendersList);
                emailNameInitialRecycler.setAdapter(emailTilesAdapter);
            }
        }

        @Override
        protected void onCancelled() {
            progressDialog.hide();
        }
    }
}
