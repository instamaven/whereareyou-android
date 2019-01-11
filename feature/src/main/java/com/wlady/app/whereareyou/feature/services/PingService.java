package com.wlady.app.whereareyou.feature.services;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.wlady.app.whereareyou.feature.App;
import com.wlady.app.whereareyou.feature.R;

public class PingService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public PingService() {
        super(PingService.class.getName());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> mLastLocation = mFusedLocationClient.getLastLocation();
        mLastLocation.addOnCompleteListener(task -> {
            Location location = task.getResult();
            if (location != null) {
                App.saveCurrentLocation(location);
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startServiceForeground();
    }

    private void startServiceForeground() {
        if (Build.VERSION.SDK_INT >= 26) {
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.background_notification_channel);
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, channelId)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.drawable.ic_binoculars)
                    .setPriority(1)
                    .build();

            startForeground(101, notification);
        }
    }
}
