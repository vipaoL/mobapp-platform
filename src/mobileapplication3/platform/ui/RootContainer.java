/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.platform.ui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.ui.IContainer;
import mobileapplication3.ui.IPopupFeedback;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.UISettings;

/**
 *
 * @author vipaol
 */
public class RootContainer extends GameCanvas implements IContainer, IPopupFeedback {

	private static RootContainer inst = null;
    private IUIComponent rootUIComponent = null;
    private KeyboardHelper kbHelper;
    private mobileapplication3.platform.ui.Graphics lastGraphics = null;
    public static boolean displayKbHints = false;
    public static boolean enableOnScreenLog = false;
    private int bgColor = 0x000000;
    public int w, h;
    protected UISettings uiSettings;
    private boolean wasDownEvent = false;
    private int lastPointerX, lastPointerY;
    private int pressedX, pressedY;

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
		    inst.rootUIComponent = rootUIComponent.setParent(inst).setFocused(true);
		    rootUIComponent.init();
		    rootUIComponent.setSize(inst.getWidth(), inst.getHeight());
		    rootUIComponent.setFocused(true);
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
    }
    
    protected void pointerReleased(int x, int y) {
    	try {
	        if (rootUIComponent != null && wasDownEvent) {
	            if (rootUIComponent.pointerReleased(x, y)) {
	                repaintt();
	            }

	            int d = Math.abs(x - pressedX) + Math.abs(y - pressedY);
                if (d <= 20) {
                	if (rootUIComponent.pointerClicked(x, y)) {
                		repaintt();
                	}
                }
	        }
    	} catch (Exception ex) {
    		Logger.log(ex);
    	}
        wasDownEvent = false;
    }
    
    protected void sizeChanged(int w, int h) {
    	this.w = w;
    	this.h = h;

    	if (enableOnScreenLog) {
    		Logger.enableOnScreenLog(h);
    	}

        if (rootUIComponent != null) {
            rootUIComponent.setSize(w, h);
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
