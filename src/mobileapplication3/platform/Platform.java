package mobileapplication3.platform;

import java.awt.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.jar.Manifest;

public class Platform {
	private static Frame inst;

	public static void init(Frame instObj) {
		inst = instObj;
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
		Logger.log(message);
		// TODO
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
				desktop.browse(URI.create(url));
			}
		} catch (IOException ex) {
			showError(ex);
		}
		return false;
	}

	public static InputStream getResource(String path) {
		return inst.getClass().getResourceAsStream(path);
	}

	private static String getStoragePath(String storageName) {
		return FileUtils.getAppStoragePath() + storageName;
	}

	public static void exit() {
		System.exit(0);
	}

	public static Frame getAppInst() {
		return inst;
	}
}
