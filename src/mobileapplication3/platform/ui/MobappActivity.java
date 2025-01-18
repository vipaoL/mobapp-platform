package mobileapplication3.platform.ui;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

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
            setRootContainer(new RootContainer(this));
            RootContainer.setUiSettings(getUISettings());
            RootContainer.setRootUIComponent(getRootUIComponent());
        } catch(Exception ex) {
            ex.printStackTrace();
            Platform.showError(ex);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableFullScreen();
    }

    protected void enableFullScreen() {
        View decorView = this.getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                decorView.setSystemUiVisibility(View.GONE);
            }
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
