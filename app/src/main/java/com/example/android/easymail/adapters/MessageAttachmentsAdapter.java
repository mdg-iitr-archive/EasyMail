package com.example.android.easymail.adapters;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.easymail.R;
import com.example.android.easymail.interfaces.MessageAttachmentClickListener;
import com.example.android.easymail.models.Attachment;

import java.util.List;

/**
 * Created by Harshit Bansal on 7/23/2017.
 */

public class MessageAttachmentsAdapter extends RecyclerView.Adapter<MessageAttachmentsAdapter.MessageAttachmentsViewHolder>{

    private Context context;
    private List<Attachment> attachmentList;
    private MessageAttachmentClickListener listener;

    public MessageAttachmentsAdapter(Context context, List<Attachment> attachmentList, MessageAttachmentClickListener listener) {
        this.context = context;
        this.attachmentList = attachmentList;
        this.listener = listener;
    }

    @Override
    public MessageAttachmentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_attachment_item, parent, false);
        return new MessageAttachmentsViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(MessageAttachmentsViewHolder holder, final int position) {
        holder.fileName.setText(attachmentList.get(position).getFileName());
        String contentType = attachmentList.get(position).getContentType();
        switch (contentType){
            case "APPLICATION/PDF":
                holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.pdf_icon, null));
                break;
            case "APPLICATION/VND.OPENXMLFORMATS-OFFICEDOCUMENT.PRESENTATIONML.PRESENTATION":
                holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ppt_icon, null));
                break;
            case "APPLICATION/VND.OPENXMLFORMATS-OFFICEDOCUMENT.WORDPROCESSINGML.DOCUMENT":
                holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.word_icon, null));
                break;
            case "APPLICATION/ZIP":
                holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.zip_icon, null));
                break;
            case "IMAGE/PNG":
                holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.png_icon, null));
                break;
            case "IMAGE/JPEG":
                holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.jpeg_icon, null));
                break;
            default:
                holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.doc_icon, null));
                break;
        }
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAttachmentClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return attachmentList.size();
    }

    public class MessageAttachmentsViewHolder extends RecyclerView.ViewHolder{

        private ImageView icon;
        private TextView fileName;

        public MessageAttachmentsViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.content_type_image);
            fileName = (TextView) itemView.findViewById(R.id.txt_file_name);
        }
    }
}
