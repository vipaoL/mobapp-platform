package mobileapplication3.platform.ui;

import java.awt.*;

public final class Graphics implements IGraphics {
    public static final int HCENTER = 1;
    public static final int VCENTER = 2;
    public static final int LEFT = 4;
    public static final int RIGHT = 8;
    public static final int TOP = 16;
    public static final int BOTTOM = 32;
    public static final int BASELINE = 64;

    private Font currentFont = Font.getDefaultFont();

    private java.awt.Graphics2D g;

    public Graphics(java.awt.Graphics g) {
        if (g == null) {
            throw new NullPointerException();
        }
        this.g = (Graphics2D) g;
        setFont(currentFont);
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        g.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    public void drawImage(Image img, int x, int y, int anchor) {
        if (img == null || img.getImage() == null) {
            throw new NullPointerException("Can't draw null image");
        } else {
            int w = img.getWidth();
            int h = img.getHeight();
            if ((anchor & HCENTER) != 0) {
                x -= w / 2;
            } else if ((anchor & RIGHT) != 0) {
                x -= w;
            }

            if ((anchor & VCENTER) != 0) {
                y -= h / 2;
            } else if ((anchor & BOTTOM) != 0) {
                y -= h;
            }

            g.drawImage(img.getImage(), x, y, null);
        }
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        g.setStroke(new BasicStroke());
        g.drawLine(x1, y1, x2, y2);
    }

    public void drawRect(int x, int y, int width, int height) {
        g.drawRect(x, y, width, height);
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        g.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public void drawString(String str, int x, int y, int anchor) {
        drawSubstring(str, 0, str.length(), x, y, anchor);
    }

    public void drawSubstring(String str, int offset, int len, int x, int y, int anchor) {
        int w = substringWidth(str, offset, len);
        int h = getFontHeight();
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        if ((anchor & HCENTER) != 0) {
            x -= w / 2;
        } else if ((anchor & RIGHT) != 0) {
            x -= w;
        }

        if ((anchor & VCENTER) != 0) {
            y -= h / 2;
        } else if ((anchor & BOTTOM) != 0) {
            y -= h;
        }
        g.drawString(str.substring(offset, offset + len), x, y + metrics.getAscent());
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        g.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    public void fillRect(int x, int y, int width, int height) {
        g.fillRect(x, y, width, height);
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        g.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        g.fillPolygon(new int[]{x1, x2, x3}, new int[]{y1, y2, y3}, 3);
    }

    public void setClip(int x, int y, int width, int height) {
        g.setClip(x, y, width, height);
    }

    public void setColor(int RGB) {
        Color color = new Color(RGB);
        if (color.getRed() > 80 || color.getGreen() > 80 || color.getBlue() > 80) {
            color = Color.WHITE;
        } else {
            color = Color.BLACK;
        }
        g.setColor(color);
    }

    public void setColor(int red, int green, int blue) {
        Color color = new Color(red, green, blue);
        if (color.getRed() > 80 || color.getGreen() > 80 || color.getBlue() > 80) {
            color = Color.WHITE;
        } else {
            color = Color.BLACK;
        }
        g.setColor(color);
    }

    @Override
    public void setFont(int face, int style, int size) {
        setFont(new Font(face, style, size));
    }

    @Override
    public Font getFont() {
        return currentFont;
    }

    @Override
    public void setFont(Font font) {
        currentFont = font;
        g.setFont(currentFont.getAwtFont());
    }

    @Override
    public int getFontFace() {
        return currentFont.getFace();
    }

    @Override
    public int getFontStyle() {
        return currentFont.getStyle();
    }

    @Override
    public int getFontSize() {
        return currentFont.getSize();
    }

    @Override
    public void setFontSize(int size) {
        currentFont = new Font(size);
    }

    @Override
    public int getFontHeight() {
        return currentFont.getHeight();
    }

    @Override
    public int stringWidth(String str) {
        return currentFont.stringWidth(str);
    }

    @Override
    public int substringWidth(String str, int offset, int len) {
        return currentFont.substringWidth(str, offset, len);
    }

    @Override
    public int getFontHeight(int face, int style, int size) {
        return currentFont.getHeight();
    }

    public int getClipWidth() {
        try {
            return g.getClipBounds().width;
        } catch (NullPointerException ex) {
            return RootContainer.getInst().w;
        }
    }

    public int getClipHeight() {
        try {
            return g.getClipBounds().height;
        } catch (NullPointerException ex) {
            return RootContainer.getInst().h;
        }
    }

    public int getClipX() {
        try {
            return g.getClipBounds().x;
        } catch (NullPointerException ex) {
            return 0;
        }
    }

    public int getClipY() {
        try {
            return g.getClipBounds().y;
        } catch (NullPointerException ex) {
            return 0;
        }
    }

    public int getColor() {
        return g.getColor().getRGB();
    }

    public void drawLine(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness) {
        drawLine(x1, y1, x2, y2, thickness, zoomOut, drawThickness, true);
    }

    public void drawLine(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness, boolean zoomThickness) {
        drawLine(x1, y1, x2, y2, thickness, zoomOut, drawThickness, zoomThickness, true, false); // TODO
    }

    public void drawLine(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness, boolean zoomThickness, boolean rounding, boolean markSkeleton) {
        if (drawThickness) {
            g.setStroke(new BasicStroke(thickness * 1000f / zoomOut, rounding ? BasicStroke.CAP_ROUND : BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        } else {
            g.setStroke(new BasicStroke(1));
        }
        g.drawLine(x1, y1, x2, y2);

        if (markSkeleton && drawThickness && thickness * 1000 / zoomOut > 8) {
            int prevCol = getColor();
            setColor(0xff0000);
            g.setStroke(new BasicStroke());
            g.drawLine(x1, y1, x2, y2);
            setColor(prevCol);
        }
    }

    public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, boolean fill) {
        if (!fill) {
            g.drawLine(x1, y1, x2, y2);
            g.drawLine(x2, y2, x3, y3);
            g.drawLine(x1, y1, x3, y3);
        } else {
            fillTriangle(x1, y1, x2, y2, x3, y3);
        }
    }

    public void drawArrow(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int arrowX = (x2*5 + x1) / 6;
        int arrowY = (y2*5 + y1) / 6;
        int arrowSideVecX = dy / 8;
        int arrowSideVecY = -dx / 8;
        drawLine(x1, y1, arrowX, arrowY, thickness, zoomOut, drawThickness, true, false, false);
        drawTriangle(x2, y2, arrowX + arrowSideVecX, arrowY + arrowSideVecY, arrowX - arrowSideVecX, arrowY - arrowSideVecY, drawThickness);
    }
}
