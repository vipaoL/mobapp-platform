// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import mobileapplication3.platform.Logger;

import org.robovm.apple.coregraphics.*;
import org.robovm.apple.foundation.NSData;
import org.robovm.apple.uikit.UIImage;
import org.robovm.apple.uikit.UIGraphics;
import mobileapplication3.platform.Platform;

import java.io.InputStream;
import java.io.IOException;
import java.util.Random;

public class Image implements IImage {
    private UIImage image;

    public Image(UIImage image) {
        if (image == null) {
            Logger.log("got null image");
        }
        this.image = image;
    }

    public static Image createImage(int width, int height) {
        UIGraphics.beginImageContext(new CGSize(width, height));
        UIImage img = UIGraphics.getImageFromCurrentImageContext();
        UIGraphics.endImageContext();
        return new Image(img);
    }

    public static Image createRGBImage(int[] rgb, int width, int height, boolean processAlpha) {
        byte[] bytes = new byte[width * height * 4];
        int i = 0;
        for (int c : rgb) {
            bytes[i++] = (byte) ((c >> 16) & 0xFF); // R
            bytes[i++] = (byte) ((c >> 8) & 0xFF);  // G
            bytes[i++] = (byte) (c & 0xFF);         // B
            bytes[i++] = (byte) (processAlpha ? ((c >> 24) & 0xFF) : 0xFF); // A
        }
        CGDataProvider provider = CGDataProvider.create(new NSData(bytes));
        CGColorSpace colorSpace = CGColorSpace.createDeviceRGB();
        CGBitmapInfo info = new CGBitmapInfo(CGImageAlphaInfo.PremultipliedLast.value());
        CGImage cgImage = CGImage.create(width, height, 8, 32, width * 4, colorSpace, info, provider, null, false, CGColorRenderingIntent.Default);
        return new Image(new UIImage(cgImage));
    }

    public static Image createImage(String source) throws IOException {
        try {
            Logger.log("reading resourse \"" + source + "\"");
            InputStream is = Platform.getResource(source);
            if (is == null) {
                return null;
            }
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            is.close();
            return new Image(new UIImage(new NSData(bytes)));
        } catch (Exception ex) {
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
            Logger.log(ex);
        }
    }

    public Graphics getGraphics() {
        return new Graphics(this);
    }

    public UIImage getImage() {
        return image;
    }

    public int getWidth() {
        return (int) image.getSize().getWidth();
    }

    public int getHeight() {
        return (int) image.getSize().getHeight();
    }

    public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height) {
        if (image == null || image.getCGImage() == null) {
            return;
        }

        CGImage cgImage = image.getCGImage();
        byte[] bytes = cgImage.getDataProvider().getData().getBytes();
        long bytesPerRow = cgImage.getBytesPerRow();
        long bpp = cgImage.getBitsPerPixel() / 8;

        for(int r = 0; r < height; r++) {
            for(int c = 0; c < width; c++) {
                int imgX = x + c;
                int imgY = y + r;
                int idx = (int)(imgY * bytesPerRow + imgX * bpp);

                int red = bytes[idx] & 0xFF;
                int green = bytes[idx+1] & 0xFF;
                int blue = bytes[idx+2] & 0xFF;
                int alpha = (bpp == 4) ? (bytes[idx+3] & 0xFF) : 255;
                rgbData[offset + r * scanlength + c] = (alpha << 24) | (red << 16) | (green << 8) | blue;
            }
        }
    }

    public Image scale(int newWidth, int newHeight) {
        UIGraphics.beginImageContext(new CGSize(newWidth, newHeight));

        CGContext ctx = UIGraphics.getCurrentContext();
        if (ctx != null) {
            ctx.setInterpolationQuality(CGInterpolationQuality.None);
        }

        image.draw(new CGRect(0, 0, newWidth, newHeight));
        UIImage scaled = UIGraphics.getImageFromCurrentImageContext();
        UIGraphics.endImageContext();
        return new Image(scaled);
    }

    public void blur() {
        blurImg(this);
    }

    public void setImage(UIImage image) {
        this.image = image;
    }
}
