package com.example.android.easymail;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.android.easymail.models.Message;
import com.example.android.easymail.models.Time;
import com.example.android.easymail.utils.AlarmReceiver;
import com.example.android.easymail.utils.NotificationReceiver;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class CustomListMessageDetailActivity extends AppCompatActivity {

    private Realm realm;
    private Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_list_message_detail);

        // initialise realm
        realm = Realm.getDefaultInstance();

        // get the first message with the id obtained
        // from the intent.
        RealmResults<Message> messageResult = realm.where(Message.class).equalTo("id", (String) getIntent().getExtras().get("messageId")).findAll();
        message = realm.copyFromRealm(messageResult).get(0);

        boolean isNotifEnabled = message.getCustomListDetails().isNotifEnabled();
        // configure the alarm for the action.
        if (isNotifEnabled) setNotificationService(message.getCustomListDetails().getTime());

        boolean isAlarmEnabled = message.getCustomListDetails().isAlarmEnabled();
        // configure the alarm for the action.
        if (isAlarmEnabled) setAlarmService(message.getCustomListDetails().getTime());
    }

    /**
     * start notifiaction service
     * @param time Used to get the time of triggering of notifiaction
     */
    @TargetApi(Build.VERSION_CODES.N)
    private void setNotificationService(Time time) {

        Intent notifReceiverIntent = new Intent(this, NotificationReceiver.class);
        notifReceiverIntent.putExtra("id", message.getId());
        notifReceiverIntent.putExtra("notif_id", 1000);
        PendingIntent notifPendingIntent = PendingIntent.getBroadcast(this, 0, notifReceiverIntent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.set(time.getYear(), time.getMonth(), time.getDayOfMonth(), time.getHourOfDay(), time.getMinute());
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), notifPendingIntent);
    }

    /**
     * start alarm service
     * @param time Used to get the time of triggering of alarm
     */
    @TargetApi(Build.VERSION_CODES.N)
    public void setAlarmService(Time time) {

        Intent alarmReceiverIntent = new Intent(this, AlarmReceiver.class);
        alarmReceiverIntent.putExtra("id", message.getId());
        alarmReceiverIntent.putExtra("alarm_id", 1000);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, 0, alarmReceiverIntent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.set(time.getYear(), time.getMonth(), time.getDayOfMonth(), time.getHourOfDay(), time.getMinute());
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
    }

    /**
     * parse the time string into the required format
     * @param time Used to find the hour and minute of day from the string time.
     * @return String with integer hour and minute separated by a colon
     */
    private String getHourAndMinute(String time){

        String amOrPm = time.split(" ")[1];
        int standardFormatHour = Integer.parseInt(time.substring(0, time.indexOf(":")));
        int standardFormatTime = Integer.parseInt(time.substring(time.indexOf(":") + 1, time.indexOf(" ")));
        if (amOrPm.equals("PM")) standardFormatHour = standardFormatHour + 12;
        return Integer.toString(standardFormatHour) + ":" + Integer.toString(standardFormatTime);
    }
}
