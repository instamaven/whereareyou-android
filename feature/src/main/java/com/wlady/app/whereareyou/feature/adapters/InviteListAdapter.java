package com.wlady.app.whereareyou.feature.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.LocaleList;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wlady.app.whereareyou.feature.App;
import com.wlady.app.whereareyou.feature.R;
import com.wlady.app.whereareyou.feature.models.InviteLocalModel;

import java.util.List;
import java.util.Locale;

public class InviteListAdapter extends RecyclerView.Adapter<InviteListAdapter.MyViewHolder> {

    private Context context;
    private List<InviteLocalModel> mDataset;
    private View empty;

    public class MyViewHolder extends RecyclerView.ViewHolder implements IMyViewHolder {
        public TextView docId, createdDate;
        private RelativeLayout viewBackground, viewForeground;
        private ConstraintLayout leftBlock, rightBlock;

        MyViewHolder(View v) {
            super(v);
            docId = v.findViewById(R.id.docId);
            createdDate = v.findViewById(R.id.createdDate);
            viewBackground = v.findViewById(R.id.view_background);
            viewForeground = v.findViewById(R.id.view_foreground);
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

    public InviteListAdapter(Context ctx, List<InviteLocalModel> myDataset, View emptyView) {
        context = ctx;
        mDataset = myDataset;
        empty = emptyView;
    }

    @Override
    public InviteListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.invitation_list_item, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.createdDate.setText(mDataset.get(position).createdDate);
        holder.viewForeground.setBackgroundColor(mDataset.get(position).active ? Color.WHITE : Color.YELLOW);
        String status = String.format(App.locale, "%s: %s",
                context.getString(R.string.status),
                mDataset.get(position).active ?
                        context.getString(R.string.active) :
                        context.getString(R.string.inactive)
        );
        holder.docId.setText(status);
    }

    @Override
    public int getItemCount() {
        empty.setVisibility(mDataset.size() > 0 ? View.GONE : View.VISIBLE);
        return mDataset.size();
    }

    public void removeItem(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
    }
}