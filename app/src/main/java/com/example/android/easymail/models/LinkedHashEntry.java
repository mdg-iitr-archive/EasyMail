package com.example.android.easymail.models;

/**
 * Created by Harshit Bansal on 6/7/2017.
 */
import com.google.api.services.gmail.model.Message;

public class LinkedHashEntry {

    String key;
    Message value;
    LinkedHashEntry next;

    LinkedHashEntry(String key, Message value)
    {
        this.key = key;
        this.value = value;
        this.next = null;
    }
}
