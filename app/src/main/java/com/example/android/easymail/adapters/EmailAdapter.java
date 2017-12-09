package com.example.android.easymail.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.example.android.easymail.R;
import com.example.android.easymail.models.CurrentDayMessageSendersRealmList;
import com.example.android.easymail.models.Message;

import java.util.List;

/**
 * Created by harshit on 15/10/17.
 */

public class EmailAdapter extends ExpandableRecyclerAdapter<CurrentDayMessageSendersRealmList, Message,
        EmailSenderViewHolder, EmailViewHolder> {

    private Context context;
    private List<CurrentDayMessageSendersRealmList> parentList;

    /**
     * Primary constructor. Sets up {@link #mParentList} and {@link #mFlatItemList}.
     * <p>
     * Any changes to {@link #mParentList} should be made on the original instance, and notified via
     * {@link #notifyParentInserted(int)}
     * {@link #notifyParentRemoved(int)}
     * {@link #notifyParentChanged(int)}
     * {@link #notifyParentRangeInserted(int, int)}
     * {@link #notifyChildInserted(int, int)}
     * {@link #notifyChildRemoved(int, int)}
     * {@link #notifyChildChanged(int, int)}
     * methods and not the notify methods of RecyclerView.Adapter.
     *
     * @param parentList List of all parents to be displayed in the RecyclerView that this
     *                   adapter is linked to
     */
    public EmailAdapter(Context context, @NonNull List<CurrentDayMessageSendersRealmList> parentList) {
        super(parentList);
        this.context = context;
        this.parentList = parentList;
    }

    @NonNull
    @Override
    public EmailSenderViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        View view = LayoutInflater.from(parentViewGroup.getContext()).
                inflate(R.layout.email_sender_layout, parentViewGroup, false);
        return new EmailSenderViewHolder(context, view);
    }

    @NonNull
    @Override
    public EmailViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        View view = LayoutInflater.from(childViewGroup.getContext()).
                inflate(R.layout.email_layout, childViewGroup, false);
        return new EmailViewHolder(view);
    }

    @Override
    public void onBindParentViewHolder(@NonNull EmailSenderViewHolder parentViewHolder, int parentPosition, @NonNull CurrentDayMessageSendersRealmList parent) {
        parentViewHolder.bind(parent);
    }

    @Override
    public void onBindChildViewHolder(@NonNull EmailViewHolder childViewHolder, int parentPosition, int childPosition, @NonNull Message child) {
        childViewHolder.bind(child);
    }

    public void setParentList(List<CurrentDayMessageSendersRealmList> list){
        this.parentList = list;
    }
}
