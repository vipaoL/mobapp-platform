// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.sdl3;

import com.sun.jna.*;

public interface SDL3Library extends Library {
    SDL3Library INSTANCE = Native.load("SDL3", SDL3Library.class);

    int SDL_INIT_VIDEO = 0x20;
    long SDL_WINDOW_FULLSCREEN = 0x1L;
    long SDL_WINDOW_RESIZABLE = 0x20L;
    long SDL_WINDOW_HIGH_PIXEL_DENSITY = 0x2000L;
    int SDL_EVENT_QUIT = 0x100;
    int SDL_EVENT_WINDOW_CLOSE_REQUESTED = 0x210;
    int SDL_EVENT_WINDOW_RESIZED = 0x206;
    int SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED = 0x207;
    int SDL_EVENT_WINDOW_FOCUS_GAINED = 0x20E;
    int SDL_EVENT_WINDOW_FOCUS_LOST = 0x20F;
    int SDL_EVENT_WINDOW_MINIMIZED = 0x209;
    int SDL_EVENT_WINDOW_RESTORED = 0x20B;
    int SDL_EVENT_KEY_DOWN = 0x300;
    int SDL_EVENT_KEY_UP = 0x301;
    int SDL_EVENT_MOUSE_MOTION = 0x400;
    int SDL_EVENT_MOUSE_BUTTON_DOWN = 0x401;
    int SDL_EVENT_MOUSE_BUTTON_UP = 0x402;
    int SDL_EVENT_MOUSE_WHEEL = 0x403;

    int SDLK_TAB = 0x09;
    int SDLK_RETURN = 0xD;
    int SDLK_ESCAPE = 0x1B;
    int SDLK_SPACE = 0x20;
    int SDLK_ASTERISK = 0x2A;
    int SDLK_PLUS = 0x2B;
    int SDLK_MINUS = 0x2D;
    int SDLK_SLASH = 0x2F;
    int SDLK_EQUALS = 0x3D;
    int SDLK_0 = 0x30;
    int SDLK_1 = 0x31;
    int SDLK_2 = 0x32;
    int SDLK_3 = 0x33;
    int SDLK_4 = 0x34;
    int SDLK_5 = 0x35;
    int SDLK_6 = 0x36;
    int SDLK_7 = 0x37;
    int SDLK_8 = 0x38;
    int SDLK_9 = 0x39;
    int SDLK_R = 0x72;
    int SDLK_F1 = 0x4000003A;
    int SDLK_F2 = 0x4000003B;
    int SDLK_F3 = 0x4000003C;
    int SDLK_F4 = 0x4000003D;
    int SDLK_RIGHT = 0x4000004F;
    int SDLK_LEFT = 0x40000050;
    int SDLK_DOWN = 0x40000051;
    int SDLK_UP = 0x40000052;
    int SDLK_LCTRL = 0x400000E0;
    int SDLK_LSHIFT = 0x400000E1;
    int SDLK_LALT = 0x400000E2;
    int SDLK_LGUI = 0x400000E3;
    int SDLK_RCTRL = 0x400000E4;
    int SDLK_RSHIFT = 0x400000E5;
    int SDLK_RALT = 0x400000E6;
    int SDLK_RGUI = 0x400000E7;

    int SDL_PIXELFORMAT_ARGB8888 = 0x16661804;
    int SDL_PIXELFORMAT_RGBA8888 = 0x16461804;
    int SDL_PIXELFORMAT_ABGR8888 = 0x16761804;
    int SDL_PIXELFORMAT_BGRA8888 = 0x16861804;

    int SDL_BLENDMODE_NONE = 0x00000000;
    int SDL_BLENDMODE_BLEND = 0x00000001;
    int SDL_BLENDMODE_ADD = 0x00000002;
    int SDL_BLENDMODE_MOD = 0x00000004;

