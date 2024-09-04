package mobileapplication3.platform;

import javax.microedition.midlet.MIDlet;

import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.ui.RootContainer;

public class MobappMIDlet extends MIDlet {

    // for numbering snapshots. e.g.: '1', '2', '3', ... .
    // '-1' if release
    // TODO: move to manifest
    public static int PRE_VERSION = -1;

    // time for one frame. 1000ms / 50ms = 20fps
    public static final int TICK_DURATION = 50;


    private boolean isStartedAlready = false;

    public void startApp() {
        if (isStartedAlready) {
            Logger.log("Main:startApp:already started");
            return;
        }

        Platform.init(this);
        isStartedAlready = true;

        Logger.log("Main:constr");
        MenuCanvas menuCanvas = new MenuCanvas();
        Platform.setCurrent(new RootContainer(menuCanvas, null));
    }

    public void pauseApp() { }

    public void destroyApp(boolean unconditional) {
    	notifyDestroyed();
    }
    
    public void closeApp() {
    	destroyApp(true);
    }

}
