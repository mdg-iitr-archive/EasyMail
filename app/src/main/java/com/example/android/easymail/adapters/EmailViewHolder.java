package com.example.android.easymail.adapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.example.android.easymail.R;
import com.example.android.easymail.models.Message;
import com.example.android.easymail.utils.MessageItem;
import com.example.android.easymail.utils.SenderEmailListItem;

/**
 * Created by harshit on 14/10/17.
 */

public class EmailViewHolder extends ChildViewHolder {

    private TextView emailSubject;
    private TextView emailSnippet;

    /**
     * Default constructor.
     *
     * @param itemView The {@link View} being hosted in this ViewHolder
     */
    public EmailViewHolder(@NonNull View itemView) {
        super(itemView);
        initViews(itemView);
    }

    private void initViews(View itemView) {
        emailSubject = (TextView) itemView.findViewById(R.id.subject);
        emailSnippet = (TextView) itemView.findViewById(R.id.snippet);
    }

    public void bind(SenderEmailListItem item){
        if (emailSnippet != null && emailSubject != null) {
            emailSubject.setText(((MessageItem)item).getSubject());
            emailSnippet.setText(((MessageItem)item).getSnippet());
        }
    }
}
