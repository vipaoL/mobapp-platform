package mobileapplication3.platform;

import android.content.Context;
import android.content.SharedPreferences;

public class Records {
    private final static String PREF_NAME = "records";

    public static int[] getRecords(String storeName) {
        SharedPreferences prefs = Platform.getActivityInst().getSharedPreferences(storeName, Context.MODE_PRIVATE);
        String recordsString = prefs.getString(PREF_NAME, "");
        if (recordsString.equals("")) {
            return new int[0];
        }
        String[] valuesStrings = recordsString.split(" ");
        int[] records = new int[valuesStrings.length];
        for (int i = 0; i < valuesStrings.length; i++) {
            records[i] = Integer.parseInt(valuesStrings[i]);
        }
        return records;
    }

    public static void saveRecord(String storeName, int value, int maxStoreSize) {
        insertRecord(storeName, value, findIndexToInsertRecord(getRecords(storeName), value), maxStoreSize);
    }

    private static int findIndexToInsertRecord(int[] records, int value) {
        int i = records.length;
        for (; i > 0; i--) {
            if (value < records[i - 1]) {
                return i;
            }
        }
        return i;
    }

    private static void insertRecord(String storeName, int value, int i, int maxStoreSize) {
        if (i >= maxStoreSize) {
            return;
        }

        int[] oldRecords = getRecords(storeName);
        int[] records = new int[oldRecords.length == maxStoreSize ? oldRecords.length : oldRecords.length + 1];
        System.arraycopy(oldRecords, 0, records, 0, oldRecords.length);
        if (i < records.length) {
            for (int j = records.length - 1; j > i; j--) {
                records[j] = records[j - 1];
            }
        }
        records[i] = value;
        SharedPreferences prefs = Platform.getActivityInst().getSharedPreferences(storeName, Context.MODE_PRIVATE);
        StringBuilder toSave = new StringBuilder();
        for (int a : records) {
            toSave.append(a);
            toSave.append(" ");
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_NAME, toSave.toString().trim());
        editor.commit();
    }
}
