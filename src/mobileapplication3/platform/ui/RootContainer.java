// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.UISettings;

/**
 *
 * @author vipaol
 */
public class RootContainer {
    public static boolean displayKbHints = false;
    public static boolean enableOnScreenLog = false;
    private static RootContainerView inst = null;

    public static RootContainerView getInst() {
        if (inst == null) {
            inst = new RootContainerView();
        }
        return inst;
    }

    public static void init() {
        if (inst != null) {
            inst.initContainer();
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
        if (inst != null) {
            inst.setRootUIComponent(rootUIComponent);
        }
        return inst;
    }

    public static int getAction(int keyCode) {
        return 0;
    }
}
