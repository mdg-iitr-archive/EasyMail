package com.example.android.easymail.interfaces;

import javax.mail.Message;
import javax.mail.Part;

/**
 * Created by Harshit Bansal on 7/22/2017.
 */

public interface AttachmentClickListener {

    void onDownloadClicked(Part part);
    void onAttachmentClicked(Message message);
}
