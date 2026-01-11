// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import android.content.Context;
import android.os.Build;

import mobileapplication3.platform.Platform;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.Keys;
import mobileapplication3.ui.UISettings;

/**
 *
 * @author vipaol
 */
public class RootContainer {
    public static boolean displayKbHints = false;
    public static boolean enableOnScreenLog = false;
    private static RootContainerView inst = null;

    public static RootContainerView createView(Context context) {
        if (Platform.SDK_INT >= Build.VERSION_CODES.FROYO) {
            inst = new RootContainerViewModern(context);
        } else {
            inst = new RootContainerViewLegacy(context);
        }
        return inst;
    }

    public static RootContainerView getInst() {
        if (inst == null) {
            throw new IllegalStateException("inst is null. Call createView(Context) first");
        }
        return inst;
    }

    public static void init() {
        if (inst != null) {
            inst.init();
        }
	}

    public static RootContainerView setUISettings(UISettings uiSettings) {
        getInst().setUiSettings(uiSettings);
        if (uiSettings != null) {
            uiSettings.onChange();
        }
        return inst;
    }

    public static RootContainerView setRootUIComponent(IUIComponent rootUIComponent) {
        inst.setRootUIComponent(rootUIComponent);
        return inst;
    }

    public static int getAction(int keyCode) {
        switch (keyCode) {
            case Keys.KEY_UP:
            case Keys.KEY_NUM2:
                return Keys.UP;
            case Keys.KEY_DOWN:
            case Keys.KEY_NUM8:
                return Keys.DOWN;
            case Keys.KEY_LEFT:
            case Keys.KEY_NUM4:
                return Keys.LEFT;
            case Keys.KEY_RIGHT:
            case Keys.KEY_NUM6:
                return Keys.RIGHT;
            case Keys.KEY_CENTER:
            case Keys.KEY_NUM5:
                return Keys.FIRE;
            case Keys.KEY_NUM1:
                return Keys.GAME_A;
            case Keys.KEY_NUM3:
                return Keys.GAME_B;
            case Keys.KEY_NUM7:
                return Keys.GAME_C;
            case Keys.KEY_NUM9:
                return Keys.GAME_D;
            default:
                return keyCode;
        }
    }
}
