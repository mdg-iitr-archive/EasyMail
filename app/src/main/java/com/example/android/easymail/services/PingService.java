package com.example.android.easymail.services;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.android.easymail.AlarmReceiver;
import com.example.android.easymail.Constants;
import com.example.android.easymail.models.Message;
import com.example.android.easymail.models.Time;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by Harshit Bansal on 7/2/2017.
 */

public class PingService extends Service {

    private Realm realm;
    private Message message;
    private java.util.Calendar calendar;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();
        if (action.equals(Constants.ACTION_DISMISS)) {
            /**
             * the user dismissed the action, so the work has been done
             */
        }
        else if (action.equals(Constants.ACTION_SNOOZE)){

            // get the id of the message from intent extra
            String messageId = (String) intent.getExtras().get("id");
            int notificationId = (int) intent.getExtras().get("notif_id");
            /**
             * the  user has chosen to snooze the notification
             */
            // Gets an instance of the NotificationManager service
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Clear the existing notification
            notificationManager.cancel(notificationId);

            RealmConfiguration configuration = new RealmConfiguration.Builder().build();
            realm = Realm.getInstance(configuration);
            RealmResults<Message> results = realm.where(Message.class).equalTo("id", messageId).findAll();
            message = realm.copyFromRealm(results).get(0);
            Time time = message.getCustomListDetails().getTime();
            int newNotificationId = getUpdatedTime(time, notificationId);
            // Create the new alarm service
            setAlarmService(time, newNotificationId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * The method returns the calendar instance with the updated time
     * @param time Get the actual time of the notification
     * @param notificationId Get the minutes to be added to the actual time
     */
    private int getUpdatedTime(Time time, int notificationId){
        // snooze the notification for 10 minutes
        notificationId = notificationId + 10;
        calendar = java.util.Calendar.getInstance();
        calendar.set(time.getYear(), time.getMonth(), time.getDayOfMonth(), time.getHourOfDay(), time.getMinute());
        calendar.add(java.util.Calendar.MINUTE, notificationId - 1000);
        return notificationId;
    }

    /**
     * start alarm service
     * @param time Used to get the time of triggering of alarm
     */
    @TargetApi(Build.VERSION_CODES.N)
    private void setAlarmService(Time time, int newNotificationId) {

        Intent alarmServiceIntent = new Intent(this, AlarmReceiver.class);
        alarmServiceIntent.putExtra("id", message.getId());
        alarmServiceIntent.putExtra("notif_id", newNotificationId);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, 0, alarmServiceIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
    }
}
