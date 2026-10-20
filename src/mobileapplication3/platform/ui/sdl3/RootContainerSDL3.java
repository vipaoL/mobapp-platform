// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui.sdl3;

import com.sun.jna.Pointer;
import mobileapplication3.platform.KeyboardHelper;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.sdl3.SDL3Library;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.IRootContainer;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.IUIComponent;
import mobileapplication3.ui.Keys;
import mobileapplication3.ui.UISettings;

import java.util.HashSet;

import static mobileapplication3.ui.IUIComponent.*;

public class RootContainerSDL3 implements IRootContainer {
    private Pointer window = null;
    private Pointer renderer = null;
    private RendererTargetSDL3 rendererTarget = null;
    private final TextTextureCacheSDL3 textCache = new TextTextureCacheSDL3();
    private final int[] outW = new int[1];
    private final int[] outH = new int[1];
    private final int[] winW = new int[1];
    private final int[] winH = new int[1];
    private GraphicsSDL3 graphicsInstance = null;

    private IUIComponent rootUIComponent = null;
    private final KeyboardHelper kbHelper;
    private UISettings uiSettings;

    public int w = 800, h = 600;
    private int bgColor = 0x000000;

    private boolean wasDownEvent = false, wasDragged = false;
    private int lastPointerX, lastPointerY;
    private int pressedX, pressedY;
    private float mouseScaleX = 1.0f;
    private float mouseScaleY = 1.0f;
    private long pressedTime;
    private final HashSet<Integer> pressedKeys = new HashSet<>();

    private int currentTargetFPS = 60;
    private boolean isRunning = false;
    private boolean mainLoopStarted = false;

    private boolean isMinimized = false;

    private final SDL3Library.SDL_Event event = new SDL3Library.SDL_Event();

    public RootContainerSDL3() {
        kbHelper = new KeyboardHelper(this);
    }

    public void initWindow(String title, String appID, String version, int width, int height, boolean fullscreen) {
        if (isRunning) {
            return;
        }

        if (title != null || appID != null || version != null) {
            SDL3Library.INSTANCE.SDL_SetAppMetadata(
                    title != null ? title : "",
                    version != null ? version : "",
                    appID != null ? appID : ""
            );
        }

        if (!SDL3Library.INSTANCE.SDL_Init(SDL3Library.SDL_INIT_VIDEO)) {
            throw new IllegalStateException("SDL_Init failed");
        }

        this.w = width;
        this.h = height;

        long flags = SDL3Library.SDL_WINDOW_RESIZABLE | SDL3Library.SDL_WINDOW_HIGH_PIXEL_DENSITY;
        if (fullscreen) {
            flags |= SDL3Library.SDL_WINDOW_FULLSCREEN;
        }
        window = SDL3Library.INSTANCE.SDL_CreateWindow(
                title != null ? title : "",
                w, h,
                flags
        );

        if (window == null) {
            throw new IllegalStateException("SDL_CreateWindow failed");
        }

        renderer = SDL3Library.INSTANCE.SDL_CreateRenderer(window, null);
        if (renderer == null) {
            throw new IllegalStateException("SDL_CreateRenderer failed");
        }

        rendererTarget = new RendererTargetSDL3(window, renderer, w, h);
        isRunning = true;

        kbHelper.start();
    }

    public synchronized void runMainLoop() {
        if (!mainLoopStarted && isRunning) {
            mainLoopStarted = true;
            mainLoop();
        }
    }

    private void mainLoop() {
        while (isRunning) {
            long startNanos = System.nanoTime();

            pollEvents();

            if (isMinimized) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) { }
                continue;
            }

            if (isRunning) {
                tickAndPaint();
            }

