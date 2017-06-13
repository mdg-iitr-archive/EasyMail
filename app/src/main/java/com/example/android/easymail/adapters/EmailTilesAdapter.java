package com.example.android.easymail.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.example.android.easymail.R;
import com.example.android.easymail.SenderNameInitialClickListener;
import com.example.android.easymail.models.CurrentDayMessageSendersList;
import com.example.android.easymail.models.HashTable;
import com.google.api.services.gmail.model.Message;

import java.util.List;

/**
 * Created by Harshit Bansal on 6/7/2017.
 */

public class EmailTilesAdapter extends ExpandableRecyclerAdapter<CurrentDayMessageSendersList, Message, SenderViewHolder, MessageViewHolder> {

    private List<CurrentDayMessageSendersList> currentDayMessagesList;
    private Context context;
    private int size;
    private int row, column;
    private SenderNameInitialClickListener senderNameInitialClickListener;

    public EmailTilesAdapter(SenderNameInitialClickListener senderNameInitialClickListener,
                             Context context, @NonNull List<CurrentDayMessageSendersList> currentDayMessagesList,
                             int row, int column) {
        super(currentDayMessagesList);
        this.context = context;
        this.currentDayMessagesList = currentDayMessagesList;
        this.senderNameInitialClickListener = senderNameInitialClickListener;
        this.row = row;
        this.column = column;
    }
/*
    @Override
    public EmailTilesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.email_name_tile, parent, false);
        return new ViewHolder(view);
    }
*/
    @NonNull
    @Override
    public SenderViewHolder onCreateParentViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.email_name_tile, parent, false);
        return new SenderViewHolder(senderNameInitialClickListener, view, row, column);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateChildViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_brief, parent, false);
        return new MessageViewHolder(view, context);
    }

    @Override
    public void onBindParentViewHolder(@NonNull SenderViewHolder holder, int position, @NonNull CurrentDayMessageSendersList parent) {
        /*
        String senderNameInitial = currentDayMessagesList.get(position).getSender().substring(0,1).toUpperCase();
        String senderEmailCount = Integer.toString( currentDayMessagesList.get(position).getSenderCurrentDayMessageList().size() );
        holder.emailSenderInitial.setText(senderNameInitial);
        holder.emailCount.setText(senderEmailCount);
        */
        holder.bind(parent);
    }

    @Override
    public void onBindChildViewHolder(@NonNull MessageViewHolder holder, int position, int childPosition, @NonNull Message child) {

        holder.bind(child);
    }
/*
    @Override
    public void onBindViewHolder(EmailTilesAdapter.ViewHolder holder, int position) {

        String senderNameInitial = currentDayMessagesList.get(position).getSender().substring(0,1).toUpperCase();
        String senderEmailCount = Integer.toString( currentDayMessagesList.get(position).getSenderCurrentDayMessageList().size() );
        holder.emailSenderInitial.setText(senderNameInitial);
        holder.emailCount.setText(senderEmailCount);
    }
*/


}
