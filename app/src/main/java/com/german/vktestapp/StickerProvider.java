package com.german.vktestapp;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.IOException;

public interface StickerProvider {
    @NonNull
    Bitmap getSticker() throws IOException;
}
