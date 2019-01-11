package com.wlady.app.whereareyou.feature.helpers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class InstanceIdHelper {

    private static String TAG = "InstanceIdHelper";

    public static void getInstanceId(@NonNull OnCompleteListener<InstanceIdResult> callback) {
        FirebaseInstanceId
                .getInstance()
                .getInstanceId()
                .addOnCompleteListener(callback)
                .addOnFailureListener(e -> {
                    Log.d(TAG, e.getMessage());
                });
    }
}
