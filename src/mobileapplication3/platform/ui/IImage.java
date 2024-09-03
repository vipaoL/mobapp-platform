package mobileapplication3.platform.ui;

public interface IImage {

    Graphics getGraphics();

    int getWidth();

    int getHeight();

    void getRGB(int[] rgbData, int offset, int scanlength, int x, int y, int width, int height);

    Image scale(int newWidth, int newHeight);

    void blur();
}
