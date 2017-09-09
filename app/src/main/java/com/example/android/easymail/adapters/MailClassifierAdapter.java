package com.example.android.easymail.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.easymail.interfaces.MailClassifierSenderClickListener;
import com.example.android.easymail.R;
import com.example.android.easymail.interfaces.MailClassifierSenderLongClickListener;
import com.example.android.easymail.models.MailClassifierSender;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.easymail.R.*;

/**
 * Created by Harshit Bansal on 6/28/2017.
 */

public class MailClassifierAdapter extends RecyclerView.Adapter<MailClassifierAdapter.MailClassifierViewHolder> {

    private Context context;
    public List<MailClassifierSender> sendersList;
    public List<MailClassifierSender> selectedSendersList = new ArrayList<>();
    private MailClassifierSenderClickListener listener;
    private MailClassifierSenderLongClickListener longClickListener;

    public MailClassifierAdapter
            (Context context, List<MailClassifierSender> sendersList, MailClassifierSenderClickListener listener,
             MailClassifierSenderLongClickListener longClickListener){

        this.context = context;
        this.sendersList = sendersList;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    @Override
    public MailClassifierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(layout.mail_classifier_sender_card, parent, false);
        return new MailClassifierViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MailClassifierViewHolder holder, int position) {

        final String senderName = sendersList.get(position).getName();
        String senderMessagesCount = sendersList.get(position).getCount();
        holder.senderName.setText(senderName);
        holder.senderMessagesCount.setText(senderMessagesCount);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSenderClicked(holder.getAdapterPosition(), senderName);
            }
        });
        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickListener.onSenderLongClicked(holder.getAdapterPosition(), senderName);
                return true;
            }
        });
        if (selectedSendersList.contains(sendersList.get(position)))
            holder.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.selectedsenderColor));
        else
            holder.layout.setBackgroundColor(ContextCompat.getColor(context, R.color.senderColor));
    }

    @Override
    public int getItemCount() {
        return sendersList.size();
    }

    public class MailClassifierViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout layout;
        TextView senderName, senderMessagesCount;

        public MailClassifierViewHolder(View itemView) {
            super(itemView);
            layout = (RelativeLayout) itemView.findViewById(R.id.sender_layout);
            senderName = (TextView) itemView.findViewById(R.id.sender_name);
            senderMessagesCount =  (TextView) itemView.findViewById(R.id.sender_message_count);
        }
    }
}
