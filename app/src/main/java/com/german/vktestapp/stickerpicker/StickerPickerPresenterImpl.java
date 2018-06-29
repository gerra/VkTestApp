package com.german.vktestapp.stickerpicker;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.german.vktestapp.StickerProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StickerPickerPresenterImpl implements StickerPickerPresenter {
    private static final String STICKERS_FOLDER_NAME = "Stickers";

    @Nullable
    private StickerPickerView mStickerPickerView;

    @Override
    public void attachView(@NonNull StickerPickerView stickerPickerView) {
        if (mStickerPickerView != null) {
            throw new IllegalStateException("View is already attached");
        }
        mStickerPickerView = stickerPickerView;
    }

    @Override
    public void loadStickers(@NonNull Context context) {
        AssetManager assetManager = context.getResources()
                .getAssets();

        String[] stickerPaths;
        try {
            stickerPaths = assetManager.list(STICKERS_FOLDER_NAME);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<StickerProvider> stickerProviders = new ArrayList<>(stickerPaths.length);
        for (String stickerFileName : stickerPaths) {
            String stickerFilePath = STICKERS_FOLDER_NAME + File.separator + stickerFileName;
            stickerProviders.add(new StickerProviderImpl(assetManager, stickerFilePath));
        }

        if (mStickerPickerView != null) {
            mStickerPickerView.showStickers(stickerProviders);
        }
    }

    @Override
    public void detachView() {
        if (mStickerPickerView == null) {
            throw new IllegalStateException("View is already detached");
        }
        mStickerPickerView = null;
    }

    static class StickerProviderImpl implements StickerProvider {
        @NonNull
        private final AssetManager mAssetManager;
        @NonNull
        private final String mPath;

        public StickerProviderImpl(@NonNull AssetManager assetManager, @NonNull String path) {
            mAssetManager = assetManager;
            mPath = path;
        }

        @NonNull
        @Override
        public Bitmap getSticker() throws IOException {
            return BitmapFactory.decodeStream(mAssetManager.open(mPath));
        }
    }
}
