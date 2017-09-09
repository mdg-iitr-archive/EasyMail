package com.example.android.easymail.models;

import io.realm.RealmObject;

/**
 * Created by Harshit Bansal on 6/18/2017.
 */

public class RealmString extends RealmObject {

    private String name;

    public RealmString() {
    }

    public RealmString(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
