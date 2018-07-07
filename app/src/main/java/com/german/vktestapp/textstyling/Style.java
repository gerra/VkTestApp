package com.german.vktestapp.textstyling;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Style {
    @NonNull
    public final TextStyle mTextStyle;
    @NonNull
    public final BackgroundStyle mBackgroundStyle;

    public Style(@NonNull TextStyle textStyle, @NonNull BackgroundStyle backgroundStyle) {
        mTextStyle = textStyle;
        mBackgroundStyle = backgroundStyle;
    }

    public static class TextStyle {
        @Nullable
        public final Integer mColor;
        @Nullable
        public final Shadow mShadow;

        public TextStyle(@Nullable Integer color, @Nullable Shadow shadow) {
            mColor = color;
            mShadow = shadow;
        }
    }

    public static class BackgroundStyle {
        @Nullable
        @ColorInt
        public final Integer mColor;
        @Nullable
        public final Shadow mShadow;

        public BackgroundStyle(@Nullable Integer color, @Nullable Shadow shadow) {
            mColor = color;
            mShadow = shadow;
        }
    }

    public static class Shadow {
        @ColorInt
        public final int mColor;
        public final float mSize;
        public final float mRadius;

        public Shadow(int color, float size, float radius) {
            mColor = color;
            mSize = size;
            mRadius = radius;
        }
    }
}
