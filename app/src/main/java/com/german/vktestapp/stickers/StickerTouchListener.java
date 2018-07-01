package com.german.vktestapp.stickers;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.german.vktestapp.utils.ViewUtils;
import com.german.vktestapp.view.StickerView;

public class StickerTouchListener implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = "[StickerTouchListener]";

    @NonNull
    private final StickerLayoutInfo mLayoutInfo;
    @NonNull
    private final View mParent;
    @NonNull
    private final OnStartTouchListener mOnStartTouchListener;

    private final ScaleGestureDetector mScaleDetector;

    private int mActivePointerId;
    private float mLastTouchX;
    private float mLastTouchY;

    public StickerTouchListener(@NonNull StickerLayoutInfo layoutInfo,
                                @NonNull View parent,
                                @NonNull OnStartTouchListener onStartTouchListener) {
        mLayoutInfo = layoutInfo;
        mParent = parent;
        mOnStartTouchListener = onStartTouchListener;

        mScaleDetector = new ScaleGestureDetector(parent.getContext(), this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!(v instanceof StickerView)) {
            return false;
        }
//        Log.d(TAG, MotionEvent.actionToString(event.getAction()) + " " + event.getRawX() + " " + event.getRawY());;

        mScaleDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                int pointerIndex = event.getActionIndex();

                float x = event.getRawX();
                float y = event.getRawY();

                mLastTouchX = x;
                mLastTouchY = y;

                mActivePointerId = event.getPointerId(pointerIndex);

                mLayoutInfo.setTouched(true);
                mOnStartTouchListener.onStartTouch((StickerView) v);

                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int pointerIndex = event.findPointerIndex(mActivePointerId);

//                Log.d(TAG, event.getRawX() + " " + event.getX(pointerIndex));

                float x = event.getRawX();
                float y = event.getRawY();

//                Log.d(TAG, "x = " + x + ", y = " + y);

                float deltaX = x - mLastTouchX;
                float deltaY = y - mLastTouchY;

//                Log.d(TAG, "dx = " + deltaX + ", dy = " + deltaY);

                float deltaPercentageX = deltaX / mParent.getMeasuredWidth();
                float deltaPercentageY = deltaY / mParent.getMeasuredHeight();

                mLayoutInfo.setCenter(mLayoutInfo.getX() + deltaPercentageX,
                                      mLayoutInfo.getY() + deltaPercentageY);

//                v.setTranslationX(deltaX + v.getTranslationX());
//                v.setTranslationY(deltaY + v.getTranslationY());

                mParent.requestLayout();

                mLastTouchX = x;
                mLastTouchY = y;
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (ViewUtils.needToPerformClick(event)) {
                    v.performClick();
                }
                mLayoutInfo.setTouched(false);
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
//                Log.d(TAG, "ActionPointerUp");
                break;
            }
        }

        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Log.d(TAG, "onScale(): " + detector.getScaleFactor());
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }
}
