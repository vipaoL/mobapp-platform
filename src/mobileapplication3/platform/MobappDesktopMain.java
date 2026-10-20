// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform;

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

        int screenW = 1280;
        int screenH = 720;
        try {
            screenW = Toolkit.getDefaultToolkit().getScreenSize().width;
            screenH = Toolkit.getDefaultToolkit().getScreenSize().height;
        } catch (Throwable ignored) { }

        int minW = Math.min(screenW * 10 / 16, Font.getDefaultFontHeight() * 10);
        int minH = Math.min(screenH * 10 / 16, Font.getDefaultFontHeight() * 8);
        int targetW = Math.min(screenW, minW * 2);
        int targetH = Math.min(screenH, minH * 2);

        if (PlatformSettings.BACKEND_SDL3.equals(backend)) {
            try {
                RootContainerSDL3 rootContainerSDL3 = new RootContainerSDL3();
                RootContainer.setInst(rootContainerSDL3);
                rootContainerSDL3.initWindow(null, targetW, targetH, PlatformSettings.getFullscreenMode());

                Platform.init();
                init();

                rootContainerSDL3.runMainLoop();

                return;
            } catch (Throwable e) {
                err = e;
                Logger.log("Can't initialize SDL3", e);
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

    protected abstract void init();
    protected abstract void parseArgs(String[] args);
}