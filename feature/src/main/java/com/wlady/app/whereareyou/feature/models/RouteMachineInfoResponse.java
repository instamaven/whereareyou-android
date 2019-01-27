package com.wlady.app.whereareyou.feature.models;

import com.google.gson.annotations.SerializedName;

public class RouteMachineInfoResponse {

    @SerializedName("bbox")
    public double[] bbox = {};
    @SerializedName("supported_vehicles")
    public String[] supported_vehicles = {};
    @SerializedName("version")
    public String version = "";

    public void setVersion(String name) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public String[] getSupported_vehicles() {
        return supported_vehicles;
    }

    public void setSupported_vehicles(String[] supported_vehicles) {
        this.supported_vehicles = supported_vehicles;
    }

    public double[] getBbox() {
        return bbox;
    }

    public void setBbox(double[] bbox) {
        this.bbox = bbox;
    }
}
