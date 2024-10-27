/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.platform;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

/**
 *
 * @author vipaol
 */
public class FileUtils {
    
    public static final String PREFIX = "file:///";
    public static final char SEP = '/';
    private static final short[] TESTDATA = new short[]{0, 1, 2, 3};
    
    private static final String[] PLACES_ON_EACH_ROOT = {"", "other" + SEP};
    private static final String[] PLACES_FROM_SYSTEM_PROPS = {"fileconn.dir.photos", "fileconn.dir.graphics"};
    private static String[] OTHER_PLACES = null;
    
    public static void saveShortArrayToFile(short[] arr, String path) throws IOException, SecurityException {
        FileConnection fc = (FileConnection) Connector.open(path, Connector.READ_WRITE);
        if (!fc.exists()) {
            fc.create();
        }

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

        OutputStream fos = fc.openOutputStream();
        fos.write(data);
        fos.close();
        fc.close();
    }
    
    public static DataInputStream fileToDataInputStream(String path) {
        try {
        	if (!path.startsWith(PREFIX)) {
        		path = PREFIX + path;
        	}
        	Logger.log("opening fc: " + path);
            FileConnection fc = (FileConnection) Connector.open(path, Connector.READ);
            Logger.log("opening stream");
            return fc.openDataInputStream();
        } catch (IOException ex) {
            Logger.log(ex);
        }
        return null;
    }
    
    public static String[] getRoots() {
        return enumToArray(FileSystemRegistry.listRoots());
    }
    
    public static String[] list(String path) throws IOException {
    	FileConnection fc = (FileConnection) Connector.open(path, Connector.READ);
    	try {
    		return enumToArray(fc.list());
    	} finally {
			fc.close();
		}
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
    	FileConnection fc = (FileConnection) Connector.open(path, Connector.READ_WRITE);
    	try {
	        if (!fc.exists()) {
	        	createFolder(getParent(path));
	            fc.mkdir();
	        }
    	} finally {
    		fc.close();
    	}
    }
    
    public static String getParent(String path) {
    	for (int i = path.length() - 2; i > PREFIX.length(); i--) {
    		if (path.charAt(i) == SEP) {
    			return path.substring(0, i + 1);
    		}
    	}
    	return null;
    }
    
    public static void checkFolder(String path) throws IOException {
        path = path + "test.mgstruct";
        
        saveShortArrayToFile(TESTDATA, path);
        
        FileConnection fc = (FileConnection) Connector.open(path, Connector.WRITE);
        fc.delete();
        fc.close();
    }
    
    public static String[] getAllPlaces(String folderName) {
    	OTHER_PLACES = new String[PLACES_FROM_SYSTEM_PROPS.length];
    	for (int i = 0; i < PLACES_FROM_SYSTEM_PROPS.length; i++) {
			OTHER_PLACES[i] = PLACES_FROM_SYSTEM_PROPS[i];
		}

        Vector pathsVec = new Vector(8);
        String[] roots = FileUtils.getRoots();
        
        for (int i = 0; i < roots.length; i++) {
            for (int j = 0; j < PLACES_ON_EACH_ROOT.length; j++) {
                pathsVec.addElement(PREFIX + roots[i] + PLACES_ON_EACH_ROOT[j] + folderName + SEP);
            }
        }
        
        for (int i = 0; i < OTHER_PLACES.length; i++) {
            String path = System.getProperty(OTHER_PLACES[i]);
            if (path != null) {
                pathsVec.addElement(path + folderName + SEP);
            }
        }
        
        String[] paths = new String[roots.length * PLACES_ON_EACH_ROOT.length + OTHER_PLACES.length];
        paths = new String[pathsVec.size()];
        
        for (int i = 0; i < pathsVec.size(); i++) {
            paths[i] = (String) pathsVec.elementAt(i);
        }
        
        return paths;
    }
    
}
