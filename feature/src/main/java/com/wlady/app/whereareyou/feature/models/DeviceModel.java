package com.wlady.app.whereareyou.feature.models;

import com.wlady.app.whereareyou.feature.helpers.InfoHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DeviceModel implements IDeviceModel, Serializable {

    private String model = "";
    private String platform = "";
    private String version = "";
    private String token = "";

    public DeviceModel() {
    }

    public DeviceModel(Boolean fulfill) {
        if (fulfill) {
            setModel(InfoHelper.getDeviceModel());
            setPlatform("Android");
            setVersion(InfoHelper.getSystemVersion());
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> device = new HashMap<>();
        device.put("model", model);
        device.put("platform", platform);
        device.put("version", version);
        device.put("token", token);

        return device;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getModel() {
        return model;
    }

    public String getPlatform() {
        return platform;
    }

    public String getVersion() {
        return version;
    }

    public String getToken() {
        return token;
    }
}