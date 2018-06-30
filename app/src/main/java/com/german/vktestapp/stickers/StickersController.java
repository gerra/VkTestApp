package com.german.vktestapp.stickers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.german.vktestapp.ViewOrderController;
import com.german.vktestapp.view.StickerView;

import java.util.WeakHashMap;

public class StickersController {
    private static final String TAG = "[StickersController]";

    @NonNull
    private final View mParent;
    @NonNull
    private final ViewOrderController mViewOrderController;

    private final WeakHashMap<StickerView, StickerLayoutInfo> mCoordinates = new WeakHashMap<>(50);

    public StickersController(@NonNull View parent,
                              @NonNull ViewOrderController viewOrderController) {
        mParent = parent;
        mViewOrderController = viewOrderController;
    }

    public void addSticker(@NonNull StickerView stickerView, float centerX, float centerY) {
        StickerLayoutInfo stickerLayoutInfo = new StickerLayoutInfo(centerX, centerY);
        mCoordinates.put(stickerView, stickerLayoutInfo);

        stickerView.setOnTouchListener(new StickerTouchListener(stickerLayoutInfo,
                                                                mParent,
                                                                this::moveToTop));
        stickerView.setOnClickListener(this::moveToTop);
    }

    public void removeSticker(@NonNull StickerView stickerView) {
        mCoordinates.remove(stickerView);
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

    private void moveToTop(@NonNull View view) {
        if (view instanceof StickerView) {
            StickerView stickerView = (StickerView) view;
            mViewOrderController.moveToTop(stickerView);
            mParent.invalidate();
        }
    }
}
