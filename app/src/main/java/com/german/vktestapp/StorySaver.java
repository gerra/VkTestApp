package com.german.vktestapp;

import android.support.annotation.NonNull;

public interface StorySaver<T extends Story> {
    @NonNull
    StorySaveResult save(@NonNull T story);

    interface StorySaveResult {
        StorySaveResult SUCCESS = () -> true;
        StorySaveResult FAIL = () -> false;

        boolean isSuccess();
    }
}
