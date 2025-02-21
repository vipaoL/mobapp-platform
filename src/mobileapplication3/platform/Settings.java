/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author vipaol
 */
public class Settings {
    public static final String
            TRUE = "1",
            FALSE = "0",
            UNDEF = "";

    private static final char SEP = '\n';

    private final String storeName;
    private String[] settingsKeysVals;
    private final String[] keys;


    public Settings(String[] keys, String storeName) {
        this.keys = keys;
        this.storeName = "mobapp-settings-" + storeName + ".txt";
    }

    public void saveToDisk() {
        Platform.storeString(getCurrentSettingsAsStr(), storeName);
    }

    public void resetSettings() {
        loadDefaults();
        saveToDisk();
    }

    public void loadDefaults() {
        settingsKeysVals = new String[keys.length * 2];
        for (int i = 0; i < keys.length; i++) {
            settingsKeysVals[i*2] = keys[i];
            settingsKeysVals[i*2 + 1] = UNDEF;
        }
    }

    public void loadFromDisk() {
        loadFromString(Platform.readStoreAsString(storeName));
    }

    public void loadFromString(String str) {
        loadDefaults();
        if (str == null) {
            return;
        }

        String[] keyValueCouples = Utils.split(str.substring(0, str.length() - 1), "" + SEP);
        for (int i = 0; i < keyValueCouples.length; i++) {
            int splitterIndex = keyValueCouples[i].indexOf(' ');
            String key = keyValueCouples[i].substring(0, splitterIndex);
            String value = keyValueCouples[i].substring(splitterIndex + 1);
            for (int j = 0; j < settingsKeysVals.length / 2; j++) {
                if (key.equals(settingsKeysVals[j*2])) {
                    settingsKeysVals[j*2 + 1] = value;
                }
            }
        }
    }

    public String getCurrentSettingsAsStr() {
        if (settingsKeysVals == null) {
            loadFromDisk();
        }

        StringBuffer sb = new StringBuffer(settingsKeysVals.length*5);
        for (int i = 0; i < settingsKeysVals.length / 2; i++) {
            sb.append(settingsKeysVals[i*2]);
            sb.append(" ");
            sb.append(settingsKeysVals[i*2 + 1]);
            sb.append(SEP);
        }
        return sb.toString();
    }

    public boolean set(String key, String value) {
        if (settingsKeysVals == null) {
            loadDefaults();
            loadFromDisk();
        }

        for (int i = 0; i < settingsKeysVals.length / 2; i++) {
            if (settingsKeysVals[i*2].equals(key)) {
                settingsKeysVals[i*2 + 1] = value;
                saveToDisk();
                return true;
            }
        }

        return false;
    }
    
    public boolean set(String key, boolean value) {
        return set(key, value ? TRUE : FALSE);
    }

    public String getStr(String key) {
        if (settingsKeysVals == null) {
            loadDefaults();
            loadFromDisk();
        }

        for (int i = 0; i < settingsKeysVals.length / 2; i++) {
            if (settingsKeysVals[i*2].equals(key)) {
                String value = settingsKeysVals[i*2 + 1];
                if (value.equals(null)) {
                    value = UNDEF;
                }
                return value;
            }
        }
        return null;
    }

    public String getStr(String key, String defValue) {
        if (settingsKeysVals == null) {
            loadFromDisk();
        }
        String value = getStr(key);
        if (UNDEF.equals(value)) {
            value = defValue;
            set(key, value);
        }
        return value;
    }
    
    public boolean toggleBool(String key) {
        boolean newValue = !getBool(key);
        set(key, newValue);
        return newValue;
    }
    
    public boolean getBool(String key) {
    	return TRUE.equals(getStr(key));
    }
    
    public boolean getBool(String key, boolean defaultValue) {
        return TRUE.equals(getStr(key, toStr(defaultValue)));
    }
    
    public int getInt(String key, int defaultValue) throws IllegalArgumentException {
    	String value = getStr(key, String.valueOf(defaultValue));
        set(key, String.valueOf(value));
    	return Integer.parseInt(value);
    }
    
    private String toStr(boolean b) {
        return b ? TRUE : FALSE;
    }
}
