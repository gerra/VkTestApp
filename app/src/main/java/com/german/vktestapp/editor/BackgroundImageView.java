package com.german.vktestapp.editor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.view.ViewGroup;

import com.german.vktestapp.view.StaticDrawable;

public class BackgroundImageView extends AppCompatImageView implements StaticDrawable {
    public BackgroundImageView(Context context) {
        super(context);

        super.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                         ViewGroup.LayoutParams.WRAP_CONTENT));
        super.setAdjustViewBounds(true);
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
