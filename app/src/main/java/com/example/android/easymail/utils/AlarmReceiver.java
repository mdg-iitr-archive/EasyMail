package com.example.android.easymail.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.android.easymail.AlarmReceiverActivity;

/**
 * Created by Harshit Bansal on 7/16/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String messageId = (String) intent.getExtras().get("id");
        int alarmId = (int) intent.getExtras().get("alarm_id");
        Intent alarmReceiverIntent =  new Intent(context, AlarmReceiverActivity.class);
        alarmReceiverIntent.putExtra("id", messageId);
        alarmReceiverIntent.putExtra("notif_id", alarmId);
        alarmReceiverIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmReceiverIntent);
    }
}
