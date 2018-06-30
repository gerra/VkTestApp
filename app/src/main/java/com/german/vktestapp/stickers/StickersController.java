package com.german.vktestapp.stickers;

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

    public void addSticker(@NonNull StickerView stickerView,
                    float centerX,
                    float centerY) {
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

    public void removeSticker(@NonNull StickerView stickerView) {
        mCoordinates.remove(stickerView);
        mOrder.remove(stickerView);
    }

    @Nullable
    public StickerLayoutInfo getLayoutInfo(@NonNull StickerView stickerView) {
        return mCoordinates.get(stickerView);
    }

    public void setRatios(@NonNull StickerView stickerView, float widthRatio, float heightRatio) {
        StickerLayoutInfo info = mCoordinates.get(stickerView);
        if (info == null) {
            Log.w(TAG, "wtf? There is no this sticker?");
            return;
        }
        info.setWidthRatio(widthRatio);
        info.setHeightRatio(heightRatio);
    }

    @Nullable
    public StickerView getStickerView(int index) {
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
