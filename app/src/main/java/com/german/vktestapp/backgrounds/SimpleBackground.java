package com.german.vktestapp.backgrounds;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SimpleBackground extends Background {
    @Nullable
    private final Drawable mDrawable;

    public SimpleBackground(@Nullable Drawable drawable) {
        mDrawable = drawable;
    }

    @Nullable
    @Override
    public Drawable getThumb(@NonNull Context context) {
        return mDrawable;
    }

    @Nullable
    @Override
    public Drawable getFull(@NonNull Context context) {
        return mDrawable;
    }

    @Override
    public boolean isEmpty() {
        return mDrawable == null;
    }
}
