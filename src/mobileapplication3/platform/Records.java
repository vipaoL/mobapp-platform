package mobileapplication3.platform;

public class Records {
    private final static String STORE_NAME = "records";

    public static int[] getRecords() {
        String recordsString = Platform.readStoreAsString(STORE_NAME);

        if (recordsString == null || recordsString.equals("")) {
            return new int[0];
        }

        String[] valuesStrings = Utils.split(recordsString, " ");
        int[] records = new int[valuesStrings.length];
        for (int i = 0; i < valuesStrings.length; i++) {
            records[i] = Integer.parseInt(valuesStrings[i]);
        }
        return records;
    }

    public static void saveRecord(int value, int maxStoreSize) {
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

    private static void insertRecord(int value, int i, int maxStoreSize) {
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
        StringBuffer toSave = new StringBuffer();
        for (int j = 0; j < records.length; j++) {
            int a = records[j];
            toSave.append(a);
            toSave.append(" ");
        }
        Platform.storeString(toSave.toString(), STORE_NAME);
    }
}
