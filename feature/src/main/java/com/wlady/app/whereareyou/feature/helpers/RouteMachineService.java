package com.wlady.app.whereareyou.feature.helpers;

import com.wlady.app.whereareyou.feature.models.RouteMachineInfoResponse;
import com.wlady.app.whereareyou.feature.models.RouteMachineRouteResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RouteMachineService {

    @GET("info")
    Call<RouteMachineInfoResponse> getInfo();

    @GET("route")
    Call<RouteMachineRouteResponse> getRoute(
            @Query("point") List<String> point,
            @Query("vehicle") String vehicle,
            @Query("weighting") String weighting,
            @Query("instructions") boolean instructions
    );
}
