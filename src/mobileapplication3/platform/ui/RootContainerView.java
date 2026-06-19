// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.ui.IContainer;
import mobileapplication3.ui.IPopupFeedback;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.UISettings;
import org.robovm.apple.coreanimation.CADisplayLink;
import org.robovm.apple.coregraphics.CGContext;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.*;
import org.robovm.apple.uikit.*;
import org.robovm.objc.Selector;
import org.robovm.objc.annotation.Method;

public class RootContainerView extends UIView implements IContainer, IPopupFeedback {
    private IUIComponent rootUIComponent = null;
    private UISettings uiSettings;
    public int w, h;
    private final double scale;

    private Graphics g;

    private int pressedX, pressedY;
    private long pressedTime;
    private boolean wasDragged = false;
    private boolean wasDownEvent = false;
    private static final int DRAG_THRESHOLD = 20;

    private static final String TICK_AND_REPAINT_SELECTOR = "tickAndRepaint:";

    private int currentTargetFPS = 0;
    private CADisplayLink displayLink = null;
    private boolean isRunning = true;

    public RootContainerView() {
        super(UIScreen.getMainScreen().getBounds());

        scale = UIScreen.getMainScreen().getScale();

        CGRect screenBounds = UIScreen.getMainScreen().getBounds();
        w = (int) (screenBounds.getWidth() * scale);
        h = (int) (screenBounds.getHeight() * scale);

        setContentScaleFactor(scale);

        Logger.log("RootContainerView initialized: " + w + "x" + h + " (Scale: " + scale + "x)");
        setMultipleTouchEnabled(true);
        setBackgroundColor(UIColor.black());

        NSNotificationCenter.getDefaultCenter().addObserver(
            UIApplication.DidBecomeActiveNotification(),
            null,
            null,
            (notification) -> onShow()
        );

        NSNotificationCenter.getDefaultCenter().addObserver(
            UIApplication.WillResignActiveNotification(),
            null,
            null,
            (notification) -> onHide()
        );
    }

    public void initContainer() {
        if (rootUIComponent != null) {
            rootUIComponent.init();

            if (w > 0 && h > 0) {
                rootUIComponent.setSize(w, h);
                rootUIComponent.postInit();
                rootUIComponent.setVisible(true);
                rootUIComponent.setFocused(true);
            }

            ensureLoopRunning();
            updateTargetFPS(rootUIComponent.getTargetFPS());
            repaint();
        }
    }

    public void setRootUIComponent(IUIComponent rootUIComponent) {
        wasDownEvent = false;

        if (this.rootUIComponent != null) {
            this.rootUIComponent.setFocused(false);
            this.rootUIComponent.setVisible(false);
            this.rootUIComponent.setParent(null);
        }

        this.rootUIComponent = rootUIComponent;
        if (this.rootUIComponent != null) {
            this.rootUIComponent.setParent(this);
            initContainer();
        }
    }

    private void updateTargetFPS(int targetFPS) {
        if (targetFPS != currentTargetFPS) {
            currentTargetFPS = targetFPS;
            if (displayLink != null) {
                if (currentTargetFPS <= 0) {
                    displayLink.setPaused(true);
                } else {
                    displayLink.setPaused(false);
                    try {
                        displayLink.setPreferredFramesPerSecond(currentTargetFPS);
                    } catch (Throwable t) {
                        long interval = 60 / (currentTargetFPS > 0 ? currentTargetFPS : 60);
                        if (interval < 1) {
                            interval = 1;
                        }
                        displayLink.setFrameInterval(interval);
                    }
                }
            }
        }
    }

    private void ensureLoopRunning() {
        if (displayLink == null && isRunning) {
            displayLink = new CADisplayLink(this, Selector.register(TICK_AND_REPAINT_SELECTOR));
            displayLink.addToRunLoop(NSRunLoop.getMain(), NSRunLoopMode.Default);
        }
    }

