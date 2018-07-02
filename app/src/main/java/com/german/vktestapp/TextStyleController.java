package com.german.vktestapp;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.widget.EditText;

import com.german.vktestapp.backgrounds.Background;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class TextStyleController implements BackgroundSetListener {
    private static final int UNKNOWN_INDEX = -1;

    @NonNull
    private final Collection<EditTextProvider> mEditTextProviders = new HashSet<>();
    @NonNull
    private final List<Style> mStyles;
    private int mCurrentStyleIndex = UNKNOWN_INDEX;

    private boolean mLastBackgroundIsEmpty;

    private MyCharacterStyle mCurrentCharacterStyle;

    public TextStyleController(@NonNull List<Style> styles) {
        mStyles = Collections.unmodifiableList(styles);
    }

    public void addEditTextProvider(@NonNull EditTextProvider editTextProvider) {
        mEditTextProviders.add(editTextProvider);
    }

    public void removeEditTextProvider(@NonNull EditTextProvider editTextProvider) {
        mEditTextProviders.remove(editTextProvider);
    }

    public void toggle() {
        if (mLastBackgroundIsEmpty || mStyles.size() <= 1) {
            return;
        }
        mCurrentStyleIndex = mCurrentStyleIndex != UNKNOWN_INDEX
                ? (mCurrentStyleIndex + 1) % mStyles.size()
                : 0;
        Style currentStyle = mStyles.get(mCurrentStyleIndex);
        if (mCurrentCharacterStyle == null) {
            mCurrentCharacterStyle = new MyCharacterStyle(currentStyle);
        } else {
            mCurrentCharacterStyle.mStyle = currentStyle;
        }

        for (EditTextProvider editTextProvider : mEditTextProviders) {
            EditText editText = editTextProvider.getEditText();
            if (editText == null) {
                continue;
            }
            Editable editable = editText.getText();
            if (editable == null) {
                continue;
            }

            editable.setSpan(mCurrentCharacterStyle,
                             0,
                             editable.length(),
                             Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
    }

    @Override
    public void onBackgroundSet(@NonNull Background background) {
        mLastBackgroundIsEmpty = background.isEmpty();

        if (mCurrentCharacterStyle == null) {
            return;
        }

        if (mLastBackgroundIsEmpty) {
            for (EditTextProvider editTextProvider : mEditTextProviders) {
                EditText editText = editTextProvider.getEditText();
                if (editText == null) {
                    continue;
                }
                Editable editable = editText.getText();
                if (editable == null) {
                    continue;
                }
                editable.removeSpan(mCurrentCharacterStyle);
            }
            mCurrentCharacterStyle = null;
        }
    }

    public static class Style {
        @ColorInt
        private final int mBackgroundColor;
        @ColorInt
        private final int mTextColor;

        public Style(int backgroundColor, int textColor) {
            mBackgroundColor = backgroundColor;
            mTextColor = textColor;
        }

        int getBackgroundColor() {
            return mBackgroundColor;
        }

        int getTextColor() {
            return mTextColor;
        }
    }

    private static class MyCharacterStyle extends BackgroundColorSpan {
        @NonNull
        Style mStyle;

        public MyCharacterStyle(@NonNull Style style) {
            super(style.getBackgroundColor());
            mStyle = style;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.bgColor = mStyle.getBackgroundColor();
            ds.setColor(mStyle.getTextColor());
        }
    }
}
