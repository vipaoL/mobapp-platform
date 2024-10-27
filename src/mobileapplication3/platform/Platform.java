package mobileapplication3.platform;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.rms.RecordStoreException;

public class Platform {
	private static MobappMIDlet midletInst = null;

	public static void init(MobappMIDlet inst) {
		midletInst = inst;
	}

	public static void showError(String message) {
		Logger.log(message);
		Image alertImage = null;
        try {
            alertImage = Image.createImage("/driver.png");
        } catch (IOException ex1) {
            try {
                alertImage = Image.createImage("resourse://driver.png");
            } catch (IOException e) {
                e.printStackTrace();
                Logger.log("Can't load alert image");
            }
        }
    	setCurrent(new Alert("Error!", message, alertImage, AlertType.ERROR));
    }

	public static void showError(Throwable ex) {
		Logger.log(ex);
		showError(ex.toString());
	}

	public static void showError(String message, Throwable ex) {
		message += " " + ex;
		Logger.log(ex);
		showError(message);
	}

	public static void vibrate(int ms) {
        getDisplay().vibrate(ms);
    }

	public static void storeShorts(short[] data, String storageName) throws RecordStoreException {
		RecordStores.writeShorts(data, storageName);
	}

	public static DataInputStream readStore(String storageName) {
		return RecordStores.openDataInputStream(storageName);
	}

	public static void clearStore(String storageName) {
		RecordStores.deleteStore(storageName);
	}

	public static void storeString(String data, String storageName) {
		try {
			RecordStores.writeStringToStore(data, storageName);
		} catch (Exception ex) {
			Logger.log(ex);
			showError("Can't write string " + data + " to " + storageName, ex);
		}
	}

	public static String readStoreAsString(String storageName) {
		return RecordStores.readStringFromStore(storageName);
	}

	public static void setCurrent(Displayable d) {
		Display display = getDisplay();
        if (d instanceof Alert) {
            try {
            	display.setCurrent((Alert) d, display.getCurrent());
            } catch (Exception ex) {
				display.setCurrent(d);
			}
        } else {
        	display.setCurrent(d);
        }
    }

	private static Display getDisplay() {
		return Display.getDisplay(midletInst);
	}

	public static String getAppProperty(String string) {
		return midletInst.getAppProperty(string);
	}

	public static boolean platformRequest(String url) {
		try {
			return midletInst.platformRequest(url);
		} catch (ConnectionNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static InputStream getResource(String path) {
		return midletInst.getClass().getResourceAsStream(path);
	}

	public static String getAppVersion() {
		return getAppProperty("MIDlet-Version");
	}

	public static void exit() {
		midletInst.destroyApp(true);
	}
}
