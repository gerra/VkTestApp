package com.german.vktestapp.stickers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
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
        mOrder.add(stickerView);

        stickerView.setOnTouchListener(new StickerTouchListener(stickerLayoutInfo, mParent));
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

}
