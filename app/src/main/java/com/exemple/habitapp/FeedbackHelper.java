package com.exemple.habitapp;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

public final class FeedbackHelper {

    private FeedbackHelper() {
    }

    public static void success(Context context) {
        try {
            ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 55);
            tone.startTone(ToneGenerator.TONE_PROP_ACK, 120);
            tone.release();
        } catch (RuntimeException ignored) {
        }

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) return;

        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(50);
        }
    }

    public static void snack(View anchor, String message) {
        Snackbar snackbar = Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(ContextCompat.getColor(anchor.getContext(), R.color.ink));
        snackbar.setTextColor(ContextCompat.getColor(anchor.getContext(), R.color.surface));
        snackbar.show();
    }
}
