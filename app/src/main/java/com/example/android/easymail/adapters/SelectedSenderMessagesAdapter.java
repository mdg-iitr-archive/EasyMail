package com.example.android.easymail.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.easymail.R;

import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by Harshit Bansal on 6/29/2017.
 */

public class SelectedSenderMessagesAdapter extends RecyclerView.Adapter<SelectedSenderMessagesAdapter.SelectedSenderMessagesViewHolder> {

    private Context context;
    private List<MimeMessage> mimeMessageList;

    public SelectedSenderMessagesAdapter(Context context, List<MimeMessage> mimeMessageList) {
        this.context = context;
        this.mimeMessageList = mimeMessageList;
    }

    @Override
    public SelectedSenderMessagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_brief, parent, false);
        return new SelectedSenderMessagesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SelectedSenderMessagesViewHolder holder, int position) {

        MimeMessage mimeMessage = mimeMessageList.get(position);
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
    }

    @Override
    public int getItemCount() {
        return mimeMessageList.size();
    }

    public class SelectedSenderMessagesViewHolder extends RecyclerView.ViewHolder{

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
    }
}
