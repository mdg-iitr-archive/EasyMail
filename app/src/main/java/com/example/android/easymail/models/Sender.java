package com.example.android.easymail.models;

import com.example.android.easymail.R;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Harshit Bansal on 7/5/2017.
 */

public class Sender extends RealmObject{

    @PrimaryKey
    private String name;

    public Sender(){

    }

    public Sender(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
