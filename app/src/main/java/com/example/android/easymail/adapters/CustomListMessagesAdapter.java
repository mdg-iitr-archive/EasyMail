package com.example.android.easymail.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.easymail.R;

import java.util.List;

import com.example.android.easymail.models.Message;

/**
 * Created by Harshit Bansal on 7/19/2017.
 */

public class CustomListMessagesAdapter extends RecyclerView.Adapter<CustomListMessagesAdapter.CustomListMessageViewHolder> {

    private Context context;
    private List<Message> messageList;

    public CustomListMessagesAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @Override
    public CustomListMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_list_message_item, parent, false);
        return new CustomListMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomListMessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.subject.setText(message.getCustomListDetails().getSubject());
        holder.notes.setText(message.getCustomListDetails().getNotes());
        holder.alarm.setText(message.getCustomListDetails().getTime().toString());
        holder.notif.setText(message.getCustomListDetails().getTime().toString());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class CustomListMessageViewHolder extends RecyclerView.ViewHolder{

        private TextView subject, notes, alarm, notif;

        public CustomListMessageViewHolder(View itemView) {
            super(itemView);
            subject = (TextView) itemView.findViewById(R.id.custom_list_message_subject);
            notes = (TextView) itemView.findViewById(R.id.custom_list_message_notes);
            alarm = (TextView) itemView.findViewById(R.id.custom_list_message_alarm);
            notif = (TextView) itemView.findViewById(R.id.custom_list_message_notif);
        }
    }
}

