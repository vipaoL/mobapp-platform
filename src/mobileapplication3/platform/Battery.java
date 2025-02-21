package mobileapplication3.platform;

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
		return ERROR;
	}
	
	public static int getMethod() {
		return method;
	}
}
