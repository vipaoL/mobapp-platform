// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import mobileapplication3.platform.KeyboardHelper;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.HashSet;

import static mobileapplication3.ui.IUIComponent.*;

/**
 *
 * @author vipaol
 */
public class RootContainer extends Canvas implements IContainer, IPopupFeedback, KeyListener, KeyboardHelper.IKeyboardListener {
    public static final int CURSOR_HIDE_DELAY = 5000;
    private static final int DEFAULT_FONT_HEIGHT = Font.getDefaultFontHeight();

    private final Toolkit toolkit = Toolkit.getDefaultToolkit();
    private java.awt.Graphics g = null;
    private BufferStrategy bufferStrategy = null;
    private IUIComponent rootUIComponent = null;
    private final KeyboardHelper kbHelper;
    public static boolean displayKbHints = false;
    public static boolean enableOnScreenLog = false;
    public int w, h;
    private static RootContainer inst = null;
    private UISettings uiSettings;

    private boolean wasDownEvent = false, wasDragged = false;
    private int lastPointerX, lastPointerY;
    private int pressedX, pressedY;
    private long pressedTime;
    private long lastMouseEvent;
    private Thread mouseHider = null;
    private final HashSet<Integer> pressedKeys = new HashSet<>();

    private int currentTargetFPS = 0;
    private Thread repaintLoopThread = null;
    private final Object loopLock = new Object();
    private boolean isRunning = true;

