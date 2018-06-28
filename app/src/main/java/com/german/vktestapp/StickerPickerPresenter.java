package com.german.vktestapp;

import android.content.Context;
import android.support.annotation.NonNull;

public interface StickerPickerPresenter {
    void attachView(@NonNull StickerPickerView stickerPickerView);
    void loadStickers(@NonNull Context context);
    void detachView();
}
