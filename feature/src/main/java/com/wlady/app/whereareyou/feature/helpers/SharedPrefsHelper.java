package com.wlady.app.whereareyou.feature.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wlady.app.whereareyou.feature.App;
import com.wlady.app.whereareyou.feature.models.DeviceModel;
import com.wlady.app.whereareyou.feature.models.UserModel;

import java.lang.reflect.Type;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefsHelper {

    private String userPreferences;
    private Context context;

    public SharedPrefsHelper(Context ctx, String suffix) {
        context = ctx;
        userPreferences = App.KEY_PREFERNCES + "-" + suffix;
    }

    /**
     * Save device parameters into SharedPreferences
     */
    public void saveDevice() {
        SharedPreferences.Editor edit = context.getSharedPreferences(userPreferences, MODE_PRIVATE).edit();
        edit.putString(App.KEY_DEVICE, new Gson().toJson(App.device));
        edit.apply();
    }

    /**
     * Fetch device parameters from SharedPreferences
     */
    public DeviceModel getDevice() {
        SharedPreferences preferences = context.getSharedPreferences(userPreferences, MODE_PRIVATE);
        String str = preferences.getString(App.KEY_DEVICE, null);
        return str == null ? null : new Gson().fromJson(str, DeviceModel.class);
    }

    /**
     * Save current contact list into SharedPreferences
     */
    public void saveList(List<UserModel> list) {
        SharedPreferences.Editor edit = context.getSharedPreferences(userPreferences, MODE_PRIVATE).edit();
        edit.putString(App.KEY_CONTACTS, new Gson().toJson(list));
        edit.putLong(App.KEY_EXPIRED, System.currentTimeMillis() + App.contacts_expired * 1000);
        edit.apply();
    }

    /**
     * Fetch contact list from SharedPreferences
     */
    public List<UserModel> getList() {
        SharedPreferences preferences = context.getSharedPreferences(userPreferences, MODE_PRIVATE);
        Type listType = new TypeToken<List<UserModel>>() {
        }.getType();
        Long expireTime = preferences.getLong(App.KEY_EXPIRED, 0);
        if (expireTime < System.currentTimeMillis()) {
            return null;
        }
        String str = preferences.getString(App.KEY_CONTACTS, null);
        return str == null ? null : new Gson().fromJson(str, listType);
    }
}
