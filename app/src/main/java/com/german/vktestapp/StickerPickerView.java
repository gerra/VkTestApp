package com.german.vktestapp;

import android.support.annotation.NonNull;

import java.util.List;

public interface StickerPickerView {
    void showStickers(@NonNull List<StickerProvider> stickerProviderList);
}
