package com.german.vktestapp.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class ViewUtils {
    private ViewUtils() {
        // no instance
    }

    public static void setEditTextGravity(@NonNull EditText editText, int cursorGravity, int textGravity) {
        fixGravity(editText, editText.getText(), cursorGravity, textGravity);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                fixGravity(editText, s, cursorGravity, textGravity);
            }
        });
    }

    public static void showKeyboard(@NonNull EditText editText, boolean requestFocus) {
        Context context = editText.getContext();
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
        if (requestFocus) {
            editText.requestFocus();
        }
    }

    public static void hideKeyboard(@NonNull View windowTokenHolder) {
        Context context = windowTokenHolder.getContext();
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(windowTokenHolder.getWindowToken(), 0);
        }
    }

    @SuppressWarnings("WeakerAccess")
    static void fixGravity(@NonNull EditText editText, @Nullable Editable editable, int cursorGravity, int textGravity) {
        editText.setGravity(!TextUtils.isEmpty(editable) ? textGravity : cursorGravity);
    }
}
