package com.example.android.easymail.adapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.example.android.easymail.R;
import com.example.android.easymail.models.Message;

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

    public void bind(Message message){
        for (int i = 0; i < message.getPayload().getHeaders().size(); i++) {
            String check = message.getPayload().getHeaders().get(i).getName();
            String value = message.getPayload().getHeaders().get(i).getValue();
            switch (check){
                case "Subject":
                    emailSubject.setText(value);
                    break;
            }
        }
        emailSnippet.setText(message.getSnippet());
    }
}
