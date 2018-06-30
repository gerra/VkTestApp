package com.german.vktestapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.german.vktestapp.utils.ViewUtils;
import com.german.vktestapp.view.StickerView;

public class StoryEditorTouchEventHandler {
    private static final String TAG = "[EditorTouchHandler]";

    @NonNull
    private final StoryEditorView mStoryEditorView;
    @NonNull
    private final ViewFinder mViewFinder;
    @NonNull
    private final TouchListener mTouchListener;

    @Nullable
    private StickerView mActiveSticker;

    public StoryEditorTouchEventHandler(@NonNull StoryEditorView storyEditorView,
                                        @NonNull ViewFinder viewFinder,
                                        @NonNull TouchListener touchListener) {
        mStoryEditorView = storyEditorView;
        mViewFinder = viewFinder;
        mTouchListener = touchListener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, event.toString());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                float touchX = event.getX();
                float touchY = event.getY();

                View touchedView = mViewFinder.findView(touchX, touchY);
                if (!(touchedView instanceof StickerView)) {
                    // If it's not Sticker, it's background
                    mTouchListener.onBackgroundTouch();
                } else {
                    mActiveSticker = (StickerView) touchedView;
                    mTouchListener.onStickerTouch(mActiveSticker);
                }

                break;
            }
            case MotionEvent.ACTION_MOVE: {

                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mActiveSticker == null && ViewUtils.needToPerformClick(event)) {
                    mStoryEditorView.performClick();
                }
                mActiveSticker = null;
                break;
            }
        }

        return true;
    }

    interface ViewFinder {
        @Nullable
        View findView(float touchX, float touchY);
    }

    interface TouchListener {
        void onStickerTouch(@NonNull StickerView stickerView);
        void onBackgroundTouch();
    }
}
