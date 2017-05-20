package com.example.android.easymail.models;

import java.util.HashMap;

/**
 * Created by Harshit Bansal on 5/20/2017.
 */

public class MessageHeader {

    public HashMap<String, String> name;
    public HashMap<String, String> value;

    public MessageHeader(HashMap<String, String> name, HashMap<String, String> value) {
        this.name = name;
        this.value = value;
    }

    public HashMap<String, String> getName() {
        return name;
    }

    public HashMap<String, String> getValue() {
        return value;
    }

    public void setValue(HashMap<String, String> value) {
        this.value = value;
    }

    public void setName(HashMap<String, String> name) {

        this.name = name;
    }
}
