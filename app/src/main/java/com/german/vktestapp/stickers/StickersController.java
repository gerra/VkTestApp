package com.german.vktestapp.stickers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    @NonNull
    public StickerLayoutInfo addSticker(@NonNull StickerView stickerView,
                                        float centerX,
                                        float centerY,
                                        int holderWidth,
                                        int holderHeight) {
        StickerLayoutInfo stickerLayoutInfo = new StickerLayoutInfo(centerX, centerY);
        stickerLayoutInfo.setHolderBackgroundSizes(holderWidth, holderHeight);
        mCoordinates.put(stickerView, stickerLayoutInfo);

        stickerView.setOnTouchListener(new StickerTouchListener(stickerLayoutInfo,
                                                                mParent,
                                                                this::moveToTop));
        stickerView.setOnClickListener(this::moveToTop);

        return stickerLayoutInfo;
    }

    public void removeSticker(@NonNull StickerView stickerView) {
        mCoordinates.remove(stickerView);
    }

    @Nullable
    public StickerLayoutInfo getLayoutInfo(@NonNull StickerView stickerView) {
        return mCoordinates.get(stickerView);
    }

    private void moveToTop(@NonNull View view) {
        if (view instanceof StickerView) {
            StickerView stickerView = (StickerView) view;
            mViewOrderController.moveToTop(stickerView);
            mParent.invalidate();
        }
    }
}
