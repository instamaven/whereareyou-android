package com.wlady.app.whereareyou.feature.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.wlady.app.whereareyou.feature.App;
import com.wlady.app.whereareyou.feature.R;
import com.wlady.app.whereareyou.feature.adapters.BlacklistListAdapter;
import com.wlady.app.whereareyou.feature.helpers.FirestoreHelper;
import com.wlady.app.whereareyou.feature.helpers.RecyclerItemTouchHelper;
import com.wlady.app.whereareyou.feature.helpers.SnackbarMessage;
import com.wlady.app.whereareyou.feature.lists.BlacklistLocalList;
import com.wlady.app.whereareyou.feature.models.BlacklistModel;

public class BlacklistActivity extends BaseActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    public final static String KEY_BLACKLIST = "blacklist";

    private BlacklistLocalList dataSet = new BlacklistLocalList();
    private TextView empty;
    private BlacklistListAdapter adapter;
    private CoordinatorLayout snackView;
    private int currentCounter = 0;
    private static Handler mHandler;

    public static Intent createIntent(@NonNull Context context) {
        return new Intent().setClass(context, BlacklistActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.currentUser == null) {
            startActivity(LoginActivity.createIntent(this));
            finish();
            return;
        }

        setContentView(R.layout.activity_blacklist);

        mHandler = new MessageHandler();

        empty = findViewById(R.id.emptyText);

        updateValuesFromBundle(savedInstanceState);

        // blacklist
        snackView = findViewById(R.id.blacklist_layout);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        adapter = new BlacklistListAdapter(dataSet.getData(), empty);
        RecyclerView recyclerView = findViewById(R.id.blacklist_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        new ThreadReloadBlacklist().run();
    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof BlacklistListAdapter.MyViewHolder) {
            if (direction == ItemTouchHelper.LEFT) {
                // delete item
                new ThreadDeleteBlacklist(
                        dataSet.getData().get(viewHolder.getAdapterPosition()).getuId()
                    ).run();
            } else {
                // restore item
                new ThreadRestoreBlacklist(
                        dataSet.getData().get(viewHolder.getAdapterPosition()).getuId()
                ).run();
            }
            adapter.removeItem(viewHolder.getAdapterPosition());
        }
    }

    /**
     * Restores activity data from the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_BLACKLIST)) {
                dataSet = (BlacklistLocalList) savedInstanceState.getSerializable(KEY_BLACKLIST);
            }
        }
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(KEY_BLACKLIST, dataSet);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void loadBlacklist() {
        adapter.clearData();
        if (App.user.getBlacklist().size() > 0) {
            currentCounter = 0;
            for (String entry : App.user.getBlacklist()) {
                if (currentCounter < App.max_contacts) {
                    FirestoreHelper.readUserDocument(entry, task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot blacklistDocument = task.getResult();
                            if (blacklistDocument.exists()) {
                                BlacklistModel blacklistModel = blacklistDocument.toObject(BlacklistModel.class);
                                adapter.addData(blacklistModel);
                            }
                        }
                    });
                } else {
                    break;
                }
            }
        }
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case App.RELOAD_BLACKLIST:
                    loadBlacklist();
                    break;
                case App.UPDATE_BLACKLIST:
                    FirestoreHelper.updateBlacklist(App.user, aVoid1 -> {
                        new MainActivity.ThreadUpdateContact().run();
                        SnackbarMessage.show(snackView, R.string.blacklist_updated);
                    });
                    break;
            }
        }
    }

    public static class ThreadReloadBlacklist extends Thread {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(App.RELOAD_BLACKLIST);
        }
    }

    public static class ThreadDeleteBlacklist extends Thread {

        private String uId;

        public ThreadDeleteBlacklist(String uId) {
            this.uId = uId;
        }

        @Override
        public void run() {
            if (!uId.equals("") && App.user.getBlacklist().contains(uId)) {
                App.user.getBlacklist().remove(uId);
                mHandler.sendEmptyMessage(App.UPDATE_BLACKLIST);
            }
        }
    }

    public static class ThreadRestoreBlacklist extends Thread {

        private String uId;

        public ThreadRestoreBlacklist(String uId) {
            this.uId = uId;
        }

        @Override
        public void run() {
            if (!uId.equals("") && App.user.getBlacklist().contains(uId)) {
                App.user.getBlacklist().remove(uId);
                if (!App.user.getBlacklist().contains(uId)) {
                    App.user.getContacts().add(uId);
                }
                mHandler.sendEmptyMessage(App.UPDATE_BLACKLIST);
            }
        }
    }
}
