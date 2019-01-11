package com.wlady.app.whereareyou.feature.models;

import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserModel implements IUserModel, Serializable {
    private String alias = "";
    private String name = "";
    private String email = "";
    private String avatar = "";
    private String uId = "";
    private DeviceModel device = new DeviceModel();
    private LocationModel location = new LocationModel();
    private List<String> contacts = new ArrayList<>();
    private List<String> blacklist = new ArrayList<>();
    private Boolean related = false;

    public UserModel() {
    }

    public UserModel(FirebaseUser user) {
        uId = user.getUid();
        name = user.getDisplayName();
        email = user.getEmail();
        Uri photo = user.getPhotoUrl();
        if (photo != null) {
            avatar = photo.toString();
        }
        device = new DeviceModel(true);
        location = new LocationModel();
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("alias", alias);
        hashMap.put("name", name);
        hashMap.put("email", email);
        hashMap.put("avatar", avatar);
        hashMap.put("uId", uId);
        hashMap.put("device", device.toMap());
        hashMap.put("contacts", contacts);
        hashMap.put("blacklist", blacklist);
        hashMap.put("location", location.toMap());

        return hashMap;
    }

    public String getuId() {
        return uId;
    }

    public List<String> getContacts() {
        return contacts;
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    public String getName() {
        return name;
    }

    public Boolean getRelated() {
        return related;
    }

    public DeviceModel getDevice() {
        return device;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getAlias() {
        return alias;
    }

    public String getEmail() {
        return email;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setRelated(Boolean related) {
        this.related = related;
    }

    public void setLocation(LocationModel location) {
        this.location = location;
    }

    public LocationModel getLocation() {
        return location;
    }
}
