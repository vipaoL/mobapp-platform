// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import mobileapplication3.platform.Mathh;

public final class Graphics implements IGraphics {
    public static final int HCENTER = 1;
    public static final int VCENTER = 2;
    public static final int LEFT = 4;
    public static final int RIGHT = 8;
    public static final int TOP = 16;
    public static final int BOTTOM = 32;
    public static final int BASELINE = 64;

    private javax.microedition.lcdui.Graphics g;

    private int[] pxBuf = new int[32];
    private int[] pyBuf = new int[32];

    public Graphics(javax.microedition.lcdui.Graphics g) {
        this.g = g;
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        drawArc(x, y, width, height, startAngle, arcAngle, 1, 1000, false, false, false);
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle, int thickness, int zoomOut, boolean drawThickness, boolean zoomThickness, boolean rounding) {
        startAngle = -startAngle;
        arcAngle = -arcAngle;
        if (arcAngle < 0) {
            startAngle += arcAngle;
            arcAngle = -arcAngle;
        }

        startAngle %= 360;

        if (startAngle + arcAngle >= 360) {
            startAngle -= 360;
        }

        g.drawArc(x, y, width, height, startAngle, arcAngle);
        if (drawThickness) {
            int n = thickness * 1000 / zoomOut;
            for (int i = 0; i < n - 1; i++) {
                int d = 1 + i / 2;
                if (i % 2 == 0) {
                    d = -d;
                }
                g.drawArc(x + d, y + d, width - 2*d, height - 2*d, startAngle, arcAngle);
            }
        }
    }

    public void drawImage(Image img, int x, int y, int anchor) {
        g.drawImage(img.getImage(), x, y, anchor);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        g.drawLine(x1, y1, x2, y2);
    }

