// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui.sdl3;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import mobileapplication3.platform.sdl3.SDL3Library;
import mobileapplication3.platform.ui.Font;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class TextTextureCacheSDL3 {
    private static final int MAX_ENTRIES = 500;

    public static class Entry {
        public final Pointer texture;
        public final int width;
        public final int height;

        public Entry(Pointer texture, int width, int height) {
            this.texture = texture;
            this.width = width;
            this.height = height;
        }
    }

    private static class Key {
        final String text;
        final int face;
        final int style;
        final int size;
        final int height;
        final int color;

        Key(String text, Font font, int color) {
            this.text = text;
            this.face = font.getFace();
            this.style = font.getStyle();
            this.size = font.getSize();
            this.height = font.getHeight();
            this.color = color;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return face == key.face
                    && style == key.style
                    && size == key.size
                    && height == key.height
                    && color == key.color
                    && Objects.equals(text, key.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, face, style, size, height, color);
        }
    }

    private final Map<Key, Entry> cache = new LinkedHashMap<Key, Entry>(128, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Key, Entry> eldest) {
            if (size() > MAX_ENTRIES) {
                if (eldest.getValue().texture != null) {
                    SDL3Library.INSTANCE.SDL_DestroyTexture(eldest.getValue().texture);
                }
                return true;
            }
            return false;
        }
    };

    public synchronized Entry getTextTexture(Pointer renderer, String text, Font font, int colorRGB) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        int colorARGB = (colorRGB & 0xFF000000) == 0 ? (colorRGB | 0xFF000000) : colorRGB;

        Key key = new Key(text, font, colorARGB);
        Entry existing = cache.get(key);
        if (existing != null) {
            return existing;
        }

        java.awt.Font awtFont = font.getAwtFont();
        int width = Math.max(1, font.stringWidth(text));
        int height = Math.max(1, font.getHeight());

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setFont(awtFont);
        g2d.setColor(new Color(colorARGB, true));

        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, 0, fm.getAscent());
        g2d.dispose();

        int[] argb = new int[width * height];
        img.getRGB(0, 0, width, height, argb, 0, width);

        byte[] rgbaBytes = new byte[width * height * 4];
        for (int i = 0; i < argb.length; i++) {
            int pixel = argb[i];
            rgbaBytes[i * 4]     = (byte) ((pixel >> 16) & 0xFF);
            rgbaBytes[i * 4 + 1] = (byte) ((pixel >> 8) & 0xFF);
            rgbaBytes[i * 4 + 2] = (byte) (pixel & 0xFF);
            rgbaBytes[i * 4 + 3] = (byte) ((pixel >> 24) & 0xFF);
        }

        try (Memory mem = new Memory(rgbaBytes.length)) {
            mem.write(0, rgbaBytes, 0, rgbaBytes.length);

            Pointer surface = SDL3Library.INSTANCE.SDL_CreateSurfaceFrom(
                    width, height, SDL3Library.SDL_PIXELFORMAT_ABGR8888, mem, width * 4
            );
            if (surface == null) {
                return null;
            }

            SDL3Library.INSTANCE.SDL_SetSurfaceBlendMode(surface, SDL3Library.SDL_BLENDMODE_BLEND);
            Pointer texture = SDL3Library.INSTANCE.SDL_CreateTextureFromSurface(renderer, surface);
            SDL3Library.INSTANCE.SDL_DestroySurface(surface);

            if (texture == null) {
                return null;
            }

            SDL3Library.INSTANCE.SDL_SetTextureBlendMode(texture, SDL3Library.SDL_BLENDMODE_BLEND);

            Entry entry = new Entry(texture, width, height);
            cache.put(key, entry);
            return entry;
        }
    }

    public synchronized void clear() {
        for (Entry entry : cache.values()) {
            if (entry.texture != null) {
                SDL3Library.INSTANCE.SDL_DestroyTexture(entry.texture);
            }
        }
        cache.clear();
    }
}