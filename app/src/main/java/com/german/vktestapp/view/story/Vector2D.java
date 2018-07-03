package com.german.vktestapp.view.story;

import android.graphics.PointF;

public class Vector2D extends PointF {
    public Vector2D() {
        super();
    }

    public Vector2D(float x, float y) {
        super(x, y);
    }

    public static float getAngle(Vector2D firstVector, Vector2D secondVector) {
        firstVector.normalize();
        secondVector.normalize();
        double degrees = Math.toDegrees(Math.atan2(secondVector.y, secondVector.x)
                                                - Math.atan2(firstVector.y, firstVector.x));
        return (float) degrees;
    }

    public void normalize() {
        float length = (float) Math.hypot(x, y);
        x /= length;
        y /= length;
    }
}
