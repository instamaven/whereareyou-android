package com.wlady.app.whereareyou.feature.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.ui.auth.AuthUI;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wlady.app.whereareyou.feature.App;
import com.wlady.app.whereareyou.feature.R;
import com.wlady.app.whereareyou.feature.helpers.FilestorageHelper;
import com.wlady.app.whereareyou.feature.helpers.FirestoreHelper;
import com.wlady.app.whereareyou.feature.helpers.SnackbarMessage;

public class ProfileActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {

    private String[] languagesKeys;
    private String language;
    private int languageKey;

    public static Intent createIntent(@NonNull Context context) {
        return new Intent().setClass(context, ProfileActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView alias = findViewById(R.id.userAlias);
        alias.setText(App.user.getAlias().equals("") ? App.user.getName() : App.user.getAlias());
        TextView email = findViewById(R.id.emailText);
        email.setText(App.currentUser.getEmail());
        SimpleDraweeView user_avatar = findViewById(R.id.avatar);
        String avatar = App.user.getAvatar();
        if (avatar.equals("")) {
            Uri uri = App.currentUser.getPhotoUrl();
            if (uri != null) {
                user_avatar.setImageURI(uri);
            }
        } else {
            user_avatar.setImageURI(avatar);
        }
        ImageView avatarBtn = findViewById(R.id.editAvatarBtn);
        avatarBtn.setOnClickListener(view -> {
            CropImage.activity()
                    .setAspectRatio(1, 1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setRequestedSize(400, 400)
                    .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                    .setOutputCompressQuality(50)
                    .start(this);
        });

        ImageView aliasBtn = findViewById(R.id.editAliasBtn);
        aliasBtn.setOnClickListener(view -> {
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.profile_edit_alias, null);
            final EditText newAlias = dialogView.findViewById(R.id.newAliasName);
            newAlias.setText(App.user.getAlias().equals("") ? App.user.getName() : App.user.getAlias());
            newAlias.selectAll();
            new AlertDialog.Builder(this)
                    .setTitle(R.string.new_alias)
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        App.user.setAlias(newAlias.getText().toString());
                        FirestoreHelper.updateUser(App.user);
                        recreate();
                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });

        languagesKeys = getResources().getStringArray(R.array.languages_keys);
        Spinner languages = findViewById(R.id.languagesSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languages.setAdapter(adapter);
        languages.setOnItemSelectedListener(this);
        languageKey = getSavedLanguage();
        languages.setSelection(languageKey);
        language = languagesKeys[languageKey];

        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            AuthUI.getInstance().signOut(this);
            new Handler().postDelayed(() -> {
                startActivity(LoginActivity.createIntent(this));
                finish();
            }, 1000);
        });

        Button quitBtn = findViewById(R.id.quitBtn);
        quitBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.want_to_delete_account)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        FirestoreHelper.deleteUser(App.user);
                        AuthUI.getInstance().signOut(this);
                        new Handler().postDelayed(() -> {
                            startActivity(LoginActivity.createIntent(this));
                            finish();
                        }, 1000);
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });

        Button updateBtn = findViewById(R.id.updateBtn);
        updateBtn.setOnClickListener(v -> {
            App.language = language;
            setNewLocale(language, false);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final View snackView = findViewById(R.id.profile_layout);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.new_avatar)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            SnackbarMessage.show(snackView, R.string.new_avatar_will_appear);
                            // delete old avatar
                            FilestorageHelper.deleteFile(
                                    App.user.getuId(),
                                    App.user.getAvatar(),
                                    aVoid -> {
                                    }
                            );
                            // upload new avatar
                            FilestorageHelper.uploadFile(
                                    App.user.getuId(),
                                    result.getUri(),
                                    taskSnapshot -> {
                                        // set new avatar
                                        App.user.setAvatar(
                                                getString(
                                                        R.string.google_storage_base_url,
                                                        App.google_storage_bucket,
                                                        taskSnapshot.getMetadata().getPath()
                                                )
                                        );
                                        FirestoreHelper.updateUser(App.user);
                                        recreate();
                                        dialog.dismiss();
                                    },
                                    e -> {
                                        SnackbarMessage.show(snackView, e.getMessage());
                                    }
                            );
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .create()
                        .show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                SnackbarMessage.show(snackView, error.getMessage());
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        language = languagesKeys[pos];
        Log.d("lang", language);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private boolean setNewLocale(String language, boolean restartProcess) {
        App.localeManager.setNewLocale(this, language);

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));

        if (restartProcess) {
            System.exit(0);
        } else {
            Toast.makeText(this, "Activity restarted", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private int getSavedLanguage() {
        for (int i = 0; i < languagesKeys.length; i++) {
            if (languagesKeys[i].equals(App.language)) {
                return i;
            }
        }

        return 0;
    }

}
