package com.wlady.app.whereareyou.feature.activities;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wlady.app.whereareyou.feature.App;
import com.wlady.app.whereareyou.feature.R;
import com.wlady.app.whereareyou.feature.models.FCMPushNotification;
import com.wlady.app.whereareyou.feature.models.UserModel;

public class InviteActivity extends BaseActivity {

    private FCMPushNotification.Data fromData;
    private String inviteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fromData = (FCMPushNotification.Data) extras.get("from");
            inviteId = extras.getString("inviteId", "");
            if (fromData == null
                    || (!fromData.getType().equals(FCMPushNotification.INVITE_MESSAGE)
                    && !fromData.getType().equals(FCMPushNotification.INVITE_DEEP_LINK))
                    || fromData.getUid().equals("")
            ) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.smth_wrong)
                        .setMessage(R.string.cannot_connect_db)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        })
                        .create()
                        .show();
            }
        }

        TextView alias = findViewById(R.id.userAlias);
        alias.setText(fromData.getName());
        SimpleDraweeView avatar = findViewById(R.id.avatar);
        if (!fromData.getAvatar().equals("")) {
            avatar.setImageURI(fromData.getAvatar());
        }
        Button acceptBtn = findViewById(R.id.acceptBtn);
        acceptBtn.setOnClickListener(view -> {
            if (!App.user.getContacts().contains(fromData.getUid())) {
                UserModel user = new UserModel();
                // set base info
                user.setName(fromData.getName());
                user.setAvatar(fromData.getAvatar());
                user.setuId(fromData.getUid());
                // set token to enable push notification sending
                user.getDevice().setToken(fromData.getToken());
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("add", user);
                intent.putExtra("type", fromData.getType());
                intent.putExtra("inviteId", inviteId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            finish();
        });

        Button cancelBtn = findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(view -> {
            finish();
        });
    }
}
