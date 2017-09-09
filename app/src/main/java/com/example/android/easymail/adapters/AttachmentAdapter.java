package com.example.android.easymail.adapters;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.easymail.R;
import com.example.android.easymail.interfaces.AttachmentClickListener;
import com.example.android.easymail.utils.AttachmentItem;
import com.example.android.easymail.utils.AttachmentListItem;
import com.example.android.easymail.utils.HeaderItem;

import java.util.List;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;

/**
 * Created by Harshit Bansal on 7/21/2017.
 */

public class AttachmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<AttachmentListItem> list;
    private AttachmentClickListener listener;

    public AttachmentAdapter(Context context, List<AttachmentListItem> list, AttachmentClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == AttachmentListItem.TYPE_HEADER){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_item, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.attachment_item, parent, false);
            return new AttachmentViewHolder(v);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        int type = getItemViewType(position);
        if (type == AttachmentListItem.TYPE_HEADER){
            HeaderItem item = (HeaderItem) list.get(position);
            HeaderViewHolder viewHolder = (HeaderViewHolder) holder;
            viewHolder.date.setText(item.getDate());
        } else {
            AttachmentItem item = (AttachmentItem) list.get(position);
            Message message = item.getMessage();
            AttachmentViewHolder viewHolder = (AttachmentViewHolder) holder;
            try {
                String[] words = message.getReceivedDate().toString().split(" ")[3].split(":");
                String hour = Integer.parseInt(words[0]) > 12 ?
                        Integer.toString(Integer.parseInt(words[0]) - 12) : words[0];
                String amOrPm = Integer.parseInt(words[0]) > 12 ?
                        "pm" : "am";
                viewHolder.time.setText(hour + ":" + words[1] + " " + amOrPm);
                viewHolder.sender.setText(((InternetAddress) message.getFrom()[0]).getPersonal());
                viewHolder.subject.setText(message.getSubject());
                Part attachmentPart;
                attachmentPart = null;
                Multipart multiPart = (Multipart) message.getContent();
                for (int i = 0; i < multiPart.getCount(); i++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        // this part is attachment
                        // code to save attachment...
                        attachmentPart = part;
                        viewHolder.fileName.setText(part.getFileName());
                        String contentType = part.getContentType().split(";")[0];
                        switch (contentType){
                            case "APPLICATION/PDF":
                                viewHolder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.pdf_icon, null));
                                break;
                            case "APPLICATION/VND.OPENXMLFORMATS-OFFICEDOCUMENT.PRESENTATIONML.PRESENTATION":
                                viewHolder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ppt_icon, null));
                                break;
                            case "APPLICATION/VND.OPENXMLFORMATS-OFFICEDOCUMENT.WORDPROCESSINGML.DOCUMENT":
                                viewHolder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.word_icon, null));
                                break;
                            case "APPLICATION/ZIP":
                                viewHolder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.zip_icon, null));
                                break;
                            case "IMAGE/PNG":
                                viewHolder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.png_icon, null));
                                break;
                            case "IMAGE/JPEG":
                                viewHolder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.jpeg_icon, null));
                                break;
                            default:
                                viewHolder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.doc_icon, null));
                                break;
                        }
                    }
                }
                viewHolder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onAttachmentClicked(((AttachmentItem) list.get(position)).getMessage());
                    }
                });
                final Part finalAttachmentPart = attachmentPart;
                viewHolder.download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onDownloadClicked(finalAttachmentPart);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).getType();
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder{

        private TextView date;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.header_date);
        }
    }

    private class AttachmentViewHolder extends RecyclerView.ViewHolder{

        private RelativeLayout layout;
        private TextView size, fileName, time, sender, subject;
        private ImageView icon, download;

        private AttachmentViewHolder(View itemView) {
            super(itemView);
            layout = (RelativeLayout) itemView.findViewById(R.id.attachment_layout);
            size =  (TextView) itemView.findViewById(R.id.attachment_size);
            fileName = (TextView) itemView.findViewById(R.id.attachment_file_name);
            time = (TextView) itemView.findViewById(R.id.attachment_time);
            sender = (TextView) itemView.findViewById(R.id.attachment_sender);
            subject = (TextView) itemView.findViewById(R.id.attachment_subject);
            icon = (ImageView) itemView.findViewById(R.id.attachment_doc_type_icon);
            download = (ImageView) itemView.findViewById(R.id.attachment_download);
        }
    }
}
