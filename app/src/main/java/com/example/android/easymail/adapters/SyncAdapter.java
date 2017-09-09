package com.example.android.easymail.adapters;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.android.easymail.R;
import com.example.android.easymail.ResponseActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Harshit Bansal on 6/23/2017.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private Context context;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        this.context = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        Log.i("sync", account.toString());
    }

    /**
     * method to create notification
     * while synchronising mails
     * for the current day
     */
    public void createNotification(){

        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(context.getResources().getString(R.string.txt_sync));

        // Creates an explicit intent for an Activity in the app
        Intent resultIntent = new Intent(context, ResponseActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // the application to the Home screen.
        // Not needed in the present scenario,
        // but needed if the activity is changed
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ResponseActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        // Gets an instance of the NotificationManager service
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        notificationManager.notify(1000, builder.build());
    }
}
