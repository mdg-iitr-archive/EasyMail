package com.example.android.easymail;

import com.example.android.easymail.models.MailClassifierSender;

import java.util.Comparator;

/**
 * Created by Harshit Bansal on 6/28/2017.
 */

public class MessageCountComparator implements Comparator<MailClassifierSender> {

    @Override
    public int compare(MailClassifierSender o1, MailClassifierSender o2) {

        MailClassifierSender sender1 = (MailClassifierSender)o1;
        MailClassifierSender sender2 = (MailClassifierSender)o2;
        int count1 = Integer.parseInt(sender1.getCount());
        int count2 = Integer.parseInt(sender2.getCount());
        if (count1 > count2)
            return -1;
        if (count2 > count1)
            return  1;
        else
            return  0;
    }
}
