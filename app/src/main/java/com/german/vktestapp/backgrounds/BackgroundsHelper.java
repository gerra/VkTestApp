package com.german.vktestapp.backgrounds;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.german.vktestapp.R;

import java.util.ArrayList;
import java.util.List;

public class BackgroundsHelper {
    private BackgroundsHelper() {
        // no instance
    }

    private static final String[][] GRADIENTS = new String[][] {
            new String[] { "#30F2D2", "#2E7AE6" },
            new String[] { "#CBE645", "#47B347" },
            new String[] { "#FFCC33", "#FF7733" },
            new String[] { "#FF3355", "#990F6B" },
            new String[] { "#F8A6FF", "#6C6CD9" },
            new String[] { "#FF0000", "#0000FF" }
    };

    @NonNull
    public static List<BackgroundProvider> getDefaultBackgrounds() {
        List<BackgroundProvider> backgroundProviders = new ArrayList<>(GRADIENTS.length + 3);
        backgroundProviders.add(() -> Background.NONE);
        for (String[] gradient : GRADIENTS) {
            final int colorFrom = Color.parseColor(gradient[0]);
            final int colorTo = Color.parseColor(gradient[1]);
            backgroundProviders.add(() -> makeGradientBackground(colorFrom, colorTo));
        }
        backgroundProviders.add(() -> new ResourcesBackground(R.drawable.thumb_beach, R.drawable.full_background_beach));
        backgroundProviders.add(() -> new ResourcesBackground(R.drawable.thumb_stars, R.drawable.full_background_stars));
        return backgroundProviders;
    }

    @NonNull
    private static Background makeGradientBackground(@ColorInt int colorFrom, @ColorInt int colorTo) {
        return new GradientBackground(colorFrom, colorTo);
    }

    private static class GradientBackground implements Background {
        @ColorInt
        private final int mColorFrom;
        @ColorInt
        private final int mColorTo;

        public GradientBackground(int colorFrom, int colorTo) {
            mColorFrom = colorFrom;
            mColorTo = colorTo;
        }

        @NonNull
        @Override
        public Drawable getThumb(@NonNull Context context) {
            return getFull(context);
        }

        @NonNull
        @Override
        public Drawable getFull(@NonNull Context context) {
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                                                                     new int[] { mColorFrom, mColorTo });
            gradientDrawable.setCornerRadius(0f);

            return gradientDrawable;
        }
    }

    private static class ResourcesBackground implements Background {
        @DrawableRes
        private final int mThumbResId;
        @DrawableRes
        private final int mFullResId;

        public ResourcesBackground(int thumbResId, int fullResId) {
            mThumbResId = thumbResId;
            mFullResId = fullResId;
        }

        @NonNull
        @Override
        public Drawable getThumb(@NonNull Context context) {
            return context.getResources()
                    .getDrawable(mThumbResId);
        }

        @NonNull
        @Override
        public Drawable getFull(@NonNull Context context) {
            return context.getResources()
                    .getDrawable(mFullResId);
        }
    }
}
