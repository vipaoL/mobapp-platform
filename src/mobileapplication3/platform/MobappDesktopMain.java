// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform;

import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.RootContainer;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class MobappDesktopMain {
    public MobappDesktopMain(String[] args) {
        parseArgs(args);

        Frame frame = new Frame();
        Platform.init(frame);
        RootContainer.getInst().setBgColor(0);
        frame.setVisible(true);
        frame.setLayout(new BorderLayout());
        frame.add(RootContainer.getInst(), BorderLayout.CENTER);

        int screenW = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenH = Toolkit.getDefaultToolkit().getScreenSize().height;
        int minW = Math.min(screenW * 10 / 16, Font.getDefaultFontHeight() * 10);
        int minH = Math.min(screenH * 10 / 16, Font.getDefaultFontHeight() * 8);
        frame.setMinimumSize(new Dimension(minW, minH));
        frame.setSize(Math.min(screenW, minW * 2), Math.min(screenH, minH * 2));
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
        frame.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                RootContainer.getInst().onWindowFocusGained();
            }
            public void windowLostFocus(WindowEvent e) {
                RootContainer.getInst().onWindowFocusLost();
            }
        });
        frame.setLocationRelativeTo(null);
    }

    protected abstract void parseArgs(String[] args);
}