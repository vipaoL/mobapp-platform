// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

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
public class RootContainer extends GameCanvas implements IContainer, IPopupFeedback {
    private static final int SE_KEY_BACK = -11;

	private static RootContainer inst = null;
    private IUIComponent rootUIComponent = null;
    private KeyboardHelper kbHelper;
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
    private boolean rootUIComponentPostInitDone = false;

    private RootContainer() {
    	super(false);
        setFullScreenMode(true);
        kbHelper = new KeyboardHelper();
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
            inst.rootUIComponent.setParent(null);
            inst.rootUIComponent.setFocused(false);
        }

        if (rootUIComponent != null) {
		    inst.rootUIComponent = rootUIComponent.setParent(inst).setVisible(true);
		    inst.rootUIComponentPostInitDone = false;
		    rootUIComponent.init();
            if (inst.getWidth() > 0 && inst.getHeight() > 0) {
                rootUIComponent.setSize(inst.getWidth(), inst.getHeight());
                rootUIComponent.postInit();
                rootUIComponent.setFocused(true);
                inst.rootUIComponentPostInitDone = true;
            }
        }
        inst.repaint();
        return inst;
    }

    public UISettings getUISettings() {
		return uiSettings;
	}

    public final void repaintt() {
    	if (rootUIComponent == null || !rootUIComponent.repaintOnlyOnFlushGraphics()) {
    		super.repaint();
    	}
    }

    public void paint(Graphics g) {
    	if (bgColor >= 0) {
    		g.fillRect(0, 0, w, h);
    	}

        if (rootUIComponent != null) {
            rootUIComponent.paint(new mobileapplication3.platform.ui.Graphics(g));
        } else {
        	g.setColor(0xaaaaaa);
        	g.drawString("Nothing to draw. " + rootUIComponent, w/2, h, Graphics.BOTTOM | Graphics.HCENTER);
        }
        Logger.paint(new mobileapplication3.platform.ui.Graphics(g));
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
        if (keyCode == SE_KEY_BACK) {
            keyCode = Keys.KEY_SOFT_RIGHT;
        }
        kbHelper.keyPressed(keyCode);
    }

    private void handleKeyPressed(int keyCode, int count) {
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
        if (keyCode == SE_KEY_BACK) {
            keyCode = Keys.KEY_SOFT_RIGHT;
        }
        kbHelper.keyReleased(keyCode);
    }

    private void handleKeyReleased(int keyCode, int count) {
        if (rootUIComponent != null && wasDownEvent) {
            rootUIComponent.setVisible(true);
            if (rootUIComponent.keyReleased(keyCode, count)) {
                repaintt();
            }
        }
        wasDownEvent = false;
    }

    protected void handleKeyRepeated(int keyCode, int pressedCount) {
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
    	if (lastPointerX == x && lastPointerY == y) {
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
	        if (d > 4) {
	        	wasDragged = true;
	        }
        }
    }
    
    protected void pointerReleased(int x, int y) {
    	try {
	        if (rootUIComponent != null && wasDownEvent) {
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
                rootUIComponent.setFocused(true);
                rootUIComponentPostInitDone = true;
            }
            repaintt();
        }
    }

    protected void showNotify() {
        kbHelper.show();
        if (rootUIComponent != null) {
        	rootUIComponent.onShow();
            rootUIComponent.setVisible(true);
            repaintt();
        }
        sizeChanged(getWidth(), getHeight());
    }
    
    protected void hideNotify() {
        kbHelper.hide();
        if (rootUIComponent != null) {
        	rootUIComponent.onHide();
            rootUIComponent.setVisible(false);
            repaintt();
        }
    }

    public void closePopup() {
		Platform.exit();
	}

    public boolean isOnScreen() {
		return true;
	}

    private class KeyboardHelper {
        private int lastKey, pressCount;
        private Thread repeatThread;
        private long lastEvent;
        private Runnable repeater = new Runnable() {
            public void run() {
                // The thread is interrupted when the key is released
                try {
                    // Wait a delay and repeat
                    Thread.sleep(500);
                    while (Thread.currentThread() == repeatThread && wasDownEvent) {
                        handleKeyRepeated(lastKey, pressCount);
                        Thread.sleep(150);
                    }
                } catch (InterruptedException ex) { }
            }
		};

        public void show() {
            pressCount = 1;
            lastKey = 0;
        }
        
        public void hide() {
            if(repeatThread != null) {
            	Thread thread = repeatThread;
            	repeatThread = null;
                thread.interrupt();
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
            handleKeyPressed(k, pressCount);
            repeatThread = new Thread(repeater);
            repeatThread.start();
        }

        public void keyReleased(int k) {
            updateLastEventTime();
            if(k != lastKey) {
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
