package com.german.vktestapp.view;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.view.MotionEvent;

public class StickerView extends AppCompatImageView {
    public StickerView(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return !isEnabled() || super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onFilterTouchEventForSecurity(MotionEvent event) {
        return isEnabled();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
