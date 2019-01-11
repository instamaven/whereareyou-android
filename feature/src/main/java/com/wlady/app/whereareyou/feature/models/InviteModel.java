package com.wlady.app.whereareyou.feature.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class InviteModel {

    public Date created = new Date();
    public String from = "";
    public String to = "";

    public InviteModel() {
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("created", created);
        hashMap.put("from", from);
        hashMap.put("to", to);

        return hashMap;
    }
}
