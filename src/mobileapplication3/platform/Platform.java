// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform;

import org.robovm.apple.audiotoolbox.AudioServices;
import org.robovm.apple.foundation.NSBundle;
import org.robovm.apple.foundation.NSOperationQueue;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.uikit.UIAlertAction;
import org.robovm.apple.uikit.UIAlertActionStyle;
import org.robovm.apple.uikit.UIAlertController;
import org.robovm.apple.uikit.UIAlertControllerStyle;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIImpactFeedbackGenerator;
import org.robovm.apple.uikit.UIImpactFeedbackStyle;
import org.robovm.apple.uikit.UIViewController;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Platform {
    private static final int kSystemSoundID_Vibrate = 4095;

    private static UIViewController rootViewController = null;

    public static void init(UIViewController c) {
        rootViewController = c;
    }

    public static void showError(String message, Throwable ex) {
        Logger.log(ex);
        showError(message + " " + ex.getMessage());
    }

    public static void showError(Throwable ex) {
        Logger.log(ex);
        showError(ex.toString());
    }

    public static void showError(final String message) {
        NSOperationQueue.getMainQueue().addOperation(() -> {
            UIAlertController alert = new UIAlertController("Error", message, UIAlertControllerStyle.Alert);
            alert.addAction(new UIAlertAction("OK", UIAlertActionStyle.Default, null));
            if (rootViewController != null) {
                rootViewController.presentViewController(alert, true, null);
            }
        });
    }

    public static void vibrate(int ms) {
        if (ms <= 0) {
            return;
        }

        NSOperationQueue.getMainQueue().addOperation(() -> {
            try {
                if (ms < 500) {
                    UIImpactFeedbackGenerator generator = new UIImpactFeedbackGenerator(UIImpactFeedbackStyle.Light);
                    generator.prepare();
                    generator.impactOccurred();
                } else {
                    AudioServices.playSystemSound(kSystemSoundID_Vibrate);
                }
            } catch (Throwable t) { }
        });
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
        new File(FileUtils.toAbsolutePath(getStoragePath(storageName))).delete();
    }

    public static String getAppProperty(String key) {
        return null;
    }

    public static String getAppVersion() {
        return NSBundle.getMainBundle().getInfoDictionary().getString("CFBundleShortVersionString");
    }

    public static boolean platformRequest(String url) {
        try {
            UIApplication.getSharedApplication().openURL(new NSURL(url));
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
            Logger.log("getResource " + path);
            String fullPath = NSBundle.getMainBundle().getResourcePath() + "/" + path;
            File f = new File(fullPath);
            if (f.exists()) {
                return new FileInputStream(f);
            }
            return null;
        } catch (Exception ex) {
            Logger.log("Could not load resource " + path + " (" + ex.toString() + ")");
            return null;
        }
    }

    private static String getStoragePath(String storageName) {
        return FileUtils.getRoots()[0] + storageName;
    }

    public static void exit() {
        System.exit(0);
    }
}
