package com.wlady.app.whereareyou.feature.models;

import com.google.gson.annotations.SerializedName;

public class RoutePath {

    @SerializedName("distance")
    public double distance = 0.0;
    @SerializedName("weight")
    public double weight = 0.0;
    @SerializedName("time")
    public int time = 0;
    @SerializedName("points_encoded")
    public boolean points_encoded = false;
    @SerializedName("bbox")
    public double[] bbox = {};
    @SerializedName("points")
    public String points = "";

    public double[] getBbox() {
        return bbox;
    }

    public double getDistance() {
        return distance;
    }

    public double getWeight() {
        return weight;
    }

    public int getTime() {
        return time;
    }

    public String getPoints() {
        return points;
    }

    public void setBbox(double[] bbox) {
        this.bbox = bbox;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public void setPoints_encoded(boolean points_encoded) {
        this.points_encoded = points_encoded;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}