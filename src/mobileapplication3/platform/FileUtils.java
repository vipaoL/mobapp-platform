/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.platform;

import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    
    public static void saveShortArrayToFile(short[] arr, String path) throws IOException, SecurityException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(arr.length*2);
        DataOutputStream dos = new DataOutputStream(buf);
        for (int i = 0; i < arr.length; i++) {
            dos.writeShort(arr[i]);
        }

        dos.flush();
        buf.flush();
        byte[] data = buf.toByteArray();
        dos.close();
        buf.close();

        OutputStream fos = new FileOutputStream(path);
        fos.write(data);
        fos.close();
        fos.close();
    }

    public static void saveStringToFile(String data, String path) {
        Logger.log("writing " + data + " to " + path);
        try {
            try {
                new File(path).getParentFile().mkdirs();
            } catch (Exception ignored) { }
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(path));
            if (data != null) {
                dos.write(data.getBytes());
            }
            dos.flush();
            dos.close();
        } catch (Exception ex) {
            Logger.log(ex);
            Platform.showError(ex);
            // TODO
        }
    }

    public static String readStringFromFile(String path) {
        Logger.log("reading string from " + path);
        try {
            File file = new File(path);
            int length = (int) file.length();

            byte[] bytes = new byte[length];

            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);
            } finally {
                in.close();
            }

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
        try {
            return new DataInputStream(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            return null;
        }
    }
    
    public static String[] getRoots() {
        return new String[]{
                //Environment.getExternalStorageDirectory().getPath() + SEP,
                String.valueOf(Platform.getFilesDir()) + SEP,
                String.valueOf(Platform.getExternalFilesDir()) + SEP
        };
    }
    
    public static String[] list(String path) throws IOException {
        return new File(path).list();
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
        File f = new File(path);
        f.mkdirs();
    }
    
    public static void checkFolder(String path) throws IOException {
        path = path + "test.mgstruct";

        saveShortArrayToFile(TESTDATA, path);
        new File(path).delete();
    }

    public static String[] getAllPlaces(String folderName) {
        String[] roots = getRoots();
        String[] paths = new String[roots.length * FOLDERS_ON_EACH_DRIVE.length];

        for (int i = 0; i < roots.length; i++) {
            Log.d("Searching for places in", roots[i]);
            for (int j = 0; j < FOLDERS_ON_EACH_DRIVE.length; j++) {
                paths[i*FOLDERS_ON_EACH_DRIVE.length + j] = roots[i] + FOLDERS_ON_EACH_DRIVE[j] + folderName + SEP;
            }
        }

        return paths;
    }
    
}
