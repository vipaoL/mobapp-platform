/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.platform;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
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
        File file = new File(path);

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
        openDirectory(Uri.parse(path));
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
        try {
            f.mkdirs();
        } catch (Exception ex) {
            openDirectory(Uri.parse(path));
            f.mkdirs();
        }
    }
    
    public static void checkFolder(String path) throws IOException {
        if (!(new File(path)).canWrite()) {
            openDirectory(Uri.parse(path));
        }

        path = path + "test.mgstruct";

        saveShortArrayToFile(TESTDATA, path);
        new File(path).delete();
    }

    public static void openDirectory(Uri uriToLoad) {
        //checkAndRequestPermissions();
//        if (SDK_INT >= Build.VERSION_CODES.O) {
//            // Choose a directory using the system's file picker.
//            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//
//            // Optionally, specify a URI for the directory that should be opened in
//            // the system file picker when it loads.
//            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);
//
//            MainActivity.inst.startActivityForResult(intent, Activity.RESULT_OK);
//            final int takeFlags = intent.getFlags()
//                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
//                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            // Check for the freshest data.
//            MainActivity.inst.getContentResolver().takePersistableUriPermission(uriToLoad, takeFlags);
//        } else {
//            checkAndRequestPermissions();
//        }

    }

    private static void checkAndRequestPermissions() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) { //request for the permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", Platform.getActivityInst().getPackageName(), null);
                intent.setData(uri);
                if (Platform.getActivityInst().hasWindowFocus()) { // to prevent spamming with permission windows
                    Platform.getActivityInst().startActivity(intent);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) { }
                }
            }
        } else {
            if (SDK_INT >= Build.VERSION_CODES.M) {
                Platform.getActivityInst().requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 123);
            }
        }
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
