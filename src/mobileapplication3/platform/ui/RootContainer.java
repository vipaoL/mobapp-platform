/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.platform.ui;

import static android.view.KeyEvent.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import mobileapplication3.platform.Platform;
import mobileapplication3.ui.IContainer;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.Keys;
import mobileapplication3.ui.UISettings;

/**
 *
 * @author vipaol
 */
public class RootContainer extends View implements IContainer {
    
    private IUIComponent rootUIComponent = null;
    private KeyboardHelper kbHelper;
    public static boolean displayKbHints = false;
    private int bgColor = 0x000000;
    public int w, h;
    private static RootContainer inst = null;
    private UISettings uiSettings;
    private Bitmap newBuffer;
    private Bitmap currentBuffer = null;
    private Paint bufferPaint;

    public RootContainer(Context context, IUIComponent rootUIComponent, UISettings uiSettings) {
        super(context);
        this.uiSettings = uiSettings;
        inst = this;
        kbHelper = new KeyboardHelper();
        displayKbHints = false;//!hasPointerEvents();
        bufferPaint = new Paint();
        //setFocusable(true);
        setRootUIComponent(rootUIComponent);
    }

    public void init() {
    	if (rootUIComponent != null) {
    		rootUIComponent.init();
    	}
	}

    public static RootContainer setRootUIComponent(IUIComponent rootUIComponent) {
        if (inst.rootUIComponent != null) {
            inst.rootUIComponent.setParent(null);
            inst.rootUIComponent.setFocused(false);
        }
        
        if (rootUIComponent != null) {
            inst.rootUIComponent = rootUIComponent.setParent(inst).setFocused(true);
            rootUIComponent.setSize(inst.getWidth(), inst.getHeight());
		    rootUIComponent.init();
		    rootUIComponent.setFocused(true);
        }
        return inst;
    }

    @Override
    public void repaint() {
        Platform.getActivityInst().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }

    public UISettings getUISettings() {
		return uiSettings;
	}

    protected void onDraw(Canvas c) {
        if (currentBuffer != null) {
            c.drawBitmap(currentBuffer, 0, 0, bufferPaint);
            currentBuffer = null;
            return;
        }

    	if (bgColor >= 0) {
    		c.drawColor(0xff000000);
    	}

        if (rootUIComponent != null) {
            rootUIComponent.paint(new mobileapplication3.platform.ui.Graphics(c));
        }
    }

    @Override
    public Graphics getUGraphics() {
        currentBuffer = newBuffer;
        return new mobileapplication3.platform.ui.Graphics(new Canvas(newBuffer = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565)));
    }

    @Override
    public void flushGraphics() {
        repaint();
    }
    
    public int getBgColor() {
		return bgColor;
	}
    
    public void setBgColor(int bgColor) {
		this.bgColor = bgColor;
	}
    
    public static int getGameActionn(int keyCode) {
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
        Log.d("keyPressed", String.valueOf(keyCode));
        kbHelper.keyPressed(keyCode);
    }
    
    private void handleKeyPressed(int keyCode, int count) {
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            if (rootUIComponent.keyPressed(keyCode, count)) {
                repaint();
            }
        }
    }

    public void keyReleased(int keyCode) {
        keyCode = convertKeyCode(keyCode);
        Log.d("keyReleased", String.valueOf(keyCode));
        kbHelper.keyReleased(keyCode);
    }

    private void handleKeyReleased(int keyCode, int count) {
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            if (rootUIComponent.keyReleased(keyCode, count)) {
                repaint();
            }
        }
    }
    
    protected void handleKeyRepeated(int keyCode, int pressedCount) {
        if (getGameActionn(keyCode) == Keys.FIRE) {
            return;
        }
        if (rootUIComponent != null) {
            if (rootUIComponent.keyRepeated(keyCode, pressedCount)) {
                repaint();
            }
        }
    }

    protected void pointerPressed(int x, int y) {
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            if (rootUIComponent.pointerPressed(x, y)) {
                repaint();
            }
        }
    }
    
    protected void pointerDragged(int x, int y) {
        if (rootUIComponent != null) {
            if (rootUIComponent.pointerDragged(x, y)) {
                repaint();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pointerPressed(Math.round(e.getX()), Math.round(e.getY()));
                break;
            case MotionEvent.ACTION_MOVE:
                pointerDragged(Math.round(e.getX()), Math.round(e.getY()));
                break;
            case MotionEvent.ACTION_UP:
                pointerReleased(Math.round(e.getX()), Math.round(e.getY()));
                break;
            default:
                return false;
        }
        return true;
    }

    protected void pointerReleased(int x, int y) {
        if (rootUIComponent != null) {
            if (rootUIComponent.pointerReleased(x, y)) {
                repaint();
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    	this.w = w;
    	this.h = h;
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
            repaint();
        }
        onSizeChanged(getWidth(), getHeight(), 0, 0);
        if (rootUIComponent != null) {
            rootUIComponent.onShow();
        }
    }
    
    protected void onHide() {
        kbHelper.hide();
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(false);
            rootUIComponent.onHide();
            repaint();
        }
    }

    protected void showNotify() {
        onShow();
    }

    protected void hideNotify() {
        onHide();
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
            case KEYCODE_SOFT_LEFT:
                return Keys.KEY_SOFT_LEFT;
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
                    } catch (InterruptedException e) { }
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
