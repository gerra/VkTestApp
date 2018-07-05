package com.german.vktestapp.textstyling;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Spanned;
import android.widget.EditText;

import com.german.vktestapp.BackgroundSetListener;
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
    private final List<SpanStyle> mStyles;
    private int mCurrentStyleIndex = 0;

    private boolean mLastBackgroundIsEmpty;

    @Nullable
    private Object[] mCurrentSpans;

    public TextStyleController(@NonNull List<SpanStyle> styles) {
        if (styles.isEmpty()) {
            throw new IllegalArgumentException("Styles should not be empty");
        }
        mStyles = Collections.unmodifiableList(styles);
    }

    public void addEditTextProvider(@NonNull EditTextProvider editTextProvider) {
        mEditTextProviders.add(editTextProvider);
    }

    public void removeEditTextProvider(@NonNull EditTextProvider editTextProvider) {
        mEditTextProviders.remove(editTextProvider);
    }

    public void toggle() {
        if (mLastBackgroundIsEmpty) {
            return;
        }
        removeSpans();

        mCurrentStyleIndex = mCurrentStyleIndex != UNKNOWN_INDEX
                ? (mCurrentStyleIndex + 1) % mStyles.size()
                : 0;
        SpanStyle currentStyle = mStyles.get(mCurrentStyleIndex);
        mCurrentSpans = currentStyle.getSpans();

        update();
    }

    private void update() {
        if (mCurrentSpans != null) {
            for (EditTextProvider editTextProvider : mEditTextProviders) {
                update(editTextProvider, mCurrentSpans);
            }
        }
    }

    private void update(@NonNull EditTextProvider editTextProvider, @NonNull Object[] spans) {
        EditText editText = editTextProvider.getEditText();
        if (editText == null) {
            return;
        }
        Editable editable = editText.getText();
        if (editable == null) {
            return;
        }

        for (Object span : spans) {
            editable.setSpan(span,
                             0,
                             editable.length(),
                             Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
    }

    private void removeSpans() {
        if (mCurrentSpans == null) {
            return;
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
            for (Object span : mCurrentSpans) {
                editable.removeSpan(span);
            }
        }
        mCurrentSpans = null;
    }

    @Override
    public void onBackgroundSet(@NonNull Background background) {
        boolean backgroundWasEmpty = mLastBackgroundIsEmpty;

        mLastBackgroundIsEmpty = background.isEmpty();
        if (mLastBackgroundIsEmpty) {
            removeSpans();
        } else if (backgroundWasEmpty) {
            SpanStyle currentStyle = mStyles.get(mCurrentStyleIndex);
            mCurrentSpans = currentStyle.getSpans();
            update();
        }
    }
}
