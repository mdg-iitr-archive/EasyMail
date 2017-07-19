package com.example.android.easymail.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.easymail.R;
import com.example.android.easymail.interfaces.SelectedSenderMessageClickListener;

import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by Harshit Bansal on 6/29/2017.
 */

public class SelectedSenderMessagesAdapter extends
        RecyclerView.Adapter<SelectedSenderMessagesAdapter.SelectedSenderMessagesViewHolder> {

    private Context context;
    private List<Message> mimeMessageList;
    private SelectedSenderMessageClickListener listener;

    public SelectedSenderMessagesAdapter
            (Context context, List<Message> mimeMessageList, SelectedSenderMessageClickListener listener) {
        this.context = context;
        this.mimeMessageList = mimeMessageList;
        this.listener =  listener;
    }

    @Override
    public SelectedSenderMessagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_sender_message_item, parent, false);
        return new SelectedSenderMessagesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SelectedSenderMessagesViewHolder holder, int position) {

        final Message mimeMessage = mimeMessageList.get(position);
        try{
            holder.date.setText(mimeMessage.getReceivedDate().toString());
            holder.subject.setText(mimeMessage.getSubject());
            holder.senderMessageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onSenderMessageClicked(mimeMessage);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        /*
        try {
            holder.subject.setText(mimeMessage.getSubject());
            Address[] froms = mimeMessage.getFrom();
            holder.from.setText(((InternetAddress) froms[0]).getPersonal());
            Address[] to = mimeMessage.getReplyTo();
            holder.to.setText(((InternetAddress) to[0]).getPersonal());
            holder.time.setText(mimeMessage.getReceivedDate().toString());
            holder.snippet.setText("No Snippet Obtained!");
        }catch (Exception e){
            e.printStackTrace();
        }
        */
    }

    @Override
    public int getItemCount() {
        return mimeMessageList.size();
    }

    public class SelectedSenderMessagesViewHolder extends RecyclerView.ViewHolder{

        private LinearLayout senderMessageLayout;
        private TextView date, subject;

        public SelectedSenderMessagesViewHolder(View itemView) {
            super(itemView);
            senderMessageLayout = (LinearLayout) itemView.findViewById(R.id.sender_message_layout);
            date = (TextView) itemView.findViewById(R.id.message_date);
            subject = (TextView) itemView.findViewById(R.id.message_subject);
        }
        /*
        private TextView subject, from, to, time, snippet, expand_collapse;
        private CardView emailCardView;
        public SelectedSenderMessagesViewHolder(View itemView) {
            super(itemView);
            subject = (TextView) itemView.findViewById(R.id.email_subject);
            from = (TextView) itemView.findViewById(R.id.email_from);
            to = (TextView) itemView.findViewById(R.id.email_to);
            time = (TextView) itemView.findViewById(R.id.email_time);
            snippet = (TextView) itemView.findViewById(R.id.email_snippet);
            expand_collapse = (TextView) itemView.findViewById(R.id.expand_collapse_email);
            emailCardView = (CardView) itemView.findViewById(R.id.email_card_view);
        }
        */
    }
}
