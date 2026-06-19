// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform;

import org.robovm.apple.uikit.UIDevice;

public class Battery {
    public static final int ERROR = -1;
    public static final int METHOD_NONE = -1;
    public static final int METHOD_DEFAULT = 0;
    private static int method = METHOD_NONE;


    public static boolean checkAndInit() {
        if (method != METHOD_NONE) {
            return true;
        }

        try {
            UIDevice.getCurrentDevice().setBatteryMonitoringEnabled(true);
            method = METHOD_DEFAULT;
            return true;
        } catch (Throwable t) {
            Platform.showError("Battery init failed", t);
            return false;
        }
    }

    public static int getBatteryLevel() {
        try {
            float level = UIDevice.getCurrentDevice().getBatteryLevel();
            if (level < 0) {
                return ERROR;
            }
            return (int) (level * 100);
        } catch (Throwable t) {
            return ERROR;
        }
    }

    public static int getMethod() {
        return method;
    }
}
