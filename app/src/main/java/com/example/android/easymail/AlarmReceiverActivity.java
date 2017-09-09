package com.example.android.easymail;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.android.easymail.models.Message;

import java.io.IOException;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

import static android.media.AudioManager.STREAM_ALARM;

public class AlarmReceiverActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener{

    private MediaPlayer mMediaPlayer;
    private Realm realm;
    private Message message;
    private int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_alarm_receiver);

        /**
         * get an instance of realm to find the subject
         * of the message saved in the custom list.
         */
        RealmConfiguration configuration = new RealmConfiguration.Builder().build();
        realm = Realm.getInstance(configuration);

        /**
         * get the id of the message from the intent extra
         * to get the message from realm db.
         */
        String messageId = (String) getIntent().getExtras().get("id");
        int notificationId = (int) getIntent().getExtras().get("notif_id");

        /**
         * get the appropriate message from realm results
         */
        RealmResults<Message> results = realm.where(Message.class).equalTo("id", messageId).findAll();
        message = realm.copyFromRealm(results).get(0);
        initMediaPlayer();
        playAlarm();
    }

    /**
     * Initialise Media Player
     */
    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
    }

    /**
     * Method to play alarm
     */
    private void playAlarm(){
        AudioManager audioManager = (AudioManager)
                getSystemService(AUDIO_SERVICE);
        if (audioManager.getStreamVolume(STREAM_ALARM) != 0){
            mMediaPlayer.setAudioStreamType(STREAM_ALARM);
        }
        try {
            mMediaPlayer.setDataSource(AlarmReceiverActivity.this, getAlarmUri());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get an alarm sound. Try for an alarm. If none set, try notification,
     * Otherwise, ringtone.
     * @return Uri of the sound to be played
     */
    private Uri getAlarmUri() {
        Uri alert = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
            alert = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                alert = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return alert;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (count < 4) {
            mp.start();
            count = count + 1;
        } else {
            snoozeAlarm(null);
        }
    }

    /**
     * alarm dismissed, nothing else left to do
     * @param view View tag associated with dismiss button
     */
    public void dismissAlarm(View view) {
    }

    /**
     * alarm snoozed, begin the alarm after snooze duration
     * @param view View tag associated with snooze button
     */
    public void snoozeAlarm(View view) {

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        int snoozeLength = Integer.parseInt(prefs.getString("snooze_length", "5"));
        Intent alarmReceiverIntent = new Intent(this, AlarmReceiver.class);
        alarmReceiverIntent.putExtra("id", message.getId());
        alarmReceiverIntent.putExtra("alarm_id", 1000);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, 0, alarmReceiverIntent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, snoozeLength);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
    }
}

