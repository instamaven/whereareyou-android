package com.wlady.app.whereareyou.feature.adapters;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wlady.app.whereareyou.feature.R;
import com.wlady.app.whereareyou.feature.activities.MainActivity;
import com.wlady.app.whereareyou.feature.activities.MapsActivity;
import com.wlady.app.whereareyou.feature.models.FCMPushNotification;
import com.wlady.app.whereareyou.feature.models.UserModel;

import java.util.List;

public class ContactsListAdapter extends RecyclerView.Adapter<ContactsListAdapter.MyViewHolder> {

    private Context context;
    private List<UserModel> mDataset;
    private SwipeRefreshLayout swiper;
    private View empty;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView name, invisibleLabel;
        private SimpleDraweeView avatar;
        final private TextView options;
        private ConstraintLayout foregroundView;

        MyViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            invisibleLabel = v.findViewById(R.id.invisibleLabel);
            avatar = v.findViewById(R.id.avatar);
            options = v.findViewById(R.id.contactOptions);
            foregroundView = v.findViewById(R.id.foregroundView);
        }
    }

    public ContactsListAdapter(
            Context ctx,
            SwipeRefreshLayout swipeRefreshLayout,
            View empty,
            List<UserModel> myDataset,
            @NonNull SwipeRefreshLayout.OnRefreshListener callback
        ) {
        context = ctx;
        mDataset = myDataset;
        swiper = swipeRefreshLayout;
        swiper.setRefreshing(true);
        swiper.setOnRefreshListener(callback);
        this.empty = empty;
    }

    @Override
    public ContactsListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_list_item, parent, false);

        return new ContactsListAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ContactsListAdapter.MyViewHolder holder, int position) {
        holder.name.setText(mDataset.get(position).getAlias().equals("") ?
                mDataset.get(position).getName() :
                mDataset.get(position).getAlias()
        );
        String avatarUri = mDataset.get(position).getAvatar();
        if (!avatarUri.equals("")) {
            holder.avatar.setImageURI(avatarUri);
        }
        if (mDataset.get(position).getRelated()) {
            holder.invisibleLabel.setVisibility(View.GONE);
            holder.foregroundView.setBackgroundColor(Color.WHITE);
        } else {
            holder.invisibleLabel.setVisibility(View.VISIBLE);
            holder.foregroundView.setBackgroundColor(Color.YELLOW);
        }
        holder.options.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.options);
            popup.inflate(R.menu.contacts_options_menu);
            popup.getMenu().findItem(R.id.action_invite).setVisible(!mDataset.get(position).getRelated());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_invite) {
                    new MainActivity.ThreadInviteContact(FCMPushNotification.INVITE_MESSAGE, mDataset.get(position)).run();
                } else if (item.getItemId() == R.id.action_remove) {
                    new MainActivity.ThreadDeleteContact(mDataset.get(position).getuId()).run();
                } else if (item.getItemId() == R.id.action_blacklist) {
                    new MainActivity.ThreadAddBlacklist(mDataset.get(position).getuId()).run();
                } else if (item.getItemId() == R.id.action_radar) {
                    Intent intent = new Intent(context, MapsActivity.class);
                    intent.putExtra("uId", mDataset.get(position).getuId());
                    context.startActivity(intent);
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        empty.setVisibility(mDataset.size() > 0 ? View.GONE : View.VISIBLE);
        return mDataset.size();
    }

    public List<UserModel> getData() {
        return mDataset;
    }

    public void addData(UserModel user) {
        mDataset.add(user);
        notifyDataSetChanged();
    }

    public void clearData() {
        mDataset.clear();
        notifyDataSetChanged();
    }

    /**
     * This method does not save contacts (do not replace current cache)
     *
     * @param list
     */
    public void replaceData(List<UserModel> list) {
        mDataset.clear();
        mDataset.addAll(list);
        notifyDataSetChanged();
        stopRefreshing();
    }

    public void stopRefreshing() {
        swiper.setRefreshing(false);
    }
}
