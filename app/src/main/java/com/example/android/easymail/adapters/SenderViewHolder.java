package com.example.android.easymail.adapters;

import android.view.View;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.example.android.easymail.R;
import com.example.android.easymail.SenderNameInitialClickListener;
import com.example.android.easymail.models.CurrentDayMessageSendersList;
import com.example.android.easymail.models.CurrentDayMessageSendersRealmList;

/**
 * Created by Harshit Bansal on 6/13/2017.
 */

public class SenderViewHolder extends ParentViewHolder {

    private TextView emailSenderInitial;
    private TextView emailCount;
    public SenderViewHolder(final SenderNameInitialClickListener listener, View itemView, final int row, final int column) {
        super(itemView);
        emailSenderInitial = (TextView) itemView.findViewById(R.id.email_item_name_initial);
        emailCount = (TextView) itemView.findViewById(R.id.email_number);
        emailSenderInitial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded()) {
                    listener.onSenderNameInitialClick(row, column, 1);
                    collapseView();
                } else {
                    listener.onSenderNameInitialClick(row, column, 0);
                    expandView();
                }
            }
        });
    }

    public void bind(CurrentDayMessageSendersRealmList currentDayMessageSendersList){
        String senderNameInitial = currentDayMessageSendersList.getSender().substring(0, 1).toUpperCase();
        String senderEmailCount = Integer.toString( currentDayMessageSendersList.getSenderCurrentDayMessageList().size() );
        emailSenderInitial.setText(senderNameInitial);
        emailCount.setText(senderEmailCount);
    }

    @Override
    public boolean shouldItemViewClickToggleExpansion() {
        return false;
    }
}
