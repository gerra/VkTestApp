package com.german.vktestapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionsUtils {
    private PermissionsUtils() {
        // no instance
    }

    public static boolean checkPermission(@NonNull Context context, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkAndRequestPermission(@NonNull Activity activity,
                                                    @NonNull String permission,
                                                    int requestCode,
                                                    @NonNull Runnable onPermissionEnabledAction) {
        if (!checkPermission(activity, permission)) {
            if (isRuntimePermissionsAvailable(activity)) {
                ActivityCompat.requestPermissions(activity,
                                                  new String[] { permission },
                                                  requestCode);
            } else {
                throw new IllegalStateException("There is no permission for read storage in manifest?");
            }
            return false;
        } else {
            onPermissionEnabledAction.run();
            return true;
        }
    }

    public static boolean isRuntimePermissionsAvailable(@NonNull Context context) {
        return context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M
                && isPlatformWithRuntimePermissions();
    }

    private static boolean isPlatformWithRuntimePermissions() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
