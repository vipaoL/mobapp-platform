// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_1;
import static android.view.KeyEvent.KEYCODE_2;
import static android.view.KeyEvent.KEYCODE_3;
import static android.view.KeyEvent.KEYCODE_4;
import static android.view.KeyEvent.KEYCODE_5;
import static android.view.KeyEvent.KEYCODE_6;
import static android.view.KeyEvent.KEYCODE_7;
import static android.view.KeyEvent.KEYCODE_8;
import static android.view.KeyEvent.KEYCODE_9;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_CHANNEL_DOWN;
import static android.view.KeyEvent.KEYCODE_CHANNEL_UP;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.KeyEvent.KEYCODE_MENU;
import static android.view.KeyEvent.KEYCODE_POUND;
import static android.view.KeyEvent.KEYCODE_SOFT_LEFT;
import static android.view.KeyEvent.KEYCODE_SOFT_RIGHT;
import static android.view.KeyEvent.KEYCODE_STAR;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.ui.IContainer;
import mobileapplication3.ui.IPopupFeedback;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.Keys;
import mobileapplication3.ui.UISettings;

public abstract class RootContainerView extends SurfaceView implements IContainer, IPopupFeedback, SurfaceHolder.Callback {
    private IUIComponent rootUIComponent = null;
    private KeyboardHelper kbHelper;
    private int bgColor = 0x000000;
    public int w, h;
    private UISettings uiSettings;
    private SurfaceHolder surfaceHolder;
    private Canvas c;
    private static Thread repaintThread = null;
    private boolean wasDownEvent = false, wasDragged = false;
    private boolean surfaceCreated = false;
    private boolean rootUIComponentPostInitDone = false;
    private boolean isLocked = false;
    private int lastPointerX, lastPointerY;
    private int pressedX, pressedY;
    private long pressedTime;

    public RootContainerView(Context context) {
        super(context);
        getHolder().addCallback(this);
        kbHelper = new KeyboardHelper();
        RootContainer.displayKbHints = false;//!hasPointerEvents();
        surfaceHolder = getHolder();
    }

    @Override
    public synchronized void repaint() {
        if (rootUIComponent != null && !rootUIComponent.repaintOnlyOnFlushGraphics()) {
            paint();
        }
    }

    @Override
    public UISettings getUISettings() {
        return uiSettings;
    }

    @Override
    public boolean isOnScreen() {
        return true;
    }

    protected synchronized void paint() {
        if (surfaceCreated) {
            Graphics g = getUGraphics();
            if (rootUIComponent != null) {
                rootUIComponent.paint(g);
            } else {
                g.setColor(0xaaaaaa);
                g.drawString("Nothing to draw. " + rootUIComponent, w/2, h, Graphics.BOTTOM | Graphics.HCENTER);
            }
            Logger.paint(g);
            flushGraphics();
        }
    }

    @Override
    public synchronized Graphics getUGraphics() {
        if (isLocked) {
            flushGraphics();
        }
        isLocked = true;
        try {
            c = surfaceHolder.lockCanvas();
        } catch (Exception ex) {
            flushGraphics();
            c = null;
        }

        if (bgColor >= 0 && c != null) {
            c.drawColor(0xff000000 + bgColor);
        }
        return new Graphics(c);
    }

    @Override
    public synchronized void flushGraphics() {
        if (!isLocked) {
            return;
        }
        isLocked = false;
        try {
            Logger.paint(new Graphics(c));
            surfaceHolder.unlockCanvasAndPost(c);
        } catch (Exception ignored) { }
    }

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public void keyPressed(int keyCode) {
        keyCode = convertKeyCode(keyCode);
        kbHelper.keyPressed(keyCode);
        wasDownEvent = true;
    }

