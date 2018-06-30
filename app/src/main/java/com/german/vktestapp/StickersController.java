package com.german.vktestapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.german.vktestapp.view.StickerView;

import java.util.LinkedList;
import java.util.WeakHashMap;

public class StickersController {
    private static final String TAG = "[StickersController]";

    private final View mParent;
    private final WeakHashMap<StickerView, StickerLayoutInfo> mCoordinates = new WeakHashMap<>(50);
    private final LinkedList<StickerView> mOrder = new LinkedList<>();

    public StickersController(View parent) {
        mParent = parent;
    }

    void addSticker(@NonNull StickerView stickerView, float centerX, float centerY) {
        StickerLayoutInfo stickerLayoutInfo = new StickerLayoutInfo(centerX, centerY);
        mCoordinates.put(stickerView, stickerLayoutInfo);

        stickerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        mOrder.add(stickerView);
    }

    void removeSticker(@NonNull StickerView stickerView) {
        mCoordinates.remove(stickerView);
        mOrder.remove(stickerView);
    }

    @Nullable
    StickerLayoutInfo getLayoutInfo(@NonNull StickerView stickerView) {
        return mCoordinates.get(stickerView);
    }

    boolean wasMeasured(@NonNull StickerView stickerView) {
        StickerLayoutInfo info = mCoordinates.get(stickerView);
        if (info == null) {
            Log.w(TAG, "wtf? There is no this sticker?");
        }
        return info != null && info.isWasMeasured();
    }

    void setMeasured(@NonNull StickerView stickerView, float widthRatio, float heightRatio) {
        StickerLayoutInfo info = mCoordinates.get(stickerView);
        if (info == null) {
            Log.w(TAG, "wtf? There is no this sticker?");
            return;
        }
        info.setWasMeasured(true);
        info.setWidthRatio(widthRatio);
        info.setHeightRatio(heightRatio);
    }

    @Nullable
    StickerView getStickerView(int index) {
        return index < mOrder.size()
                ? mOrder.get(index)
                : null;
    }

    public static class StickerLayoutInfo {
        // Percentage relative to parent
        private float mX;
        private float mY;
        private float mWidthRatio;
        private float mHeightRatio;

        private boolean mWasMeasured;

        public StickerLayoutInfo(float x, float y) {
            mX = x;
            mY = y;
        }

        public float getX() {
            return mX;
        }

        public float getY() {
            return mY;
        }

        public boolean isWasMeasured() {
            return mWasMeasured;
        }

        public void setWasMeasured(boolean wasMeasured) {
            mWasMeasured = wasMeasured;
        }

        public void setWidthRatio(float widthRatio) {
            mWidthRatio = widthRatio;
        }

        public float getWidthRatio() {
            return mWidthRatio;
        }

        public float getHeightRatio() {
            return mHeightRatio;
        }

        public void setHeightRatio(float heightRatio) {
            mHeightRatio = heightRatio;
        }
    }
}
