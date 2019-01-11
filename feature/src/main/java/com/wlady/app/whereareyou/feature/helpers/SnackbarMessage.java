package com.wlady.app.whereareyou.feature.helpers;

import android.support.design.widget.Snackbar;
import android.view.View;

public class SnackbarMessage {

    public static void show(View view, int resId) {
        Snackbar.make(view, resId, Snackbar.LENGTH_SHORT).show();
    }

    public static void show(View view, String msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
    }

}
