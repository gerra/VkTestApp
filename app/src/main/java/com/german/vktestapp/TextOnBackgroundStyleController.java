package com.german.vktestapp;

import android.support.annotation.NonNull;

import com.german.vktestapp.backgrounds.Background;
import com.german.vktestapp.editor.BackgroundSetListener;
import com.german.vktestapp.textstyling.Style;
import com.german.vktestapp.textstyling.StyleableProvider;
import com.german.vktestapp.textstyling.TextStyleController;

import java.util.List;

public class TextOnBackgroundStyleController extends TextStyleController
        implements BackgroundSetListener {
    private boolean mLastBackgroundIsEmpty;

    public TextOnBackgroundStyleController(@NonNull StyleableProvider styleableProvider,
                                           @NonNull List<Style> styles) {
        super(styleableProvider, styles);
    }

    @Override
    public void onBackgroundSet(@NonNull Background background) {
        boolean backgroundWasEmpty = mLastBackgroundIsEmpty;

        mLastBackgroundIsEmpty = background.isEmpty();
        if (mLastBackgroundIsEmpty) {
            clearStyle();
        } else if (backgroundWasEmpty) {
            updateStyle();
        }
    }

    @Override
    protected boolean checkStyleCanBeSet() {
        return !mLastBackgroundIsEmpty;
    }
}
