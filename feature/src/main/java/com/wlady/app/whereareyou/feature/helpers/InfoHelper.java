package com.wlady.app.whereareyou.feature.helpers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

public class InfoHelper {

    private static String TAG = "InfoHelper";

    public static int getVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            Log.d(TAG, "Could not get package name: " + e);
        }
        return 0;
    }

    /**
     * @return Application's version name from the {@code PackageManager}.
     */
    public static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            Log.d(TAG, "Could not get package name: " + e);
        }
        return "";
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public static String getDeviceModel() {
        return android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
    }

    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getSystemLanguage(Resources resources) {
        return resources.getConfiguration().locale.toString();
    }
}
