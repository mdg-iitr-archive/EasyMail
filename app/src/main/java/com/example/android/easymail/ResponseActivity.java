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
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
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
    TextView responseText;
    TextView tokenText;
    ProgressDialog progressDialog;
    Button animationButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(scopes)).
                setBackOff(new ExponentialBackOff());

        final AuthorizationResponse response = AuthorizationResponse.fromIntent(getIntent());
        final AuthorizationException exception = AuthorizationException.fromIntent(getIntent());

        responseText = (TextView) findViewById(R.id.txt_response);
        tokenText = (TextView) findViewById(R.id.txt_token);
        animationButton = (Button) findViewById(R.id.animation);
        animationButton.setVisibility(View.GONE);
        /*
        animationButton.setOnTouchListener(new View.OnTouchListener() {

            private static final int MIN_CLICK_TIME = 1000;
            private long startClickTime;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_UP
                    return false;
                }
            }
        });
*/
        Bundle extras = getIntent().getExtras();
        String isAutoSignedInToken = extras.getString("is_auto_signed_in_token");
        if( isAutoSignedInToken != null ){
            performTask(isAutoSignedInToken);
        }

        if (response != null){
                    //use the access token to do something
            Toast.makeText(context, "Authorization response completed", Toast.LENGTH_SHORT).show();
            TextView responseText = (TextView) findViewById(R.id.txt_response);
            AuthorizationService service = new AuthorizationService(context);
            service.performTokenRequest(
                    response.createTokenExchangeRequest(),
                    new AuthorizationService.TokenResponseCallback() {
                        @Override
                        public void onTokenRequestCompleted(@Nullable TokenResponse resp, @Nullable AuthorizationException ex) {
                            if (resp != null) {
                                        //exchange succeeded
                                Toast.makeText(context, "Token Request Completed", Toast.LENGTH_SHORT).show();
                                final TextView tokenText = (TextView) findViewById(R.id.txt_token);
                                //tokenText.setText(resp.accessToken);
                                accessToken = resp.accessToken;
                                AuthState state = new AuthState(response, resp, ex);
                                writeAuthState(state);
                                performTask(accessToken);

                                /*
                                MessageApi messageApi = NetworkingFactory.getClient(accessToken).create(MessageApi.class);
                                Call<Message> message = messageApi.getMessage(accessToken, "harshit.bansalec@gmail.com", "15c250a16e894dc8");
                                message.enqueue(new Callback<Message>() {
                                    @Override
                                    public void onResponse(Call<Message> call, Response<Message> response) {
                                        tokenText.setText(response.toString());
                                        ArrayList<MessageHeader> headerList = response.body().getPayload().getHeaders();
                                        String subject = headerList.get(21).getValue();
                                        TextView tokenText = (TextView)findViewById(R.id.txt_token);
                                        tokenText.setText(subject);
                                    }

                                    @Override
                                    public void onFailure(Call<Message> call, Throwable t) {
                                        Toast.makeText(ResponseActivity.this, "Message Response Failed", Toast.LENGTH_SHORT).show();
                                        TextView tokenText = (TextView)findViewById(R.id.txt_token);
                                        tokenText.setText(t.toString());
                                    }
                                });
                                */
                                    } else {
                                        //authorization failed\\
                                        TextView responseText = (TextView) findViewById(R.id.txt_response);
                                        responseText.setText(response.toString());
                                        Toast.makeText(context, "Token Response Failed", Toast.LENGTH_SHORT).show();
                                        AuthState state = new AuthState(response, null, ex);
                                        writeAuthState(state);
                                    }
                                }
                            }
                    );
                    //Use access token to make api request
                    AuthState state = readAuthState();

                } else {
                    Toast.makeText(context, "Authorization response failed", Toast.LENGTH_SHORT).show();
                }


        }

    public void performTask(String accessToken) {

        googleCredential = new GoogleCredential().setAccessToken(accessToken);
        progressDialog = new ProgressDialog(ResponseActivity.this);
        progressDialog.setMessage("Calling Gmail API ...");
        new MakeMessageRequestTask(googleCredential).execute();

    }

    /*
    if (response == null) {
        //Token request to exchange the authorization code
        Toast.makeText(context, "Authorization response completed", Toast.LENGTH_SHORT).show();
        TextView responseText = (TextView) findViewById(R.id.txt_response);
        responseText.setText(response.toString());
        AuthorizationService service = new AuthorizationService(context);
        service.performTokenRequest(
                response.createTokenExchangeRequest(),
                new AuthorizationService.TokenResponseCallback() {
                    @Override
                    public void onTokenRequestCompleted
                            (@Nullable TokenResponse resp, @Nullable AuthorizationException ex) {
                        if (resp != null) {
                            //exchange succeeded
                            Toast.makeText(context, "Token Request Completed", Toast.LENGTH_SHORT).show();
                            final TextView tokenText = (TextView) findViewById(R.id.txt_token);
                            //tokenText.setText(resp.accessToken);
                            accessToken = resp.accessToken;
                            AuthState state = new AuthState(response, resp, ex);
                            writeAuthState(state);
                            googleCredential = new GoogleCredential().setAccessToken(readAuthState().getAccessToken());
                            progressDialog = new ProgressDialog(ResponseActivity.this);
                            progressDialog.setMessage("Calling Gmail API ...");
                            new MakeMessageRequestTask(googleCredential).execute();
                            /*
                            MessageApi messageApi = NetworkingFactory.getClient(accessToken).create(MessageApi.class);
                            Call<Message> message = messageApi.getMessage(accessToken, "harshit.bansalec@gmail.com", "15c250a16e894dc8");
                            message.enqueue(new Callback<Message>() {
                                @Override
                                public void onResponse(Call<Message> call, Response<Message> response) {
                                    tokenText.setText(response.toString());
                                    ArrayList<MessageHeader> headerList = response.body().getPayload().getHeaders();
                                    String subject = headerList.get(21).getValue();
                                    TextView tokenText = (TextView)findViewById(R.id.txt_token);
                                    tokenText.setText(subject);
                                }

                                @Override
                                public void onFailure(Call<Message> call, Throwable t) {
                                    Toast.makeText(ResponseActivity.this, "Message Response Failed", Toast.LENGTH_SHORT).show();
                                    TextView tokenText = (TextView)findViewById(R.id.txt_token);
                                    tokenText.setText(t.toString());
                                }
                            });
                            */
        /*
                            } else {
                                //authorization failed\\
                                TextView responseText = (TextView) findViewById(R.id.txt_response);
                                responseText.setText(response.toString());
                                Toast.makeText(context, "Token Response Failed", Toast.LENGTH_SHORT).show();
                                AuthState state = new AuthState(response, null, ex);
                                writeAuthState(state);
                            }
                        }
                    }
            );
            //Use access token to make api request
            AuthState state = readAuthState();

        } else {
            Toast.makeText(context, "Authorization response failed", Toast.LENGTH_SHORT).show();
        }

*/
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
                tokenText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            progressDialog.hide();
        }
    }
}