    public void drawRect(int x, int y, int width, int height) {
        g.drawRect(x, y, width, height);
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        g.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public void drawString(String str, int x, int y, int anchor) {
        if ((anchor & VCENTER) != 0) {
            anchor -= VCENTER;
            anchor |= TOP;
            y -= getFontHeight() / 2;
        }
        g.drawString(str, x, y, anchor);
    }

    public void drawSubstring(String str, int offset, int len, int x, int y, int anchor) {
        if ((anchor & VCENTER) != 0) {
            anchor -= VCENTER;
            anchor |= TOP;
            y -= getFontHeight() / 2;
        }
        g.drawSubstring(str, offset, len, x, y, anchor);
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        startAngle = -startAngle;
        arcAngle = -arcAngle;
        if (arcAngle < 0) {
            startAngle += arcAngle;
            arcAngle = -arcAngle;
        }
        while (startAngle < 0) {
            startAngle += 360;
        }
        g.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    public void fillRect(int x, int y, int width, int height) {
        g.fillRect(x, y, width, height);
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        g.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        g.fillTriangle(x1, y1, x2, y2, x3, y3);
    }

    public void setClip(int x, int y, int width, int height) {
        if (width > 0 && height > 0) {
            g.setClip(x, y, width, height);
        }
    }

    public void setColor(int RGB) {
        g.setColor(RGB);
    }

    public void setColor(int red, int green, int blue) {
        g.setColor(red, green, blue);
    }

    public void setFontSize(int size) {
        g.setFont(javax.microedition.lcdui.Font.getFont(getFontFace(), getFontStyle(), size));
    }

    public void setFont(int face, int style, int size) {
        g.setFont(javax.microedition.lcdui.Font.getFont(face, style, size));
    }

    public void setFont(Font font) {
        g.setFont(font.getFont());
    }

    public Font getFont() {
        return new Font(g.getFont());
    }

    public int getFontFace() {
        return g.getFont().getFace();
    }

    public int getFontStyle() {
        return g.getFont().getStyle();
    }

    public int getFontSize() {
        return g.getFont().getSize();
    }

    public int getFontHeight() {
        return g.getFont().getHeight();
    }

    public int stringWidth(String str) {
        return g.getFont().stringWidth(str);
    }

    public int substringWidth(String str, int offset, int len) {
        return g.getFont().substringWidth(str, offset, len);
    }

    public int getFontHeight(int face, int style, int size) {
        return javax.microedition.lcdui.Font.getFont(face, style, size).getHeight();
    }

    public int getClipWidth() {
        return g.getClipWidth();
    }

    public int getClipHeight() {
        return g.getClipHeight();
    }

    public int getClipX() {
        return g.getClipX();
    }

    public int getClipY() {
        return g.getClipY();
    }

    public int getColor() {
        return g.getColor();
    }

    public void drawLine(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness) {
        drawLine(x1, y1, x2, y2, thickness, zoomOut, drawThickness, true);
    }

    public void drawLine(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness, boolean zoomThickness) {
        drawLine(x1, y1, x2, y2, thickness, zoomOut, drawThickness, zoomThickness, true, false); // TODO
    }

    public void drawLine(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness, boolean zoomThickness, boolean rounding, boolean markSkeleton) {
        if (thickness > 0) {
            int dx = x2 - x1;
            int dy = y2 - y1;
            int l = (int) Math.sqrt(dx*dx+dy*dy);

            if (l == 0 || !drawThickness) {
                g.drawLine(x1, y1, x2, y2);
                return;
            }

            // normal vector
            int nx = dy*thickness * 500 / zoomOut / l;
            int ny = dx*thickness * 500 / zoomOut / l;

            if (nx == 0 && ny == 0) {
                g.drawLine(x1, y1, x2, y2);
                return;
            }

            // draw bold line with two triangles (splitting by diagonal)
            g.fillTriangle(x1-nx, y1+ny, x2-nx, y2+ny, x1+nx, y1-ny);
            g.fillTriangle(x2-nx, y2+ny, x2+nx, y2-ny, x1+nx, y1-ny);
            if (rounding) {
                int r = thickness * 400 / zoomOut;
                int d = thickness * 500 / zoomOut;
                if (r > 0) {
                    g.fillArc(x1-r + 1, y1-r + 1, d, d, 0, 360);
                    g.fillArc(x2-r + 1, y2-r + 1, d, d, 0, 360);
                }
            }
            if (markSkeleton && thickness * 1000 / zoomOut > 8) {
                int prevCol = g.getColor();
                g.setColor(0xff0000);
                g.drawLine(x1, y1, x2, y2);
                g.setColor(prevCol);
            }
        } else {
            g.drawLine(x1, y1, x2, y2);
        }
    }

    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints, int thickness, int zoomOut, boolean drawThickness, boolean zoomThickness, boolean roundCaps) {
        if (nPoints < 2) {
            return;
        }

        int effThickness = Mathh.FP_ONE;
        if (drawThickness) {
            if (zoomThickness) {
                effThickness = (int) (Mathh.toFP(thickness) * 1000L / zoomOut);
            } else {
                effThickness = Mathh.toFP(thickness);
            }
        }

        if (effThickness <= (Mathh.FP_ONE * 3 / 2)) {
            for (int i = 0; i < nPoints - 1; i++) {
                g.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
            }
            return;
        }

        if (pxBuf.length < nPoints) {
            pxBuf = new int[nPoints];
            pyBuf = new int[nPoints];
        }
        int[] px = pxBuf;
        int[] py = pyBuf;

        int validPoints = 0;
        for (int i = 0; i < nPoints; i++) {
            if (validPoints > 0) {
                int dx = xPoints[i] - px[validPoints - 1];
                int dy = yPoints[i] - py[validPoints - 1];
                if (dx == 0 && dy == 0) {
                    continue;
                }
            }
            px[validPoints] = xPoints[i];
            py[validPoints] = yPoints[i];
            validPoints++;
        }

        if (validPoints < 2) {
            return;
        }

        int radius = effThickness / 2;
        long rSq = (long) radius * radius;
        int numSegs = validPoints - 1;

        long fdx0 = Mathh.toFP(px[1] - px[0]);
        long fdy0 = Mathh.toFP(py[1] - py[0]);
        long len0 = Mathh.isqrt(fdx0 * fdx0 + fdy0 * fdy0);
        int curNx = (len0 > 0) ? (int) ((-fdy0 * radius) / len0) : 0;
        int curNy = (len0 > 0) ? (int) (( fdx0 * radius) / len0) : radius;

        for (int i = 0; i < numSegs; i++) {
            g.drawLine(px[i], py[i], px[i + 1], py[i + 1]);

            int cx0 = Mathh.toFP(px[i]);
            int cy0 = Mathh.toFP(py[i]);
            int cx1 = Mathh.toFP(px[i + 1]);
            int cy1 = Mathh.toFP(py[i + 1]);

            int x0 = Mathh.fpRound(cx0 - curNx);
            int y0 = Mathh.fpRound(cy0 - curNy);
            int x1 = Mathh.fpRound(cx0 + curNx);
            int y1 = Mathh.fpRound(cy0 + curNy);
            int x2 = Mathh.fpRound(cx1 + curNx);
            int y2 = Mathh.fpRound(cy1 + curNy);
            int x3 = Mathh.fpRound(cx1 - curNx);
            int y3 = Mathh.fpRound(cy1 - curNy);

            g.fillTriangle(x0, y0, x1, y1, x2, y2);
            g.fillTriangle(x0, y0, x2, y2, x3, y3);

            if (i < numSegs - 1) {
                long fdx1 = Mathh.toFP(px[i + 2] - px[i + 1]);
                long fdy1 = Mathh.toFP(py[i + 2] - py[i + 1]);
                long len1 = Mathh.isqrt(fdx1 * fdx1 + fdy1 * fdy1);
                int nextNx = (len1 > 0) ? (int) ((-fdy1 * radius) / len1) : 0;
                int nextNy = (len1 > 0) ? (int) (( fdx1 * radius) / len1) : radius;

                int dx1 = px[i + 1] - px[i];
                int dy1 = py[i + 1] - py[i];
                int dx2 = px[i + 2] - px[i + 1];
                int dy2 = py[i + 2] - py[i + 1];

                long cross = (long) dx1 * dy2 - (long) dy1 * dx2;
                if (cross != 0) {
                    int outX1, outY1, outX2, outY2;
                    if (cross > 0) {
                        outX1 = -curNx;
                        outY1 = -curNy;
                        outX2 = -nextNx;
                        outY2 = -nextNy;
                    } else {
                        outX1 = curNx;
                        outY1 = curNy;
                        outX2 = nextNx;
                        outY2 = nextNy;
                    }

                    int sumX = outX1 + outX2;
                    int sumY = outY1 + outY2;
                    long sumLenSq = (long) sumX * sumX + (long) sumY * sumY;

                    int cx = Mathh.toFP(px[i + 1]);
                    int cy = Mathh.toFP(py[i + 1]);

                    int sx = Mathh.fpRound(cx + outX1);
                    int sy = Mathh.fpRound(cy + outY1);
                    int ex = Mathh.fpRound(cx + outX2);
                    int ey = Mathh.fpRound(cy + outY2);

                    if (sumLenSq > 0 && (sumLenSq * 5 >= rSq * 4)) {
                        int mX = (int) ((2L * rSq * sumX) / sumLenSq);
                        int mY = (int) ((2L * rSq * sumY) / sumLenSq);

                        int mx = Mathh.fpRound(cx + mX);
                        int my = Mathh.fpRound(cy + mY);

                        g.fillTriangle(px[i + 1], py[i + 1], sx, sy, mx, my);
                        g.fillTriangle(px[i + 1], py[i + 1], mx, my, ex, ey);
                    } else {
                        g.fillTriangle(px[i + 1], py[i + 1], sx, sy, ex, ey);
                    }
                }

                curNx = nextNx;
                curNy = nextNy;
            }
        }

        if (roundCaps) {
            int d = Mathh.fpRound(effThickness);
            int r = d / 2;
            g.fillArc(px[0] - r, py[0] - r, d, d, 0, 360);
            g.fillArc(px[numSegs] - r, py[numSegs] - r, d, d, 0, 360);
        }
    }

    public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, boolean fill) {
        if (!fill) {
            g.drawLine(x1, y1, x2, y2);
            g.drawLine(x2, y2, x3, y3);
            g.drawLine(x1, y1, x3, y3);
        } else {
            g.fillTriangle(x1, y1, x2, y2, x3, y3);
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
