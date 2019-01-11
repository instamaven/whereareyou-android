package com.wlady.app.whereareyou.feature.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.InstanceIdResult;
import com.wlady.app.whereareyou.feature.App;
import com.wlady.app.whereareyou.feature.R;
import com.wlady.app.whereareyou.feature.adapters.ContactsListAdapter;
import com.wlady.app.whereareyou.feature.helpers.DynamicLinksHelper;
import com.wlady.app.whereareyou.feature.helpers.FCMPushClient;
import com.wlady.app.whereareyou.feature.helpers.FirestoreHelper;
import com.wlady.app.whereareyou.feature.helpers.InstanceIdHelper;
import com.wlady.app.whereareyou.feature.helpers.SharedPrefsHelper;
import com.wlady.app.whereareyou.feature.lists.UserList;
import com.wlady.app.whereareyou.feature.helpers.SnackbarMessage;
import com.wlady.app.whereareyou.feature.models.DeviceModel;
import com.wlady.app.whereareyou.feature.models.FCMPushNotification;
import com.wlady.app.whereareyou.feature.models.InviteModel;
import com.wlady.app.whereareyou.feature.models.UserModel;

import java.util.List;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final private static int REQUEST_CHECK_SETTINGS = 101;
    final private static int LOCATIONS_PERMISSIONS = 102;

    private ListenerRegistration invitesListener;
    private UserList dataSet = new UserList();
    private TextView empty;
    private ContactsListAdapter adapter;
    private ProgressBar progressBar;
    private static View snackView;
    private SharedPrefsHelper sharedPrefsHelper;
    private int currentCounter = 0;

    private static Handler mHandler;
    private static Integer codeMessage;

    private FusedLocationProviderClient mFusedLocationClient;

    public static Intent createIntent(@NonNull Context context) {
        return new Intent().setClass(context, MainActivity.class);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (invitesListener != null) {
            invitesListener.remove();
            invitesListener = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.currentUser == null) {
            startActivity(LoginActivity.createIntent(this));
            finish();
            return;
        }

        sharedPrefsHelper = new SharedPrefsHelper(this, App.currentUser.getUid());
        mHandler = new MessageHandler();

        setContentView(R.layout.activity_main);
        snackView = findViewById(R.id.main_layout);
        empty = findViewById(R.id.emptyText);
        progressBar = findViewById(R.id.progressBar);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swiper);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            UserModel contact = (UserModel) extras.get("add");
            Integer messageType = extras.getInt("type", 0);
            String inviteId = extras.getString("inviteId", "");
            if (contact != null) {
                if (messageType.equals(FCMPushNotification.INVITE_DEEP_LINK)) {
                    // add to my list
                    new ThreadAddContact(contact.getuId()).run();
                    // update invite record
                    FirestoreHelper.updateInvites(contact.getuId(), inviteId, App.currentUser.getUid());
                    // send push notification
                    FCMPushClient.sendConfirmed(this, contact.getDevice().getToken());
                } else if (messageType.equals(FCMPushNotification.INVITE_MESSAGE)) {
                    new ThreadAddContact(contact.getuId()).run();
                    new ThreadInviteContact(FCMPushNotification.ACCEPTED_MESSAGE, contact).run();
                }
            }
            Boolean reload = extras.getBoolean("reload", false);
            if (reload) {
                new ThreadReloadContact().run();
            }
        }

        getDeviceParams();
        App.getRemoteConfig();

        invitesListener = FirestoreHelper.invitesListener(App.currentUser.getUid(), this::setInviteEventListener);

        DynamicLinksHelper.processDynamicLink(getIntent(), this::processDeepLinkCallBack);

        updateValuesFromBundle(savedInstanceState);

        // contacts
        adapter = new ContactsListAdapter(
                this,
                swipeRefreshLayout,
                empty,
                dataSet.getData(),
                this::runReload
        );
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        adapter.stopRefreshing();

        getContacts();

        // User info
        View hView = navigationView.getHeaderView(0);
        TextView user_name = hView.findViewById(R.id.user_name);
        user_name.setText(App.user.getAlias().equals("") ? App.user.getName() : App.user.getAlias());
        TextView user_email = hView.findViewById(R.id.user_email);
        user_email.setText(App.currentUser.getEmail());
        SimpleDraweeView user_avatar = hView.findViewById(R.id.user_avatar);
        String avatar = App.user.getAvatar();
        if (avatar.equals("")) {
            Uri uri = App.currentUser.getPhotoUrl();
            if (uri != null) {
                user_avatar.setImageURI(uri);
            }
        } else {
            user_avatar.setImageURI(avatar);
        }
        user_avatar.setOnClickListener( v -> {
            startActivity(ProfileActivity.createIntent(this));
        });
        progressBar.setVisibility(View.GONE);

        // locations
        checkPermissions();
        if (App.canManageLocations) {
            getLastLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        App.mLocationRequest = new LocationRequest();
        App.mLocationRequest.setInterval(App.LOCATION_REQUEST_INTERVAL);
        App.mLocationRequest.setFastestInterval(App.LOCATION_REQUEST_FASTEST_INTERVAL);
        App.mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(App.mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, locationSettingsResponse -> {
            App.canManageLocations = true;
        });
        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied
                try {
                    // Show the dialog
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        App.saveCurrentLocation(location);
                    }
                });
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(snackView, R.string.grant_locations, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok, view -> {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATIONS_PERMISSIONS);
                        }).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATIONS_PERMISSIONS);
            }
        } else {
            App.canManageLocations = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                App.canManageLocations = true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATIONS_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                App.canManageLocations = (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED);
            }
            break;
        }
    }

    private void processDeepLinkCallBack(PendingDynamicLinkData data) {
        Uri deepLink = null;
        if (data != null) {
            deepLink = data.getLink();
        }
        if (deepLink != null) {
            processDeepLink(deepLink);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_contact) {
            invite();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_contacts) {
            recreate();
        } else if (id == R.id.nav_invites) {
            startActivity(InvitesActivity.createIntent(this));
        } else if (id == R.id.nav_blacklist) {
            startActivity(BlacklistActivity.createIntent(this));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Process Firestore data changes
     *
     * @param queryDocumentSnapshots
     */
    private void processQueryDocumentSnapshots(QuerySnapshot queryDocumentSnapshots) {
        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
            InviteModel inviteModel = doc.getDocument().toObject(InviteModel.class);
            switch (doc.getType()) {
                case ADDED:
                case MODIFIED:
                    if (inviteModel.from.equals(App.currentUser.getUid())) {
                        new ThreadAddContact(inviteModel.to).run();
                    }
                    break;
            }
        }
    }

    /**
     * Process deep link request
     *
     * @param deepLink
     */
    private void processDeepLink(Uri deepLink) {
        String link = deepLink.toString();
        String search = getString(R.string.invitation_deep_link, "", "");
        search = search.substring(0, search.length() - 1);
        int index = link.indexOf(search);
        if (index != -1) {
            // skip last slash
            String paths = link.substring(index + search.length());
            if (!paths.equals("")) {
                index = paths.lastIndexOf("/");
                if (index != 1) {
                    final String userId = paths.substring(0, index);
                    final String inviteId = paths.substring(index + 1);
                    if (!inviteId.equals("") && !userId.equals("") && !userId.equals(App.currentUser.getUid())) {
                        FirestoreHelper.readUserDocument(userId, task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    UserModel userModel = document.toObject(UserModel.class);
                                    Intent intent = new Intent(this, InviteActivity.class);
                                    FCMPushNotification.Data fromData = new FCMPushNotification.Data();
                                    fromData.setType(FCMPushNotification.INVITE_DEEP_LINK);
                                    fromData.setName(userModel.getName());
                                    fromData.setAvatar(userModel.getAvatar());
                                    fromData.setUid(userModel.getuId());
                                    fromData.setToken(userModel.getDevice().getToken());
                                    intent.putExtra("from", fromData);
                                    intent.putExtra("inviteId", inviteId);
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Generate short deep link and send it to your contact
     * The structure of original link is deepLinkPrefix/userId/docId
     */
    private void invite() {
        InviteModel inviteModel = new InviteModel();
        inviteModel.from = App.currentUser.getUid();
        FirestoreHelper.addInvite(inviteModel, this::addInviteCallBack);
    }

    /**
     * Restore activity data from the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(App.KEY_DEVICE)) {
                App.device = (DeviceModel) savedInstanceState.getSerializable(App.KEY_DEVICE);
            }
            if (savedInstanceState.keySet().contains(App.KEY_CONTACTS)) {
                dataSet = (UserList) savedInstanceState.getSerializable(App.KEY_CONTACTS);
            }
            if (savedInstanceState.keySet().contains(App.KEY_LANGUAGE)) {
                App.language = savedInstanceState.getString(App.KEY_LANGUAGE);
            }
        }
    }

    /**
     * Store activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(App.KEY_DEVICE, App.device);
        savedInstanceState.putSerializable(App.KEY_CONTACTS, dataSet);
        savedInstanceState.putString(App.KEY_LANGUAGE, App.language);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void getDeviceParams() {
        if (sharedPrefsHelper.getDevice() == null) {
            // save values in App.device
            sharedPrefsHelper.saveDevice();
        }
        if (App.device.getToken().equals("")) {
            // refresh FCM token
            InstanceIdHelper.getInstanceId(this::getInstanceIdCallBack);
        }
    }

    private void getInstanceIdCallBack(Task<InstanceIdResult> task) {
        String token = task.getResult().getToken();
        App.device.setToken(token);
        sharedPrefsHelper.saveDevice();
        FirestoreHelper.saveDevice(App.currentUser.getUid(), App.device);
    }

    /**
     * Fill adapter with contacts
     */
    private void getContacts() {
        if (adapter.getItemCount() == 0) {
            // try to get list from SharedPreferences
            List<UserModel> data = sharedPrefsHelper.getList();
            if (data != null) {
                adapter.replaceData(data);
            } else {
                // if not found then get it from Firestore
                runReload();
            }
        }
    }

    private void runReload() {
        new ThreadReloadContact().run();
    }

    private void shareDeepLink(String deepLink) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.invite_from, App.currentUser.getDisplayName()));
        intent.putExtra(Intent.EXTRA_TEXT, deepLink);
        startActivity(intent);
    }

    private void addInviteCallBack(DocumentReference inviteReference) {
        String deepLink = getString(R.string.invitation_deep_link, App.currentUser.getUid(), inviteReference.getId());
        DynamicLinksHelper.crateShortDeepLink(deepLink, getString(R.string.deep_link_prefix), this::createShortDeepLinkCallBack);
    }

    private void createShortDeepLinkCallBack(Task<ShortDynamicLink> task) {
        if (task.isSuccessful()) {
            Uri shortLink = task.getResult().getShortLink();
            shareDeepLink(shortLink.toString());
        } else {
            SnackbarMessage.show(snackView, R.string.error_creating_deep_link);
        }
    }

    private void setInviteEventListener(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
        if (e != null) {
            return;
        }
        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
            processQueryDocumentSnapshots(queryDocumentSnapshots);
        }
    }

    private class MessageHandler extends Handler {
        @SuppressLint("MissingPermission")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case App.RELOAD_CONTACTS:
                    loadContacts();
                    break;
                case App.UPDATE_CONTACTS:
                    FirestoreHelper.updateContacts(App.user, aVoid -> {
                        loadContacts();
//                        SnackbarMessage.show(snackView, R.string.contacts_updated);
                    });
                    break;
                case App.UPDATE_BLACKLIST:
                    FirestoreHelper.updateBlacklist(App.user, aVoid1 -> {
                        FirestoreHelper.updateContacts(App.user, aVoid2 -> {
                            loadContacts();
                            SnackbarMessage.show(snackView, R.string.contacts_updated);
                        });
                    });
                    break;
                case App.SHOW_CODE_MESSAGE:
                    if (codeMessage == 200) {
                        SnackbarMessage.show(snackView, R.string.success);
                    } else {
                        SnackbarMessage.show(snackView, getString(R.string.error_code, codeMessage));
                    }
                    break;
            }
        }

        private void loadContacts() {
            adapter.clearData();
            sharedPrefsHelper.saveList(adapter.getData());
            if (App.user.getContacts().size() > 0) {
                currentCounter = 0;
                for (String entry : App.user.getContacts()) {
                    if (currentCounter < App.max_contacts) {
                        FirestoreHelper.readUserDocument(entry, task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot contactDocument = task.getResult();
                                if (contactDocument.exists()) {
                                    UserModel contact = contactDocument.toObject(UserModel.class);
                                    contact.setRelated(contact.getContacts().contains(App.currentUser.getUid()));
                                    adapter.addData(contact);
                                    sharedPrefsHelper.saveList(adapter.getData());
                                }
                                if (++currentCounter >= App.user.getContacts().size()) {
                                    adapter.stopRefreshing();
                                }
                            }
                        });
                    } else {
                        adapter.stopRefreshing();
                        break;
                    }
                }
            } else {
                adapter.stopRefreshing();
            }
        }
    }

    public static class ThreadReloadContact extends Thread {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(App.RELOAD_CONTACTS);
        }
    }

    public static class ThreadUpdateContact extends Thread {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(App.UPDATE_CONTACTS);
        }
    }

    public static class ThreadAddContact extends Thread {

        private String uId;

        public ThreadAddContact(String uId) {
            this.uId = uId;
        }

        @Override
        public void run() {
            if (!uId.equals("") && !App.user.getContacts().contains(uId)) {
                App.user.getContacts().add(uId);
                mHandler.sendEmptyMessage(App.UPDATE_CONTACTS);
            }
        }
    }

    public static class ThreadDeleteContact extends Thread {

        private String uId;

        public ThreadDeleteContact(String uId) {
            this.uId = uId;
        }

        @Override
        public void run() {
            if (App.user.getContacts().contains(uId)) {
                App.user.getContacts().remove(uId);
                mHandler.sendEmptyMessage(App.UPDATE_CONTACTS);
            }
        }
    }

    public static class ThreadAddBlacklist extends Thread {

        private String uId;

        public ThreadAddBlacklist(String uId) {
            this.uId = uId;
        }

        @Override
        public void run() {
            if (!uId.equals("") && App.user.getContacts().contains(uId)) {
                App.user.getContacts().remove(uId);
                if (!App.user.getBlacklist().contains(uId)) {
                    App.user.getBlacklist().add(uId);
                }
                mHandler.sendEmptyMessage(App.UPDATE_BLACKLIST);
            }
        }
    }

    public static class ThreadInviteContact extends Thread {

        private int messageType;
        private UserModel user;

        public ThreadInviteContact(int messageType, UserModel user) {
            this.messageType = messageType;
            this.user = user;
        }

        @Override
        public void run() {
            if (!user.getDevice().getToken().equals("")) {
                FirestoreHelper.readUserDocument(user.getuId(), task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot contactDocument = task.getResult();
                        if (contactDocument.exists()) {
                            UserModel contact = contactDocument.toObject(UserModel.class);
                            if (contact.getDevice().getToken().equals("")) {
                                SnackbarMessage.show(snackView, R.string.user_offline);
                            } else {
                                if (messageType == FCMPushNotification.INVITE_MESSAGE) {
                                    FCMPushClient.sendInvite(snackView.getContext(), contact.getDevice().getToken());
                                } else if (messageType == FCMPushNotification.ACCEPTED_MESSAGE) {
                                    FCMPushClient.sendAccepted(snackView.getContext(), contact.getDevice().getToken());
                                }
                            }
                        }
                    }

                });
            } else {
                SnackbarMessage.show(snackView, R.string.cant_receive);
            }
        }
    }

    public static class ThreadShackbarCodeMessage extends Thread {

        public ThreadShackbarCodeMessage(Integer code) {
            codeMessage = code;
        }

        @Override
        public void run() {
            mHandler.sendEmptyMessage(App.SHOW_CODE_MESSAGE);
        }
    }
}
