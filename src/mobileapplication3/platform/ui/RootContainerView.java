// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import mobileapplication3.platform.KeyboardHelper;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.ModernAndroidUtils;
import mobileapplication3.platform.Platform;
import mobileapplication3.ui.*;

import static android.view.KeyEvent.*;

public class RootContainerView extends SurfaceView implements IContainer, IPopupFeedback, SurfaceHolder.Callback, KeyboardHelper.IKeyboardListener {
    private static final int DEFAULT_FONT_HEIGHT = Font.getDefaultFontHeight();

    private IUIComponent rootUIComponent = null;
    private final KeyboardHelper kbHelper;
    private int bgColor = 0x000000;
    public int w, h;
    private UISettings uiSettings;
    private final SurfaceHolder surfaceHolder;
    private Canvas c;
    private boolean wasDownEvent = false, wasDragged = false;
    private boolean surfaceCreated = false;
    private boolean rootUIComponentPostInitDone = false;
    private boolean isLocked = false;
    private int lastPointerX, lastPointerY;
    private int pressedX, pressedY;
    private long pressedTime;

    private Object vsyncHelper;
    private int currentTargetFPS = 0;

    private Thread legacyLoopThread = null;
    private final Object loopLock = new Object();
    private boolean isRunning = true;

    public RootContainerView(Context context) {
        super(context);
        getHolder().addCallback(this);
        kbHelper = new KeyboardHelper(this);
        RootContainer.displayKbHints = false;//!hasPointerEvents();
        surfaceHolder = getHolder();

        if (Platform.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                vsyncHelper = ModernAndroidUtils.createVsyncHelper(this);
            } catch (Throwable ignored) { }
        }
    }

    @Override
    public synchronized void repaint() {
        if (rootUIComponent != null && rootUIComponent.isVisible()) {
            int targetFPS = rootUIComponent.getTargetFPS();
            if (targetFPS != currentTargetFPS) {
                updateTargetFPS(targetFPS);
            }

            if (currentTargetFPS <= 0 && !rootUIComponent.repaintOnlyOnFlushGraphics()) {
                paint();
            }
        }
    }

    private void updateTargetFPS(int targetFPS) {
        Logger.log("new target FPS: " + targetFPS);
        if (targetFPS != currentTargetFPS) {
            Logger.log("setting new target FPS...");
            currentTargetFPS = targetFPS;

            // Android 11+
            if (Platform.SDK_INT >= Build.VERSION_CODES.R) {
                ModernAndroidUtils.setFrameRate(surfaceHolder.getSurface(), (float) targetFPS);
            }

            // Older Android versions
            if (vsyncHelper != null) {
                if (currentTargetFPS > 0) {
                    ModernAndroidUtils.startVsync(vsyncHelper);
                } else {
                    ModernAndroidUtils.stopVsync(vsyncHelper);
                }
            }

            synchronized (loopLock) {
                loopLock.notifyAll();
            }
        }
    }

    private void ensureLegacyLoopRunning() {
        if (legacyLoopThread == null) {
            legacyLoopThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning) {
                        if (currentTargetFPS <= 0 || vsyncHelper != null) {
                            synchronized (loopLock) {
                                try {
                                    loopLock.wait();
                                } catch (InterruptedException ignored) { }
                            }
                            continue;
                        }

                        long start = System.currentTimeMillis();
                        int currentTargetFPS = RootContainerView.this.currentTargetFPS;

                        tickAndPaint();

                        if (currentTargetFPS > 0) {
                            long frameTime = 1000 / currentTargetFPS;
                            long sleep = frameTime - (System.currentTimeMillis() - start);
                            if (sleep > 0) {
                                try {
                                    Thread.sleep(sleep);
                                } catch (InterruptedException ignored) { }
                            } else {
                                Thread.yield();
                            }
                        }
                    }
                }
            });
            legacyLoopThread.start();
        }
    }

    public int getTargetFPS() {
        return currentTargetFPS;
    }

    public synchronized void tickAndPaint() {
        if (rootUIComponent != null && rootUIComponent.isVisible()) {
            rootUIComponent.tick();
        }
        if (rootUIComponent == null || rootUIComponent.isVisible()) {
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
            if (rootUIComponent != null && rootUIComponent.isVisible()) {
                rootUIComponent.paint(g);
            } else {
                g.setColor(0xaaaaaa);
                if (rootUIComponent != null && !rootUIComponent.isVisible()) {
                    g.drawString("root component is not visible", w/2, h - g.getFontHeight(), Graphics.BOTTOM | Graphics.HCENTER);
                }
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
        return new Graphics(c != null ? c : new Canvas());
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

    public void handleKeyPressed(int keyCode, int count) {
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

    public void handleKeyReleased(int keyCode, int count) {
        if (rootUIComponent != null && wasDownEvent) {
            rootUIComponent.setVisible(true);
            if (rootUIComponent.keyReleased(keyCode, count)) {
                repaint();
            }
        }
        wasDownEvent = false;
    }

    public void handleKeyRepeated(int keyCode, int pressedCount) {
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
            if (d > DEFAULT_FONT_HEIGHT) {
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
        kbHelper.start();
        if (rootUIComponent != null) {
            onSizeChanged(getWidth(), getHeight(), 0, 0);
            rootUIComponent.onShow();
        }
        repaint();
    }

    protected void onHide() {
        kbHelper.stop();
        if (rootUIComponent != null) {
            rootUIComponent.onHide();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        Logger.log("Window focus changed (in " + getClass().getSimpleName() + ", " + hasWindowFocus + ")");
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            onShow();
        } else {
            onHide();
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
        isRunning = false;
        synchronized (loopLock) {
            loopLock.notifyAll();
        }
        Platform.exit();
    }

    public void setUiSettings(UISettings uiSettings) {
        this.uiSettings = uiSettings;
    }

    public void setRootUIComponent(final IUIComponent rootUIComponent) {
        Logger.log("setting new root component: " + (rootUIComponent != null ? rootUIComponent.getClass().getSimpleName() : rootUIComponent));
        wasDownEvent = false;
        rootUIComponentPostInitDone = false;

        if (this.rootUIComponent != null) {
            this.rootUIComponent.setVisible(false);
            this.rootUIComponent.setParent(null);
            this.rootUIComponent.setFocused(false);
        }

        if (rootUIComponent != null) {
            this.rootUIComponent = rootUIComponent.setParent(this);
            rootUIComponent.init();

            if (getWidth() > 0 && getHeight() > 0) {
                rootUIComponent.setSize(getWidth(), getHeight());
                rootUIComponent.postInit();
                rootUIComponent.setVisible(true);
                rootUIComponent.setFocused(true);
                rootUIComponentPostInitDone = true;
            }

            ensureLegacyLoopRunning();
            updateTargetFPS(rootUIComponent.getTargetFPS());
            repaint();
        } else {
            try {
                throw new Exception("setRootUIComponent(): got null");
            } catch (Exception ex) {
                Logger.log(ex);
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
}
