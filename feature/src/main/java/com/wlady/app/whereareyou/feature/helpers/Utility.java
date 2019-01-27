package com.wlady.app.whereareyou.feature.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import static android.content.pm.PackageManager.GET_META_DATA;

public class Utility {

    public static void resetActivityTitle(Activity a) {
        try {
            ActivityInfo info = a.getPackageManager().getActivityInfo(a.getComponentName(), GET_META_DATA);
            if (info.labelRes != 0) {
                a.setTitle(info.labelRes);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Resources getTopLevelResources(Context c) {
        try {
            return c.getPackageManager().getResourcesForApplication(c.getApplicationInfo());
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isAtLeastVersion(int version) {
        return Build.VERSION.SDK_INT >= version;
    }

    public static ArrayList<LatLng> decodePolyline(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0;
        int len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            // latitude
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int deltaLatitude = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += deltaLatitude;

            // longitute
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int deltaLongitude = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += deltaLongitude;

            poly.add(new LatLng((double) lat / 1e5, (double) lng / 1e5));
        }

        return poly;
    }
}