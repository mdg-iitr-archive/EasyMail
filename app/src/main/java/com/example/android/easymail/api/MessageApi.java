package com.example.android.easymail.api;

import com.example.android.easymail.models.Message;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/**
 * Created by Harshit Bansal on 5/20/2017.
 */

public interface MessageApi {

    @GET("{user}?alt=json")
    Call<JSONObject> getUserInfo(@Path("user") String user);
}
