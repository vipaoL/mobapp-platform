package mobileapplication3.platform.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import mobileapplication3.platform.Platform;

public class Image implements IImage {
    private Bitmap image;

    public Image(Bitmap image) {
        if (image == null) {
            Log.d("new Image", "null");
        }
        this.image = image;
    }

    public static Image createImage(int width, int height) {
        return new Image(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888));
    }

    public static Image createRGBImage(int[] rgb, int width, int height, boolean processAlpha) {
        return new Image(Bitmap.createBitmap(rgb, width, height, processAlpha ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565));
    }

    public static Image createImage(String source) throws IOException {
        InputStream is = Platform.getResource(source);
        if (is == null) {
            return null;
        }
        return new Image(BitmapFactory.decodeStream(is));
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

    public Graphics getGraphics() {
        return new Graphics(new Canvas(image));
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
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
            image.getPixels(rgbData, offset, scanlength, x, y, width, height);
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
