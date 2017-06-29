package com.example.android.easymail.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.android.easymail.Authenticator;
import com.google.android.gms.auth.api.Auth;

/**
 * Created by Harshit Bansal on 6/23/2017.
 */

public class AuthenticatorService extends Service {

    private Authenticator authenticator;

    @Override
    public void onCreate() {
        authenticator = new Authenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
