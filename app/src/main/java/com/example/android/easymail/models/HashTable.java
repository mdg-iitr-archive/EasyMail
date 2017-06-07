package com.example.android.easymail.models;

import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.model.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harshit Bansal on 6/7/2017.
 */

/** Class LinearProbingHashTable **/
public class HashTable {

    private int currentSize, maxSize;
    public String[] keys;
    public ArrayList<List<Message>> vals;

    /** Constructor **/
    public HashTable(int capacity)
    {
        currentSize = 0;
        maxSize = capacity;
        keys = new String[maxSize];
        vals = new ArrayList<List<Message>>(maxSize);
        for (int index = 0; index < maxSize; index++) {
            vals.add(index, new ArrayList<Message>());
        }
    }

    /** Function to clear hash table **/
    public void makeEmpty()
    {
        currentSize = 0;
        keys = new String[maxSize];
        vals = new ArrayList<List<Message>>(maxSize);
    }

    /** Function to get size of hash table **/
    public int getSize()
    {
        return currentSize;
    }

    /** Function to check if hash table is full **/
    public boolean isFull()
    {
        return currentSize == maxSize;
    }

    /** Function to check if hash table is empty **/
    public boolean isEmpty()
    {
        return getSize() == 0;
    }

    /** Function to check if hash table contains a key **/
    public boolean contains(String key)
    {
        return get(key) !=  null;
    }

    /** Function to get hash code of a given key **/
    private int hash(String key)
    {
        return key.hashCode() % maxSize;
    }

    /** Function to insert key-value pair **/
    public void insert(String key, Message message)
    {
        int tmp = hash(key);
        int i = Math.abs(tmp);
        do
        {
            if (keys[i] == null)
            {
                keys[i] = key;
                vals.get(i).add(message);
                currentSize++;
                return;
            }
            if (keys[i].equals(key))
            {
                vals.get(i).add(message);
                return;
            }
            i = (i + 1) % maxSize;
        } while (i != tmp);
    }

    /** Function to get value for a given key **/
    public List<Message> get(String key)
    {
        int i = hash(key);
        while (keys[i] != null)
        {
            if (keys[i].equals(key))
                return vals.get(i);
            i = (i + 1) % maxSize;
        }
        return null;
    }

    /** Function to remove key and its value **/
    public void remove(String key)
    {
        if (!contains(key))
            return;

        /** find position key and delete **/
        int i = hash(key);
        while (!key.equals(keys[i]))
            i = (i + 1) % maxSize;
        keys[i] = null;
        vals.remove(i);
        /** rehash all keys **/
        for (i = (i + 1) % maxSize; keys[i] != null; i = (i + 1) % maxSize)
        {
            String tmp1 = keys[i];
            List<Message> tmp2 = vals.get(i);
            keys[i] = null;
            vals.remove(i);
            currentSize--;
            // To be completed...
        }
        currentSize--;
    }
}
