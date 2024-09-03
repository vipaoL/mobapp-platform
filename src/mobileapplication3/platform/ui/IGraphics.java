package mobileapplication3.platform.ui;

public interface IGraphics {

    int HCENTER = 1, VCENTER = 2, LEFT = 4, RIGHT = 8, TOP = 16, BOTTOM = 32, BASELINE = 64;

    void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle);

    void drawArrow(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean fill);

    void drawImage(Image img, int x, int y, int anchor);

    void drawLine(int x1, int y1, int x2, int y2);

    void drawLine(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness, boolean zoomThickness, boolean rounding, boolean markSkeleton);

    void drawLine(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness);

    void drawLine(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness, boolean zoomThickness);

    void drawRect(int x, int y, int width, int height);

    void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight);

    void drawString(String str, int x, int y, int anchor);

    void drawSubstring(String str, int offset, int len, int x, int y, int anchor);

    void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, boolean fill);

    void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle);

    void fillRect(int x, int y, int width, int height);

    void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight);

    void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3);

    void setClip(int x, int y, int width, int height);

    void setColor(int red, int green, int blue);

    void setFont(int face, int style, int size);

    Font getFont();

    void setFont(Font font);

    int getFontFace();

    int getFontStyle();

    int getFontSize();

    void setFontSize(int size);

    int getFontHeight();

    int stringWidth(String str);

    int substringWidth(String str, int offset, int len);

    int getFontHeight(int face, int style, int size);

    int getClipWidth();

    int getClipHeight();

    int getClipX();

    int getClipY();

    int getColor();

    void setColor(int RGB);
}
