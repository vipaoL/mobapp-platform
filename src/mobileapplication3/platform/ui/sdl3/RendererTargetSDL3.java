// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui.sdl3;

import com.sun.jna.Pointer;

import java.awt.Rectangle;

public class RendererTargetSDL3 {
    private final Pointer renderer;
    private final Pointer window;
    private int width;
    private int height;
    private Rectangle clipBounds;

    public RendererTargetSDL3(Pointer window, Pointer renderer, int width, int height) {
        this.window = window;
        this.renderer = renderer;
        this.width = width;
        this.height = height;
        this.clipBounds = new Rectangle(0, 0, width, height);
    }

    public Pointer getRenderer() {
        return renderer;
    }

    public Pointer getWindow() {
        return window;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Rectangle getClipBounds() {
        return new Rectangle(clipBounds);
    }

    public void setClipBounds(Rectangle clip) {
        if (clip == null) {
            this.clipBounds = new Rectangle(0, 0, width, height);
        } else {
            this.clipBounds = new Rectangle(clip);
        }
    }
}
