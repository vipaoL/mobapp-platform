// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import org.robovm.apple.coregraphics.*;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.uikit.*;

import java.util.HashMap;
import java.util.Map;

public class Graphics implements IGraphics {
    private static final Map<Integer, UIColor> colorCache = new HashMap<>();
    private CGContext context;
    private Font currentFont;
    private UIColor currentColor = UIColor.black();
    private int currentRGB = 0x000000;
    private CGRect clipBounds;

    private final CGPoint cachedPoint = new CGPoint(0, 0);

    public Graphics(CGContext c) {
        context = c;
        currentFont = new Font();
        if (context != null) {
            context.saveGState();
            context.setInterpolationQuality(CGInterpolationQuality.None);
            clipBounds = c.getClipBoundingBox();
        }
    }

    public Graphics(Image img) {
        UIGraphics.beginImageContext(img.getImage().getSize());
        img.getImage().draw(new CGPoint(0, 0));
        context = UIGraphics.getCurrentContext();
        if (context != null) {
            context.setInterpolationQuality(CGInterpolationQuality.None);
        }
        currentFont = new Font();
        context.saveGState();
        clipBounds = context.getClipBoundingBox();
    }

    public void setContext(CGContext c) {
        this.context = c;
        if (c != null) {
            context.saveGState();
            context.setInterpolationQuality(CGInterpolationQuality.None);
            clipBounds = c.getClipBoundingBox();
        }
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        drawArc(x, y, width, height, startAngle, arcAngle, false);
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle, int thickness, int zoomOut, boolean drawThickness, boolean zoomThickness, boolean rounding) {
        float strokeWidth = drawThickness ? (thickness * (zoomThickness ? (1000f / zoomOut) : 1)) : 1;

        double startRad = Math.toRadians(startAngle);
        double endRad = Math.toRadians(startAngle + arcAngle);

        int clockwise = (arcAngle < 0) ? 1 : 0;

        context.saveGState();
        context.translateCTM(x + width / 2.0, y + height / 2.0);
        context.scaleCTM(width / 2.0, height / 2.0);

        context.beginPath();
        context.addArc(0, 0, 1.0, startRad, endRad, clockwise);

        context.restoreGState();

        context.setLineWidth(strokeWidth);
        context.setLineCap(rounding ? CGLineCap.Round : CGLineCap.Butt);
        context.setStrokeColor(currentColor.getCGColor());
        context.strokePath();
    }

