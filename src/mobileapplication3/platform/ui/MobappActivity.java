// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.ModernAndroidUtils;
import mobileapplication3.platform.Platform;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.UISettings;

public abstract class MobappActivity extends Activity {
    protected RootContainerView rootContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Platform.init(this);
            setRootContainer(RootContainer.createView(this));
            RootContainer.setUISettings(getUISettings());
            RootContainer.setRootUIComponent(getRootUIComponent());
        } catch(Exception ex) {
            Platform.showError(ex);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableFullScreen();
    }

    protected void enableFullScreen() {
        if (Platform.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ModernAndroidUtils.enableFullScreen();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getRepeatCount() > 0)  {
            return false;
        }
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_HOME:
                return false;
        }
        if (rootContainer != null) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                rootContainer.keyPressed(keyCode);
                return true;
            }
            if (event.getAction() == KeyEvent.ACTION_UP) {
                rootContainer.keyReleased(keyCode);
                return true;
            }
        }
        return false;
    }

    public void setRootContainer(final RootContainerView newRootContainer) {
        rootContainer = newRootContainer;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    setContentView(newRootContainer);
                } catch (Throwable ex) {
                    Platform.showError(ex);
                    Platform.exit();
                }
            }
        });
    }

    protected void onUISettingsChange() {
        try {
            rootContainer.init();
        } catch (Exception ex) {
            Logger.log(ex);
        }
    }

    protected abstract IUIComponent getRootUIComponent();
    protected abstract UISettings getUISettings();

}
