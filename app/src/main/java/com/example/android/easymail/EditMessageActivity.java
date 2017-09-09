package com.example.android.easymail;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.example.android.easymail.models.CustomListDetails;
import com.example.android.easymail.models.Message;
import com.example.android.easymail.models.Time;
import com.example.android.easymail.utils.DatePickerFragment;
import com.example.android.easymail.utils.TimePickerFragment;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class EditMessageActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private ActionBar actionBar;
    private EditText subject, notes, date, time;
    private CheckBox alarmCheckBox, notifCheckBox;
    private Message message;
    private String listName;
    private Realm realm;
    private String subject_text, notes_text, date_text, time_text;
    private int year, month, dayOfMonth, hourOfDay, minute;
    private boolean isAlarmEnabled, isNotifEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_message);
        actionBar = getSupportActionBar();
        initViews();
        RealmConfiguration configuration = new RealmConfiguration.Builder().build();
        realm = Realm.getInstance(configuration);
        listName = getIntent().getExtras().getString("listName");
        String messageId = (String) getIntent().getExtras().get("messageId");
        RealmResults<Message> results = realm.where(com.example.android.easymail.models.Message.class).equalTo("id", messageId).findAll();
        message = realm.copyFromRealm(results).get(0);
        for (int i = 0; i < message.getPayload().getHeaders().size(); i++) {
            String check = message.getPayload().getHeaders().get(i).getName();
            if (check.equals("Subject")) {
                String value = message.getPayload().getHeaders().get(i).getValue();
                subject.setText(value);
            }
        }
    }

    public void initViews(){
        subject = (EditText) findViewById(R.id.edit_subject);
        notes = (EditText) findViewById(R.id.edit_notes);
        date = (EditText) findViewById(R.id.edit_date);
        time = (EditText) findViewById(R.id.edit_time);
        alarmCheckBox = (CheckBox) findViewById(R.id.show_alarm_checkbox);
        notifCheckBox = (CheckBox) findViewById(R.id.show_notif_checkbox);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth, 0, 0);
        String[] words = c.getTime().toString().split(" ");
        String setDate = words[0] + ", " + words[1] + " " + words[2] + ", " + Integer.toString(year);
        date.setText(setDate);
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        String amOrPm = hourOfDay > 12 ? "PM" : "AM";
        hourOfDay = hourOfDay > 12 ? hourOfDay - 12 : hourOfDay;
        String setTime = hourOfDay + ":" + minute + " " + amOrPm;
        time.setText(setTime);
        this.hourOfDay = hourOfDay;
        this.minute = minute;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_message_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.edit_message_save:
                extractDetails();
                realm.beginTransaction();
                message.setCustomListName(listName);
                message.setCustomListDetails(
                        new CustomListDetails
                                (listName, subject_text, notes_text,
                                        new Time(year, month, dayOfMonth, hourOfDay, minute), isAlarmEnabled, isNotifEnabled ));
                realm.copyToRealmOrUpdate(message);
                Intent customListMessageDetailIntent = new Intent(EditMessageActivity.this, CustomListMessageDetailActivity.class);
                customListMessageDetailIntent.putExtra("messageId", message.getId());
                startActivity(customListMessageDetailIntent);
                break;
            case R.id.edit_message_cancel:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void extractDetails(){
        subject_text = subject.getText().toString();
        notes_text = notes.getText().toString();
        date_text = date.getText().toString();
        time_text = time.getText().toString();
        isAlarmEnabled = alarmCheckBox.isSelected();
        isNotifEnabled = notifCheckBox.isSelected();
    }
}

