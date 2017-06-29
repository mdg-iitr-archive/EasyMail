package com.example.android.easymail.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.easymail.interfaces.MailClassifierSenderClickListener;
import com.example.android.easymail.R;
import com.example.android.easymail.models.MailClassifierSender;

import java.util.List;

import static com.example.android.easymail.R.*;

/**
 * Created by Harshit Bansal on 6/28/2017.
 */

public class MailClassifierAdapter extends RecyclerView.Adapter<MailClassifierAdapter.MailClassifierViewHolder> {

    private Context context;
    private List<MailClassifierSender> sendersList;
    private MailClassifierSenderClickListener listener;

    public MailClassifierAdapter(Context context, List<MailClassifierSender> sendersList, MailClassifierSenderClickListener listener){

        this.context = context;
        this.sendersList = sendersList;
        this.listener = listener;
    }

    @Override
    public MailClassifierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(layout.mail_classifier_sender_card, parent, false);
        return new MailClassifierViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MailClassifierViewHolder holder, int position) {

        final String senderName = sendersList.get(position).getName();
        String senderMessagesCount = sendersList.get(position).getCount();
        holder.senderName.setText(senderName);
        holder.senderMessagesCount.setText(senderMessagesCount);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSenderClicked(senderName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sendersList.size();
    }

    public class MailClassifierViewHolder extends RecyclerView.ViewHolder{

        LinearLayout layout;
        TextView senderName, senderMessagesCount;

        public MailClassifierViewHolder(View itemView) {
            super(itemView);
            layout = (LinearLayout) itemView.findViewById(id.sender_layout);
            senderName = (TextView) itemView.findViewById(R.id.sender_name);
            senderMessagesCount =  (TextView) itemView.findViewById(R.id.sender_message_count);
        }
    }

}
