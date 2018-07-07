package com.german.vktestapp.editor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;

import com.german.vktestapp.view.StaticDrawable;

public class StickerView extends AppCompatImageView implements StaticDrawable {
    public StickerView(Context context) {
        super(context);
    }

    @Override
    public void drawStatic(@NonNull Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        Matrix drawMatrix = getImageMatrix();
        if (drawMatrix == null) {
            drawable.draw(canvas);
        } else {
            int count = canvas.save();
            canvas.concat(drawMatrix);
            drawable.draw(canvas);
            canvas.restoreToCount(count);
        }
    }
}
