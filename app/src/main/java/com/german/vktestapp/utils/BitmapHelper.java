package com.german.vktestapp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class BitmapHelper {
    private BitmapHelper() {
        // no instance
    }

    public static Bitmap createFromUri(@NonNull Context context, @NonNull Uri selectedUri) {
        Bitmap notResizedBitmap = null;

        String scheme = selectedUri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.equals(scheme)
                || ContentResolver.SCHEME_FILE.equals(scheme)) {
            InputStream inputStream = null;
            try {
                inputStream = context.getContentResolver().openInputStream(selectedUri);
                notResizedBitmap = BitmapFactory.decodeStream(inputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                Utils.close(inputStream);
            }
        } else {
            String[] filePath = { MediaStore.Images.Media.DATA };
            Cursor cursor = context.getContentResolver()
                    .query(selectedUri, filePath, null, null, null);
            if (cursor == null) {
                return null;
            }
            String imagePath;
            try {
                cursor.moveToFirst();
                imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
            } finally {
                Utils.close(cursor);
            }

            if (imagePath != null) {
                notResizedBitmap = BitmapFactory.decodeFile(imagePath);
            }
        }

        return fixBitmapSize(context, notResizedBitmap);
    }

    @Nullable
    private static Bitmap fixBitmapSize(@NonNull Context context, @Nullable Bitmap notResizedBitmap) {
        if (notResizedBitmap == null) {
            return null;
        }

        Bitmap actualBitmap;
        int width = notResizedBitmap.getWidth();
        int height = notResizedBitmap.getHeight();
        Point screenSize = ViewUtils.getScreenSize(context);
        if (width > screenSize.x || height > screenSize.y) {
            float scale = Math.min(1f * screenSize.x / width, 1f * screenSize.y / height);
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            actualBitmap = Bitmap.createBitmap(notResizedBitmap,
                                               0, 0,
                                               width, height,
                                               matrix,
                                               false);
            notResizedBitmap.recycle();
        } else {
            actualBitmap = notResizedBitmap;
        }

        return actualBitmap;
    }
}
