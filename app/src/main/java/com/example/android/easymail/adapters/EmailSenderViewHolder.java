package com.example.android.easymail.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.example.android.easymail.R;
import com.example.android.easymail.api.MessageApi;
import com.example.android.easymail.api.NetworkingFactory;
import com.example.android.easymail.models.CurrentDayMessageSendersRealmList;
import com.example.android.easymail.utils.DateItem;
import com.example.android.easymail.utils.MessageItem;
import com.example.android.easymail.utils.SenderEmail;
import com.example.android.easymail.utils.SenderListItem;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by harshit on 14/10/17.
 */

public class EmailSenderViewHolder extends ParentViewHolder{

    private Context context;
    private TextView emailSenderTitle;
    private TextView lastEmailTime;
    private TextView emailSenderSubject;
    private TextView emailSenderSnippet;
    private TextView emailSenderNameInitial;
    private TextView emailCount;
    private ImageView emailSenderPhoto;

    private TextView date;

    /**
     * Default constructor.
     *
     * @param itemView The {@link View} being hosted in this ViewHolder
     */
    public EmailSenderViewHolder(Context context, @NonNull View itemView) {
        super(itemView);
        this.context = context;
        initViews(itemView);
    }

    private void initViews(View itemView) {
        emailSenderTitle = (TextView) itemView.findViewById(R.id.email_sender_title);
        lastEmailTime = (TextView) itemView.findViewById(R.id.last_email_time);
        emailSenderSubject = (TextView) itemView.findViewById(R.id.email_sender_subject);
        emailSenderSnippet = (TextView) itemView.findViewById(R.id.email_sender_snippet);
        emailSenderNameInitial = (TextView) itemView.findViewById(R.id.sender_name_initial);
        emailCount = (TextView) itemView.findViewById(R.id.sender_email_number);
        emailSenderPhoto = (ImageView) itemView.findViewById(R.id.email_sender_photo);

        date = (TextView) itemView.findViewById(R.id.date);
    }

    public void bind(SenderListItem senderEmail){

        if (date != null){
            date.setText(((DateItem)senderEmail).getDate());
        }

        if (emailSenderTitle != null){
            String sender = ((SenderEmail)senderEmail).getSender();
            String senderEmailCount = Integer.toString
                    (((SenderEmail)senderEmail).getSenderMessageList().size());
            emailCount.setText(senderEmailCount);
            emailSenderNameInitial.setText(sender.substring(0, 1).toUpperCase());
            String email = null;
            try {
                String senderName = sender.split("<")[0];
                emailSenderTitle.setText(senderName);
                email = sender.split("<")[1].split(">")[0];
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                emailSenderTitle.setText(sender);
            }
            Long timestamp = ((MessageItem)
                    ((SenderEmail)senderEmail).getSenderMessageList().get(0)).getInternalDate();
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(timestamp * 1000);
            lastEmailTime.setText(DateFormat.format("dd-MM-yyyy hh:mm:ss", cal));
            emailSenderSnippet.setText
                    (((MessageItem) ((SenderEmail)senderEmail).getSenderMessageList().get(0)).getSnippet());
            if (email != null) {
                Retrofit retrofit = NetworkingFactory.getClient();
                MessageApi messageApi = retrofit.create(MessageApi.class);
                Call<JsonObject> userInfoCall = messageApi.getUserInfo(email);
                userInfoCall.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        try {
                            String body = response.body().toString();
                            String photoUrl = response.body().getAsJsonObject("entry")
                                    .getAsJsonObject("gphoto$thumbnail").get("$t").getAsString();
                            Picasso.with(context).load(photoUrl).into(emailSenderPhoto);
                            emailSenderNameInitial.setVisibility(View.INVISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        Log.e("User Info Retrival Fail", t.toString());
                    }
                });
            }
        }
    }
}
