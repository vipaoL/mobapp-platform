/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.platform;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
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
                Files.createDirectories(Paths.get(path).getParent());
            } catch (FileAlreadyExistsException e) { }
            Files.write(Paths.get(path), data.getBytes());
        } catch (Exception ex) {
            Platform.showError("Can't save settings", ex);
        }
    }

    public static String readStringFromFile(String path) {
        Logger.log("reading string from " + path);
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (FileNotFoundException | NoSuchFileException ex) {
            Logger.log(ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.log(ex);
            Platform.showError(ex);
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
            getAppStoragePath()
        };
    }

    public static String getAppStoragePath() { // TODO add a command line argument
        return "." + SEP + "Mobapp" + SEP;
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
            Logger.log("Searching for places in " + roots[i]);
            for (int j = 0; j < FOLDERS_ON_EACH_DRIVE.length; j++) {
                paths[i*FOLDERS_ON_EACH_DRIVE.length + j] = roots[i] + FOLDERS_ON_EACH_DRIVE[j] + folderName + SEP;
            }
        }

        return paths;
    }
    
}
