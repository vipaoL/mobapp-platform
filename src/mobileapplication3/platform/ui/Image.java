package mobileapplication3.platform.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;

import javax.imageio.ImageIO;

public class Image implements IImage {
    private BufferedImage image;

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

    public Graphics getGraphics() {
        return new Graphics(image.getGraphics());
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
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
