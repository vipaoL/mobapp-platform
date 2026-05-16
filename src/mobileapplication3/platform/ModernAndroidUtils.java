// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Choreographer;
import android.view.View;
import mobileapplication3.platform.ui.RootContainerView;

import java.io.File;

import static android.content.Context.BATTERY_SERVICE;

public class ModernAndroidUtils {

    // SDK_INT is supported since API 4
    public static int getAndroidAPIVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static void enableFullScreen() {
        View decorView = Platform.getActivityInst().getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                decorView.setSystemUiVisibility(View.GONE);
            }
        }
    }

    public static File getExternalFilesDir() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return Platform.getActivityInst().getExternalFilesDir(null);
        } else {
            return null;
        }
    }

    public static void vibrate(int ms) {
        Vibrator v = (Vibrator) Platform.getActivityInst().getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        } else { //deprecated in API 26 (Oreo)
            v.vibrate(ms);
        }
    }

    public static void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle, Canvas c, Paint p) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            c.drawArc(x, y, x + width, y + height, startAngle, arcAngle, false, p);
        } else {
            c.drawCircle(x + width / 2f, y + height / 2f, width / 2f, p);
            c.drawCircle(x + width / 2f, y + height / 2f, height / 2f, p);
        }
    }

    public static void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight, Canvas c, Paint p) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && arcWidth > 0 && arcHeight > 0) {
            c.drawRoundRect(x + 0.5f, y + 0.5f, x + width + 0.5f, y + height + 0.5f, arcWidth / 2f, arcHeight / 2f, p);
        } else {
            c.drawRect(x + 0.5f, y + 0.5f, x + width + 0.5f, y + height + 0.5f, p);
        }
    }

    public static int getBatteryLevel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager bm = (BatteryManager) Platform.getActivityInst().getSystemService(BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            return Battery.ERROR;
        }
    }

    public static void setFrameRate(android.view.Surface surface, float frameRate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                surface.setFrameRate(frameRate, android.view.Surface.FRAME_RATE_COMPATIBILITY_DEFAULT);
            } catch (Throwable ignored) { }
        }
    }

    public static Object createVsyncHelper(final RootContainerView view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long frameTimeNanos) {
                    if (view.getTargetFPS() > 0) {
                        view.tickAndPaint();
                        Choreographer.getInstance().postFrameCallback(this);
                    }
                }
            };
        }
        return null;
    }

    public static void startVsync(final Object helper) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && helper instanceof Choreographer.FrameCallback) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Choreographer c = Choreographer.getInstance();
                    c.removeFrameCallback((Choreographer.FrameCallback) helper);
                    c.postFrameCallback((Choreographer.FrameCallback) helper);
                }
            });
        }
    }

    public static void stopVsync(final Object helper) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && helper instanceof Choreographer.FrameCallback) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Choreographer.getInstance().removeFrameCallback((Choreographer.FrameCallback) helper);
                }
            });
        }
    }
}
