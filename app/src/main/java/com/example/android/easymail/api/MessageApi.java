package com.example.android.easymail.api;

import com.example.android.easymail.models.Message;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/**
 * Created by Harshit Bansal on 5/20/2017.
 */

public interface MessageApi {

    @GET("users/{user}/messages/{id}")
    Call<Message> getMessage(@Header("Authorization") String accessToken,
                             @Path("user") String user,
                             @Path("id") String id);
}
