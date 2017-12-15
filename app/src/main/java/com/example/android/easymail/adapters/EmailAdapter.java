package com.example.android.easymail.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.example.android.easymail.R;
import com.example.android.easymail.interfaces.EmailLongClickListener;
import com.example.android.easymail.interfaces.SenderEmailItemClickListener;
import com.example.android.easymail.models.CurrentDayMessageSendersRealmList;
import com.example.android.easymail.models.Message;
import com.example.android.easymail.utils.SenderEmail;
import com.example.android.easymail.utils.SenderEmailListItem;
import com.example.android.easymail.utils.SenderListItem;

import java.util.List;

/**
 * Created by harshit on 15/10/17.
 */

public class EmailAdapter extends ExpandableRecyclerAdapter<SenderListItem, SenderEmailListItem,
        EmailSenderViewHolder, EmailViewHolder> {

    private Context context;
    private List<SenderListItem> parentList;
    private SenderEmailItemClickListener listener;
    private EmailLongClickListener longListener;

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
    public EmailAdapter(Context context, @NonNull List<SenderListItem> parentList,
                        SenderEmailItemClickListener listener, EmailLongClickListener longListener) {
        super(parentList);
        this.context = context;
        this.parentList = parentList;
        this.listener = listener;
        this.longListener = longListener;
    }

    @NonNull
    @Override
    public EmailSenderViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        View itemView;
        if (viewType == SenderListItem.TYPE_SENDER) {
            itemView = LayoutInflater.from(parentViewGroup.getContext()).
                    inflate(R.layout.email_sender_layout, parentViewGroup, false);
            return new EmailSenderViewHolder(context, itemView);
        }else if (viewType == SenderListItem.TYPE_DATE) {
            itemView = LayoutInflater.from(parentViewGroup.getContext()).
                    inflate(R.layout.day_title_layout, parentViewGroup, false);
            return new EmailSenderViewHolder(context, itemView);
        }else{
            itemView = LayoutInflater.from(parentViewGroup.getContext()).
                    inflate(R.layout.load_more_progress_layout, parentViewGroup, false);
            return new EmailSenderViewHolder(context, itemView);
        }
    }

    @NonNull
    @Override
    public EmailViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        View itemView;
        if (viewType == SenderEmailListItem.TYPE_MESSAGE) {
            itemView = LayoutInflater.from(childViewGroup.getContext()).
                    inflate(R.layout.email_layout, childViewGroup, false);
            return new EmailViewHolder(itemView, listener, longListener);
        }
        itemView = LayoutInflater.from(childViewGroup.getContext()).
                    inflate(R.layout.load_more_layout, childViewGroup, false);
        return new EmailViewHolder(itemView, listener, longListener);
    }

    @Override
    public void onBindParentViewHolder(@NonNull EmailSenderViewHolder parentViewHolder, int parentPosition, @NonNull SenderListItem parent) {
        parentViewHolder.bind(parent);
    }

    @Override
    public void onBindChildViewHolder(@NonNull EmailViewHolder childViewHolder, int parentPosition, int childPosition, @NonNull SenderEmailListItem child) {
        childViewHolder.bind(child);
    }

    public void setParentList(List<SenderListItem> list){
        this.parentList = list;
    }

    @Override
    public int getParentViewType(int parentPosition) {
        return parentList.get(parentPosition).getType();
    }

    @Override
    public boolean isParentViewType(int viewType) {
        return viewType == 4 || viewType == 5 || viewType == 6;
    }

    @Override
    public int getChildViewType(int parentPosition, int childPosition) {
        return ((SenderEmail)parentList.get(parentPosition)).getChildList().get(childPosition).getType();
    }
}
