package mobileapplication3.platform;

import static mobileapplication3.platform.FileUtils.SEP;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Platform {
	private static Activity activityInst = null;

	public static void init(Activity inst) {
		activityInst = inst;
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

	public static void showError(String message) {
		Log.e("Showing toast", message);
		activityInst.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activityInst, "Error: " + message, Toast.LENGTH_LONG).show();
			}
		});
	}

	public static void vibrate(int ms) {
		Vibrator v = (Vibrator) activityInst.getSystemService(Context.VIBRATOR_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
		} else { //deprecated in API 26 (Oreo)
			v.vibrate(ms);
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
		try (InputStream is = getActivityInst().getAssets().open("app.properties")) {
			Properties props = new Properties();
			props.load(is);
			return props.getProperty(key);
		} catch (Exception ex) {
			return null;
		}
	}

	public static String getAppVersion() {
		try{
			return getActivityInst().getPackageManager()
					.getPackageInfo(getActivityInst().getPackageName(), 0).versionName;
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
		return activityInst.getFilesDir().getPath() + SEP + storageName;
	}

	public static File getFilesDir() {
		return activityInst.getFilesDir();
	}

	public static File getExternalFilesDir() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			return activityInst.getExternalFilesDir(null);
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
}
