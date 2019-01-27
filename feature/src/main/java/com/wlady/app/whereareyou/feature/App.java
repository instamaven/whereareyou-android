package com.wlady.app.whereareyou.feature;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.location.Location;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.wlady.app.whereareyou.feature.helpers.FirestoreHelper;
import com.wlady.app.whereareyou.feature.helpers.LocaleManager;
import com.wlady.app.whereareyou.feature.models.DeviceModel;
import com.wlady.app.whereareyou.feature.models.LocationModel;
import com.wlady.app.whereareyou.feature.models.UserModel;

import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class App extends Application {

    final public static int RELOAD_CONTACTS = 1;
    final public static int RELOAD_BLACKLIST = 2;
    final public static int UPDATE_CONTACTS = 3;
    final public static int UPDATE_BLACKLIST = 4;
    final public static int SHOW_CODE_MESSAGE = 5;
    final public static int PING_MESSAGE = 6;
    final public static int DRAW_CAR_ROUTE = 7;
    final public static int DRAW_BIKE_ROUTE = 8;
    final public static int DRAW_FOOT_ROUTE = 9;

    final public static int LOCATION_REQUEST_INTERVAL = 10000;
    final public static int LOCATION_REQUEST_FASTEST_INTERVAL = 5000;

    final public static String KEY_PREFERNCES = "preferences";
    final public static String KEY_DEVICE = "device";
    final public static String KEY_CONTACTS = "contacts";
    final public static String KEY_EXPIRED = "expired";
    final public static String KEY_SEARCH = "search";
    final public static String KEY_ZOOM_BTN = "zoom_btn";
    final public static String KEY_SOUND_BTN = "sound_btn";
    final public static String KEY_COMPASS_BTN = "compass_btn";
    final public static String KEY_LANGUAGE = "language";

    final private static String REMOTE_MAX_CONTACTS = "max_contacts";
    final private static String REMOTE_CONTACTS_EXPIRED = "contacts_expired";
    final private static String REMOTE_FCM_ACCESS_TOKEN = "fcm_access_token";
    final private static String REMOTE_GOOGLE_STORAGE_BUCKET = "google_storage_bucket";
    final private static String ROUTE_MACHINE_URL = "route_machine_url";
    final private static String ROUTE_MACHINE_TOKEN = "route_machine_token";

    public static String language;
    public static Locale locale;
    public static FirebaseUser currentUser;
    public static DeviceModel device;
    public static UserModel user;
    public static Location currentLocation;
    public static Retrofit firebaseClient, routeMachineClient;
    public static LocationRequest mLocationRequest;

    public static Boolean canManageLocations = false;
    public static long max_contacts = 5;
    public static long contacts_expired = 86400; // 24 hours in seconds
    public static String fcm_access_token = ""; // FCM access token
    public static String google_storage_bucket = ""; // FCM access token
    public static String route_machine_url = "";
    public static String route_machine_token = "";

    public static LocaleManager localeManager;

    @Override
    public void onCreate() {
        super.onCreate();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        device = new DeviceModel(true);
        firebaseClient = new Retrofit.Builder()
                .client(firebaseOkHttpClient())
                .baseUrl(getString(R.string.google_apis_base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Fresco.initialize(this);
        locale = localeManager.getLocale(getResources());
        language = localeManager.getLanguage();
        localeManager.setLocale(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        localeManager = new LocaleManager(base);
        super.attachBaseContext(localeManager.setLocale(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        localeManager.setLocale(this);
    }

    public static void initRouteMachineClient() {
        if (!route_machine_url.isEmpty() && !route_machine_token.isEmpty()) {
            routeMachineClient = new Retrofit.Builder()
                    .client(routeMachineOkHttpClient())
                    .baseUrl(route_machine_url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
    }

    private static OkHttpClient firebaseOkHttpClient() {
        // legacy FCM method to send push notifications
        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", "key=" + fcm_access_token)
                            .build();
                    return chain.proceed(newRequest);
                })
                .build();
    }

    private static OkHttpClient routeMachineOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Accept", "application/json")
                            .addHeader("Authorization", "key=" + route_machine_token)
                            .build();
                    return chain.proceed(newRequest);
                })
                .build();
    }

    public static void getRemoteConfig() {
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setConfigSettings(configSettings);
        remoteConfig.setDefaults(R.xml.remote_config_defaults);
        assignConfigValues(remoteConfig);
        long cacheExpiration = 3600; // 1 hour in seconds.
        if (remoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        remoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        remoteConfig.activateFetched();
                        assignConfigValues(remoteConfig);
                    }
                });
    }

    private static void assignConfigValues(FirebaseRemoteConfig remoteConfig) {
        App.max_contacts = remoteConfig.getLong(REMOTE_MAX_CONTACTS);
        App.contacts_expired = remoteConfig.getLong(REMOTE_CONTACTS_EXPIRED);
        App.fcm_access_token = remoteConfig.getString(REMOTE_FCM_ACCESS_TOKEN);
        App.google_storage_bucket = remoteConfig.getString(REMOTE_GOOGLE_STORAGE_BUCKET);
        App.route_machine_url = remoteConfig.getString(ROUTE_MACHINE_URL);
        App.route_machine_token = remoteConfig.getString(ROUTE_MACHINE_TOKEN);
        // init when correct settings have been configured
        initRouteMachineClient();
    }

    public static void saveCurrentLocation(Location location) {
        App.currentLocation = location;
        LocationModel locationModel = new LocationModel();
        locationModel.setUpdated(new Date(location.getTime()));
        locationModel.setLatitude(location.getLatitude());
        locationModel.setLongitude(location.getLongitude());
        locationModel.setAltitude(location.getAltitude());
        locationModel.setSpeed(location.getSpeed());
        App.user.setLocation(locationModel);
        FirestoreHelper.updateLocation(App.user, aVoid -> {});
    }
}
