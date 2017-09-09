package com.example.android.easymail.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.example.android.easymail.interfaces.CurrentDayMessageClickListener;
import com.example.android.easymail.R;
import com.example.android.easymail.interfaces.SenderNameInitialClickListener;
import com.example.android.easymail.models.CurrentDayMessageSendersRealmList;
import com.example.android.easymail.models.Message;


import java.util.List;

/**
 * Created by Harshit Bansal on 6/7/2017.
 */

public class EmailTilesAdapter extends ExpandableRecyclerAdapter<CurrentDayMessageSendersRealmList, Message,
        SenderViewHolder, MessageViewHolder> {

    private List<CurrentDayMessageSendersRealmList> currentDayMessagesList;
    private Context context;
    private int size;
    private int day, row, column;
    private SenderNameInitialClickListener senderNameInitialClickListener;
    private CurrentDayMessageClickListener currentDayMessageClickListener;

    public EmailTilesAdapter(SenderNameInitialClickListener senderNameInitialClickListener,
                             CurrentDayMessageClickListener currentDayMessageClickListener,
                             Context context, @NonNull List<CurrentDayMessageSendersRealmList> currentDayMessagesList,
                             int day, int row, int column) {
        super(currentDayMessagesList);
        this.context = context;
        this.currentDayMessagesList = currentDayMessagesList;
        this.senderNameInitialClickListener = senderNameInitialClickListener;
        this.currentDayMessageClickListener = currentDayMessageClickListener;
        this.day = day;
        this.row = row;
        this.column = column;
    }

    @NonNull
    @Override
    public SenderViewHolder onCreateParentViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.email_name_tile, parent, false);
        return new SenderViewHolder(context, senderNameInitialClickListener, view, day, row, column);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateChildViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_brief, parent, false);
        return new MessageViewHolder(currentDayMessageClickListener, view, day, row, column, context);
    }

    @Override
    public void onBindParentViewHolder(@NonNull SenderViewHolder holder, int position, @NonNull CurrentDayMessageSendersRealmList parent) {
        holder.bind(parent);
    }

    @Override
    public void onBindChildViewHolder(@NonNull MessageViewHolder holder, int position, int childPosition, @NonNull Message child) {

        holder.bind(child);
    }
}

