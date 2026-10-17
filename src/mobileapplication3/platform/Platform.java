// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform;

import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.PopupMessage;

import java.awt.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.jar.Manifest;

public class Platform {
    public static void init() {
        refreshPlatformSettings();
    }

    public static void refreshPlatformSettings() {
        RootContainer.getInst().setFullscreenMode(PlatformSettings.getFullscreenMode());
        Graphics.blackAndWhiteMode = PlatformSettings.getBlackAndWhiteMode();
    }

    public static void showError(String message, Throwable ex) {
        message += " " + ex;
        Logger.log(ex);
        showError(message);
    }

    public static void showError(Throwable ex) {
        Logger.log(ex);
        showError(ex.toString());
    }

    public static void showError(String message) {
        Logger.log("showError: " + message);

        IUIComponent currentScreen = RootContainer.getInst().getRootUIComponent();

        PopupMessage popup = new PopupMessage("Error", message, currentScreen);
        RootContainer.setRootUIComponent(popup);
    }

    public static void vibrate(int ms) {

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
        try {
            return new Manifest(getResource("/META-INF/MANIFEST.MF")).getMainAttributes().getValue(key);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String getAppVersion() {
        return getAppProperty("Implementation-Version");
    }

    public static boolean platformRequest(String url) {
        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                String filePref = "file://";
                if (url.startsWith(filePref)) {
                    url = filePref + new File(url.substring(filePref.length())).getAbsolutePath();
                }
                desktop.browse(URI.create(url));
                return true;
            }
            Logger.logErr("Desktop.Action.BROWSE is not supported. Could not open " + url);
        } catch (Exception ex) {
            Logger.log("Could not open " + url + ": ", ex);
        }

        // XDG fallback: useful when AWT is unavailable in a Wayland session.
        if (System.getProperty("os.name", "").toLowerCase().contains("linux")) {
            try {
                new ProcessBuilder("xdg-open", url).start();
                return true;
            } catch (Exception ex) {
                Logger.logErr("xdg-open could not open " + url + ": " + ex);
            }
        }

        return false;
    }

    public static InputStream getResource(String path) {
        return Platform.class.getResourceAsStream(path);
    }

    private static String getStoragePath(String storageName) {
        return FileUtils.getAppStoragePath() + storageName;
    }

    public static void exit() {
        System.exit(0);
    }
}
