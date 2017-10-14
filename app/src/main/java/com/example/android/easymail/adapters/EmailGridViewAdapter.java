package com.example.android.easymail.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.example.android.easymail.R;
import com.example.android.easymail.models.CurrentDayMessageSendersList;

import java.util.List;

/**
 * Created by Harshit Bansal on 6/9/2017.
 */

public class EmailGridViewAdapter extends BaseAdapter {

    private List<CurrentDayMessageSendersList> currentDayMessagesList;
    private Context context;
    private int size;

    public EmailGridViewAdapter(Context context, List<CurrentDayMessageSendersList> currentDayMessagesList) {

        this.context = context;
        this.currentDayMessagesList = currentDayMessagesList;
    }
/*
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.email_name_tile, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        String senderNameInitial = currentDayMessagesList.get(position).getSender().substring(0, 1).toUpperCase();
        String senderEmailCount = Integer.toString( currentDayMessagesList.get(position).getSenderCurrentDayMessageList().size() );
        holder.emailSenderInitial.setText(senderNameInitial);
        holder.emailCount.setText(senderEmailCount);
    }

    @Override
    public int getItemCount() {
        return 0;
    }
*/
    @Override
    public int getCount() {
        return currentDayMessagesList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        if (convertView == null){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.email_name_tile, parent, false);
        }
        else{
            view = convertView;
        }
        String senderNameInitial = currentDayMessagesList.get(position).getSender().substring(0, 1).toUpperCase();
        String senderEmailCount = Integer.toString( currentDayMessagesList.get(position).getSenderCurrentDayMessageList().size() );
        //TextView emailSenderInitial = (TextView) view.findViewById(R.id.email_item_name_initial);
        TextView emailCount = (TextView) view.findViewById(R.id.email_number);
        //emailSenderInitial.setText(senderNameInitial);
        emailCount.setText(senderEmailCount);
        return view;
    }
/*
    public class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView emailSenderInitial;
        private TextView emailCount;

        public MyViewHolder(View itemView) {

            super(itemView);
            emailSenderInitial = (TextView) itemView.findViewById(R.id.email_item_name_initial);
            emailCount = (TextView) itemView.findViewById(R.id.email_number);
        }
    }
    */
}
