package com.german.vktestapp;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StoryPresenterImpl<T extends Story> implements StoryPresenter<T> {
    private final Executor mSaveExecutor = Executors.newSingleThreadExecutor();

    @NonNull
    private final StorySaver<T> mStorySaver;
    @NonNull
    private final Handler mNotifyHandler;
    @Nullable
    private StoryView mStoryView;

    public StoryPresenterImpl(@NonNull StorySaver<T> storySaver, @NonNull Handler notifyHandler) {
        mStorySaver = storySaver;
        mNotifyHandler = notifyHandler;
    }

    @Override
    public void attachView(@NonNull StoryView storyView) {
        mStoryView = storyView;
    }

    @Override
    public void save(@NonNull T story) {
        mSaveExecutor.execute(() -> {
            showProgress();
            StorySaver.StorySaveResult result = mStorySaver.save(story);
            hideProgress();
            if (result.isSuccess()) {
                onSaveComplete();
            } else {
                onSaveError();
            }
        });
    }

    @Override
    public void detachView() {
        if (mStoryView == null) {
            throw new IllegalStateException("View was already detached");
        }

        mStoryView = null;
    }

    void showProgress() {
        mNotifyHandler.post(() -> {
            if (mStoryView != null) {
                mStoryView.showProgress();
            }
        });
    }

    void hideProgress() {
        mNotifyHandler.post(() -> {
            if (mStoryView != null) {
                mStoryView.hideProgress();
            }
        });
    }

    void onSaveComplete() {
        mNotifyHandler.post(() -> {
            if (mStoryView != null) {
                mStoryView.onSaveComplete();
            }
        });
    }

    void onSaveError() {
        mNotifyHandler.post(() -> {
            if (mStoryView != null) {
                mStoryView.onSaveError();
            }
        });
    }
}
