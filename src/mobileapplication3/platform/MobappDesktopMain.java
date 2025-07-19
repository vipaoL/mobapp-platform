package mobileapplication3.platform;

import mobileapplication3.platform.ui.RootContainer;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class MobappDesktopMain extends Frame {
    public MobappDesktopMain(String[] args) {
        setSize(1200, 900);
        parseArgs(args);
        Platform.init(this);
        RootContainer.getInst().setBgColor(0);
        setVisible(true);
        setLayout(new BorderLayout());
        add(RootContainer.getInst(), BorderLayout.CENTER);
        setMinimumSize(new Dimension(400, 300));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
        setLocationRelativeTo(null);
    }

    protected abstract void parseArgs(String[] args);
}