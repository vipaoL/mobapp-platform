// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static mobileapplication3.platform.FileUtils.SEP;

public class Platform {
    public static final int SDK_INT = getAndroidAPIVersion();
    private static Activity activityInst = null;
    private static Context context = null;

    public static void init(Activity inst) {
        activityInst = inst;
        context = activityInst;
    }

    public static void init(Context c) {
        context = c;
    }

    public static void showError(String message, Throwable ex) {
        message += " " + ex;
        Logger.log(ex);
        showError(message);
    }

    public static void showError(Throwable ex) {
        Log.e("mobapp error", "error", ex);
        showError(ex.toString());
    }

    public static void showError(final String message) {
        Logger.logErr(message);
        if (activityInst != null) {
            activityInst.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast.makeText(context, "Error: " + message, Toast.LENGTH_LONG).show();
                    } catch (RuntimeException ex) {
                        Logger.logErr("Can't show toast: \"" + message + "\": " + ex);
                    }
                }
            });
        } else {
            Logger.logErr("Can't show toast: activityInst is null");
        }
    }

    public static void vibrate(int ms) {
        if (SDK_INT >= Build.VERSION_CODES.O) {
            ModernAndroidUtils.vibrate(ms);
        } else {
            //deprecated in API 26 (Oreo)
            ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(ms);
        }
    }

    public static void storeString(String str, String storageName) {
        FileUtils.saveStringToFile(str, getStoragePath(storageName));
    }

    public static String readStoreAsString(String storageName) {
        return FileUtils.readStringFromFile(getStoragePath(storageName));
    }

    public static void storeShorts(short[] data, String storageName) throws IOException {
        FileUtils.saveShortArrayToFile(data, getStoragePath(storageName));
    }

    public static DataInputStream readStore(String storageName) {
        return FileUtils.fileToDataInputStream(getStoragePath(storageName));
    }

    public static void clearStore(String storageName) {
        try {
            new File(getStoragePath(storageName)).delete();
        } catch (Exception ignored) { }
    }

    public static String getAppProperty(String key) {
        InputStream is = null;
        try {
            is = getActivityInst().getAssets().open("app.properties");
            Properties props = new Properties();
            props.load(is);
            return props.getProperty(key);
        } catch (Exception ex) {
            return null;
        } finally {
            try {
                is.close();
            } catch (Exception ignored) { }
        }
    }

    public static String getAppVersion() {
        return getAppVersion(context);
    }

    public static String getAppVersion(Context context) {
        try{
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception ex) {
            Log.e("mobapp error", "Can't get app version", ex);
            return null;
        }
    }

    public static boolean platformRequest(String url) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            getActivityInst().startActivity(browserIntent);
        } catch (Exception ex) {
            showError(ex);
        }
        return false;
    }

    public static InputStream getResource(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        try {
            Logger.log("Getting asset: " + path);
            return getActivityInst().getAssets().open(path);
        } catch (Exception ex) {
            Logger.log("Could not load resource " + path + " (" + ex.toString() + ")");
            return null;
        }
    }

    private static String getStoragePath(String storageName) {
        return context.getFilesDir().getPath() + SEP + storageName;
    }

    public static File getFilesDir() {
        return context.getFilesDir();
    }

    public static File getExternalFilesDir() {
        if (SDK_INT >= Build.VERSION_CODES.FROYO) {
            return ModernAndroidUtils.getExternalFilesDir();
        } else {
            return null;
        }
    }

    public static Activity getActivityInst() {
        return activityInst;
    }

    public static void exit() {
        System.exit(0);
    }

    private static int getAndroidAPIVersion() {
        try {
            return ModernAndroidUtils.getAndroidAPIVersion();
        } catch (VerifyError ex) {
            return Integer.parseInt(Build.VERSION.SDK);
        }
    }
}
