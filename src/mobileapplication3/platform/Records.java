package mobileapplication3.platform;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

public class Records {
	private final static String STORE_NAME = "records";
	private final static String OLD_STORE_NAME = "Records";

	public static int[] getRecords() throws RecordStoreNotOpenException, RecordStoreException {
		try {
			RecordStore store = RecordStore.openRecordStore(STORE_NAME, true);

			// ------- migrate "Records" to "records"
			try {
				RecordStore oldStore = RecordStore.openRecordStore(OLD_STORE_NAME, false);
				System.out.println("oldStore.getNumRecords():" + oldStore.getNumRecords());
				if (oldStore.getNumRecords() > 0 && store.getNumRecords() == 0) {
					for (int i = 1; i < oldStore.getNumRecords(); i++) {
						byte[] data = oldStore.getRecord(i + 1);
						store.addRecord(data, 0, data.length);
					}
					RecordStore.deleteRecordStore(OLD_STORE_NAME);
					Logger.log("RecordStore " + OLD_STORE_NAME + " renamed to " + STORE_NAME);
				}
			} catch (RecordStoreNotFoundException ex) {
			} catch (Exception ex) {
				Logger.log(ex);
			}
			// -------

			int[] records = new int[store.getNumRecords()];
			for (int i = 0; i < records.length; i++) {
				records[i] = byteArrayToInt(store.getRecord(i + 1));
			}
			store.closeRecordStore();
			return records;
		} catch (RecordStoreNotFoundException ex) {
			return new int[0];
		}
	}

	public static void saveRecord(int value, int maxStoreSize) throws RecordStoreNotOpenException, RecordStoreFullException, RecordStoreException {
		insertRecord(value, findIndexToInsertRecord(getRecords(), value), maxStoreSize);
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

	private static void insertRecord(int value, int i, int maxStoreSize) throws RecordStoreNotFoundException, RecordStoreException {
		if (i >= maxStoreSize) {
			return;
		}

		int[] oldRecords = getRecords();
		int[] records = new int[oldRecords.length == maxStoreSize ? oldRecords.length : oldRecords.length + 1];
		System.arraycopy(oldRecords, 0, records, 0, oldRecords.length);
		if (i < records.length) {
			for (int j = records.length - 1; j > i; j--) {
				records[j] = records[j - 1];
			}
		}
		records[i] = value;
		try {
			RecordStore.deleteRecordStore(STORE_NAME);
		} catch (Exception ignored) { }
		RecordStore store = RecordStore.openRecordStore(STORE_NAME, true);
		for (int j = 0; j < records.length; j++) {
			byte[] data = intToByteArray(records[j]);
			store.addRecord(data, 0, data.length);
		}
		store.closeRecordStore();
	}

	public static byte[] intToByteArray(int value) {
        return new byte[] {
            (byte) (value >> 24),
            (byte) (value >> 16),
            (byte) (value >> 8),
            (byte) value
        };
    }

    public static int byteArrayToInt(byte[] bytes) {
        return (bytes[0] << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
    }
}
