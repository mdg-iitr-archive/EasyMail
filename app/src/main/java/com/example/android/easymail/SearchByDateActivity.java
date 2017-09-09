package com.example.android.easymail;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;

public class SearchByDateActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    private EditText fromDateEditText, toDateEditText;
    private boolean isFromDate, isToDate;
    private Date fromDate, toDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_by_date);
        initViews();
    }

    private void initViews() {
        fromDateEditText = (EditText) findViewById(R.id.search_from_date);
        toDateEditText = (EditText) findViewById(R.id.search_to_date);
    }

    public void setFromDate(View v){
        isFromDate = true;
        showDatePickerDialog();
    }

    public void setToDate(View v){
        isToDate = true;
        showDatePickerDialog();
    }

    public void showDatePickerDialog(){
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void searchByDate(View v){
        SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GT, fromDate);
        SearchTerm olderThan = new ReceivedDateTerm(ComparisonTerm.LT, toDate);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth, 0, 0);
        String[] words = c.getTime().toString().split(" ");
        String setDate = words[0] + ", " + words[1] + " " + words[2] + ", " + Integer.toString(year);
        if (isFromDate){
            fromDateEditText.setText(setDate);
            fromDate = c.getTime();
            isFromDate = false;}
        else {
            toDateEditText.setText(setDate);
            toDate = c.getTime();
            isToDate = false;}
    }
}

