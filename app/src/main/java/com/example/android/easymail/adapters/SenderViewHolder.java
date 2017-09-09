package com.example.android.easymail.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.example.android.easymail.R;
import com.example.android.easymail.api.MessageApi;
import com.example.android.easymail.api.NetworkingFactory;
import com.example.android.easymail.interfaces.SenderNameInitialClickListener;
import com.example.android.easymail.models.CurrentDayMessageSendersRealmList;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Harshit Bansal on 6/13/2017.
 */

public class SenderViewHolder extends ParentViewHolder {

    private Context context;
    private ImageView emailSenderPhoto;
    private TextView emailCount, emailSenderName;

    public SenderViewHolder(Context context, final SenderNameInitialClickListener listener, View itemView, final int day, final int row, final int column) {
        super(itemView);
        this.context = context;
        emailSenderPhoto = (ImageView) itemView.findViewById(R.id.sender_photo);
        emailCount = (TextView) itemView.findViewById(R.id.email_number);
        emailSenderName = (TextView) itemView.findViewById(R.id.email_sender_name);
        emailSenderPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded()) {
                    listener.onSenderNameInitialClick(day, row, column, 1);
                    collapseView();
                } else {
                    listener.onSenderNameInitialClick(day, row, column, 0);
                    expandView();
                }
            }
        });
    }

    public void bind(CurrentDayMessageSendersRealmList currentDayMessageSendersList){
        String senderName = currentDayMessageSendersList.getSender();
        String senderEmailCount = Integer.toString( currentDayMessageSendersList.getSenderCurrentDayMessageList().size() );
        Retrofit retrofit = NetworkingFactory.getClient();
        MessageApi messageApi = retrofit.create(MessageApi.class);
        Call<JSONObject> userInfoCall = messageApi.getUserInfo(senderName);
        userInfoCall.enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(@NonNull Call<JSONObject> call, @NonNull Response<JSONObject> response) {
                try {
                    String photoUrl = response.body().getJSONObject("entry")
                            .getJSONObject("gphoto$thumbnail").getString("$t");
                    Picasso.with(context).load(photoUrl).into(emailSenderPhoto);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JSONObject> call, @NonNull Throwable t) {
                Log.e("User Info Retrival Fail", t.toString());
            }
        });
        emailCount.setText(senderEmailCount);
    }

    @Override
    public boolean shouldItemViewClickToggleExpansion() {
        return false;
    }
}
