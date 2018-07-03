package com.tarunsmalviya.solarcalculator.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

public class CommonMethods {

    private CommonMethods() {
    }

    public static Boolean checkPermission(Context context, String which) {
        return ContextCompat.checkSelfPermission(context, which) == PackageManager.PERMISSION_GRANTED;
    }
}