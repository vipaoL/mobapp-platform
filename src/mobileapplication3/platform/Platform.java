package mobileapplication3.platform;

import static mobileapplication3.platform.FileUtils.SEP;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Platform {
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

	public static void showError(Exception ex) {
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

	private static String getStoragePath(String storageName) {
		return activityInst.getFilesDir().getPath() + SEP + storageName;
	}

	public static File getFilesDir() {
		return activityInst.getFilesDir();
	}

	public static Activity getActivityInst() {
		return activityInst;
	}
}
