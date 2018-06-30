package com.german.vktestapp.view;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;

public class StickerView extends AppCompatImageView {
    private static final String TAG = "[StickerView]";

    // Relative to parent:
    private float mRatioCenterX;
    private float mRatioCenterY;

    public StickerView(Context context, float widthRatio, float heightRatio) {
        super(context);

        mRatioCenterX = widthRatio;
        mRatioCenterY = heightRatio;
    }

    public float getRatioCenterX() {
        return mRatioCenterX;
    }

    public float getRatioCenterY() {
        return mRatioCenterY;
    }

    @Override
    public boolean performClick() {
        Log.d(TAG, "performCLick()");
        return super.performClick();
    }
}
