// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform;

import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSFileManager;
import org.robovm.apple.foundation.NSSearchPathDirectory;
import org.robovm.apple.foundation.NSSearchPathDomainMask;
import org.robovm.apple.foundation.NSURL;

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author vipaol
 */
public class FileUtils {

    public static final String PREFIX = "";
    public static final char SEP = '/';
    private static final String[] FOLDERS_ON_EACH_DRIVE = {""};
    private static final short[] TESTDATA = new short[]{0, 1, 2, 3};

    private static final String DOCUMENTS_ROOT = "documents://";

    static String toAbsolutePath(String path) {
        if (path == null) {
            return null;
        }
        if (path.startsWith(DOCUMENTS_ROOT)) {
            return getAbsoluteDocumentsRoot() + path.substring(DOCUMENTS_ROOT.length());
        }
        return path;
    }

    private static String getAbsoluteDocumentsRoot() {
        NSArray<NSURL> urls = NSFileManager.getDefaultManager()
                .getURLsForDirectory(
                        NSSearchPathDirectory.DocumentDirectory,
                        NSSearchPathDomainMask.UserDomainMask
                );
        if (urls != null && !urls.isEmpty()) {
            return urls.get(0).getPath() + SEP;
        }
        return System.getProperty("user.home") + "/Documents/";
    }

    public static void saveShortArrayToFile(short[] arr, String path) throws IOException, SecurityException {
        path = toAbsolutePath(path);
        Logger.log("writing " + (arr != null ? (arr.length + " shorts") : null) + " to " + path);
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            if (!file.createNewFile()) {
                throw new IOException("Can't create file \"" + path + "\"");
            }
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream(arr.length*2);
        DataOutputStream dos = new DataOutputStream(buf);
        for (int i = 0; i < arr.length; i++) {
            dos.writeShort(arr[i]);
        }

        dos.flush();
        byte[] data = buf.toByteArray();
        dos.close();

        OutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();
    }

    public static void saveStringToFile(String data, String path) {
        path = toAbsolutePath(path);
        Logger.log("writing " + data + " to " + path);
        try {
            new File(path).getParentFile().mkdirs();
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(path));
            dos.write(data != null ? data.getBytes() : new byte[0]);
            dos.flush();
            dos.close();
        } catch (Exception ex) {
            Logger.log(ex);
            Platform.showError(ex);
            // TODO
        }
    }

    public static String readStringFromFile(String path) {
        path = toAbsolutePath(path);
        Logger.log("reading string from " + path);
        try {
            File file = new File(path);
            byte[] bytes = new byte[(int) file.length()];
            FileInputStream in = new FileInputStream(file);
            in.read(bytes);
            in.close();
            return new String(bytes);
        } catch (FileNotFoundException ex) {
            return null;
        } catch (Exception ex) {
            Logger.log(ex);
            Platform.showError(ex);
            // TODO
        }
        return null;
    }

    public static DataInputStream fileToDataInputStream(String path) {
        path = toAbsolutePath(path);
        try {
            return new DataInputStream(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static String[] getRoots() {
        return new String[]{
                DOCUMENTS_ROOT,
        };
    }

    public static String[] list(String path) throws IOException {
        return new File(toAbsolutePath(path)).list();
    }

    public static String[] enumToArray(Enumeration en) {
        Vector tmp = new Vector(5);
        while (en.hasMoreElements()) {
            tmp.addElement(en.nextElement());
        }

        String[] arr = new String[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            arr[i] = (String) tmp.elementAt(i);
        }
        return arr;
    }

    public static void createFolder(String path) throws IOException {
        new File(toAbsolutePath(path)).mkdirs();
    }

    public static void checkFolder(String path) throws IOException {
        path = path + "test.mgstruct";

        saveShortArrayToFile(TESTDATA, path);
        new File(toAbsolutePath(path)).delete();
    }

    public static String[] getAllPlaces(String folderName) {
        String[] roots = getRoots();
        String[] paths = new String[roots.length * FOLDERS_ON_EACH_DRIVE.length];

        for (int i = 0; i < roots.length; i++) {
            Logger.log("Searching for places in " + roots[i]);
            for (int j = 0; j < FOLDERS_ON_EACH_DRIVE.length; j++) {
                paths[i*FOLDERS_ON_EACH_DRIVE.length + j] = roots[i] + FOLDERS_ON_EACH_DRIVE[j] + folderName + SEP;
            }
        }

        return paths;
    }

}
