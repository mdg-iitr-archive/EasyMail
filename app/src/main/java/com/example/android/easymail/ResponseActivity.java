package com.example.android.easymail;

import android.content.Context;
import android.content.SharedPreferences;
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

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResponseActivity extends AppCompatActivity {

    Context context = this;
    private String accessToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        final AuthorizationResponse response = AuthorizationResponse.fromIntent(getIntent());
        final AuthorizationException exception = AuthorizationException.fromIntent(getIntent());

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
                                TextView tokenText = (TextView)findViewById(R.id.txt_token);
                                tokenText.setText(resp.toString());
                                accessToken = resp.accessToken;
                                AuthState state = new AuthState(response,resp,ex);
                                writeAuthState(state);
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
                    MessageApi messageApi = NetworkingFactory.getClient(accessToken).create(MessageApi.class);
                    Call<Message> message = messageApi.getMessage(idToken, "15c250a16e894dc8");
                    message.enqueue(new Callback<Message>() {
                        @Override
                        public void onResponse(Call<Message> call, Response<Message> response) {
                            ArrayList<MessageHeader> headerList = response.body().getPayload().getHeaders();
                            String subject = headerList.get(21).getValue();
                        }

                        @Override
                        public void onFailure(Call<Message> call, Throwable t) {
                            Toast.makeText(ResponseActivity.this, "Message Response Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
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

    }
