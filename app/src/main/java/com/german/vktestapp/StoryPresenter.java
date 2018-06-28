package com.german.vktestapp;

import android.support.annotation.NonNull;

public interface StoryPresenter {
    void changeTextStyle();
    void attachView(@NonNull StoryView storyView);
    void detachView();
}
