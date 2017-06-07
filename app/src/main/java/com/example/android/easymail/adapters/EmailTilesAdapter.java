package com.example.android.easymail.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.easymail.R;
import com.example.android.easymail.models.CurrentDayMessageSendersList;
import com.example.android.easymail.models.HashTable;
import com.google.api.services.gmail.model.Message;

import java.util.List;

/**
 * Created by Harshit Bansal on 6/7/2017.
 */

public class EmailTilesAdapter extends RecyclerView.Adapter<EmailTilesAdapter.ViewHolder> {

    private List<CurrentDayMessageSendersList> currentDayMessagesList;
    private Context context;
    private int size;

    public EmailTilesAdapter(Context context, List<CurrentDayMessageSendersList> currentDayMessagesList) {
        this.context = context;
        this.currentDayMessagesList = currentDayMessagesList;
    }

    @Override
    public EmailTilesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.email_name_tile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EmailTilesAdapter.ViewHolder holder, int position) {

        String senderNameInitial = currentDayMessagesList.get(position).getSender().substring(0,1).toUpperCase();
        String senderEmailCount = Integer.toString( currentDayMessagesList.get(position).getSenderCurrentDayMessageList().size() );
        holder.emailSenderInitial.setText(senderNameInitial);
        holder.emailCount.setText(senderEmailCount);
    }

    @Override
    public int getItemCount() {
        return currentDayMessagesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView emailSenderInitial;
        private TextView emailCount;
        public ViewHolder(View itemView) {
            super(itemView);
            emailSenderInitial = (TextView) itemView.findViewById(R.id.email_item_name_initial);
            emailCount = (TextView) itemView.findViewById(R.id.email_number);
        }
    }
}

