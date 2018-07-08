package com.german.vktestapp.backgrounds;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SimpleBackground extends Background {
    @NonNull
    private final Drawable mDrawable;

    public SimpleBackground(@NonNull Drawable drawable) {
        mDrawable = drawable;
    }

    @NonNull
    @Override
    public Drawable getThumb(@NonNull Context context) {
        return mDrawable.mutate();
    }

    @Nullable
    @Override
    public Drawable getFull(@NonNull Context context) {
        return mDrawable;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
