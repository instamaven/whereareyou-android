package com.wlady.app.whereareyou.feature.activities;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.wlady.app.whereareyou.feature.App;
import com.wlady.app.whereareyou.feature.R;
import com.wlady.app.whereareyou.feature.helpers.FCMPushClient;
import com.wlady.app.whereareyou.feature.helpers.FirestoreHelper;
import com.wlady.app.whereareyou.feature.helpers.RouteMachineClient;
import com.wlady.app.whereareyou.feature.models.IUserModel;
import com.wlady.app.whereareyou.feature.models.UserModel;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.LinkedList;
import java.util.List;

import static com.wlady.app.whereareyou.feature.helpers.Utility.decodePolyline;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnCameraIdleListener {

    final private static int ANIMATION_LENGTH = 500;

    private GoogleMap mMap;
    private String uId = "";
    private ListenerRegistration userListener;
    private static UserModel userModel;

    private TextView updatedText, pointText, altText, velText, distanceText;
    private Button pingBtn, zoomBtn, soundBtn, compassBtn, carBtn, bikeBtn, footBtn;
    private SimpleDraweeView avatar;
    private boolean alreadyDrawAvatar = false;
    private boolean firstDraw = true;
    private boolean autoZoom = false, soundEnabled = false, compassEnabled = false;
    private boolean carRoute = false, bikeRoute = false, footRoute = false;
    private static List<LatLng> carRouteEncoded = new LinkedList<>();
    private static List<LatLng> bikeRouteEncoded = new LinkedList<>();
    private static List<LatLng> footRouteEncoded = new LinkedList<>();

    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;

    private static Handler mHandler;

    private static final int MAX_STREAMS = 5;

    private SoundPool soundPool;
    private AudioManager audioManager;
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private boolean soundLoaded;
    private float soundVolume;
    private int sonarSound;

    private LatLng searchLocation, myLocation;
    private float[] geoCalculationResults = {0f, 0f};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.currentUser == null) {
            startActivity(LoginActivity.createIntent(this));
            finish();
            return;
        }

        mHandler = new MessageHandler();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    App.saveCurrentLocation(location);
                    drawMarkers();
                    drawRoutes();
                }
            }
        };

        setContentView(R.layout.activity_maps);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            uId = extras.getString("uId");
        }

        updateValuesFromBundle(savedInstanceState);

        updatedText = findViewById(R.id.updated);
        pointText = findViewById(R.id.point);
        altText = findViewById(R.id.altitude);
        velText = findViewById(R.id.velocity);
        avatar = findViewById(R.id.avatar1);
        if (!App.user.getAvatar().equals("")) {
            avatar.setImageURI(App.user.getAvatar());
        }
        distanceText = findViewById(R.id.distance);
        distanceText.setText(getString(R.string.distance));
        pingBtn = findViewById(R.id.pingBtn);
        // button icon animator
        final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        final Drawable[] drawables = pingBtn.getCompoundDrawables();
        animator.addUpdateListener(animation -> {
            if (drawables[0] != null) {
                // convert to alpha level
                Float alpha = ((Float) animation.getAnimatedValue() * 255);
                drawables[0].setAlpha(Math.round(alpha));
            }
        });
        animator.setDuration(ANIMATION_LENGTH);
        pingBtn.setOnClickListener(v -> {
            new ThreadPing().run();
            playSound(v);
            animator.start();
        });

        // zoom button drawables
        final Drawable zoomOff = getResources().getDrawable(R.drawable.ic_crop_free_black_24dp, null);
        final Drawable zoomOn = getResources().getDrawable(R.drawable.ic_center_focus_strong_black_24dp, null);

        zoomBtn = findViewById(R.id.zoomButton);
        drawButtonState(zoomBtn, autoZoom, zoomOn, zoomOff);
        zoomBtn.setOnClickListener(v -> {
            autoZoom = !autoZoom;
            drawButtonState(zoomBtn, autoZoom, zoomOn, zoomOff);
        });

        // sound button drawables
        final Drawable volumeOff = getResources().getDrawable(R.drawable.ic_volume_off_black_24dp, null);
        final Drawable volumeOn = getResources().getDrawable(R.drawable.ic_volume_up_black_24dp, null);

        soundBtn = findViewById(R.id.soundButton);
        drawButtonState(soundBtn, soundEnabled, volumeOn, volumeOff);
        soundBtn.setOnClickListener(v -> {
            soundEnabled = !soundEnabled;
            drawButtonState(soundBtn, soundEnabled, volumeOn, volumeOff);
        });

        // compass button drawables
        final Drawable compassOff = getResources().getDrawable(R.drawable.ic_location_disabled_black_24dp, null);
        final Drawable compassOn = getResources().getDrawable(R.drawable.ic_my_location_black_24dp, null);

        compassBtn = findViewById(R.id.compasButton);
        drawButtonState(compassBtn, compassEnabled, compassOn, compassOff);
        compassBtn.setOnClickListener(v -> {
            compassEnabled = !compassEnabled;
            drawButtonState(compassBtn, compassEnabled, compassOn, compassOff);
            // re-draw compass immediately
            drawMarkers();
        });

        // car button
        carBtn = findViewById(R.id.carBtn);
        drawButtonStateColor(carBtn, carRoute, R.color.light_green);
        carBtn.setOnClickListener(v -> {
            carRoute = !carRoute;
            drawButtonStateColor(carBtn, carRoute, R.color.light_green);
            drawRoutes();
        });

        // bike button
        bikeBtn = findViewById(R.id.bikeBtn);
        drawButtonStateColor(bikeBtn, bikeRoute, R.color.magenta);
        bikeBtn.setOnClickListener(v -> {
            bikeRoute = !bikeRoute;
            drawButtonStateColor(bikeBtn, bikeRoute, R.color.magenta);
            drawRoutes();
        });

        // walk button
        footBtn = findViewById(R.id.footBtn);
        drawButtonStateColor(footBtn, footRoute, R.color.blue);
        footBtn.setOnClickListener(v -> {
            footRoute = !footRoute;
            drawButtonStateColor(footBtn, footRoute, R.color.blue);
            drawRoutes();
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // listen user's coordinates changes
        if (!uId.equals("")) {
            userListener = FirestoreHelper.userListener(uId, this::setDocumentEventListener);
        }

        prepareSound();
    }

    private void drawRoutes() {
        if (bikeRoute) {
            RouteMachineClient.getRoute(myLocation, searchLocation, "bike");
        } else {
            bikeRouteEncoded.clear();
            drawMarkers();
        }
        if (carRoute) {
            RouteMachineClient.getRoute(myLocation, searchLocation, "car");
        } else {
            carRouteEncoded.clear();
            drawMarkers();
        }
        if (footRoute) {
            RouteMachineClient.getRoute(myLocation, searchLocation, "foot");
        } else {
            footRouteEncoded.clear();
            drawMarkers();
        }
    }

    /**
     * Set text color, background color and drawables according to toggle button state
     *
     * @param btn      Button
     * @param state    toggle state
     * @param stateOn  Drawable On state
     * @param stateOff Drawable Off state
     */
    private void drawButtonState(Button btn, boolean state, Drawable stateOn, Drawable stateOff) {
        if (state) {
            btn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.yellow, null)));
            btn.setCompoundDrawablesWithIntrinsicBounds(stateOn, null, null, null);
            btn.setCompoundDrawableTintList(ColorStateList.valueOf(Color.WHITE));
            btn.setTextColor(Color.WHITE);
        } else {
            btn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.disabled_btn, null)));
            btn.setCompoundDrawablesWithIntrinsicBounds(stateOff, null, null, null);
            btn.setCompoundDrawableTintList(ColorStateList.valueOf(Color.BLACK));
            btn.setTextColor(Color.BLACK);
        }
    }

    /**
     * Set background color according to toggle button state
     *
     * @param btn            Button
     * @param state          Toggle state
     * @param backgroundTint Background tint color
     */
    private void drawButtonStateColor(Button btn, boolean state, int backgroundTint) {
        if (state) {
            btn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(backgroundTint, null)));
            btn.setCompoundDrawableTintList(ColorStateList.valueOf(Color.WHITE));
            btn.setTextColor(Color.WHITE);
        } else {
            btn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white, null)));
            btn.setCompoundDrawableTintList(ColorStateList.valueOf(Color.BLACK));
            btn.setTextColor(Color.BLACK);
        }
    }

    /**
     * Prepare audio manager and load sonar sound
     */
    private void prepareSound() {
        // AudioManager audio settings for adjusting the volume
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Current volume Index of particular stream type.
        float currentVolumeIndex = (float) audioManager.getStreamVolume(streamType);

        // Get the maximum volume index for a particular stream type.
        float maxVolumeIndex = (float) audioManager.getStreamMaxVolume(streamType);

        // Volume (0 -> 1)
        soundVolume = currentVolumeIndex / maxVolumeIndex;

        // Suggests an audio stream whose volume should be changed by
        // the hardware volume controls.
        setVolumeControlStream(streamType);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setAudioAttributes(audioAttributes).setMaxStreams(MAX_STREAMS);

        soundPool = builder.build();

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            soundLoaded = true;
        });
        sonarSound = soundPool.load(this, R.raw.sonar_edited, 1);
    }

    public void playSound(View view) {
        if (soundLoaded && soundEnabled) {
            soundPool.play(sonarSound, soundVolume, soundVolume, 1, 0, 1f);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
        stopUserListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void stopUserListener() {
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(App.mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    /**
     * Firestore Document event listener callback
     *
     * @param documentSnapshots
     * @param e
     */
    private void setDocumentEventListener(DocumentSnapshot documentSnapshots, FirebaseFirestoreException e) {
        if (e != null) {
            return;
        }
        if (documentSnapshots != null) {
            processQueryDocumentSnapshots(documentSnapshots);
        }
    }

    /**
     * Process Firestore data changes
     *
     * @param documentSnapshots
     */
    private void processQueryDocumentSnapshots(DocumentSnapshot documentSnapshots) {
        userModel = documentSnapshots.toObject(UserModel.class);
        drawMarkers();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        int mapPadding = (int) getResources().getDimension(R.dimen.fab_margin);
        mMap.setPadding(mapPadding, mapPadding, mapPadding, mapPadding);
    }

    private void drawMarkers() {
        mMap.clear();
        myLocation = drawIcon(App.user, BitmapDescriptorFactory.HUE_AZURE, 0, 0);
        if (uId.equals("")) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            drawInfoPanel(App.user);
        } else if (userModel != null) {
            searchLocation = drawIcon(userModel, BitmapDescriptorFactory.HUE_RED, R.mipmap.ic_map_marker, 0);
            if (carRoute && carRouteEncoded.size() > 1) {
                drawRoute(Color.GREEN, carRouteEncoded);
            }
            if (bikeRoute && bikeRouteEncoded.size() > 1) {
                drawRoute(Color.MAGENTA, bikeRouteEncoded);
            }
            if (footRoute && footRouteEncoded.size() > 1) {
                drawRoute(Color.BLUE, footRouteEncoded);
            }
            drawInfoPanel(userModel);
            if (firstDraw || autoZoom) {
                firstDraw = false;
                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(myLocation)
                        .include(searchLocation)
                        .build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
            }
            Location loc = new Location("app");
            loc.reset();
            loc.setLatitude(userModel.getLocation().getLatitude());
            loc.setLongitude(userModel.getLocation().getLongitude());
            if (App.currentLocation == null) {
                distanceText.setText(getString(R.string.distance));
            } else {
                Location.distanceBetween(App.user.getLocation().getLatitude(), App.user.getLocation().getLongitude(), userModel.getLocation().getLatitude(), userModel.getLocation().getLongitude(), geoCalculationResults);
                Float dist = geoCalculationResults[0];
                if (dist < 1000) {
                    distanceText.setText(getString(R.string.distance_m, dist));
                } else {
                    distanceText.setText(getString(R.string.distance_km, dist / 1000));
                }
                if (compassEnabled) {
                    // compensate map bearing
                    float markerBearing;
                    if (mMap.getCameraPosition().bearing <= 180) {
                        markerBearing = -mMap.getCameraPosition().bearing + geoCalculationResults[1];
                    } else {
                        markerBearing = 360 % mMap.getCameraPosition().bearing + geoCalculationResults[1];
                    }
                    drawIcon(App.user, BitmapDescriptorFactory.HUE_AZURE, R.mipmap.ic_compass_arrow, markerBearing);
                }
            }
        }
    }

    private LatLng drawIcon(IUserModel userModel, float color, int iconRes, float angle) {
        LatLng point = new LatLng(userModel.getLocation().getLatitude(), userModel.getLocation().getLongitude());
        mMap.addMarker(
                new MarkerOptions()
                        .position(point)
                        .icon(iconRes == 0 ?
                                BitmapDescriptorFactory.defaultMarker(color) :
                                BitmapDescriptorFactory.fromResource(iconRes)
                        )
                        .title(!userModel.getAlias().equals("") ? userModel.getAlias() : userModel.getName())
        )
                .setRotation(angle);

        return point;
    }

    private void drawInfoPanel(IUserModel userModel) {
        PrettyTime prettyTime = new PrettyTime();
        updatedText.setText(String.format(App.locale, getString(R.string.updated), prettyTime.format(userModel.getLocation().getUpdated())));
        pointText.setText(String.format(App.locale, getString(R.string.coordinates), userModel.getLocation().getLatitude(), App.user.getLocation().getLongitude()));
        Double altitude = userModel.getLocation().getAltitude();
        if (altitude == 0.0) {
            altText.setText(String.format(App.locale, getString(R.string.alt_na)));
        } else {
            altText.setText(String.format(App.locale, getString(R.string.alt_m), altitude));
        }
        float velocity = userModel.getLocation().getSpeed();
        if (velocity == 0.0) {
            velText.setText(String.format(App.locale, getString(R.string.speed_na)));
        } else {
            double speed = velocity * 3.6;
            if (speed < 1) {
                velText.setText(String.format(App.locale, getString(R.string.speed_ms), velocity));
            } else {
                velText.setText(String.format(App.locale, getString(R.string.speed_kmh), speed));
            }
        }
        if (!alreadyDrawAvatar && !userModel.getAvatar().equals("")) {
            avatar.setImageURI(userModel.getAvatar());
            alreadyDrawAvatar = true;
        }

    }

    /**
     * Store activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(App.KEY_SEARCH, uId);
        savedInstanceState.putBoolean(App.KEY_ZOOM_BTN, autoZoom);
        savedInstanceState.putBoolean(App.KEY_SOUND_BTN, soundEnabled);
        savedInstanceState.putBoolean(App.KEY_COMPASS_BTN, compassEnabled);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Restore activity data from the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(App.KEY_SEARCH)) {
                uId = savedInstanceState.getString(App.KEY_SEARCH);
            }
            if (savedInstanceState.keySet().contains(App.KEY_ZOOM_BTN)) {
                autoZoom = savedInstanceState.getBoolean(App.KEY_ZOOM_BTN);
            }
            if (savedInstanceState.keySet().contains(App.KEY_SOUND_BTN)) {
                soundEnabled = savedInstanceState.getBoolean(App.KEY_SOUND_BTN);
            }
            if (savedInstanceState.keySet().contains(App.KEY_COMPASS_BTN)) {
                compassEnabled = savedInstanceState.getBoolean(App.KEY_COMPASS_BTN);
            }
        }
    }

    private void drawRoute(int color, List<LatLng> points) {
        PolylineOptions lineOptions = new PolylineOptions();
        lineOptions.addAll(points);
        lineOptions.width(12);
        lineOptions.color(color);
        lineOptions.geodesic(true);
        mMap.addPolyline(lineOptions);
    }

    @Override
    public void onCameraIdle() {
        drawMarkers();
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case App.PING_MESSAGE:
                    FCMPushClient.sendPing(getApplicationContext(), userModel.getDevice().getToken());
                    break;
                case App.DRAW_CAR_ROUTE:
                case App.DRAW_BIKE_ROUTE:
                case App.DRAW_FOOT_ROUTE:
                    drawMarkers();
                    break;
            }
        }
    }

    public static class ThreadPing extends Thread {
        @Override
        public void run() {
            if (userModel != null) {
                mHandler.sendEmptyMessage(App.PING_MESSAGE);
            }
        }
    }

    public static class ThreadDrawRoute extends Thread {
        int mode = 0;

        public ThreadDrawRoute(String vehicle, String routeEncoded) {
            switch (vehicle) {
                case "car":
                    carRouteEncoded.clear();
                    carRouteEncoded = decodePolyline(routeEncoded);
                    mode = App.DRAW_CAR_ROUTE;
                    break;
                case "bike":
                    bikeRouteEncoded.clear();
                    bikeRouteEncoded = decodePolyline(routeEncoded);
                    mode = App.DRAW_BIKE_ROUTE;
                    break;
                case "foot":
                    footRouteEncoded.clear();
                    footRouteEncoded = decodePolyline(routeEncoded);
                    mode = App.DRAW_FOOT_ROUTE;
                    break;
            }
        }

        @Override
        public void run() {
            mHandler.sendEmptyMessage(mode);
        }
    }

}
