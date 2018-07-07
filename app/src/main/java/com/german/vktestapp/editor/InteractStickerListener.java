package com.german.vktestapp.editor;

import android.support.annotation.NonNull;

public interface InteractStickerListener {
    void onStartInteract(@NonNull StickerView stickerView);
    void onStopInteract(@NonNull StickerView stickerView);
}
