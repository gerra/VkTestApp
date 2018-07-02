package com.german.vktestapp.backgrounds;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.german.vktestapp.R;

public abstract class Background {
    public static final Background NONE = new Background() {
        @NonNull
        @Override
        public Drawable getThumb(@NonNull Context context) {
            return context.getResources()
                    .getDrawable(R.drawable.none_background_drawable);
        }

        @NonNull
        @Override
        public Drawable getFull(@NonNull Context context) {
            return new ColorDrawable(context.getResources().getColor(R.color.color_primary));
        }
    };

    @NonNull
    public abstract Drawable getThumb(@NonNull Context context);

    @Nullable
    public abstract Drawable getFull(@NonNull Context context);

    public boolean isEmpty() {
        return this == NONE;
    }
}
