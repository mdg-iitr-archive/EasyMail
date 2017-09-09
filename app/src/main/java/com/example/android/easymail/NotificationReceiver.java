package com.example.android.easymail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.android.easymail.services.NotificationService;

/**
 * Created by Harshit Bansal on 7/2/2017.
 */

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String messageId = (String) intent.getExtras().get("id");
        int notificationId = (int) intent.getExtras().get("notif_id");
        Intent notifServiceIntent = new Intent(context, NotificationService.class);
        notifServiceIntent.putExtra("id", messageId);
        notifServiceIntent.putExtra("notif_id", notificationId);
        context.startService(notifServiceIntent);
    }
}
