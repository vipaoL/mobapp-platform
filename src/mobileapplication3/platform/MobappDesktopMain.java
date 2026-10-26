// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform;

import mobileapplication3.platform.sdl3.SDL3Library;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.platform.ui.RootContainerAWT;
import mobileapplication3.platform.ui.sdl3.RootContainerSDL3;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class MobappDesktopMain {
    protected boolean isBackendExplicit = false;

    public MobappDesktopMain(String[] args) {
        parseArgs(args);

        String backend = PlatformSettings.getBackend();
        Throwable err = null;

        float scale = 1.0f;
        int screenW = 1280;
        int screenH = 720;

        if (PlatformSettings.BACKEND_SDL3.equals(backend)) {
            try {
                RootContainerSDL3 rootContainerSDL3 = new RootContainerSDL3();
                RootContainer.setInst(rootContainerSDL3);
                rootContainerSDL3.initSDL3(getAppTitle(), getAppId(), getAppVersion());

                int displayId = SDL3Library.INSTANCE.SDL_GetPrimaryDisplay();

                SDL3Library.SDL_DisplayMode mode = SDL3Library.INSTANCE.SDL_GetCurrentDisplayMode(displayId);
                if (mode != null && mode.pixel_density > 0.0f) {
                    scale = mode.pixel_density;
                }

                SDL3Library.SDL_Rect bounds = new SDL3Library.SDL_Rect();
                if (SDL3Library.INSTANCE.SDL_GetDisplayBounds(displayId, bounds) && bounds.w > 0) {
                    screenW = bounds.w;
                    screenH = bounds.h;
                }
            } catch (Throwable e) {
                err = e;
                Logger.log("Can't initialize SDL3", e);
            }
        } else if (PlatformSettings.BACKEND_AWT.equals(backend)) {
            try {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                screenW = screenSize.width;
                screenH = screenSize.height;
            } catch (Throwable ignored) { }
        }

        PlatformSettings.systemScale = scale;

        int logicalFontHeight = Math.round(Font.getDefaultFontHeight() / scale);

        int minW = Math.min(screenW * 10 / 16, logicalFontHeight * 10);
        int minH = Math.min(screenH * 10 / 16, logicalFontHeight * 8);
        int targetW = Math.min(screenW, minW * 2);
        int targetH = Math.min(screenH, minH * 2);

        if (PlatformSettings.BACKEND_SDL3.equals(backend) && err == null) {
            boolean windowInitialized = false;
            RootContainerSDL3 rootContainerSDL3 = (RootContainerSDL3) RootContainer.getInst();

            try {
                rootContainerSDL3.initWindow(targetW, targetH, PlatformSettings.getFullscreenMode());
                windowInitialized = true;
            } catch (Throwable e) {
                err = e;
                Logger.log("Can't initialize SDL3 window", e);
            }

            if (windowInitialized) {
                try {
                    Platform.init();
                    init();
                    rootContainerSDL3.runMainLoop();
                } catch (Throwable t) {
                    Logger.log("Game crashed: ", t);
                    System.exit(1);
                }
                return;
            }
        }

        Frame frame = new Frame();
        RootContainerAWT rootContainerAWT = new RootContainerAWT();
        RootContainer.setInst(rootContainerAWT);

        frame.setLayout(new BorderLayout());
        frame.add(rootContainerAWT, BorderLayout.CENTER);

        frame.setMinimumSize(new Dimension(minW, minH));
        frame.setSize(targetW, targetH);
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
        frame.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                RootContainer.getInst().onShow();
            }
            public void windowLostFocus(WindowEvent e) {
                RootContainer.getInst().onHide();
            }
        });

        frame.setVisible(true);

        Platform.init();

        init();

        if (err != null && isBackendExplicit) {
            Platform.showError("Can't initialize " + backend + ". Falling back to the default backend.\n\n", err);
        }
    }

    protected String getAppTitle() {
        return null;
    }

    protected String getAppId() {
        return null;
    }

    protected String getAppVersion() {
        return null;
    }

    protected abstract void init();
    protected abstract void parseArgs(String[] args);
}