    @Method(selector = TICK_AND_REPAINT_SELECTOR)
    private void tickAndRepaint(CADisplayLink sender) {
        if (!isRunning) {
            return;
        }

        if (rootUIComponent != null && rootUIComponent.isVisible()) {
            if (currentTargetFPS > 0) {
                rootUIComponent.tick();
            }
            setNeedsDisplay();
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
                NSOperationQueue.getMainQueue().addOperation(this::setNeedsDisplay);
            }
        }
    }

    public void setUiSettings(UISettings uiSettings) {
        this.uiSettings = uiSettings;
    }

    @Override
    public UISettings getUISettings() {
        return uiSettings;
    }

    @Override
    public boolean isOnScreen() {
        return true;
    }

    @Override
    public void closePopup() {
        isRunning = false;
        if (displayLink != null) {
            displayLink.invalidate();
            displayLink = null;
        }
        NSNotificationCenter.getDefaultCenter().removeObserver(this);
        Platform.exit();
    }

    public void onShow() {
        if (displayLink != null) {
            displayLink.setPaused(false);
        }

        if (rootUIComponent != null) {
            rootUIComponent.setSize(w, h);
            rootUIComponent.onShow();
        }
        repaint();
    }

    public void onHide() {
        if (displayLink != null) {
            displayLink.setPaused(true);
        }

        if (rootUIComponent != null) {
            rootUIComponent.onHide();
        }
    }

    @Override
    public synchronized Graphics getUGraphics() {
        return null;
    }

    @Override
    public synchronized void flushGraphics() { }

    @Override
    public void layoutSubviews() {
        super.layoutSubviews();
        CGRect bounds = getBounds();
        int newW = (int) (bounds.getWidth() * scale);
        int newH = (int) (bounds.getHeight() * scale);

        if (newW > 0 && newH > 0 && (newW != w || newH != h)) {
            this.w = newW;
            this.h = newH;

            if (RootContainer.enableOnScreenLog) {
                Logger.enableOnScreenLog(h);
            }

            if (rootUIComponent != null) {
                rootUIComponent.setSize(w, h);
                repaint();
            }
        }
    }

    @Override
    public synchronized void draw(CGRect rect) {
        CGContext context = UIGraphics.getCurrentContext();
        if (context != null && rootUIComponent != null && rootUIComponent.isVisible()) {
            context.saveGState();
            context.scaleCTM(1.0 / scale, 1.0 / scale);

            if (g == null) {
                g = new Graphics(context);
            } else {
                g.setContext(context);
            }

            rootUIComponent.paint(g);
            Logger.paint(g);

            context.restoreGState();
        }
    }

    @Override
    public void touchesBegan(NSSet<UITouch> touches, UIEvent event) {
        if (rootUIComponent == null || !rootUIComponent.isVisible()) {
            return;
        }

        UITouch touch = touches.any();
        if (touch != null) {
            pressedX = (int) (touch.getLocationInView(this).getX() * scale);
            pressedY = (int) (touch.getLocationInView(this).getY() * scale);
            pressedTime = System.currentTimeMillis();
            wasDragged = false;
            wasDownEvent = true;
            rootUIComponent.pointerPressed(pressedX, pressedY);
            repaint();
        }
    }

    @Override
    public void touchesMoved(NSSet<UITouch> touches, UIEvent event) {
        if (rootUIComponent == null || !rootUIComponent.isVisible()) {
            return;
        }
        if (!wasDownEvent) {
            return;
        }

        UITouch touch = touches.any();
        if (touch != null) {
            int currentX = (int) (touch.getLocationInView(this).getX() * scale);
            int currentY = (int) (touch.getLocationInView(this).getY() * scale);

            if (!wasDragged) {
                if (Math.abs(currentX - pressedX) + Math.abs(currentY - pressedY) > DRAG_THRESHOLD * scale) {
                    wasDragged = true;
                }
            }
            rootUIComponent.pointerDragged(currentX, currentY);
            repaint();
        }
    }

    @Override
    public void touchesEnded(NSSet<UITouch> touches, UIEvent event) {
        if (rootUIComponent == null || !rootUIComponent.isVisible()) {
            return;
        }

        UITouch touch = touches.any();
        if (touch != null) {
            int x = (int) (touch.getLocationInView(this).getX() * scale);
            int y = (int) (touch.getLocationInView(this).getY() * scale);

            if (wasDownEvent) {
                rootUIComponent.pointerReleased(x, y);

                if (!wasDragged && (System.currentTimeMillis() - pressedTime < 1000)) {
                    rootUIComponent.pointerClicked(x, y);
                }
            }
            repaint();
        }
        wasDownEvent = false;
    }

    @Override
    public void touchesCancelled(NSSet<UITouch> touches, UIEvent event) {
        wasDownEvent = false;
        super.touchesCancelled(touches, event);
        repaint();
    }
}
