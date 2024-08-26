package mobileapplication3.platform.ui;

import java.io.DataInputStream;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStoreException;

import mobileapplication3.platform.RecordStores;

public class Platform {
	private static MIDlet midletInst = null;
	
	public static void init(MIDlet inst) {
		midletInst = inst;
	}

	public static void showError(String message) {
    	setCurrent(new Alert("Error!", message, null, AlertType.ERROR));
    }

	public static void showError(Exception ex) {
		showError(ex.toString());
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
}
