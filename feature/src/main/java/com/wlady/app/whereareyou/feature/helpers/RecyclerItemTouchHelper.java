package com.wlady.app.whereareyou.feature.helpers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.wlady.app.whereareyou.feature.adapters.IMyViewHolder;

public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    private RecyclerItemTouchHelperListener listener;

    public RecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            final View foregroundView = ((IMyViewHolder) viewHolder).getForeground();

            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((IMyViewHolder) viewHolder).getForeground();
        final View backgroundView = ((IMyViewHolder) viewHolder).getBackground();
        final View leftBlockView = ((IMyViewHolder) viewHolder).getBackgroundLeft();
        final View rightBlockView = ((IMyViewHolder) viewHolder).getBackgroundRight();

        if (leftBlockView != null && rightBlockView != null) {
            if (dX > 0) {
                // right move
                backgroundView.setBackgroundColor(Color.GREEN);
                rightBlockView.setVisibility(View.GONE);
                leftBlockView.setVisibility(View.VISIBLE);
            } else if (dX < 0) {
                // left move
                backgroundView.setBackgroundColor(Color.RED);
                rightBlockView.setVisibility(View.VISIBLE);
                leftBlockView.setVisibility(View.GONE);
            } else {
                // stay
                backgroundView.setBackgroundColor(Color.WHITE);
            }
        }
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = ((IMyViewHolder) viewHolder).getForeground();
        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((IMyViewHolder) viewHolder).getForeground();
        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    public interface RecyclerItemTouchHelperListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }
}