            if (currentTargetFPS > 0) {
                long frameTimeNanos = 1_000_000_000L / currentTargetFPS;
                long targetTime = startNanos + frameTimeNanos;

                long remainingNanos = targetTime - System.nanoTime();

                if (remainingNanos > 0) {
                    if (remainingNanos > 3_000_000L) {
                        long sleepMs = (remainingNanos - 2_000_000L) / 1_000_000L;
                        try {
                            Thread.sleep(sleepMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    while (System.nanoTime() < targetTime) { }
                }
            }
        }

        cleanup();
    }

    private void checkRenderOutputSize() {
        if (renderer == null || window == null) {
            return;
        }

        if (SDL3Library.INSTANCE.SDL_GetRenderOutputSize(renderer, outW, outH)
                && SDL3Library.INSTANCE.SDL_GetWindowSize(window, winW, winH)) {

            if (outW[0] > 0 && outH[0] > 0) {
                if (winW[0] > 0 && winH[0] > 0) {
                    mouseScaleX = (float) outW[0] / winW[0];
                    mouseScaleY = (float) outH[0] / winH[0];
                }

                if (outW[0] != w || outH[0] != h) {
                    onSizeChanged(outW[0], outH[0]);
                }
            }
        }
    }

    private void pollEvents() {
        while (SDL3Library.INSTANCE.SDL_PollEvent(event)) {
            switch (event.type) {
                case SDL3Library.SDL_EVENT_QUIT:
                case SDL3Library.SDL_EVENT_WINDOW_CLOSE_REQUESTED:
                    isRunning = false;
                    Platform.exit();
                    break;

                case SDL3Library.SDL_EVENT_WINDOW_FOCUS_LOST:
                case SDL3Library.SDL_EVENT_WINDOW_MINIMIZED:
                    if (!isMinimized && rootUIComponent != null) {
                        onHide();
                    }
                    isMinimized = true;
                    break;

                case SDL3Library.SDL_EVENT_WINDOW_FOCUS_GAINED:
                case SDL3Library.SDL_EVENT_WINDOW_RESTORED:
                    if (isMinimized && rootUIComponent != null) {
                        onShow();
                    }
                    isMinimized = false;
                    break;

                case SDL3Library.SDL_EVENT_WINDOW_RESIZED:
                case SDL3Library.SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED:
                    checkRenderOutputSize();
                    break;

                case SDL3Library.SDL_EVENT_KEY_DOWN: {
                    SDL3Library.SDL_KeyboardEvent key = event.key();
                    int keycode = key.key;
                    boolean repeat = key.repeat != 0;
                    if (!repeat && pressedKeys.add(keycode)) {
                        int converted = convertSdlKeyCode(keycode);
                        kbHelper.keyPressed(converted);
                        wasDownEvent = true;
                    }
                    break;
                }

                case SDL3Library.SDL_EVENT_KEY_UP:
                {
                    int keycode = event.key().key;
                    pressedKeys.remove(keycode);
                    int converted = convertSdlKeyCode(keycode);
                    kbHelper.keyReleased(converted);
                    break;
                }

                case SDL3Library.SDL_EVENT_MOUSE_MOTION:
                {
                    SDL3Library.SDL_MouseMotionEvent motion = event.motion();
                    float x = motion.x * mouseScaleX;
                    float y = motion.y * mouseScaleY;
                    if (wasDownEvent) {
                        pointerDragged((int) x, (int) y);
                    }
                    break;
                }

                case SDL3Library.SDL_EVENT_MOUSE_BUTTON_DOWN:
                {
                    SDL3Library.SDL_MouseButtonEvent btn = event.button();
                    byte button = btn.button;
                    float x = btn.x * mouseScaleX;
                    float y = btn.y * mouseScaleY;
                    pressedX = (int) x;
                    pressedY = (int) y;
                    pressedTime = System.currentTimeMillis();

                    int event;
                    if (button == 1) {
                        event = MOUSE_PRIMARY_PRESSED;
                    } else if (button == 3) {
                        event = MOUSE_SECONDARY_PRESSED;
                    } else {
                        event = MOUSE_WHEEL_PRESSED;
                    }

                    if (!mouseEvent(event, pressedX, pressedY)) {
                        pointerPressed(pressedX, pressedY);
                    }
                    wasDownEvent = true;
                    break;
                }

                case SDL3Library.SDL_EVENT_MOUSE_BUTTON_UP:
                {
                    SDL3Library.SDL_MouseButtonEvent btn = event.button();
                    byte button = btn.button;
                    float x = btn.x * mouseScaleX;
                    float y = btn.y * mouseScaleY;
                    int rx = (int) x;
                    int ry = (int) y;

                    int event;
                    if (button == 1) {
                        event = MOUSE_PRIMARY_RELEASED;
                    } else if (button == 3) {
                        event = MOUSE_SECONDARY_RELEASED;
                    } else {
                        event = MOUSE_WHEEL_RELEASED;
                    }

                    if (!mouseEvent(event, rx, ry)) {
                        if (!wasDragged && System.currentTimeMillis() - pressedTime < 1000) {
                            pointerClicked(rx, ry);
                        }
                        pointerReleased(rx, ry);
                    }
                    wasDownEvent = false;
                    wasDragged = false;
                    break;
                }

                case SDL3Library.SDL_EVENT_MOUSE_WHEEL:
                {
                    float wy = event.wheel().y;
                    mouseEvent(wy > 0 ? MOUSE_WHEEL_SCROLLED_UP : MOUSE_WHEEL_SCROLLED_DOWN, lastPointerX, lastPointerY);
                    break;
                }
            }
        }
    }

    private void tickAndPaint() {
        if (rootUIComponent != null && rootUIComponent.isVisible()) {
            rootUIComponent.tick();
        }
        paint();
    }

    private void paint() {
        if (renderer == null || rendererTarget == null) {
            return;
        }

        byte r = (byte) ((bgColor >> 16) & 0xFF);
        byte g = (byte) ((bgColor >> 8) & 0xFF);
        byte b = (byte) (bgColor & 0xFF);
        SDL3Library.INSTANCE.SDL_SetRenderDrawColor(renderer, r, g, b, (byte) 255);
        SDL3Library.INSTANCE.SDL_RenderClear(renderer);

        Graphics graphics = getUGraphics();
        graphics.setClip(0, 0, w, h);

        if (rootUIComponent != null) {
            rootUIComponent.paint(graphics);
        } else {
            graphics.setColor(0xaaaaaa);
            graphics.drawString("Nothing to draw.", w / 2, h / 2, Graphics.BOTTOM | Graphics.HCENTER);
        }

        Logger.paint(graphics);
        flushGraphics();
    }

    @Override
    public Graphics getUGraphics() {
        if (graphicsInstance == null) {
            graphicsInstance = new GraphicsSDL3(rendererTarget, textCache);
        }
        return graphicsInstance;
    }

    @Override
    public void flushGraphics() {
        if (renderer != null) {
            SDL3Library.INSTANCE.SDL_RenderPresent(renderer);
        }
    }

    @Override
    public void repaint() {
        if (rootUIComponent != null && rootUIComponent.isVisible()) {
            int targetFPS = rootUIComponent.getTargetFPS();
            if (targetFPS != currentTargetFPS) {
                currentTargetFPS = targetFPS;
            }
        }
    }

    @Override
    public int getWidth() {
        return w;
    }

    @Override
    public int getHeight() {
        return h;
    }

    public void init() {
        RootContainer.enableOnScreenLog = uiSettings == null || uiSettings.enableOnScreenLog();
        if (RootContainer.enableOnScreenLog) {
            if (h > 0) {
                Logger.enableOnScreenLog(h);
            }
        } else {
            Logger.disableOnScreenLog();
        }

        if (rootUIComponent != null) {
            rootUIComponent.init();
        }
    }

    @Override
    public IUIComponent getRootUIComponent() {
        return rootUIComponent;
    }

    public void setRootUIComponent(IUIComponent rootUIComponent) {
        Logger.log("setRootUIComponent");
        wasDownEvent = false;
        if (this.rootUIComponent != null) {
            this.rootUIComponent.setVisible(false);
            this.rootUIComponent.setParent(null);
            this.rootUIComponent.setFocused(false);
        }

        if (rootUIComponent != null) {
            rootUIComponent.setParent(this);
            rootUIComponent.init();
            rootUIComponent.setSize(getWidth(), getHeight());
            rootUIComponent.postInit();

            rootUIComponent.setVisible(true);
            rootUIComponent.setFocused(true);

            this.rootUIComponent = rootUIComponent;
            currentTargetFPS = rootUIComponent.getTargetFPS();
        }
    }

    public void setUISettings(UISettings uiSettings) {
        this.uiSettings = uiSettings;
        if (uiSettings != null) {
            uiSettings.onChange();
        }
    }

    public void setFullscreenMode(boolean value) {
        if (window != null) {
            SDL3Library.INSTANCE.SDL_SetWindowFullscreen(window, value);
        }
    }

    protected void onSizeChanged(int newW, int newH) {
        this.w = newW;
        this.h = newH;
        if (rendererTarget != null) {
            rendererTarget.setSize(newW, newH);
        }
        if (RootContainer.enableOnScreenLog) {
            Logger.enableOnScreenLog(newH);
        }
        if (rootUIComponent != null) {
            rootUIComponent.setSize(newW, newH);
        }
    }

    @Override
    public void onShow() {
        kbHelper.start();
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            onSizeChanged(getWidth(), getHeight());
            rootUIComponent.onShow();
        }
        repaint();
    }