    @Structure.FieldOrder({"x", "y", "w", "h"})
    class SDL_FRect extends Structure {
        public float x;
        public float y;
        public float w;
        public float h;

        public SDL_FRect() {}
        public SDL_FRect(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    @Structure.FieldOrder({"x", "y", "w", "h"})
    class SDL_Rect extends Structure {
        public int x;
        public int y;
        public int w;
        public int h;

        public SDL_Rect() {}
        public SDL_Rect(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    @Structure.FieldOrder({"x", "y"})
    class SDL_FPoint extends Structure {
        public float x;
        public float y;

        public SDL_FPoint() {}
        public SDL_FPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    @Structure.FieldOrder({"r", "g", "b", "a"})
    class SDL_FColor extends Structure {
        public float r;
        public float g;
        public float b;
        public float a;

        public SDL_FColor() {}
        public SDL_FColor(float r, float g, float b, float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }
    }

    @Structure.FieldOrder({"position", "color", "tex_coord"})
    class SDL_Vertex extends Structure {
        public SDL_FPoint position;
        public SDL_FColor color;
        public SDL_FPoint tex_coord;

        public SDL_Vertex() {
            position = new SDL_FPoint();
            color = new SDL_FColor();
            tex_coord = new SDL_FPoint();
        }

        public SDL_Vertex(float x, float y, float r, float g, float b, float a) {
            position = new SDL_FPoint(x, y);
            color = new SDL_FColor(r, g, b, a);
            tex_coord = new SDL_FPoint(0, 0);
        }
    }

    @Structure.FieldOrder({"type", "reserved", "timestamp"})
    class SDL_CommonEvent extends Structure {
        public int type;
        public int reserved;
        public long timestamp;
    }

    @Structure.FieldOrder({"type", "reserved", "timestamp", "windowID", "which", "scancode", "key", "mod", "raw", "down", "repeat"})
    class SDL_KeyboardEvent extends Structure {
        public int type;
        public int reserved;
        public long timestamp;
        public int windowID;
        public int which;
        public int scancode;
        public int key;
        public short mod;
        public short raw;
        public byte down;
        public byte repeat;
    }

    @Structure.FieldOrder({"type", "reserved", "timestamp", "windowID", "which", "button", "down", "clicks", "padding", "x", "y"})
    class SDL_MouseButtonEvent extends Structure {
        public int type;
        public int reserved;
        public long timestamp;
        public int windowID;
        public int which;
        public byte button;
        public byte down;
        public byte clicks;
        public byte padding;
        public float x;
        public float y;
    }

    @Structure.FieldOrder({"type", "reserved", "timestamp", "windowID", "which", "state", "x", "y", "xrel", "yrel"})
    class SDL_MouseMotionEvent extends Structure {
        public int type;
        public int reserved;
        public long timestamp;
        public int windowID;
        public int which;
        public int state;
        public float x;
        public float y;
        public float xrel;
        public float yrel;
    }

    @Structure.FieldOrder({"type", "reserved", "timestamp", "windowID", "which", "x", "y", "direction", "mouse_x", "mouse_y"})
    class SDL_MouseWheelEvent extends Structure {
        public int type;
        public int reserved;
        public long timestamp;
        public int windowID;
        public int which;
        public float x;
        public float y;
        public int direction;
        public float mouse_x;
        public float mouse_y;
    }

    class SDL_Event extends Union {
        public int type;
        public SDL_CommonEvent common;
        public SDL_KeyboardEvent key;
        public SDL_MouseButtonEvent button;
        public SDL_MouseMotionEvent motion;
        public SDL_MouseWheelEvent wheel;
        public byte[] padding = new byte[128];

        public SDL_KeyboardEvent key() {
            setType(SDL_KeyboardEvent.class);
            read();
            return key;
        }
        public SDL_MouseButtonEvent button() {
            setType(SDL_MouseButtonEvent.class);
            read();
            return button;
        }
        public SDL_MouseMotionEvent motion() {
            setType(SDL_MouseMotionEvent.class);
            read();
            return motion;
        }
        public SDL_MouseWheelEvent wheel() {
            setType(SDL_MouseWheelEvent.class);
            read();
            return wheel;
        }
    }

    @Structure.FieldOrder({"displayID", "format", "w", "h", "pixel_density", "refresh_rate", "refresh_rate_numerator", "refresh_rate_denominator", "internal"})
    class SDL_DisplayMode extends Structure {
        public static class ByReference extends SDL_DisplayMode implements Structure.ByReference {}

        public int displayID;
        public int format;
        public int w;
        public int h;
        public float pixel_density;
        public float refresh_rate;
        public int refresh_rate_numerator;
        public int refresh_rate_denominator;
        public Pointer internal;
    }

    SDL_DisplayMode.ByReference SDL_GetCurrentDisplayMode(int displayID);

    boolean SDL_Init(int flags);
    void SDL_Quit();

    int SDL_GetPrimaryDisplay();
    boolean SDL_GetDisplayBounds(int displayID, SDL_Rect rect);

    Pointer SDL_CreateWindow(String title, int width, int height, long flags);
    void SDL_DestroyWindow(Pointer window);

    Pointer SDL_CreateRenderer(Pointer window, String name);
    void SDL_DestroyRenderer(Pointer renderer);

    boolean SDL_SetRenderDrawColor(Pointer renderer, byte r, byte g, byte b, byte a);
    boolean SDL_RenderClear(Pointer renderer);
    boolean SDL_RenderPresent(Pointer renderer);

    boolean SDL_RenderLine(Pointer renderer, float x1, float y1, float x2, float y2);
    boolean SDL_RenderLines(Pointer renderer, Pointer points, int count);
    boolean SDL_RenderRect(Pointer renderer, SDL_FRect rect);
    boolean SDL_RenderFillRect(Pointer renderer, SDL_FRect rect);
    boolean SDL_RenderPoint(Pointer renderer, float x, float y);
    boolean SDL_RenderGeometry(Pointer renderer, Pointer texture, Pointer vertices, int num_vertices, int[] indices, int num_indices);

    boolean SDL_SetRenderClipRect(Pointer renderer, SDL_Rect rect);
    boolean SDL_GetRenderClipRect(Pointer renderer, SDL_Rect rect);

    Pointer SDL_CreateSurfaceFrom(int width, int height, int format, Pointer pixels, int pitch);
    Pointer SDL_CreateTextureFromSurface(Pointer renderer, Pointer surface);
    boolean SDL_SetSurfaceBlendMode(Pointer surface, int blendMode);
    boolean SDL_SetTextureBlendMode(Pointer texture, int blendMode);
    boolean SDL_RenderTexture(Pointer renderer, Pointer texture, SDL_FRect srcrect, SDL_FRect dstrect);
    void SDL_DestroyTexture(Pointer texture);
    void SDL_DestroySurface(Pointer surface);

    boolean SDL_PollEvent(SDL_Event event);
    boolean SDL_SetWindowFullscreen(Pointer window, boolean fullscreen);
    boolean SDL_GetWindowSize(Pointer window, int[] w, int[] h);
    boolean SDL_GetWindowSizeInPixels(Pointer window, int[] w, int[] h);
    boolean SDL_GetRenderOutputSize(Pointer renderer, int[] w, int[] h);
    boolean SDL_SetRenderScale(Pointer renderer, float scaleX, float scaleY);
    boolean SDL_SetRenderVSync(Pointer renderer, int vsync);
    void SDL_SetWindowTitle(Pointer window, String title);

    boolean SDL_HideCursor();
    boolean SDL_ShowCursor();

    void SDL_Delay(int ms);
    long SDL_GetTicks();

    boolean SDL_SetAppMetadata(String appname, String appversion, String appidentifier);
}