    private void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle, boolean fill) {
        double startRad = Math.toRadians(startAngle);
        double endRad = Math.toRadians(startAngle + arcAngle);

        int clockwise = (arcAngle < 0) ? 1 : 0;

        context.saveGState();
        context.translateCTM(x + width / 2.0, y + height / 2.0);
        context.scaleCTM(width / 2.0, height / 2.0);

        context.beginPath();
        context.addArc(0, 0, 1.0, startRad, endRad, clockwise);

        if (fill) {
            context.addLineToPoint(0, 0);
            context.closePath();
            context.restoreGState();
            context.setFillColor(currentColor.getCGColor());
            context.fillPath();
        } else {
            context.restoreGState();
            context.setLineWidth(1);
            context.setStrokeColor(currentColor.getCGColor());
            context.strokePath();
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

            UIGraphics.pushContext(context);
            cachedPoint.setX(x);
            cachedPoint.setY(y);
            img.getImage().draw(cachedPoint);
            UIGraphics.popContext();
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
        float w = 1;
        if (drawThickness) {
            w = zoomThickness ? (thickness * 1000f / zoomOut) : thickness;
        }

        context.setLineWidth(w);
        context.setLineCap(rounding ? CGLineCap.Round : CGLineCap.Butt);
        context.setStrokeColor(currentColor.getCGColor());

        context.beginPath();
        context.moveToPoint(x1 + 0.5, y1 + 0.5);
        context.addLineToPoint(x2 + 0.5, y2 + 0.5);
        context.strokePath();

        if (markSkeleton && drawThickness && w > 8) {
            int prevCol = getColor();
            setColor(0xff0000);
            context.setLineWidth(1);
            context.setLineCap(CGLineCap.Butt);
            context.setStrokeColor(currentColor.getCGColor());

            context.beginPath();
            context.moveToPoint(x1 + 0.5, y1 + 0.5);
            context.addLineToPoint(x2 + 0.5, y2 + 0.5);
            context.strokePath();

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
        if (arcWidth <= 0 && arcHeight <= 0) {
            context.beginPath();
            context.moveToPoint(x + 0.5, y + 0.5);
            context.addLineToPoint(x + width + 0.5, y + 0.5);
            context.addLineToPoint(x + width + 0.5, y + height + 0.5);
            context.addLineToPoint(x + 0.5, y + height + 0.5);
            context.closePath();
        } else {
            CGRect rect = new CGRect(x + 0.5, y + 0.5, width, height);

            double cornerWidth = Math.min(arcWidth / 2.0, width / 2.0);
            double cornerHeight = Math.min(arcHeight / 2.0, height / 2.0);

            CGPath path = CGPath.createWithRoundedRect(rect, cornerWidth, cornerHeight, null);

            context.beginPath();
            context.addPath(path);

        }
        if (fill) {
            context.setFillColor(currentColor.getCGColor());
            context.fillPath();
        } else {
            context.setLineWidth(1);
            context.setStrokeColor(currentColor.getCGColor());
            context.strokePath();
        }
    }

    @Override
    public void drawString(String str, int x, int y, int anchor) {
        drawSubstring(str, 0, str.length(), x, y, anchor);
    }

    @Override
    public void drawSubstring(String str, int offset, int len, int x, int y, int anchor) {
        if (str == null || len <= 0) {
            return;
        }

        NSString nsStr = NSStringCache.get(str, offset, len);

        if (anchor != 0) {
            CGSize size = nsStr.getSize(currentFont.getAttributes());
            int w = (int) size.getWidth();
            int h = (int) size.getHeight();

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
        }

        UIGraphics.pushContext(context);
        currentFont.getAttributes().setForegroundColor(currentColor);

        cachedPoint.setX(x);
        cachedPoint.setY(y);
        nsStr.draw(cachedPoint, currentFont.getAttributes());

        UIGraphics.popContext();
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
        context.saveGState();
        context.setShouldAntialias(false);

        context.beginPath();
        context.moveToPoint(x1, y1);
        context.addLineToPoint(x2, y2);
        context.addLineToPoint(x3, y3);
        context.closePath();

        context.setFillColor(currentColor.getCGColor());
        context.fillPath();

        context.restoreGState();
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        context.restoreGState();
        context.saveGState();
        clipBounds = new CGRect(x, y, width, height);
        context.clipToRect(clipBounds);
    }

    @Override
    public void setColor(int red, int green, int blue) {
        setColor((red << 16) | (green << 8) | blue);
    }

    @Override
    public void setFont(int face, int style, int size) {
        currentFont = Font.getFont(face, style, size);
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
        return (int) clipBounds.getWidth();
    }

    @Override
    public int getClipHeight() {
        return (int) clipBounds.getHeight();
    }

    @Override
    public int getClipX() {
        return (int) clipBounds.getMinX();
    }

    @Override
    public int getClipY() {
        return (int) clipBounds.getMinY();
    }

    @Override
    public int getColor() {
        return currentRGB;
    }

    @Override
    public void setColor(int RGB) {
        if (this.currentRGB == RGB) {
            return;
        }
        currentRGB = RGB;

        UIColor color = colorCache.get(/*TODO*/RGB/**/);
        if (color == null) {
            int red = (RGB >> 16) & 0xFF;
            int green = (RGB >> 8) & 0xFF;
            int blue = RGB & 0xFF;
            color = UIColor.fromRGBA(red / 255f, green / 255f, blue / 255f, 1.0f);
            colorCache.put(RGB, color);
        }
        currentColor = color;
    }
}
