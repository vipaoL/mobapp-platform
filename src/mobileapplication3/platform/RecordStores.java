// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author vipaol
 */
class RecordStores {
    private static final Object lock = new Object();

    public static void writeBytesToStore(byte[] data, String recordStoreName) throws RecordStoreException {
        synchronized (lock) {
            try {
                RecordStore.deleteRecordStore(recordStoreName);
            } catch (Exception ignored) { }

            RecordStore rs = RecordStore.openRecordStore(recordStoreName, true);
            try {
                rs.addRecord(data, 0, data.length);
            } finally {
                rs.closeRecordStore();
            }
        }
    }

    public static void writeStringToStore(String str, String recordStoreName) throws UnsupportedEncodingException, RecordStoreException {
        if (str != null) {
            writeBytesToStore(str.getBytes("UTF-8"), recordStoreName);
        }
    }

    public static String readStringFromStore(String recordStoreName) {
        String ret = null;
        byte[] bytes = readBytesFromStore(recordStoreName);
        if (bytes != null) {
            try {
                ret = new String(bytes, "UTF-8");
            } catch (Exception ex) {
                Logger.log(ex);
            }
        }
        return ret;
    }

    public static DataInputStream openDataInputStream(String recordStoreName) {
        DataInputStream ret = null;
        try {
            byte[] data = readBytesFromStore(recordStoreName);
            if (data != null && data.length != 0) {
                ret = new DataInputStream(new ByteArrayInputStream(data));
            }
        } catch (Exception ex) {
            Logger.log(ex);
        }
        return ret;
    }

    public static byte[] readBytesFromStore(String recordStoreName) {
        byte[] data = null;
        synchronized (lock) {
            try {
                RecordStore rs = RecordStore.openRecordStore(recordStoreName, false);
                try {
                    data = rs.getRecord(1);
                } finally {
                    rs.closeRecordStore();
                }
            } catch (RecordStoreNotFoundException ignored) {
            } catch (Exception ex) {
                Logger.log(ex);
            }
        }
        return data;
    }

    public static void writeShorts(short[] data, String recordStoreName) throws RecordStoreException {
        writeBytesToStore(shortsToBytes(data), recordStoreName);
    }

    private static byte[] shortsToBytes(short[] data) {
        byte[] resultData = new byte[data.length * 2];
        int c = 0;
        for (int i = 0; i < data.length; i++) {
            resultData[c++] = (byte) (((data[i]) >>> 8) & 0xFF);
            resultData[c++] = (byte) (((data[i])) & 0xFF);
        }
        return resultData;
    }

    public static void deleteStore(String recordStoreName) {
        synchronized (lock) {
            try {
                RecordStore.deleteRecordStore(recordStoreName);
            } catch (RecordStoreNotFoundException ignored) {
            } catch (RecordStoreException ex) {
                Logger.log(ex);
            }
        }
    }
}
