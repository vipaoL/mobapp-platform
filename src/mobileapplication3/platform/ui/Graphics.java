package mobileapplication3.platform.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;

public class Graphics implements IGraphics {

    private final Canvas c;
    private final Paint p;
    private Font currentFont;

    public Graphics(Canvas c) {
        c.save();
        this.c = c;
        p = new Paint();
        p.setStrokeWidth(1);
        p.setTextSize(88);
        currentFont = new Font();
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        drawArc(x, y, width, height, startAngle, arcAngle, false);
    }

    private void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle, boolean fill) {
        if (fill) {
            p.setStyle(Paint.Style.FILL_AND_STROKE);
        } else {
            p.setStyle(Paint.Style.STROKE);
        }
        p.setStrokeWidth(1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            c.drawArc(x, y, x + width, y + height, startAngle, arcAngle, false, p);
        } else {
            c.drawCircle(x + width / 2f, y + height / 2f, width / 2f, p);
            c.drawCircle(x + width / 2f, y + height / 2f, height / 2f, p);
        }
    }

    @Override
    public void drawArrow(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int arrowX = (x2 * 5 + x1) / 6;
        int arrowY = (y2 * 5 + y1) / 6;
        int arrowSideVecX = dy / 8;
        int arrowSideVecY = -dx / 8;
        drawLine(x1, y1, arrowX, arrowY, thickness, zoomOut, drawThickness, true, false, false);
        drawTriangle(x2, y2, arrowX + arrowSideVecX, arrowY + arrowSideVecY, arrowX - arrowSideVecX, arrowY - arrowSideVecY, drawThickness);
    }

    @Override
    public void drawImage(Image img, int x, int y, int anchor) {
        if (img != null && img.getImage() != null) {
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

            c.drawBitmap(img.getImage(), x, y, p);
        }
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        drawLine(x1, y1, x2, y2, 1, 1000, false, false, true, false);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness) {
        drawLine(x1, y1, x2, y2, thickness, zoomOut, drawThickness, true, true, false);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness, boolean zoomThickness) {
        drawLine(x1, y1, x2, y2, thickness, zoomOut, drawThickness, zoomThickness, true, false);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness, boolean zoomThickness, boolean rounding, boolean markSkeleton) {
        p.setStrokeCap(rounding ? Paint.Cap.ROUND : Paint.Cap.BUTT);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        if (drawThickness) {
            if (zoomThickness) {
                p.setStrokeWidth(thickness * 1000f / zoomOut);
            } else {
                p.setStrokeWidth(thickness);
            }
        } else {
            p.setStrokeWidth(1);
        }

        float startX = x1 + 0.5f;
        float startY = y1 + 0.5f;
        float stopX = x2 + 0.5f;
        float stopY = y2 + 0.5f;

        c.drawLine(startX, startY, stopX, stopY, p);

        if (markSkeleton && drawThickness && thickness * 1000 / zoomOut > 8) {
            int prevCol = getColor();
            setColor(0xff0000);
            p.setStrokeWidth(1);
            p.setStrokeCap(Paint.Cap.BUTT);
            c.drawLine(startX, startY, stopX, stopY, p);
            setColor(prevCol);
        }
    }

    @Override
    public void drawRect(int x, int y, int width, int height) {
        drawRoundRect(x, y, width, height, 0, 0, false);
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        drawRoundRect(x, y, width, height, arcWidth, arcHeight, false);
    }

    private void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight, boolean fill) {
        if (fill) {
            p.setStyle(Paint.Style.FILL_AND_STROKE);
        } else {
            p.setStyle(Paint.Style.STROKE);
        }
        p.setStrokeWidth(1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && arcWidth > 0 && arcHeight > 0) {
            c.drawRoundRect(x + 0.5f, y + 0.5f, x + width + 0.5f, y + height + 0.5f, arcWidth / 2f, arcHeight / 2f, p);
        } else {
            c.drawRect(x + 0.5f, y + 0.5f, x + width + 0.5f, y + height + 0.5f, p);
        }
    }

    @Override
    public void drawString(String str, int x, int y, int anchor) {
        drawSubstring(str, 0, str.length(), x, y, anchor);
    }

    @Override
    public void drawSubstring(String str, int offset, int len, int x, int y, int anchor) {
        Paint p = currentFont.getPaint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(this.p.getColor());
        int textW = substringWidth(str, offset, len);
        int textH = (int) (p.descent() - p.ascent());
        y -= (int) ((p.descent() + p.ascent()) / 2);
        y += textH / 2;
        if ((anchor & HCENTER) != 0) {
            x -= textW / 2;
        } else if ((anchor & RIGHT) != 0) {
            x -= textW;
        }
        if ((anchor & VCENTER) != 0) {
            y -= textH / 2;
        } else if ((anchor & BOTTOM) != 0) {
            y -= textH;
        }
        c.drawText(str, offset, offset + len, x, y, p);
    }

    @Override
    public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, boolean fill) {
        if (!fill) {
            drawLine(x1, y1, x2, y2);
            drawLine(x2, y2, x3, y3);
            drawLine(x1, y1, x3, y3);
        } else {
            fillTriangle(x1, y1, x2, y2, x3, y3);
        }
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        drawArc(x, y, width, height, startAngle, arcAngle, true);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        drawRoundRect(x, y, width, height, 0, 0, true);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        drawRoundRect(x, y, width, height, arcWidth, arcHeight, true);
    }

    @Override
    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setStrokeWidth(1);

        Path path = new Path();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.lineTo(x1, y1);
        path.close();

        c.drawPath(path, p);
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        try {
            c.restore();
        } catch (IllegalStateException ignored) { }
        c.save();
        c.clipRect(x, y, x + width, y + height);
    }

    @Override
    public void setColor(int red, int green, int blue) {
        p.setARGB(255, red, green, blue);
    }

    @Override
    public void setFont(int face, int style, int size) {
        currentFont = new Font(face, style, size);
    }

    @Override
    public Font getFont() {
        return currentFont;
    }

    @Override
    public void setFont(Font font) {
        currentFont = font;
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

    @Override
    public int getClipWidth() {
        return c.getClipBounds().width();
    }

    @Override
    public int getClipHeight() {
        return c.getClipBounds().height();
    }

    @Override
    public int getClipX() {
        return c.getClipBounds().left;
    }

    @Override
    public int getClipY() {
        return c.getClipBounds().top;
    }

    @Override
    public int getColor() {
        return p.getColor() % 0xff000000;
    }

    @Override
    public void setColor(int RGB) {
        p.setColor(RGB + 0xff000000);
    }
}
