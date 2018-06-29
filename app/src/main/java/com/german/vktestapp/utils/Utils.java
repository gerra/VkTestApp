package com.german.vktestapp.utils;

import android.database.Cursor;
import android.support.annotation.Nullable;

public class Utils {
    private Utils() {
        // no instance
    }

    public static void close(@Nullable Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Exception ignored) {
            }
        }
    }
}
