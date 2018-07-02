package com.german.vktestapp.stickers;

public class StickerLayoutInfo {
    // Percentage relative to parent
    private float mX;
    private float mY;
    private int mHolderBackgroundWidth;
    private int mHolderBackgroundHeight;
    private float mSelfRatioX = 1f;
    private float mSelfRatioY = 1f;

    private boolean mIsTouched;

    public void setCenter(float x, float y) {
        mX = x;
        mY = y;
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    public int getHolderBackgroundWidth() {
        return mHolderBackgroundWidth;
    }

    public void setHolderBackgroundSizes(int holderBackgroundWidth, int holderBackgroundHeight) {
        mHolderBackgroundWidth = holderBackgroundWidth;
        mHolderBackgroundHeight = holderBackgroundHeight;
    }

    public int getHolderBackgroundHeight() {
        return mHolderBackgroundHeight;
    }

    public void setHolderBackgroundHeight(int holderBackgroundHeight) {
        mHolderBackgroundHeight = holderBackgroundHeight;
    }

    public void setSelfRatios(float selfRatioX, float selfRatioY) {
        mSelfRatioX = selfRatioX;
        mSelfRatioY = selfRatioY;
    }

    public float getSelfRatioX() {
        return mSelfRatioX;
    }

    public float getSelfRatioY() {
        return mSelfRatioY;
    }

    public void setTouched(boolean touched) {
        mIsTouched = touched;
    }

    public boolean isTouched() {
        return mIsTouched;
    }
}
