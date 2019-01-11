package com.wlady.app.whereareyou.feature.models;

import com.google.gson.annotations.SerializedName;

public class FCMResponse {

    @SerializedName("name")
    public String name = "";

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
