package com.german.vktestapp.editor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.german.vktestapp.view.StaticDrawable;

public class BitmapCreator {
    private static final int DEFAULT_WIDTH = 1440;

    private BitmapCreator() {
        // no instance
    }

    @Nullable
    @MainThread
    public static Bitmap createBitmap(@NonNull StoryEditorView storyEditorView) {
        int measuredWidth = storyEditorView.getMeasuredWidth();
        int measuredHeight = storyEditorView.getMeasuredHeight();

        if (measuredWidth <= 0 || measuredHeight <= 0) {
            return null;
        }

        int width = DEFAULT_WIDTH;
        float scale = 1f * width / measuredWidth;

        int height = (int) (width * (1f * measuredHeight / measuredWidth));

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        int initialCount = canvas.save();

        canvas.scale(scale, scale);

        int childCount = storyEditorView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            int drawingOrder = storyEditorView.getChildDrawingOrder(childCount, i);
            View child = storyEditorView.getChildAt(drawingOrder);
            if (child instanceof StaticDrawable) {
                int sc = canvas.save();
                canvas.translate(child.getLeft() - child.getScrollX(),
                                 child.getTop() - child.getScrollY());
                canvas.concat(child.getMatrix());
                ((StaticDrawable) child).drawStatic(canvas);
                canvas.restoreToCount(sc);
            }
        }

        canvas.restoreToCount(initialCount);

        return bitmap;
    }
}
