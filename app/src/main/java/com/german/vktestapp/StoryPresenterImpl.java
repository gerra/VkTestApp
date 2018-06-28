package com.german.vktestapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class StoryPresenterImpl implements StoryPresenter {


    @Nullable
    private StoryView mStoryView;

    public StoryPresenterImpl() {

    }



    @Override
    public void changeTextStyle() {

    }

    @Override
    public void attachView(@NonNull StoryView storyView) {
        mStoryView = storyView;
    }

    @Override
    public void detachView() {
        if (mStoryView == null) {
            throw new IllegalStateException("View was already detached");
        }

        mStoryView = null;
    }
}
