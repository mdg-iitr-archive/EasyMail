package com.example.android.easymail.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.example.android.easymail.utils.Constants;
import com.example.android.easymail.CustomListMessageDetailActivity;
import com.example.android.easymail.R;
import com.example.android.easymail.models.Message;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by Harshit Bansal on 7/2/2017.
 */

public class NotificationService extends Service {

    private Realm realm;
    private Message message;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // get an instance of realm to find the subject
        // of the message saved in the custom list.
        RealmConfiguration configuration = new RealmConfiguration.Builder().build();
        realm = Realm.getInstance(configuration);
        // get the id of the message from the intent extra
        // to get the message from realm db.
        String messageId = (String) intent.getExtras().get("id");
        int notificationId = (int) intent.getExtras().get("notif_id");
        // get the appropriate message from realm results
        RealmResults<Message> results = realm.where(Message.class).equalTo("id", messageId).findAll();
        message = realm.copyFromRealm(results).get(0);
        //call the method to create notification.
        createNotification(messageId, notificationId);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Used to create notification for the event.
     * @param messageId used to display the text message
     *                  in the big text view and pass it
     *                  as an extra in case of a button
     *                  click in the expanded view
     */
    public void createNotification(String messageId, int notificationId) {
        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(this.getResources().getString(R.string.txt_alarm_service))
                .setContentText(this.getResources().getString(R.string.txt_sync));

        // Creates an explicit intent for an Activity in the app
        Intent resultIntent = new Intent(this, CustomListMessageDetailActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // the application to the Home screen.
        // Not needed in the present scenario,
        // but needed if the activity is changed
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(CustomListMessageDetailActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        /*
         * Sets up the Snooze and Dismiss action buttons that will appear in the
         * big view of the notification.
         */
        Intent dismissIntent = new Intent(this, PingService.class);
        dismissIntent.setAction(Constants.ACTION_DISMISS);
        dismissIntent.putExtra("id", messageId);
        dismissIntent.putExtra("notif_id", notificationId);
        PendingIntent piDismiss = PendingIntent.getService(this, 0, dismissIntent, 0);

        Intent snoozeIntent = new Intent(this, PingService.class);
        snoozeIntent.setAction(Constants.ACTION_SNOOZE);
        snoozeIntent.putExtra("id", messageId);
        snoozeIntent.putExtra("notif_id", notificationId);
        PendingIntent piSnooze = PendingIntent.getService(this, 0, snoozeIntent, 0);
        /*
         * Sets the big view "big text" style and supplies the
         * text (the user's reminder message) that will be displayed
         * in the detail area of the expanded notification.
         * These calls are ignored by the support library for
         * pre-4.1 devices.
         */
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message.getCustomListDetails().getSubject()))
                .addAction(R.drawable.ic_clear_black_24dp,
                        getString(R.string.dismiss), piDismiss)
                .addAction(R.drawable.ic_hotel_black_24dp,
                        getString(R.string.snooze), piSnooze);
        // Gets an instance of the NotificationManager service
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        notificationManager.notify(notificationId, builder.build());
    }
}
