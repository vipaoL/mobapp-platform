/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.platform;

import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;

/**
 *
 * @author vipaol
 */
public class Logger {

    // enable or disable on-screen log on start
    private static boolean isOnScreenLogEnabled = false;
    private static boolean isOnScreenLogInited = false;
    private static int lastWroteI = 0;
    private static int logMessageDelay = 0;
    private static String[] onScreenLog = new String[1];
    private static int onScreenLogOffset = 0;
    private static boolean logToStdout = false;

    public static void enableOnScreenLog(int screenHeight) {
        isOnScreenLogEnabled = true;
        int n = screenHeight / Math.max(1, Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL).getHeight());
        if (n < 5) {
            n = 5;
        }
        String[] newLog = new String[n];
        if (onScreenLog != null) {
            System.out.println(Math.min(onScreenLog.length, newLog.length));
            int minL = Math.min(onScreenLog.length, newLog.length);
            System.arraycopy(onScreenLog, 0, newLog, 0, minL);
            if (onScreenLogOffset >= minL) {
                onScreenLogOffset = 0;
            }
        }
        onScreenLog = newLog;
        isOnScreenLogInited = true;
        log("log enabled");
    }

    public static void disableOnScreenLog() {
        log("disabling log...");
        isOnScreenLogEnabled = false;
        lastWroteI = 0;
        if (isOnScreenLogInited) {
            onScreenLog = new String[1];
            isOnScreenLogInited = false;
            onScreenLogOffset = 0;
        }
    }
    
    
    
    public static boolean isOnScreenLogEnabled() {
        return isOnScreenLogEnabled;
    }
    
    public static void logToStdout(boolean enable) {
        logToStdout = enable;
    }

    public static boolean logReplaceLast(String prevMsg, String newMsg) {
        if (logToStdout) {
            System.out.println(newMsg);
        }

        if (!isOnScreenLogEnabled) {
            return false;
        }

        try {
            if (onScreenLog[lastWroteI] == null) {
                return false;
            }
            if (onScreenLog[lastWroteI].equals(prevMsg)) {
                onScreenLog[lastWroteI] = newMsg;
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static void log(Throwable ex) {
        ex.printStackTrace();
        log(ex.toString());
    }

    /*public static void logErr(String text, int value) {
    }*/
    public static void log(String text, int value) {
        if (isOnScreenLogEnabled || logToStdout) {
            log(text + value);
        }
    }

    public static void log(int i) {
        if (isOnScreenLogEnabled || logToStdout) {
            log(String.valueOf(i));
        }
    }

    public static void log(String text) {
        if (logToStdout) {
            System.out.println(text);
        }
        try {
            if (isOnScreenLogEnabled) {
                if (onScreenLog[onScreenLogOffset] != null) {
                    for (int i = 0; i < onScreenLog.length - 1; i++) {
                        onScreenLog[i] = onScreenLog[i + 1];
                    }
                }
                onScreenLog[onScreenLogOffset] = text;
                lastWroteI = onScreenLogOffset;
                if (onScreenLogOffset < onScreenLog.length - 1) {
                    onScreenLogOffset++;
                }
                try {
                    // slowing for log readability
                    if (logMessageDelay > 0) {
                        Thread.sleep(logMessageDelay);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void paint(Graphics g) {
        if (isOnScreenLogEnabled) {
            g.setColor(150, 255, 150);
            Font font = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            g.setFont(font);
            for (int j = 0; j <= onScreenLogOffset; j++) {
                try {
                    if (onScreenLog[j] != null) {
                        g.drawString(onScreenLog[j], 0, font.getHeight() * j, Graphics.TOP | Graphics.LEFT);
                    }
                } catch (NullPointerException ex) {
                    g.drawString(j + "can't show log:NPE", 0, 0, Graphics.TOP | Graphics.LEFT);
                } catch (IllegalArgumentException ex) {
                    g.drawString(j + "can't show log:IAE", 0, 0, Graphics.TOP | Graphics.LEFT);
                }
            }
        }
    }

    public static void setLogMessageDelay(int ms) {
        logMessageDelay = ms;
    }
    
}
