package com.german.vktestapp.view.story;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.german.vktestapp.view.StickerView;

import java.util.Collection;
import java.util.WeakHashMap;

public class StickersController {
    private static final String TAG = "[StickersController]";

    @NonNull
    private final View mParent;
    @NonNull
    private final ViewOrderController mViewOrderController;

    private final WeakHashMap<StickerView, StickerInfo> mCoordinates = new WeakHashMap<>(50);

    public StickersController(@NonNull View parent,
                              @NonNull ViewOrderController viewOrderController) {
        mParent = parent;
        mViewOrderController = viewOrderController;
    }

    @NonNull
    public StickerInfo addSticker(@NonNull StickerView stickerView,
                                  int holderWidth,
                                  int holderHeight) {
        StickerInfo stickerInfo = new StickerInfo();
        stickerInfo.setHolderBackgroundSizes(holderWidth, holderHeight);
        mCoordinates.put(stickerView, stickerInfo);
        stickerView.setOnClickListener(this::moveToTop);

        return stickerInfo;
    }

    public void removeSticker(@NonNull StickerView stickerView) {
        mCoordinates.remove(stickerView);
    }

    @Nullable
    public StickerInfo getStickerInfo(@NonNull StickerView stickerView) {
        return mCoordinates.get(stickerView);
    }

    @NonNull
    public Collection<StickerView> getAllStickers() {
        return mCoordinates.keySet();
    }

    private void moveToTop(@NonNull View view) {
        if (view instanceof StickerView) {
            StickerView stickerView = (StickerView) view;
            mViewOrderController.moveToTop(stickerView);
            mParent.invalidate();
        }
    }
}
