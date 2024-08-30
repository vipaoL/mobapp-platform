package mobileapplication3.platform.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import mobileapplication3.platform.Platform;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.UISettings;

public abstract class MobappActivity extends Activity {
    protected RootContainer rootContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Platform.init(this);
            IUIComponent rootUIComponent = getRootUIComponent();
            UISettings uiSettings = getUISettings();
            RootContainer rootContainer = new RootContainer(this, rootUIComponent, uiSettings);
            setRootContainer(rootContainer);
        } catch(Exception ex) {
            ex.printStackTrace();
            Platform.showError(ex);
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

    public void setRootContainer(RootContainer newRootContainer) {
        rootContainer = newRootContainer;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(newRootContainer);
            }
        });
    }

    protected void onUISettingsChange() {
        try {
            rootContainer.init();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected abstract IUIComponent getRootUIComponent();
    protected abstract UISettings getUISettings();

}
