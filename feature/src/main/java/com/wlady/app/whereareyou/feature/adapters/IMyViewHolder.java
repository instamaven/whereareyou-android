package com.wlady.app.whereareyou.feature.adapters;


import android.support.constraint.ConstraintLayout;
import android.text.Layout;
import android.widget.RelativeLayout;

public interface IMyViewHolder  {
    public RelativeLayout getForeground();
    public RelativeLayout getBackground();
    public ConstraintLayout getBackgroundLeft();
    public ConstraintLayout getBackgroundRight();
}
