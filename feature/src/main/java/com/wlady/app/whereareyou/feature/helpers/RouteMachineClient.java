package com.wlady.app.whereareyou.feature.helpers;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.wlady.app.whereareyou.feature.App;
import com.wlady.app.whereareyou.feature.activities.MainActivity;
import com.wlady.app.whereareyou.feature.activities.MapsActivity;
import com.wlady.app.whereareyou.feature.models.RouteMachineInfoResponse;
import com.wlady.app.whereareyou.feature.models.RouteMachineRouteResponse;

import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouteMachineClient {

    public static void getInfo() {
        if (App.routeMachineClient != null) {
            Call<RouteMachineInfoResponse> routeMachineInfoResponseCall = App.routeMachineClient
                    .create(RouteMachineService.class)
                    .getInfo();
            routeMachineInfoResponseCall.enqueue(new Callback<RouteMachineInfoResponse>() {
                @Override
                public void onResponse(Call<RouteMachineInfoResponse> call, Response<RouteMachineInfoResponse> response) {
                    if (response.code() >= 400) {
                        new MainActivity.ThreadShackbarCodeMessage(response.code()).run();
                    }
                }
                @Override
                public void onFailure(Call<RouteMachineInfoResponse> call, Throwable t) {
                }
            });
        }
    }

    public static void getRoute(LatLng source, LatLng target, final String vehicle) {
        if (App.routeMachineClient != null) {
            List<String> points = new LinkedList<>();
            points.add(source.latitude + "," + source.longitude);
            points.add(target.latitude + "," + target.longitude);
            Call<RouteMachineRouteResponse> routeMachineInfoResponseCall = App.routeMachineClient
                    .create(RouteMachineService.class)
                    .getRoute(points, vehicle, "fastest", false);
            routeMachineInfoResponseCall.enqueue(new Callback<RouteMachineRouteResponse>() {
                @Override
                public void onResponse(Call<RouteMachineRouteResponse> call, Response<RouteMachineRouteResponse> response) {
                    if (response.code() >= 400) {
                        new MainActivity.ThreadShackbarCodeMessage(response.code()).run();
                    } else {
                        new MapsActivity.ThreadDrawRoute(vehicle, response.body().getPath().getPoints()).run();
                    }
                }
                @Override
                public void onFailure(Call<RouteMachineRouteResponse> call, Throwable t) {
                }
            });
        }
    }

}
