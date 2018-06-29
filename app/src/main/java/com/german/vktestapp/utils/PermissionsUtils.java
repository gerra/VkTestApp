package com.german.vktestapp.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

public class PermissionsUtils {
    private PermissionsUtils() {
        // no instance
    }

    public static boolean checkPermission(@NonNull Context context, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isRuntimePermissionsAvailable(@NonNull Context context) {
        return context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M
                && isPlatformWithRuntimePermissions();
    }

    private static boolean isPlatformWithRuntimePermissions() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
