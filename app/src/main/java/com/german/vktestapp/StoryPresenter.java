package com.german.vktestapp;

import android.support.annotation.NonNull;

public interface StoryPresenter<T extends Story> {
    void attachView(@NonNull StoryView storyView);
    void save(@NonNull T story);
    void detachView();
}
