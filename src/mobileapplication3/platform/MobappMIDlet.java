package mobileapplication3.platform;

import javax.microedition.midlet.MIDlet;

import mobileapplication3.platform.ui.RootContainer;

public abstract class MobappMIDlet extends MIDlet {

    private boolean isStartedAlready = false;

    protected void startApp() {
        if (isStartedAlready) {
            return;
        }

        Platform.init(this);
        Platform.setCurrent(RootContainer.getInst());
        isStartedAlready = true;
        onStart();
    }

    public abstract void onStart();

    public void pauseApp() { }

    public void destroyApp(boolean unconditional) {
    	notifyDestroyed();
    }

    public void closeApp() {
    	destroyApp(true);
    }

}
