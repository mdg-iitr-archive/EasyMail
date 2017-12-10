package com.example.android.easymail.adapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.example.android.easymail.R;
import com.example.android.easymail.interfaces.EmailLongClickListener;
import com.example.android.easymail.interfaces.SenderEmailItemClickListener;
import com.example.android.easymail.models.Message;
import com.example.android.easymail.utils.MessageItem;
import com.example.android.easymail.utils.SenderEmailListItem;

/**
 * Created by harshit on 14/10/17.
 */

public class EmailViewHolder extends ChildViewHolder {

    private SenderEmailItemClickListener listener;
    private EmailLongClickListener longListener;
    private LinearLayout emailLinearLayout;
    private TextView emailSubject;
    private TextView emailSnippet;
    private TextView loadMoreText;

    /**
     * Default constructor.
     *
     * @param itemView The {@link View} being hosted in this ViewHolder
     */
    public EmailViewHolder
    (@NonNull View itemView, SenderEmailItemClickListener listener, EmailLongClickListener longListener) {
        super(itemView);
        this.listener = listener;
        this.longListener = longListener;
        initViews(itemView);
    }

    private void regListeners(final SenderEmailListItem item) {
        if (emailLinearLayout != null){
            emailLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onSenderItemClicked(item);
                }
            });
            emailLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    longListener.onEmailLongClicked(item);
                    return false;
                }
            });
        }
        if (loadMoreText != null) {
            loadMoreText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onSenderItemClicked(item);
                }
            });
        }
    }

    private void initViews(View itemView) {
        emailLinearLayout = (LinearLayout) itemView.findViewById(R.id.email_layout);
        emailSubject = (TextView) itemView.findViewById(R.id.subject);
        emailSnippet = (TextView) itemView.findViewById(R.id.snippet);
        loadMoreText = (TextView) itemView.findViewById(R.id.txt_load_more);
    }

    public void bind(SenderEmailListItem item){
        if (emailSnippet != null && emailSubject != null) {
            emailSubject.setText(((MessageItem)item).getSubject());
            emailSnippet.setText(((MessageItem)item).getSnippet());
        }
        regListeners(item);
    }
}
