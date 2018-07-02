package com.german.vktestapp;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class BitmapStory implements Story {
    @NonNull
    private final String mName;

    protected BitmapStory(@NonNull String name) {
        mName = name;
    }

    @Nullable
    public abstract Bitmap getBitmap();

    @NonNull
    @Override
    public String getName() {
        return mName;
    }
}
