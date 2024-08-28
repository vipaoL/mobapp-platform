package mobileapplication3.platform;

import static mobileapplication3.platform.FileUtils.SEP;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Platform {
	public static final String RES_TYPE_DRAWABLE = "drawable";
	public static final String RES_TYPE_RAW = "raw";
	private static Activity activityInst = null;

	public static void init(Activity inst) {
		activityInst = inst;
	}

	public static void showError(String message) {
		activityInst.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(activityInst, "Error: " + message, Toast.LENGTH_LONG).show();
			}
		});
    }

	public static void showError(Throwable ex) {
		showError(ex.toString());
		ex.printStackTrace();
	}

	public static void vibrate(int ms) {
		Vibrator v = (Vibrator) activityInst.getSystemService(Context.VIBRATOR_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
		} else {
			//deprecated in API 26
			v.vibrate(ms);
		}
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

	public static String getAppProperty(String string) {
		return "can't get app prop: not implemented yet"; // TODO
	}

	public static boolean platformRequest(String url) {
		// TODO
		return false;
	}

	public static InputStream getResource(String path) {
		try {
			return Resources.getSystem().openRawResource(getResourceID(path, RES_TYPE_RAW));
		} catch (Exception ex) {
			return null;
		}
	}

	public static int getResourceID(String resourceName, String type) {
		if (resourceName.startsWith("/")) {
			resourceName = resourceName.substring(1);
		}
		if (resourceName.contains(".")) {
			resourceName = resourceName.substring(0, resourceName.lastIndexOf("."));
		}
		Log.d("Getting resource", resourceName);
		Resources resources = Platform.getActivityInst().getResources();
		return resources.getIdentifier(resourceName, type, Platform.getActivityInst().getPackageName());
	}

	private static String getStoragePath(String storageName) {
		return activityInst.getFilesDir().getPath() + SEP + storageName;
	}

	public static File getFilesDir() {
		return activityInst.getFilesDir();
	}

	public static Activity getActivityInst() {
		return activityInst;
	}

	public static void exit() {
		System.exit(0);
	}
}
