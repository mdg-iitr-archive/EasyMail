package com.example.android.easymail;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.easymail.api.MessageApi;
import com.example.android.easymail.api.NetworkingFactory;
import com.example.android.easymail.models.Message;
import com.example.android.easymail.models.MessageHeader;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
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
import java.util.ArrayList;
import java.util.Arrays;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(scopes)).
                setBackOff(new ExponentialBackOff());

        final AuthorizationResponse response = AuthorizationResponse.fromIntent(getIntent());
        final AuthorizationException exception = AuthorizationException.fromIntent(getIntent());

        responseText = (TextView)findViewById(R.id.txt_response);
        tokenText = (TextView)findViewById(R.id.txt_token);

        if (response != null){
            //Token request to exchange the authorization code
            Toast.makeText(context, "Authorization response completed", Toast.LENGTH_SHORT).show();
            TextView responseText = (TextView)findViewById(R.id.txt_response);
            responseText.setText(response.toString());
            AuthorizationService service = new AuthorizationService(context);
            service.performTokenRequest(
                    response.createTokenExchangeRequest(),
                    new AuthorizationService.TokenResponseCallback(){
                        @Override
                        public void onTokenRequestCompleted
                                (@Nullable TokenResponse resp, @Nullable AuthorizationException ex) {
                            if(resp !=  null){
                                //exchange succeeded
                                Toast.makeText(context, "Token Request Completed", Toast.LENGTH_SHORT).show();
                                final TextView tokenText = (TextView)findViewById(R.id.txt_token);
                                tokenText.setText(resp.accessToken);
                                accessToken = resp.accessToken;
                                AuthState state = new AuthState(response,resp,ex);
                                writeAuthState(state);
                                googleCredential = new GoogleCredential().setAccessToken(accessToken);
                                new MakeRequestTask(googleCredential).execute();

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
                            }
                            else{
                                //authorization failed\\
                                TextView responseText = (TextView)findViewById(R.id.txt_response);
                                responseText.setText(response.toString());
                                Toast.makeText(context, "Token Response Failed",Toast.LENGTH_SHORT).show();
                                AuthState state = new AuthState(response,null,ex);
                                writeAuthState(state);
                            }
                        }
                    }
            );
            //Use access token to make api request
            AuthState state = readAuthState();
            state.performActionWithFreshTokens(service, new AuthState.AuthStateAction() {
                @Override
                public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException ex) {
                    if (ex != null){
                        ex.printStackTrace();
                    }
                    //use the access token to do something

                }
            });
        }
        else{
            Toast.makeText(context, "Authorization response failed", Toast.LENGTH_SHORT).show();
        }
    }

    public AuthState readAuthState() {
        SharedPreferences authPrefs = getSharedPreferences("auth", MODE_PRIVATE);
        String stateJson = authPrefs.getString("stateJson",null);
        AuthState state;
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
        SharedPreferences authPrefs = getSharedPreferences("auth", MODE_PRIVATE);
        authPrefs.edit()
                .putString("stateJson", state.jsonSerializeString())
                .apply();
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, com.google.api.services.gmail.model.Message>{

        private com.google.api.services.gmail.Gmail service = null;
        private Exception lastError = null;

        MakeRequestTask(GoogleCredential credential){

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            service = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential
            ).setApplicationName("Gmail Api").build();
        }

        @Override
        protected com.google.api.services.gmail.model.Message doInBackground(Void... params) {
            try{
                return getMessageFromApi();
            } catch (Exception c){
                lastError = c;
                cancel(true);
                return null;
            }
        }

        private com.google.api.services.gmail.model.Message getMessageFromApi() throws IOException{

            String user = "harshit.bansalec@gmail.com";
            List<com.google.api.services.gmail.model.Message> messages = new ArrayList<com.google.api.services.gmail.model.Message>();
            com.google.api.services.gmail.model.Message message =
                    service.users().messages().get(user,"15c250a16e894dc8").execute();

            return message;
        }
        @Override
        protected void onPostExecute(com.google.api.services.gmail.model.Message output) {

            if (output == null) {
                responseText.setText("No results returned.");
            } else {
                tokenText.setText(output.getPayload().getHeaders().get(21).getValue());
            }
        }
    }
    }
