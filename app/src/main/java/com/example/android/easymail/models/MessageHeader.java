package com.example.android.easymail.models;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Created by Harshit Bansal on 5/20/2017.
 */

public class MessageHeader {

    @SerializedName("name")
    public String name;
    @SerializedName("value")
    public String value;

    public MessageHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setName(String name) {

        this.name = name;
    }
}
