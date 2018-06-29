package com.german.vktestapp.stickerpicker;

import android.support.annotation.NonNull;

import com.german.vktestapp.StickerProvider;

import java.util.List;

public interface StickerPickerView {
    void showStickers(@NonNull List<StickerProvider> stickerProviderList);
}