    @Override
    public void onHide() {
        kbHelper.stop();
        if (rootUIComponent != null) {
            rootUIComponent.onHide();
            rootUIComponent.setVisible(false);
        }
    }

    @Override
    public void handleKeyPressed(int keyCode, int count) {
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            if (rootUIComponent.keyPressed(keyCode, count)) {
                if (!RootContainer.displayKbHints) {
                    RootContainer.displayKbHints = true;
                    if (uiSettings != null) uiSettings.onChange();
                }
            }
        }
    }

    @Override
    public void handleKeyReleased(int keyCode, int count) {
        if (rootUIComponent != null && wasDownEvent) {
            rootUIComponent.setVisible(true);
            rootUIComponent.keyReleased(keyCode, count);
        }
        wasDownEvent = false;
    }

    @Override
    public void handleKeyRepeated(int keyCode, int pressedCount) {
        if (RootContainer.getAction(keyCode) == Keys.FIRE) return;
        if (rootUIComponent != null && wasDownEvent) {
            rootUIComponent.keyRepeated(keyCode, pressedCount);
        }
    }

    protected boolean mouseEvent(int event, int x, int y) {
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            return rootUIComponent.mouseEvent(event, x, y);
        }
        return false;
    }

    protected void pointerPressed(int x, int y) {
        lastPointerX = x;
        lastPointerY = y;
        if (rootUIComponent != null) {
            rootUIComponent.setVisible(true);
            rootUIComponent.pointerPressed(x, y);
        }
    }

    protected void pointerDragged(int x, int y) {
        if (lastPointerX == x && lastPointerY == y) return;
        lastPointerX = x;
        lastPointerY = y;
        if (rootUIComponent != null && wasDownEvent) {
            rootUIComponent.pointerDragged(x, y);
        }

        if (!wasDragged) {
            int d = Math.abs(x - pressedX) + Math.abs(y - pressedY);
            if (d > Font.getDefaultFontHeight()) {
                wasDragged = true;
            }
        }
    }

    protected void pointerReleased(int x, int y) {
        if (rootUIComponent != null && wasDownEvent) {
            rootUIComponent.pointerReleased(x, y);
        }
    }

    protected void pointerClicked(int x, int y) {
        if (rootUIComponent != null && wasDownEvent) {
            rootUIComponent.pointerClicked(x, y);
        }
    }

    private int convertSdlKeyCode(int sdlKey) {
        switch (sdlKey) {
            case SDL3Library.SDLK_RETURN:
            case SDL3Library.SDLK_SPACE:
                return Keys.FIRE;

            case SDL3Library.SDLK_UP:
                return Keys.UP;
            case SDL3Library.SDLK_DOWN:
                return Keys.DOWN;
            case SDL3Library.SDLK_LEFT:
                return Keys.LEFT;
            case SDL3Library.SDLK_RIGHT:
                return Keys.RIGHT;

            case SDL3Library.SDLK_F1:
            case SDL3Library.SDLK_MINUS:
                return Keys.KEY_SOFT_LEFT;

            case SDL3Library.SDLK_ESCAPE:
            case SDL3Library.SDLK_F2:
            case SDL3Library.SDLK_PLUS:
            case SDL3Library.SDLK_EQUALS:
                return Keys.KEY_SOFT_RIGHT;

            case SDL3Library.SDLK_0: return Keys.KEY_NUM0;
            case SDL3Library.SDLK_1: return Keys.KEY_NUM1;
            case SDL3Library.SDLK_2: return Keys.KEY_NUM2;
            case SDL3Library.SDLK_3: return Keys.KEY_NUM3;
            case SDL3Library.SDLK_4: return Keys.KEY_NUM4;
            case SDL3Library.SDLK_5: return Keys.KEY_NUM5;
            case SDL3Library.SDLK_6: return Keys.KEY_NUM6;
            case SDL3Library.SDLK_7: return Keys.KEY_NUM7;
            case SDL3Library.SDLK_8: return Keys.KEY_NUM8;
            case SDL3Library.SDLK_9: return Keys.KEY_NUM9;

            case SDL3Library.SDLK_F3:
            case SDL3Library.SDLK_ASTERISK:
                return Keys.KEY_STAR;

            case SDL3Library.SDLK_F4:
            case SDL3Library.SDLK_SLASH:
                return Keys.KEY_POUND;

            default:
                return 0;
        }
    }

    @Override
    public void closePopup() {
        isRunning = false;
        Platform.exit();
    }

    @Override
    public UISettings getUISettings() {
        return uiSettings;
    }

    @Override
    public boolean isOnScreen() {
        return true;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public int getBgColor() {
        return bgColor;
    }

    private void cleanup() {
        textCache.clear();
        if (renderer != null) {
            SDL3Library.INSTANCE.SDL_DestroyRenderer(renderer);
            renderer = null;
        }
        if (window != null) {
            SDL3Library.INSTANCE.SDL_DestroyWindow(window);
            window = null;
        }
        SDL3Library.INSTANCE.SDL_Quit();
    }
}