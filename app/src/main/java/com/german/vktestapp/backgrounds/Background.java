package com.german.vktestapp.backgrounds;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.german.vktestapp.R;

public interface Background {
    Background NONE = new Background() {
        @NonNull
        @Override
        public Drawable getThumb(@NonNull Context context) {
            return context.getResources()
                    .getDrawable(R.drawable.none_background_drawable);
        }

        @Nullable
        @Override
        public Drawable getFull(@NonNull Context context) {
            return null;
        }
    };

    @NonNull
    Drawable getThumb(@NonNull Context context);

    @Nullable
    Drawable getFull(@NonNull Context context);
}
