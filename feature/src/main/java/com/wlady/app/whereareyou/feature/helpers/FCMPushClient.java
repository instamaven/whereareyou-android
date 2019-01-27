package com.wlady.app.whereareyou.feature.helpers;

import android.content.Context;

import com.wlady.app.whereareyou.feature.App;
import com.wlady.app.whereareyou.feature.R;
import com.wlady.app.whereareyou.feature.activities.MainActivity;
import com.wlady.app.whereareyou.feature.models.FCMPushNotification;
import com.wlady.app.whereareyou.feature.models.FCMResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FCMPushClient {

    public static void sendInvite(Context context, String token) {

        FCMPushNotification.Message fcmNotification = new FCMPushNotification.Message()
                .setTo(token)
                .setTitle(context.getString(R.string.invite_title))
                .setBody(context.getString(R.string.invite_body, App.currentUser.getDisplayName()))
                .setType(FCMPushNotification.INVITE_MESSAGE)
                .setFrom(App.user);

        Call<FCMResponse> fcmResponseCall = App.firebaseClient
                .create(FCMPushService.class)
                .sendMessage(fcmNotification);

        fcmResponseCall.enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                new MainActivity.ThreadShackbarCodeMessage(response.code()).run();
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
            }
        });
    }

    public static void sendConfirmed(Context context, String token) {

        FCMPushNotification.Message fcmNotification = new FCMPushNotification.Message()
                .setTo(token)
                .setType(FCMPushNotification.CONFIRMED_MESSAGE)
                .setFrom(App.user);

        Call<FCMResponse> fcmResponseCall = App.firebaseClient
                .create(FCMPushService.class)
                .sendMessage(fcmNotification);

        fcmResponseCall.enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                new MainActivity.ThreadShackbarCodeMessage(response.code()).run();
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
            }
        });
    }

    public static void sendAccepted(Context context, String token) {

        FCMPushNotification.Message fcmNotification = new FCMPushNotification.Message()
                .setTo(token)
                .setTitle(context.getString(R.string.accepted_title))
                .setBody(context.getString(R.string.accepted_body, App.currentUser.getDisplayName()))
                .setType(FCMPushNotification.ACCEPTED_MESSAGE);

        Call<FCMResponse> fcmResponseCall = App.firebaseClient
                .create(FCMPushService.class)
                .sendMessage(fcmNotification);

        fcmResponseCall.enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                new MainActivity.ThreadShackbarCodeMessage(response.code()).run();
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
            }
        });
    }

    public static void sendPing(Context context, String token) {

        FCMPushNotification.DataMessage fcmNotification = new FCMPushNotification.DataMessage()
                .setTo(token)
                .setType(FCMPushNotification.PING_MESSAGE);

        Call<FCMResponse> fcmResponseCall = App.firebaseClient
                .create(FCMPushService.class)
                .sendMessage(fcmNotification);

        fcmResponseCall.enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
            }
        });
    }
}
