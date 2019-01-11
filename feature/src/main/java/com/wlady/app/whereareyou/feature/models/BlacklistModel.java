package com.wlady.app.whereareyou.feature.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BlacklistModel implements Serializable {

    public Date created = new Date();
    public String name = "";
    public String avatar = "";
    public String uId = "";

    public BlacklistModel() {
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("created", created);
        hashMap.put("name", name);
        hashMap.put("avatar", avatar);
        hashMap.put("uId", uId);

        return hashMap;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getuId() {
        return uId;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getName() {
        return name;
    }

    public Date getCreated() {
        return created;
    }
}
