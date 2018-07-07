package com.german.vktestapp.utils;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.concurrent.TimeUnit;

public class ViewUtils {
    private static final long CLICK_DOWN_TIME = TimeUnit.MILLISECONDS.toMillis(150);
    private static final Rect RECT = new Rect();
    private static final float[] VECTOR = new float[2];

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

    public static boolean needToPerformClick(@NonNull MotionEvent motionEvent) {
        return MotionEventCompat.getActionMasked(motionEvent) == MotionEvent.ACTION_UP
                && motionEvent.getEventTime() - motionEvent.getDownTime() <= CLICK_DOWN_TIME;
    }

    public static boolean isPointInView(@NonNull View view, int pointX, int pointY) {
        synchronized (RECT) {
            view.getHitRect(RECT);
            return RECT.contains(pointX, pointY);
        }
    }

    @NonNull
    public static PointF getPointRelativeToParent(@NonNull View view, float x, float y) {
        float v0;
        float v1;
        synchronized (VECTOR) {
            VECTOR[0] = x;
            VECTOR[1] = y;
            view.getMatrix()
                    .mapPoints(VECTOR);
            v0 = VECTOR[0];
            v1 = VECTOR[1];
        }
        return new PointF(v0 + view.getLeft(), v1 + view.getTop());
    }

    @NonNull
    public static Point getScreenSize(final Context context) {
        final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final Point result = new Point();
        wm.getDefaultDisplay().getSize(result);
        return result;
    }

    @SuppressWarnings("WeakerAccess")
    static void fixGravity(@NonNull EditText editText, @Nullable Editable editable, int cursorGravity, int textGravity) {
        editText.setGravity(!TextUtils.isEmpty(editable) ? textGravity : cursorGravity);
    }
}
