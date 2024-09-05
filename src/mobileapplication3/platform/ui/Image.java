package mobileapplication3.platform.ui;

import java.io.IOException;
import java.util.Random;

public final class Image implements IImage {
	javax.microedition.lcdui.Image image;
	
	public Image(javax.microedition.lcdui.Image image) {
		this.image = image;
	}

	public static Image createImage(int width, int height) {
		return new Image(javax.microedition.lcdui.Image.createImage(width, height));
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
	
	public javax.microedition.lcdui.Image getImage() {
		return image;
	}

	public int getWidth() {
		return image.getWidth();
	}

	public int getHeight() {
		return image.getHeight();
	}
	
	public Image scale(int newWidth, int newHeight) {
        int[] rawInput = new int[image.getHeight() * image.getWidth()];
        image.getRGB(rawInput, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

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
        rawInput = null;
        return new Image(javax.microedition.lcdui.Image.createRGBImage(rawOutput, newWidth, newHeight, true));

    }

	public static Image createImage(String source) throws IOException {
		return new Image(javax.microedition.lcdui.Image.createImage(source));
	}

	public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height) {
		image.getRGB(rgbData, offset, scanlength, x, y, width, height);
	}

	public void blur() {
		blurImg(this);
	}
}
