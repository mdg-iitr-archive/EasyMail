package com.example.android.easymail.models;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

/**
 * Created by Harshit Bansal on 7/1/2017.
 */

public class Time extends RealmObject implements Parcelable{

    private int year;
    private int month;
    private int dayOfMonth;
    private int hourOfDay;
    private int minute;

    public Time() {
    }

    public Time(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.hourOfDay = hourOfDay;
        this.minute = minute;
    }

    protected Time(Parcel in) {
        year = in.readInt();
        month = in.readInt();
        dayOfMonth = in.readInt();
        hourOfDay = in.readInt();
        minute = in.readInt();
    }

    public static final Creator<Time> CREATOR = new Creator<Time>() {
        @Override
        public Time createFromParcel(Parcel in) {
            return new Time(in);
        }

        @Override
        public Time[] newArray(int size) {
            return new Time[size];
        }
    };

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {

        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMinute() {

        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getHourOfDay() {

        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public int getDayOfMonth() {

        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(dayOfMonth);
        dest.writeInt(hourOfDay);
        dest.writeInt(minute);
    }
}
