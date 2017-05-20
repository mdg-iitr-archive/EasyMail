package com.example.android.easymail;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final String mAuthEndPoint = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String mTokenEndPoint = "https://www.googleapis.com/oauth2/v4/token";
    public static final String clientId = "449830779939-3bedim4r2ospjnbvuieofa7kv2513l3t.apps.googleusercontent.com";
    public static final String otherClientId = "449830779939-8hthqv0iijh9kb8dhmq3oi175ous7mh0.apps.googleusercontent.com";
    public static final String reverseClientId = "com.googleusercontent.apps.449830779939-3bedim4r2ospjnbvuieofa7kv2513l3t";
    public static final String reverseDomainName = "com.example.android.easymail:/oauth2redirect";
    public static final String scope = "https://www.googleapis.com/auth/gmail.readonly";
    Context context = this;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        regListeners();
    }

    private void regListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                configurationDiscoveryAndRequest();
            }
        });
    }

    private void initViews() {
        loginButton = (Button)findViewById(R.id.login_button);
    }

    private void configurationDiscoveryAndRequest(){
        Uri mAuthEndPointUri = Uri.parse(mAuthEndPoint);
        Uri mTokenEndPointURi = Uri.parse(mTokenEndPoint);
        Uri redirectUri = Uri.parse(reverseDomainName);
        final Uri issuerUri = Uri.parse("https://accounts.google.com");
        AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(mAuthEndPointUri,mTokenEndPointURi,null);

        AuthorizationRequest req = new AuthorizationRequest.Builder(
                config,
                clientId,
                ResponseTypeValues.CODE,
                redirectUri
        ).setScope(scope).build();

        AuthorizationService service = new AuthorizationService(context);
        Intent postAuthIntent = new Intent(MainActivity.this, ResponseActivity.class);
        postAuthIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent authCanceledIntent = new Intent(MainActivity.this, MainActivity.class);
        authCanceledIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        authCanceledIntent.putExtra("test","Cancel");
        service.performAuthorizationRequest(
                req,
                PendingIntent.getActivity(MainActivity.this, req.hashCode(), postAuthIntent, 0)
        );
    }

}
