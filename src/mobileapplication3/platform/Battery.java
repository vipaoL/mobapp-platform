package mobileapplication3.platform;

import static android.content.Context.BATTERY_SERVICE;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

public class Battery {
	public static final int ERROR = -1;
	public static final int METHOD_NONE = -1;
	public static final int METHOD_DEFAULT = 0;
	private static int method = METHOD_NONE;
	
	
	public static boolean checkAndInit() {
		if (method != METHOD_NONE) {
			return true;
		}

        if (getBatteryLevel() == ERROR) {
            return false;
        } else {
            method = METHOD_DEFAULT;
            return true;
        }
    }
	
	public static int getBatteryLevel() {
		try {
			BatteryManager bm = (BatteryManager) Platform.getActivityInst().getSystemService(BATTERY_SERVICE);
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
				return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
			} else {
				IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
				Intent batteryStatus = Platform.getActivityInst().registerReceiver(null, iFilter);
				int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
				int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
				double batteryPct = level / (double) scale;
				return (int) (batteryPct * 100);
			}
		} catch (Exception ex) {
			Logger.log("can't get battery level:");
			Logger.log(ex.toString());
		}
		return ERROR;
	}
	
	public static int getMethod() {
		return method;
	}
}
