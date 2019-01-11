package com.wlady.app.whereareyou.feature.models;

import android.location.Location;

import java.util.List;
import java.util.Map;

public interface IUserModel {
    Map<String, Object> toMap();
    String getuId();
    String getAvatar();
    String getName();
    String getAlias();
    List<String> getContacts();
    List<String> getBlacklist();
    LocationModel getLocation();
}
