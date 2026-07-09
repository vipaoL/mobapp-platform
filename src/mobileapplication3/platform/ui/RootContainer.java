// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import mobileapplication3.platform.KeyboardHelper;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.ui.*;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

/**
 *
 * @author vipaol
 */
public class RootContainer extends GameCanvas implements IContainer, IPopupFeedback, KeyboardHelper.IKeyboardListener {
    private static final int SE_KEY_BACK = -11;
    private static final int DEFAULT_FONT_HEIGHT = Font.getDefaultFontHeight();

    private static RootContainer inst = null;
    private IUIComponent rootUIComponent = null;
    private final KeyboardHelper kbHelper;
    private mobileapplication3.platform.ui.Graphics lastGraphics = null;
    public static boolean displayKbHints = false;
    public static boolean enableOnScreenLog = false;
    private int bgColor = 0x000000;
    public int w, h;
    protected UISettings uiSettings;
    private boolean wasDownEvent = false, wasDragged = false;
    private int lastPointerX, lastPointerY;
    private int pressedX, pressedY;
    private long pressedTime;
    private long lastDraggedEventTime;
    private boolean rootUIComponentPostInitDone = false;

    private int currentTargetFPS = 0;
    private Thread repaintLoopThread = null;
    private final Object loopLock = new Object();
    private boolean isRunning = true;

    private RootContainer() {
        super(false);
        setFullScreenMode(true);
        kbHelper = new KeyboardHelper(this);
        displayKbHints = !hasPointerEvents();
    }

    public static RootContainer getInst() {
        if (inst == null) {
            inst = new RootContainer();
        }
        return inst;
    }

    public static void init() {
        RootContainer.inst = getInst();

        enableOnScreenLog = inst.uiSettings == null || inst.uiSettings.enableOnScreenLog();
        if (enableOnScreenLog) {
            if (inst.h > 0) {
                Logger.enableOnScreenLog(inst.h);
            }
        } else {
            Logger.disableOnScreenLog();
        }

        if (inst.rootUIComponent != null) {
            inst.rootUIComponent.init();
        }
    }

    public static RootContainer setUISettings(UISettings uiSettings) {
        getInst().uiSettings = uiSettings;
        if (uiSettings != null) {
            uiSettings.onChange();
        }
        return inst;
    }

    public static RootContainer setRootUIComponent(IUIComponent rootUIComponent) {
        getInst();
        inst.wasDownEvent = false;
        if (inst.rootUIComponent != null) {
            inst.rootUIComponent.setVisible(false);
            inst.rootUIComponent.setParent(null);
            inst.rootUIComponent.setFocused(false);
        }

        if (rootUIComponent != null) {
            rootUIComponent.setParent(inst);
            inst.rootUIComponentPostInitDone = false;
            rootUIComponent.init();
            if (inst.getWidth() > 0 && inst.getHeight() > 0) {
                rootUIComponent.setSize(inst.getWidth(), inst.getHeight());
                rootUIComponent.postInit();
                rootUIComponent.setVisible(true);
                rootUIComponent.setFocused(true);
                inst.rootUIComponentPostInitDone = true;
            }

            inst.rootUIComponent = rootUIComponent;

            inst.ensureRepaintLoopRunning();
            inst.updateTargetFPS(rootUIComponent.getTargetFPS());
        }
        inst.repaintt();
        return inst;
    }

    private void updateTargetFPS(int targetFPS) {
        Logger.log("new target FPS: " + targetFPS);
        if (targetFPS != currentTargetFPS) {
            Logger.log("setting new target FPS...");
            currentTargetFPS = targetFPS;

            synchronized (loopLock) {
                loopLock.notifyAll();
            }
        }
    }

