package com.wlady.app.whereareyou.feature.helpers;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

public class DynamicLinksHelper {

    public static void crateShortDeepLink(String deepLink, String prefix, @NonNull OnCompleteListener<ShortDynamicLink> callback) {
        FirebaseDynamicLinks.getInstance()
                .createDynamicLink()
                .setDomainUriPrefix(prefix)
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder()
                        .setMinimumVersion(0)
                        .build())
                .setLink(Uri.parse(deepLink))
                .buildShortDynamicLink()
                .addOnCompleteListener(callback);
    }

    public static void processDynamicLink(Intent intent, @NonNull OnSuccessListener<PendingDynamicLinkData> callback) {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(callback);
    }
}
