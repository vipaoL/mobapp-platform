/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.platform.ui;

import static android.view.KeyEvent.*;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.ui.IContainer;
import mobileapplication3.ui.IPopupFeedback;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.Keys;
import mobileapplication3.ui.UISettings;

/**
 *
 * @author vipaol
 */
public class RootContainer extends SurfaceView implements IContainer, IPopupFeedback, SurfaceHolder.Callback {

    private IUIComponent rootUIComponent = null;
    private KeyboardHelper kbHelper;
    public static boolean displayKbHints = false;
    public static boolean enableOnScreenLog = false;
    private int bgColor = 0x000000;
    public int w, h;
    private static RootContainer inst = null;
    private UISettings uiSettings;
    private SurfaceHolder surfaceHolder;
    private Canvas c;
    private static Thread repaintThread = null;
    private boolean wasDownEvent = false, wasDragged = false;
    private boolean surfaceCreated = false;
    private boolean isLocked = false;
    private int lastPointerX, lastPointerY;
    private int pressedX, pressedY;
    private long pressedTime;

    public RootContainer(Context context) {
        super(context);
        getHolder().addCallback(this);
        inst = this;
        kbHelper = new KeyboardHelper();
        displayKbHints = false;//!hasPointerEvents();
        surfaceHolder = getHolder();
    }

    public static RootContainer getInst() {
        if (inst == null) {
            throw new IllegalStateException("inst is null. Call constructor first");
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
            //inst.rootUIComponent.setParent(null);
            inst.rootUIComponent.setFocused(false);
        }
        
        if (rootUIComponent != null) {
            inst.rootUIComponent = rootUIComponent.setParent(inst).setVisible(true);
            rootUIComponent.init();
            rootUIComponent.setSize(inst.getWidth(), inst.getHeight());
		    rootUIComponent.setFocused(true);
            if (!rootUIComponent.repaintOnlyOnFlushGraphics() && repaintThread == null) {
                repaintThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!inst.rootUIComponent.repaintOnlyOnFlushGraphics()) {
                            try {
                                Thread.yield();
                                Thread.sleep(200);
                            } catch (InterruptedException ignored) { }
                            inst.repaint();
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
        return inst;
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

    public static RootContainer setUiSettings(UISettings uiSettings) {
        getInst().uiSettings = uiSettings;
        return getInst();
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

        if (bgColor >= 0) {
            c.drawColor(0xff000000);
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
            surfaceHolder.unlockCanvasAndPost(c);
        } catch (Exception ignored) { }
    }
    
    public int getBgColor() {
		return bgColor;
	}
    
    public void setBgColor(int bgColor) {
		this.bgColor = bgColor;
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
    
    public void keyPressed(int keyCode) {
        keyCode = convertKeyCode(keyCode);
        kbHelper.keyPressed(keyCode);
        wasDownEvent = true;
    }
    
    private void handleKeyPressed(int keyCode, int count) {
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
        if (getAction(keyCode) == Keys.FIRE) {
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

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        Log.d("Visibility changed", changedView.getClass().getSimpleName() + " " + visibility);
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            onShow();
        }
        if (visibility == INVISIBLE) {
            onHide();
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
            case KEYCODE_MENU:
            case KEYCODE_SOFT_LEFT:
                return Keys.KEY_SOFT_LEFT;
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
                        while(true) {
                            if(!pressState) {
                                synchronized(tillPressed) {
                                    tillPressed.wait();
                                }
                            }
                            
                            int k = lastKey;
                            Thread.sleep(200);
                            while (!isLastEventOld()) {
                                Thread.sleep(200);
                            }
                            
                            while(pressState && lastKey == k) {
                                handleKeyRepeated(k, pressCount);
                                Thread.sleep(100);
                            }
                            
                            pressCount = 1;
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
