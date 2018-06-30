package com.german.vktestapp.stickerpicker;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

public interface StickerPickListener {
    void onStickerPicked(@NonNull Bitmap sticker);
}