    public RootContainer() {
        inst = this;
        kbHelper = new KeyboardHelper(this);
        displayKbHints = false;//!hasPointerEvents();
        setFocusable(true);
        setIgnoreRepaint(true);
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                pressedX = e.getX();
                pressedY = e.getY();
                pressedTime = System.currentTimeMillis();
                int event;
                if (e.getButton() == MouseEvent.BUTTON1) {
                    event = MOUSE_PRIMARY_PRESSED;
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    event = MOUSE_SECONDARY_PRESSED;
                } else {
                    event = MOUSE_WHEEL_PRESSED;
                }
                if (!mouseEvent(event, pressedX, pressedY)) {
                    pointerPressed(pressedX, pressedY);
                }
                wasDownEvent = true;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int event;
                if (e.getButton() == MouseEvent.BUTTON1) {
                    event = MOUSE_PRIMARY_DRAGGED;
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    event = MOUSE_SECONDARY_DRAGGED;
                } else {
                    event = MOUSE_WHEEL_DRAGGED;
                }
                if (!mouseEvent(event, e.getX(), e.getY())) {
                    pointerDragged(e.getX(), e.getY());
                }
                hideCursorAfterDelay();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int releasedX = e.getX();
                int releasedY = e.getY();
                int event;
                if (e.getButton() == MouseEvent.BUTTON1) {
                    event = MOUSE_PRIMARY_RELEASED;
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    event = MOUSE_SECONDARY_RELEASED;
                } else {
                    event = MOUSE_WHEEL_RELEASED;
                }
                if (!mouseEvent(event, releasedX, releasedY)) {
                    if (!wasDragged && System.currentTimeMillis() - pressedTime < 1000) {
                        pointerClicked(releasedX, releasedY);
                    }
                    pointerReleased(releasedX, releasedY);
                }
                wasDownEvent = false;
                wasDragged = false;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                hideCursorAfterDelay();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                mouseEvent(e.getWheelRotation() > 0 ? MOUSE_WHEEL_SCROLLED_DOWN : MOUSE_WHEEL_SCROLLED_UP, e.getX(), e.getY());
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
        addKeyListener(this);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                onSizeChanged(getWidth(), getHeight(), w, h);
            }
        });
        hideCursorAfterDelay();
    }

    private void hideCursorAfterDelay() {
        lastMouseEvent = System.currentTimeMillis();
        setCursor(Cursor.getDefaultCursor());

        if (mouseHider == null) {
            mouseHider = new Thread(new Runnable() {
                @Override
                public void run() {
                    int t;
                    try {
                        while ((t = (int) (System.currentTimeMillis() - lastMouseEvent)) < CURSOR_HIDE_DELAY) {
                            Thread.yield();
                            Thread.sleep(CURSOR_HIDE_DELAY - t);
                        }
                        if (Thread.currentThread() == mouseHider) {
                            setBlankCursor();
                        }
                    } catch (InterruptedException e) { }
                    mouseHider = null;
                }
            });
            mouseHider.start();
        }
    }

    private void setBlankCursor() {
        BufferedImage transparentImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = toolkit.createCustomCursor(transparentImage, new Point(0, 0), "");
        setCursor(blankCursor);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        onShow();
        createBufferStrategy(3);
    }

    public static RootContainer getInst() {
        if (inst == null) {
            inst = new RootContainer();
        }
        return inst;
    }

    public static void init() {
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
        inst.wasDownEvent = false;
        if (inst.rootUIComponent != null) {
            inst.rootUIComponent.setVisible(false);
            inst.rootUIComponent.setParent(null);
            inst.rootUIComponent.setFocused(false);
        }

        if (rootUIComponent != null) {
            rootUIComponent.setParent(inst);
            rootUIComponent.init();
            while (true) {
                if (inst.getWidth() > 0 && inst.getHeight() > 0) {
                    Logger.log("RootContainer.inst: " + inst.getWidth() + "x" + inst.getHeight());
                    rootUIComponent.setSize(inst.getWidth(), inst.getHeight());
                    rootUIComponent.postInit();
                    rootUIComponent.setVisible(false);
                    rootUIComponent.setFocused(true);
                    break;
                } else {
                    Logger.log("Error: Window size is 0");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) { }
                }
            }
            inst.rootUIComponent = rootUIComponent.setVisible(true);

            inst.ensureRepaintLoopRunning();
            inst.updateTargetFPS(rootUIComponent.getTargetFPS());
            inst.repaint();

        } else {
            try {
                throw new Exception("setRootUIComponent(): got null");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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
                @Override
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
            }, "repaint loop");
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

    @Override
    public UISettings getUISettings() {
        return uiSettings;
    }

    @Override
    public boolean isOnScreen() {
        return true;
    }

    public static RootContainer setUiSettings(UISettings uiSettings) {
        getInst().uiSettings = uiSettings;
        return getInst();
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

    protected synchronized void paint() {
        Graphics g = getUGraphics();
        if (g == null) {
            Logger.log("got null Graphics, skipping paint");
            return;
        }
        if (rootUIComponent != null) {
            rootUIComponent.paint(g);
        } else {
            g.setColor(0xaaaaaa);
            g.drawString("Nothing to draw. " + rootUIComponent, w/2, h, Graphics.BOTTOM | Graphics.HCENTER);
        }
        Logger.paint(g);
        flushGraphics();
    }

    @Override
    public void paint(java.awt.Graphics g) { }

    @Override
    public synchronized Graphics getUGraphics() {
        bufferStrategy = getBufferStrategy();
        g = bufferStrategy.getDrawGraphics();
        java.awt.Graphics g = this.g;
        if (g != null) {
            g.clearRect(0, 0,getWidth(), getHeight());
        } else {
            g = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY).getGraphics();
        }
        return new Graphics(g);
    }

    @Override
    public synchronized void flushGraphics() {
        if (g != null) {
            Logger.paint(new Graphics(g));
        }
        if (bufferStrategy != null) {
            bufferStrategy.show();
        }
        if (g != null) {
            g.dispose();
        }
        toolkit.sync();
    }

    public int getBgColor() {
        return getBackground().getRGB();
    }

    public void setBgColor(int bgColor) {
        setBackground(new Color(bgColor));
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

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (pressedKeys.add(keyCode)) {
            keyCode = convertKeyCode(keyCode);
            kbHelper.keyPressed(keyCode);
            wasDownEvent = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (pressedKeys.contains(keyCode)) {
            pressedKeys.remove(keyCode);
        }
        keyCode = convertKeyCode(keyCode);
        kbHelper.keyReleased(keyCode);
    }

    public void handleKeyPressed(int keyCode, int count) {
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            if (rootUIComponent.keyPressed(keyCode, count)) {
                if (!displayKbHints) {
                    displayKbHints = true;
                    if (uiSettings != null) {
                        uiSettings.onChange();
                    }
                }
                repaint();
            }
        }
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
        if (getAction(keyCode) == Keys.FIRE) {
            return;
        }
        if (rootUIComponent != null && wasDownEvent) {
            if (rootUIComponent.keyRepeated(keyCode, pressedCount)) {
                repaint();
            }
        }
    }

    protected boolean mouseEvent(int event, int x, int y) {
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            if (rootUIComponent.mouseEvent(event, x, y)) {
                repaint();
                return true;
            }
        }
        return false;
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

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
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
            repaint();
        }
    }

    protected void onShow() {
        kbHelper.start();
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            onSizeChanged(getWidth(), getHeight(), 0, 0);
            rootUIComponent.onShow();
        }
        repaint();
    }

    protected void onHide() {
        kbHelper.stop();
        if (rootUIComponent != null) {
            rootUIComponent.onHide();
            rootUIComponent.setVisible(false);
        }
    }

    public void onWindowFocusGained() {
        Logger.log("Window focus gained");
        onShow();
    }

    public void onWindowFocusLost() {
        Logger.log("Window focus lost");
        onHide();
    }

    private int convertKeyCode(int javaSEKeyCode) { // TODO
        switch (javaSEKeyCode) {
            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                return Keys.FIRE;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_KP_UP:
                return Keys.UP;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_KP_DOWN:
                return Keys.DOWN;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_KP_LEFT:
                return Keys.LEFT;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_KP_RIGHT:
                return Keys.RIGHT;
            case KeyEvent.VK_F1:
            case KeyEvent.VK_MINUS:
                return Keys.KEY_SOFT_LEFT;
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_F2:
            case KeyEvent.VK_PLUS:
            case KeyEvent.VK_EQUALS:
                return Keys.KEY_SOFT_RIGHT;
            case KeyEvent.VK_0:
            case KeyEvent.VK_NUMPAD0:
                return Keys.KEY_NUM0;
            case KeyEvent.VK_1:
            case KeyEvent.VK_NUMPAD1:
                return Keys.KEY_NUM1;
            case KeyEvent.VK_2:
            case KeyEvent.VK_NUMPAD2:
                return Keys.KEY_NUM2;
            case KeyEvent.VK_3:
            case KeyEvent.VK_NUMPAD3:
                return Keys.KEY_NUM3;
            case KeyEvent.VK_4:
            case KeyEvent.VK_NUMPAD4:
                return Keys.KEY_NUM4;
            case KeyEvent.VK_5:
            case KeyEvent.VK_NUMPAD5:
                return Keys.KEY_NUM5;
            case KeyEvent.VK_6:
            case KeyEvent.VK_NUMPAD6:
                return Keys.KEY_NUM6;
            case KeyEvent.VK_7:
            case KeyEvent.VK_NUMPAD7:
            case KeyEvent.VK_R: // HACK, FIXME, TODO
                return Keys.KEY_NUM7;
            case KeyEvent.VK_8:
            case KeyEvent.VK_NUMPAD8:
                return Keys.KEY_NUM8;
            case KeyEvent.VK_9:
            case KeyEvent.VK_NUMPAD9:
                return Keys.KEY_NUM9;
            case KeyEvent.VK_F3:
            case KeyEvent.VK_MULTIPLY:
            case KeyEvent.VK_HOME:
                return Keys.KEY_STAR;
            case KeyEvent.VK_F4:
            case KeyEvent.VK_DIVIDE:
            case KeyEvent.VK_END:
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
}
