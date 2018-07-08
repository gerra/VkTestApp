package com.german.vktestapp.utils;

import android.database.Cursor;
import android.support.annotation.Nullable;

import java.io.Closeable;

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

    public static void close(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
