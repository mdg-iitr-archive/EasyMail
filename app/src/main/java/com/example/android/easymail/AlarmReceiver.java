package com.example.android.easymail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.android.easymail.models.Time;
import com.example.android.easymail.services.AlarmService;

/**
 * Created by Harshit Bansal on 7/2/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String messageId = (String) intent.getExtras().get("id");
        int notificationId = (int) intent.getExtras().get("notif-id");
        Intent alarmServiceIntent = new Intent(context, AlarmService.class);
        alarmServiceIntent.putExtra("id", messageId);
        alarmServiceIntent.putExtra("notif_id", notificationId);
        context.startService(alarmServiceIntent);
    }
}
