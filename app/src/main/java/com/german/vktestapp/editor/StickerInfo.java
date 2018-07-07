package com.german.vktestapp.editor;

public class StickerInfo {
    private int mHolderBackgroundWidth;
    private int mHolderBackgroundHeight;
    private float mSelfRatioX = 1f;
    private float mSelfRatioY = 1f;

    public StickerInfo() {
    }

    public void setHolderBackgroundSizes(int holderBackgroundWidth, int holderBackgroundHeight) {
        mHolderBackgroundWidth = holderBackgroundWidth;
        mHolderBackgroundHeight = holderBackgroundHeight;
    }

    public int getHolderBackgroundWidth() {
        return mHolderBackgroundWidth;
    }

    public int getHolderBackgroundHeight() {
        return mHolderBackgroundHeight;
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
}