    private void handleKeyPressed(int keyCode, int count) {
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            if (rootUIComponent.keyPressed(keyCode, count)) {
                if (!RootContainer.displayKbHints) {
                    RootContainer.displayKbHints = true;
                    if (uiSettings != null) {
                        uiSettings.onChange();
                    }
                }
                repaint();
            }
        }
    }

    public void keyReleased(int keyCode) {
        keyCode = convertKeyCode(keyCode);
        kbHelper.keyReleased(keyCode);
    }

    private void handleKeyReleased(int keyCode, int count) {
        if (rootUIComponent != null && wasDownEvent) {
            rootUIComponent.setVisible(true);
            if (rootUIComponent.keyReleased(keyCode, count)) {
                repaint();
            }
        }
        wasDownEvent = false;
    }

    protected void handleKeyRepeated(int keyCode, int pressedCount) {
        if (RootContainer.getAction(keyCode) == Keys.FIRE) {
            return;
        }
        if (rootUIComponent != null && wasDownEvent) {
            if (rootUIComponent.keyRepeated(keyCode, pressedCount)) {
                repaint();
            }
        }
    }

    protected void pointerPressed(int x, int y) {
        lastPointerX = x;
        lastPointerY = y;
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            if (rootUIComponent.pointerPressed(x, y)) {
                repaint();
            }
        }
    }

    protected void pointerDragged(int x, int y) {
        if (lastPointerX == x && lastPointerY == y) {
            return;
        }

        lastPointerX = x;
        lastPointerY = y;
        if (rootUIComponent != null && wasDownEvent) {
            if (rootUIComponent.pointerDragged(x, y)) {
                repaint();
            }
        }

        if (!wasDragged) {
            int d = Math.abs(x - pressedX) + Math.abs(y - pressedY);
            if (d > 4) {
                wasDragged = true;
            }
        }
    }

    protected void pointerReleased(int x, int y) {
        if (rootUIComponent != null && wasDownEvent) {
            if (rootUIComponent.pointerReleased(x, y)) {
                repaint();
            }
        }
    }

    protected void pointerClicked(int x, int y) {
        if (rootUIComponent != null && wasDownEvent) {
            if (rootUIComponent.pointerClicked(x, y)) {
                repaint();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pressedX = Math.round(e.getX());
                pressedY = Math.round(e.getY());
                pressedTime = System.currentTimeMillis();
                pointerPressed(pressedX, pressedY);
                wasDownEvent = true;
                break;
            case MotionEvent.ACTION_MOVE:
                pointerDragged(Math.round(e.getX()), Math.round(e.getY()));
                break;
            case MotionEvent.ACTION_UP:
                int releasedX = Math.round(e.getX());
                int releasedY = Math.round(e.getY());
                if (!wasDragged && System.currentTimeMillis() - pressedTime < 1000) {
                    pointerClicked(releasedX, releasedY);
                }
                pointerReleased(releasedX, releasedY);
                wasDownEvent = false;
                wasDragged = false;
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w <= 0 || h <= 0) {
            return;
        }

        this.w = w;
        this.h = h;

        if (RootContainer.enableOnScreenLog) {
            Logger.enableOnScreenLog(h);
        }

        if (rootUIComponent != null) {
            rootUIComponent.setSize(w, h);
            if (!rootUIComponentPostInitDone) {
                rootUIComponent.postInit();
                rootUIComponent.setFocused(true);
                rootUIComponentPostInitDone = true;
            }
            repaint();
        }
    }

    protected void onShow() {
        kbHelper.show();
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            onSizeChanged(getWidth(), getHeight(), 0, 0);
            rootUIComponent.onShow();
        }
        repaint();
    }

    protected void onHide() {
        kbHelper.hide();
        if (rootUIComponent != null) {
            rootUIComponent.onHide();
            rootUIComponent.setVisible(false);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        surfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) { }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        surfaceCreated = false;
    }

    private int convertKeyCode(int androidKeyCode) {
        switch (androidKeyCode) {
            case KEYCODE_ENTER:
            case KEYCODE_DPAD_CENTER:
                return Keys.FIRE;
            case KEYCODE_DPAD_UP:
                return Keys.UP;
            case KEYCODE_DPAD_DOWN:
                return Keys.DOWN;
            case KEYCODE_DPAD_LEFT:
                return Keys.LEFT;
            case KEYCODE_DPAD_RIGHT:
                return Keys.RIGHT;
            case KEYCODE_CHANNEL_UP:
            case KEYCODE_MENU:
            case KEYCODE_SOFT_LEFT:
                return Keys.KEY_SOFT_LEFT;
            case KEYCODE_CHANNEL_DOWN:
            case KEYCODE_BACK:
            case KEYCODE_SOFT_RIGHT:
                return Keys.KEY_SOFT_RIGHT;
            case KEYCODE_0:
                return Keys.KEY_NUM0;
            case KEYCODE_1:
                return Keys.KEY_NUM1;
            case KEYCODE_2:
                return Keys.KEY_NUM2;
            case KEYCODE_3:
                return Keys.KEY_NUM3;
            case KEYCODE_4:
                return Keys.KEY_NUM4;
            case KEYCODE_5:
                return Keys.KEY_NUM5;
            case KEYCODE_6:
                return Keys.KEY_NUM6;
            case KEYCODE_7:
                return Keys.KEY_NUM7;
            case KEYCODE_8:
                return Keys.KEY_NUM8;
            case KEYCODE_9:
                return Keys.KEY_NUM9;
            case KEYCODE_STAR:
                return Keys.KEY_STAR;
            case KEYCODE_POUND:
                return Keys.KEY_POUND;
            default:
                return 0;
        }
    }

    @Override
    public void closePopup() {
        Platform.exit();
    }

    public void setUiSettings(UISettings uiSettings) {
        this.uiSettings = uiSettings;
    }

    public void setRootUIComponent(final IUIComponent rootUIComponent) {
        wasDownEvent = false;
        rootUIComponentPostInitDone = false;
        if (this.rootUIComponent != null) {
            this.rootUIComponent.setVisible(false);
            //this.rootUIComponent.setParent(null);
            this.rootUIComponent.setFocused(false);
        }

        if (rootUIComponent != null) {
            this.rootUIComponent = rootUIComponent.setParent(this).setVisible(true);
            rootUIComponent.init();
            if (getWidth() > 0 && getHeight() > 0) {
                rootUIComponent.setSize(getWidth(), getHeight());
                rootUIComponent.postInit();
                rootUIComponent.setFocused(true);
                rootUIComponentPostInitDone = true;
            }
            if (!rootUIComponent.repaintOnlyOnFlushGraphics() && repaintThread == null) {
                repaintThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!rootUIComponent.repaintOnlyOnFlushGraphics()) {
                            try {
                                Thread.yield();
                                Thread.sleep(200);
                            } catch (InterruptedException ignored) { }
                            repaint();
                        }
                        repaintThread = null;
                    }
                });
                repaintThread.start();
            }
        } else {
            try {
                throw new Exception("setRootUIComponent(): got null");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public IUIComponent getRootUIComponent() {
        return rootUIComponent;
    }

    public void init() {
        UISettings uiSettings = getUISettings();
        RootContainer.enableOnScreenLog = uiSettings == null || uiSettings.enableOnScreenLog();
        if (RootContainer.enableOnScreenLog) {
            if (h > 0) {
                Logger.enableOnScreenLog(h);
            }
        } else {
            Logger.disableOnScreenLog();
        }

        IUIComponent rootUIComponent = getRootUIComponent();
        if (rootUIComponent != null) {
            rootUIComponent.init();
        }
    }

    private class KeyboardHelper {
        private Object tillPressed = new Object();
        private int lastKey, pressCount;
        private boolean pressState;
        private Thread repeatThread;
        private long lastEvent;

        public void show() {
            pressState = false;
            pressCount = 1;
            lastKey = 0;
            repeatThread = new Thread() {
                public void run() {
                    try {
                        while (true) {
                            // Wait until a key is pressed
                            if (!pressState) {
                                synchronized(tillPressed) {
                                    tillPressed.wait();
                                }
                            }

                            // The thread is interrupted when the key is released
                            try {
                                // Wait a delay and repeat
                                Thread.sleep(500);
                                while (true) {
                                    handleKeyRepeated(lastKey, pressCount);
                                    Thread.sleep(150);
                                }
                            } catch (InterruptedException ex) { }
                        }
                    } catch (InterruptedException ignored) { }
                }
            };
            repeatThread.start();
        }

        public void hide() {
            if(repeatThread != null) {
                repeatThread.interrupt();
            }
        }

        public void keyPressed(int k) {
            if (!isLastEventOld() && k == lastKey) {
                pressCount++;
            } else {
                pressCount = 1;
            }

            updateLastEventTime();
            lastKey = k;
            pressState = true;
            synchronized(tillPressed) {
                tillPressed.notify();
            }
            handleKeyPressed(k, pressCount);
        }

        public void keyReleased(int k) {
            updateLastEventTime();
            if(lastKey == k) {
                pressState = false;
            } else {
                pressCount = 0;
            }
            repeatThread.interrupt();
            handleKeyReleased(k, pressCount);
        }

        private boolean isLastEventOld() {
            return System.currentTimeMillis() - lastEvent > 200;
        }

        private void updateLastEventTime() {
            lastEvent = System.currentTimeMillis();
        }
    }
}
