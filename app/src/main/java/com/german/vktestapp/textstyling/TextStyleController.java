package com.german.vktestapp.textstyling;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

public class TextStyleController {
    @NonNull
    private final StyleableProvider mStyleableProvider;
    @NonNull
    private final List<Style> mStyles;
    private int mCurrentStyleIndex = 0;

    public TextStyleController(@NonNull StyleableProvider styleableProvider,
                               @NonNull List<Style> styles) {
        mStyleableProvider = styleableProvider;
        mStyles = Collections.unmodifiableList(styles);
    }

    public void toggle() {
        if (mStyles.isEmpty() || !checkStyleCanBeSet()) {
            return;
        }
        mCurrentStyleIndex = (mCurrentStyleIndex + 1) % mStyles.size();
        updateStyle();
    }

    protected void updateStyle() {
        if (mCurrentStyleIndex >= mStyles.size() || !checkStyleCanBeSet()) {
            return;
        }

        Style style = mStyles.get(mCurrentStyleIndex);
        setStyle(style);
    }

    protected void clearStyle() {
        Styleable styleable = mStyleableProvider.getStyleable();
        if (styleable != null) {
            styleable.clearStyle();
        }
    }

    protected boolean checkStyleCanBeSet() {
        return true;
    }

    private void setStyle(@NonNull Style style) {
        Styleable styleable = mStyleableProvider.getStyleable();
        if (styleable != null) {
            styleable.setStyle(style);
        }
    }
}
