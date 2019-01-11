package com.wlady.app.whereareyou.feature.models;

import java.util.Map;

public interface IDeviceModel {
    Map<String, Object> toMap();
    String getModel();
    String getPlatform();
    String getVersion();
    String getToken();
}
