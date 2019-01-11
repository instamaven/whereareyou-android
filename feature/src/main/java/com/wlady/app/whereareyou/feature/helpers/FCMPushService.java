package com.wlady.app.whereareyou.feature.helpers;

import com.wlady.app.whereareyou.feature.models.FCMPushNotification;
import com.wlady.app.whereareyou.feature.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FCMPushService {

    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body FCMPushNotification.Message message);

    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body FCMPushNotification.DataMessage message);
}
