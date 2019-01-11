package com.wlady.app.whereareyou.feature.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.wlady.app.whereareyou.feature.App;
import com.wlady.app.whereareyou.feature.R;
import com.wlady.app.whereareyou.feature.helpers.FirestoreHelper;
import com.wlady.app.whereareyou.feature.models.UserModel;

import java.util.Arrays;

public class LoginActivity extends BaseActivity {

    private static final int RC_SIGN_IN = 0;

    public static Intent createIntent(@NonNull Context context) {
        return new Intent().setClass(context, LoginActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auth);

        if (FirebaseAuth.getInstance().getUid() == null) {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build()
                    ))
                    .setTheme(R.style.LoginTheme)
                    .setIsSmartLockEnabled(true)
                    .setLogo(R.mipmap.ic_launcher_round)
                    .build(), RC_SIGN_IN);
        } else {
            goToMain();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            goToMain();
        }
    }

    private void goToMain() {
        App.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirestoreHelper.readUserDocument(App.currentUser.getUid(), this::readUserDocCallBack);
    }

    private void readUserDocCallBack(Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            if (document.exists()) {
                App.user = document.toObject(UserModel.class);
                // toObject returns only few fields from Firestore (!?)
                App.user.setuId(App.currentUser.getUid());
            } else {
                App.user = new UserModel(App.currentUser);
                FirestoreHelper.addUser(App.user);
            }
            startActivity(MainActivity.createIntent(this));
            finish();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.smth_wrong)
                    .setMessage(R.string.cannot_connect_db)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
                    .show();
        }
    }

}
