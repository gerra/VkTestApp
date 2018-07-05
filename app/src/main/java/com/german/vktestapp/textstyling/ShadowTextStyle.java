package com.german.vktestapp.textstyling;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.CharacterStyle;

public class ShadowTextStyle extends CharacterStyle {
    @ColorInt
    private final int mTextColor;
    private final float mShadowRadius;
    private final float mShadowDy;
    @ColorInt
    private final int mShadowColor;

    public ShadowTextStyle(@ColorInt int textColor,
                           float shadowRadius,
                           float shadowDy,
                           @ColorInt int shadowColor) {
        mTextColor = textColor;
        mShadowRadius = shadowRadius;
        mShadowDy = shadowDy;
        mShadowColor = shadowColor;
    }

    @Override
    public void updateDrawState(@NonNull TextPaint tp) {
        tp.setColor(mTextColor);
        tp.setShadowLayer(mShadowRadius, 0f, mShadowDy, mShadowColor);
    }
}
