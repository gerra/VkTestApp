package com.german.vktestapp;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

public class VibrateEffect implements ActivateRecycleBinEffect {
    private static final long VIBRATE_DURATION = TimeUnit.MILLISECONDS.toMillis(100);

    @Override
    public void playEffectOnActivate(@NonNull Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATE_DURATION,
                                                           VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }
}
