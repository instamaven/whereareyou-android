package com.wlady.app.whereareyou.feature.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wlady.app.whereareyou.feature.App;
import com.wlady.app.whereareyou.feature.R;
import com.wlady.app.whereareyou.feature.activities.InviteActivity;
import com.wlady.app.whereareyou.feature.activities.MainActivity;
import com.wlady.app.whereareyou.feature.helpers.FirestoreHelper;
import com.wlady.app.whereareyou.feature.models.FCMPushNotification;

import java.util.Map;

public class MessagingService extends FirebaseMessagingService {

    private FCMPushNotification.Data fromData;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        fromData = null;
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            handleMessage(remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            sendNotification(remoteMessage.getNotification());
        } else if (fromData != null) {
            Intent intent;
            if (fromData.getType().equals(FCMPushNotification.PING_MESSAGE)) {
                // somebody ask to post our location
                intent = new Intent(this, PingService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
            }
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void handleMessage(Map<String, String> data) {
        if (data.containsKey("type")) {
            // our custom data message
            fromData = new FCMPushNotification.Data();
            String type = data.get("type");
            if (type == null) {
                type = "";
            }
            fromData.setType(Integer.parseInt(type));
            fromData.setName(data.get("name"));
            fromData.setAvatar(data.get("avatar"));
            fromData.setUid(data.get("uId"));
            fromData.setToken(data.get("token"));
        }
    }

    @Override
    public void onNewToken(String token) {
        if (App.currentUser != null) {
            FirestoreHelper.saveDevice(App.currentUser.getUid(), App.device);
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param message RemoteMessage.Notification
     */
    private void sendNotification(RemoteMessage.Notification message) {
        Intent intent;
        if (fromData != null && fromData.getType().equals(FCMPushNotification.INVITE_MESSAGE)) {
            intent = new Intent(this, InviteActivity.class);
            intent.putExtra("from", fromData);
        } else {
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("reload", true);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        String channelName = getString(R.string.background_notification_channel);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_binoculars)
                        .setContentTitle(message.getTitle())
                        .setContentText(message.getBody())
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
