package mobileapplication3.platform;

public class PlatformSettings {
    public static final int UNDEF = -1, TRUE = 1, FALSE = 0;
    private static final String
            STORE_NAME = "platformsettings",
            FONT_SIZE = "fontSize",
            FULLSCREEN_MODE = "fullscreen";

    private static Settings settingsInst = null;
    public static int fontSizeOverride = UNDEF, fullscreenModeOverride = UNDEF;

    private PlatformSettings() { }

    public static void reset() {
        getSettingsInst().resetSettings();
    }

    private static Settings getSettingsInst() {
        if (settingsInst == null) {
            settingsInst = new Settings(new String[]{
                    FONT_SIZE, FULLSCREEN_MODE
            }, STORE_NAME);
        }
        return settingsInst;
    }

    public static int getFontSize() {
        if (fontSizeOverride == UNDEF) {
            return getSettingsInst().getInt(FONT_SIZE, 32);
        } else {
            return fontSizeOverride;
        }
    }

    public static void setFontSize(int value) {
        getSettingsInst().set(FONT_SIZE, String.valueOf(value));
    }

    public static void setFontSizeOverride(int size) {
        fontSizeOverride = size;
    }

    ///

    public static boolean getFullscreenMode() {
        if (fullscreenModeOverride == UNDEF) {
            return getSettingsInst().getBool(FULLSCREEN_MODE, false);
        } else {
            return fullscreenModeOverride == TRUE;
        }
    }

    public static void setFullscreenMode(boolean value) {
        getSettingsInst().set(FULLSCREEN_MODE, value);
    }

    public static void setFullscreenModeOverride(boolean value) {
        fullscreenModeOverride = value ? TRUE : FALSE;
    }
}
