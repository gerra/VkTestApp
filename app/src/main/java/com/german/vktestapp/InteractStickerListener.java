package com.german.vktestapp;

import android.support.annotation.NonNull;

import com.german.vktestapp.view.StickerView;

public interface InteractStickerListener {
    void onStartInteract(@NonNull StickerView stickerView);
    void onStopInteract(@NonNull StickerView stickerView);
}
