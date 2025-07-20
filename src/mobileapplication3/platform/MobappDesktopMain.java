package mobileapplication3.platform;

import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.RootContainer;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class MobappDesktopMain extends Frame {
    public MobappDesktopMain(String[] args) {
        parseArgs(args);
        Platform.init(this);
        RootContainer.getInst().setBgColor(0);
        setVisible(true);
        setLayout(new BorderLayout());
        add(RootContainer.getInst(), BorderLayout.CENTER);
        int screenW = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenH = Toolkit.getDefaultToolkit().getScreenSize().height;
        int minW = Math.min(screenW * 10 / 16, Font.getDefaultFontHeight() * 10);
        int minH = Math.min(screenH * 10 / 16, Font.getDefaultFontHeight() * 8);
        setMinimumSize(new Dimension(minW, minH));
        setSize(Math.min(screenW, minW * 2), Math.min(screenH, minH * 2));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
        setLocationRelativeTo(null);
    }

    protected abstract void parseArgs(String[] args);
}