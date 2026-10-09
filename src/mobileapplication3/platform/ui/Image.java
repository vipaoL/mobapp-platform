// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.sdl3.SDL3Library;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class Image implements IImage {
    private BufferedImage image;

    private Pointer sdlTexture = null;
    private Pointer sdlRendererRef = null;

    public Image(BufferedImage image) {
        if (image == null) {
            Logger.log("got null image");
        }
        this.image = image;
    }

    public static Image createImage(int width, int height) {
        return new Image(new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB));
    }

    public static Image createRGBImage(int[] rgb, int width, int height, boolean processAlpha) {
        BufferedImage image = new BufferedImage(width, height, processAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_USHORT_565_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, rgb[y * width + x]);
            }
        }
        return new Image(image);
    }

    public static Image createImage(String source) throws IOException {
        try {
            Logger.log("reading resourse \"" + source + "\"");
            return new Image(ImageIO.read(Platform.getResource(source)));
        } catch (NullPointerException | IllegalArgumentException | IOException ex) {
            Logger.log(ex);
            throw new IOException("can't read image");
        }
    }

    public static void blurImg(Image img) {
        try {
            Graphics g = img.getGraphics();
            int x0 = 0, y0 = 0;
            int w = img.getWidth();
            int h = img.getHeight();
            int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
            int a = 3;
            int offset = new Random().nextInt(a);
            x0 += offset;
            for (int i = -offset; i < (w + h) / a; i++) {
                g.setColor(0x110033);
                g.drawLine(x1 + x0, y1 + y0, x2 + x0, y2 + y0);
                g.drawLine(x1 + x0, h - (y1 + y0), x2 + x0, h - (y2 + y0));

                if (y1 < h) {
                    y1 += a;
                } else {
                    x1 += a;
                }

                if (x2 < w) {
                    x2 += a;
                } else {
                    y2 += a;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized Pointer getSdl3Texture(Pointer renderer) {
        if (image == null) {
            return null;
        }
        if (sdlTexture != null && sdlRendererRef == renderer) {
            return sdlTexture;
        }
        if (sdlTexture != null) {
            SDL3Library.INSTANCE.SDL_DestroyTexture(sdlTexture);
            sdlTexture = null;
        }
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return null;
        }

        int[] argb = new int[w * h];
        image.getRGB(0, 0, w, h, argb, 0, w);

        byte[] rgbaBytes = new byte[w * h * 4];
        for (int i = 0; i < argb.length; i++) {
            int pixel = argb[i];
            rgbaBytes[i * 4]     = (byte) ((pixel >> 16) & 0xFF); // Red -> Byte 0
            rgbaBytes[i * 4 + 1] = (byte) ((pixel >> 8) & 0xFF);  // Green -> Byte 1
            rgbaBytes[i * 4 + 2] = (byte) (pixel & 0xFF);         // Blue -> Byte 2
            rgbaBytes[i * 4 + 3] = (byte) ((pixel >> 24) & 0xFF); // Alpha -> Byte 3
        }

        try (Memory mem = new Memory(rgbaBytes.length)) {
            mem.write(0, rgbaBytes, 0, rgbaBytes.length);

            Pointer surface = SDL3Library.INSTANCE.SDL_CreateSurfaceFrom(
                    w, h, SDL3Library.SDL_PIXELFORMAT_ABGR8888, mem, w * 4
            );
            if (surface == null) {
                return null;
            }

            SDL3Library.INSTANCE.SDL_SetSurfaceBlendMode(surface, SDL3Library.SDL_BLENDMODE_BLEND);
            sdlTexture = SDL3Library.INSTANCE.SDL_CreateTextureFromSurface(renderer, surface);
            SDL3Library.INSTANCE.SDL_DestroySurface(surface);

            if (sdlTexture != null) {
                SDL3Library.INSTANCE.SDL_SetTextureBlendMode(sdlTexture, SDL3Library.SDL_BLENDMODE_BLEND);
            }
            sdlRendererRef = renderer;
            return sdlTexture;
        }
    }

    public synchronized void invalidateSdlTexture() {
        if (sdlTexture != null) {
            mobileapplication3.platform.sdl3.SDL3Library.INSTANCE.SDL_DestroyTexture(sdlTexture);
            sdlTexture = null;
            sdlRendererRef = null;
        }
    }

    public Graphics getGraphics() {
        invalidateSdlTexture();
        return new GraphicsAWT(image.getGraphics());
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        invalidateSdlTexture();
        this.image = image;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height) {
        if (image != null) {
            image.getRGB(0, 0, getWidth(), getHeight(), rgbData, 0, getWidth());
        }
    }

    public Image scale(int newWidth, int newHeight) {
        if (image == null) {
            return null;
        }

        int[] rawInput = new int[image.getHeight() * image.getWidth()];
        getRGB(rawInput, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

        int[] rawOutput = new int[newWidth * newHeight];

        // YD compensates for the x loop by subtracting the width back out
        int YD = (image.getHeight() / newHeight) * image.getWidth() - image.getWidth();
        int YR = image.getHeight() % newHeight;
        int XD = image.getWidth() / newWidth;
        int XR = image.getWidth() % newWidth;
        int outOffset = 0;
        int inOffset = 0;

        for (int y = newHeight, YE = 0; y > 0; y--) {
            for (int x = newWidth, XE = 0; x > 0; x--) {
                rawOutput[outOffset++] = rawInput[inOffset];
                inOffset += XD;
                XE += XR;
                if (XE >= newWidth) {
                    XE -= newWidth;
                    inOffset++;
                }
            }
            inOffset += YD;
            YE += YR;
            if (YE >= newHeight) {
                YE -= newHeight;
                inOffset += image.getWidth();
            }
        }
        return createRGBImage(rawOutput, newWidth, newHeight, true);

    }

    public void blur() {
        blurImg(this);
    }
}
