package com.wlady.app.whereareyou.feature.models;

import com.google.gson.annotations.SerializedName;

public class RouteMachineRouteResponse {

    @SerializedName("paths")
    public RoutePath[] paths = {};

    public RoutePath getPath() {
        return paths[0];
    }

    public void setPath(RoutePath[] paths) {
        this.paths = paths;
    }

}