    private void ensureRepaintLoopRunning() {
        if (repaintLoopThread == null) {
            repaintLoopThread = new Thread(new Runnable() {
                public void run() {
                    while (isRunning) {
                        if (currentTargetFPS <= 0) {
                            synchronized (loopLock) {
                                try {
                                    loopLock.wait();
                                } catch (InterruptedException ignored) { }
                            }
                            continue;
                        }

                        long start = System.currentTimeMillis();
                        int currentTargetFPS = RootContainer.this.currentTargetFPS;

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
            repaintLoopThread.start();
        }
    }

    public synchronized void tickAndPaint() {
        if (rootUIComponent != null && rootUIComponent.isVisible()) {
            rootUIComponent.tick();
        }
        if (rootUIComponent == null || rootUIComponent.isVisible()) {
            paint();
        }
    }

    public UISettings getUISettings() {
        return uiSettings;
    }

    public final void repaintt() {
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

    private synchronized void paint() {
        paint(getUGraphics());
        flushGraphics();
    }

    public synchronized void paint(mobileapplication3.platform.ui.Graphics g) {
        if (bgColor >= 0) {
            g.fillRect(0, 0, w, h);
        }

        if (rootUIComponent != null) {
            rootUIComponent.paint(g);
        } else {
            g.setColor(0xaaaaaa);
            g.drawString("Nothing to draw. " + rootUIComponent, w/2, h, Graphics.BOTTOM | Graphics.HCENTER);
        }

        // workaround for Sony Ericsson phones to fix setClip() bugs. I don't know why it works (sleep(1) doesn't)
        g.drawString(" ", 0, 0, Graphics.TOP | Graphics.LEFT);
    }

    public synchronized void paint(Graphics g) {
        mobileapplication3.platform.ui.Graphics graphics = new mobileapplication3.platform.ui.Graphics(g);
        paint(graphics);
        Logger.paint(graphics);
    }

    public mobileapplication3.platform.ui.Graphics getUGraphics() {
        return lastGraphics = new mobileapplication3.platform.ui.Graphics(getGraphics());
    }

    public void flushGraphics() {
        Logger.paint(lastGraphics);
        super.flushGraphics();
    }

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public static int getAction(int keyCode) {
        return inst.getGameAction(keyCode);
    }

    protected void keyPressed(int keyCode) {
        switch (keyCode) {
            case 'q':
                keyCode = Keys.KEY_SOFT_LEFT;
                break;
            case SE_KEY_BACK:
            case 'p':
                keyCode = Keys.KEY_SOFT_RIGHT;
                break;
        }
        kbHelper.keyPressed(keyCode);
    }

    public void handleKeyPressed(int keyCode, int count) {
        wasDownEvent = true;
        try {
            if (rootUIComponent != null) {
                rootUIComponent.setVisible(true);
                if (rootUIComponent.keyPressed(keyCode, count)) {
                    if (!displayKbHints) {
                        displayKbHints = true;
                        if (uiSettings != null) {
                            uiSettings.onChange();
                        }
                    }
                    repaintt();
                }
            }
        } catch (Exception ex) {
            Logger.log(ex);
        }
    }

    protected void keyReleased(int keyCode) {
        switch (keyCode) {
            case 'q':
                keyCode = Keys.KEY_SOFT_LEFT;
                break;
            case SE_KEY_BACK:
            case 'p':
                keyCode = Keys.KEY_SOFT_RIGHT;
                break;
        }
        kbHelper.keyReleased(keyCode);
    }

    public void handleKeyReleased(int keyCode, int count) {
        if (rootUIComponent != null && wasDownEvent) {
            rootUIComponent.setVisible(true);
            if (rootUIComponent.keyReleased(keyCode, count)) {
                repaintt();
            }
        }
        wasDownEvent = false;
    }

    public void handleKeyRepeated(int keyCode, int pressedCount) {
        if (getGameAction(keyCode) == Canvas.FIRE) {
            return;
        }
        if (rootUIComponent != null && wasDownEvent) {
            if (rootUIComponent.keyRepeated(keyCode, pressedCount)) {
                repaintt();
            }
        }
    }

    protected void pointerPressed(int x, int y) {
        lastPointerX = pressedX = x;
        lastPointerY = pressedY = y;
        pressedTime = System.currentTimeMillis();
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            if (rootUIComponent.pointerPressed(x, y)) {
                repaintt();
            }
        }
        wasDownEvent = true;
    }

    protected void pointerDragged(int x, int y) {
        if (lastPointerX == x && lastPointerY == y || (System.currentTimeMillis() - lastDraggedEventTime <= 1)) {
            return;
        }

        lastPointerX = x;
        lastPointerY = y;
        if (rootUIComponent != null && wasDownEvent) {
            if (rootUIComponent.pointerDragged(x, y)) {
                repaintt();
            }
        }

        if (!wasDragged) {
            int d = Math.abs(x - pressedX) + Math.abs(y - pressedY);
            if (d > DEFAULT_FONT_HEIGHT) {
                wasDragged = true;
            }
        }

        lastDraggedEventTime = System.currentTimeMillis();
    }

    protected void pointerReleased(int x, int y) {
        try {
            if (rootUIComponent != null && wasDownEvent) {
                if (lastPointerX != x || lastPointerY != y) {
                    pointerDragged(x, y);
                }
                if (rootUIComponent.pointerReleased(x, y)) {
                    repaintt();
                }

                if (!wasDragged && System.currentTimeMillis() - pressedTime < 1000) {
                    if (rootUIComponent.pointerClicked(x, y)) {
                        repaintt();
                    }
                }
            }
        } catch (Exception ex) {
            Logger.log(ex);
        }
        wasDownEvent = false;
        wasDragged = false;
    }

    protected void sizeChanged(int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }

        this.w = w;
        this.h = h;

        if (enableOnScreenLog) {
            Logger.enableOnScreenLog(h);
        }

        if (rootUIComponent != null) {
            rootUIComponent.setSize(w, h);
            if (!rootUIComponentPostInitDone) {
                rootUIComponent.postInit();
                rootUIComponent.setVisible(true);
                rootUIComponent.setFocused(true);
                rootUIComponentPostInitDone = true;
            }
            repaintt();
        }
    }

    protected void showNotify() {
        kbHelper.start();
        if (rootUIComponent != null) {
            rootUIComponent.onShow();
            rootUIComponent.setVisible(true);
            repaintt();
        }
        sizeChanged(getWidth(), getHeight());
    }

    protected void hideNotify() {
        kbHelper.stop();
        if (rootUIComponent != null) {
            rootUIComponent.onHide();
            rootUIComponent.setVisible(false);
            repaintt();
        }
    }

    public void closePopup() {
        isRunning = false;
        synchronized (loopLock) {
            loopLock.notifyAll();
        }
        Platform.exit();
    }

    public boolean isOnScreen() {
        return true;
    }
}
