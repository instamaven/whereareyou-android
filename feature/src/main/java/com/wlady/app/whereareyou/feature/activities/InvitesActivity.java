package com.wlady.app.whereareyou.feature.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.wlady.app.whereareyou.feature.App;
import com.wlady.app.whereareyou.feature.helpers.FirestoreHelper;
import com.wlady.app.whereareyou.feature.lists.InvitesLocalList;
import com.wlady.app.whereareyou.feature.R;
import com.wlady.app.whereareyou.feature.helpers.RecyclerItemTouchHelper;
import com.wlady.app.whereareyou.feature.adapters.InviteListAdapter;
import com.wlady.app.whereareyou.feature.models.InviteLocalModel;
import com.wlady.app.whereareyou.feature.models.InviteModel;

import java.util.Date;

public class InvitesActivity extends BaseActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    public final static String KEY_INVITES = "invites";

    private InvitesLocalList dataSet = new InvitesLocalList();
    private TextView empty;
    private InviteListAdapter adapter;
    private CoordinatorLayout coordinatorLayout;

    public static Intent createIntent(@NonNull Context context) {
        return new Intent().setClass(context, InvitesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (App.currentUser == null) {
            startActivity(LoginActivity.createIntent(this));
            finish();
            return;
        }

        setContentView(R.layout.activity_invitations);

        empty = findViewById(R.id.emptyText);

        updateValuesFromBundle(savedInstanceState);

        // invites
        coordinatorLayout = findViewById(R.id.blacklist_layout);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        adapter = new InviteListAdapter(this, dataSet.getData(), empty);
        RecyclerView recyclerView = findViewById(R.id.blacklist_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        getFromFirestore();
    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof InviteListAdapter.MyViewHolder) {
            String docId = dataSet.getData().get(viewHolder.getAdapterPosition()).docId;
            adapter.removeItem(viewHolder.getAdapterPosition());
            FirestoreHelper.deleteInvite(App.currentUser.getUid(), docId);
        }
    }

    /**
     * Restores activity data from the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_INVITES)) {
                dataSet = (InvitesLocalList) savedInstanceState.getSerializable(KEY_INVITES);
            }
        }
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(KEY_INVITES, dataSet);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void getFromFirestore() {
        new Handler().postDelayed(() -> {
            dataSet.clear();
            FirestoreHelper.readInviteDocument(App.currentUser.getUid(), this::completeListenerCallBack);
        }, 0);
    }

    private void completeListenerCallBack(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            for (QueryDocumentSnapshot document : task.getResult()) {
                InviteModel inviteModel = document.toObject(InviteModel.class);
                InviteLocalModel inviteLocalModel = new InviteLocalModel();
                inviteLocalModel.docId = document.getId();
                inviteLocalModel.active = inviteModel.to.equals("");
                Date created = inviteModel.created;
                if (created != null) {
                    inviteLocalModel.createdDate = created.toString();
                }
                dataSet.addData(inviteLocalModel);
            }
            adapter.notifyDataSetChanged();
        }
    }
}
