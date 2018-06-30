package com.german.vktestapp.stickers;

public class StickerLayoutInfo {
    // Percentage relative to parent
    private float mX;
    private float mY;
    private float mWidthRatio;
    private float mHeightRatio;

    private boolean mIsTouched;

    public StickerLayoutInfo(float x, float y) {
        mX = x;
        mY = y;
    }

    public float getX() {
        return mX;
    }

    public void setX(float x) {
        mX = x;
    }

    public float getY() {
        return mY;
    }

    public void setY(float y) {
        mY = y;
    }

    public float getWidthRatio() {
        return mWidthRatio;
    }

    public void setWidthRatio(float widthRatio) {
        mWidthRatio = widthRatio;
    }

    public float getHeightRatio() {
        return mHeightRatio;
    }

    public void setHeightRatio(float heightRatio) {
        mHeightRatio = heightRatio;
    }

    public boolean isTouched() {
        return mIsTouched;
    }

    public void setTouched(boolean touched) {
        mIsTouched = touched;
    }
}
