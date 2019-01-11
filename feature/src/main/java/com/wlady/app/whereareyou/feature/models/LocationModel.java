package com.wlady.app.whereareyou.feature.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LocationModel implements Serializable {

    public Date updated = new Date();
    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private Double altitude = 0.0;
    private Float velocity = 0f;

    public LocationModel() {
    }

    public Map<String, Object> toMap() {
        Map<String, Object> locHash = new HashMap<>();
        locHash.put("updated", updated);
        locHash.put("latitude", latitude);
        locHash.put("longitude", longitude);
        locHash.put("altitude", altitude);
        locHash.put("velocity", velocity);

        return locHash;
    }

    public Date getUpdated() {
        return updated;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public Float getSpeed() {
        return velocity;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public void setSpeed(Float speed) {
        this.velocity = speed;
    }

}