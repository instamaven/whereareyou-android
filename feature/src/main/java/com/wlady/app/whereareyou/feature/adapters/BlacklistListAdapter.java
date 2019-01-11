package com.wlady.app.whereareyou.feature.adapters;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wlady.app.whereareyou.feature.R;
import com.wlady.app.whereareyou.feature.models.BlacklistModel;

import java.util.List;

public class BlacklistListAdapter extends RecyclerView.Adapter<BlacklistListAdapter.MyViewHolder> {

    private List<BlacklistModel> mDataset;
    private View empty;

    public class MyViewHolder extends RecyclerView.ViewHolder implements IMyViewHolder {
        public TextView name, createdDate;
        private SimpleDraweeView avatar;
        private RelativeLayout viewBackground, viewForeground;
        private ConstraintLayout leftBlock, rightBlock;

        MyViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            createdDate = v.findViewById(R.id.createdDate);
            avatar = v.findViewById(R.id.avatar);
            viewBackground = v.findViewById(R.id.view_background);
            viewForeground = v.findViewById(R.id.view_foreground);
            leftBlock = v.findViewById(R.id.leftBlock);
            rightBlock = v.findViewById(R.id.rightBlock);
        }

        @Override
        public RelativeLayout getForeground() {
            return viewForeground;
        }

        @Override
        public RelativeLayout getBackground() {
            return viewBackground;
        }

        @Override
        public ConstraintLayout getBackgroundLeft() {
            return leftBlock;
        }

        @Override
        public ConstraintLayout getBackgroundRight() {
            return rightBlock;
        }
    }

    public BlacklistListAdapter(List<BlacklistModel> myDataset, View emptyView) {
        mDataset = myDataset;
        empty = emptyView;
    }

    @Override
    public BlacklistListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.blacklist_list_item, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.createdDate.setText(mDataset.get(position).created.toString());
        holder.name.setText( mDataset.get(position).name);
        String avatarUri = mDataset.get(position).avatar;
        if (!avatarUri.equals("")) {
            holder.avatar.setImageURI(avatarUri);
        }
    }

    @Override
    public int getItemCount() {
        empty.setVisibility(mDataset.size() > 0 ? View.GONE : View.VISIBLE);
        return mDataset.size();
    }

    public void clearData() {
        mDataset.clear();
        notifyDataSetChanged();
    }

    public void addData(BlacklistModel blacklistModel) {
        mDataset.add(blacklistModel);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

}