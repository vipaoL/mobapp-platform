// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui.sdl3;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import mobileapplication3.platform.sdl3.SDL3Library;
import mobileapplication3.platform.ui.Font;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.Image;

import java.awt.*;

public class GraphicsSDL3 extends Graphics {
    private final RendererTargetSDL3 sdlTarget;
    private final TextTextureCacheSDL3 textCache;

    private Font currentFont = Font.getDefaultFont();
    private int currentColorRGB = 0x000000;
    private float currentR = 0, currentG = 0, currentB = 0, currentA = 1f;

    private final SDL3Library.SDL_FRect sharedFRect = new SDL3Library.SDL_FRect();
    private final SDL3Library.SDL_Rect sharedRect = new SDL3Library.SDL_Rect();

    private static final int MAX_VERTICES = 2048;
    private final Memory vertexBuffer = new Memory(MAX_VERTICES * 32L);
    private final Memory sharedPointsBuffer = new Memory(MAX_VERTICES * 8L);

    private final int[] indicesBuffer = new int[MAX_VERTICES * 3];
    private static final int[] TRIANGLE_INDICES = new int[]{0, 1, 2};

    public GraphicsSDL3(RendererTargetSDL3 sdlTarget, TextTextureCacheSDL3 textCache) {
        if (sdlTarget == null) {
            throw new NullPointerException("sdlTarget is null");
        }
        this.sdlTarget = sdlTarget;
        this.textCache = textCache;
        setFont(currentFont);
    }

    @Override
    public void setColor(int RGB) {
        int a = 255;
        int r = (RGB >> 16) & 0xFF;
        int g = (RGB >> 8) & 0xFF;
        int b = RGB & 0xFF;

        if (blackAndWhiteMode) {
            if (r * 3 + g * 6 + b > 500) {
                r = g = b = 255;
            } else {
                r = g = b = 0;
            }
        }

        int currentColorRGB = (a << 24) | (r << 16) | (g << 8) | b;
        if (this.currentColorRGB == currentColorRGB) {
            return;
        }
        this.currentColorRGB = currentColorRGB;
        currentR = r / 255.0f;
        currentG = g / 255.0f;
        currentB = b / 255.0f;
        currentA = a / 255.0f;

        SDL3Library.INSTANCE.SDL_SetRenderDrawColor(sdlTarget.getRenderer(), (byte) r, (byte) g, (byte) b, (byte) a);
    }

    @Override
    public void setColor(int red, int green, int blue) {
        setColor((255 << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF));
    }

    @Override
    public int getColor() {
        return currentColorRGB;
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        SDL3Library.INSTANCE.SDL_RenderLine(sdlTarget.getRenderer(), x1, y1, x2, y2);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness) {
        drawLine(x1, y1, x2, y2, thickness, zoomOut, drawThickness, false, false, false);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness, boolean zoomThickness) {
        drawLine(x1, y1, x2, y2, thickness, zoomOut, drawThickness, zoomThickness, true, false); // TODO
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean drawThickness, boolean zoomThickness, boolean rounding, boolean markSkeleton) {
        float effThickness = 1f;
        if (drawThickness) {
            if (zoomOut < 0) {
                zoomOut = -zoomOut;
            }
            effThickness = thickness * (zoomThickness ? (1000f / zoomOut) : 1f);
        }

        if (effThickness <= 1f) {
            SDL3Library.INSTANCE.SDL_RenderLine(sdlTarget.getRenderer(), x1, y1, x2, y2);
        } else {
            float dx = x2 - x1;
            float dy = y2 - y1;
            float len = (float) Math.hypot(dx, dy);

            if (len > 0) {
                float radius = effThickness / 2.0f;
                float nx = -dy / len * radius;
                float ny =  dx / len * radius;

                int vIdx;
                int iIdx = 0;

                writeVertex(vertexBuffer, 0, x1 - nx, y1 - ny);
                writeVertex(vertexBuffer, 1, x1 + nx, y1 + ny);
                writeVertex(vertexBuffer, 2, x2 + nx, y2 + ny);
                writeVertex(vertexBuffer, 3, x2 - nx, y2 - ny);
                vIdx = 4;

                indicesBuffer[iIdx++] = 0;
                indicesBuffer[iIdx++] = 1;
                indicesBuffer[iIdx++] = 2;

                indicesBuffer[iIdx++] = 0;
                indicesBuffer[iIdx++] = 2;
                indicesBuffer[iIdx++] = 3;

                if (rounding) {
                    int capSegs = 6;
                    double lineAngle = Math.atan2(dy, dx);

                    int c1Center = vIdx++;
                    writeVertex(vertexBuffer, c1Center, x1, y1);

                    int prevC1 = 1;
                    for (int i = 1; i <= capSegs; i++) {
                        int currC1;
                        if (i == capSegs) {
                            currC1 = 0;
                        } else {
                            currC1 = vIdx++;
                            double a = (lineAngle + Math.PI / 2) + Math.PI * ((double) i / capSegs);
                            writeVertex(vertexBuffer, currC1, (float) (x1 + radius * Math.cos(a)), (float) (y1 + radius * Math.sin(a)));
                        }
                        indicesBuffer[iIdx++] = c1Center;
                        indicesBuffer[iIdx++] = prevC1;
                        indicesBuffer[iIdx++] = currC1;
                        prevC1 = currC1;
                    }

                    int c2Center = vIdx++;
                    writeVertex(vertexBuffer, c2Center, x2, y2);

                    int prevC2 = 2;
                    for (int i = 1; i <= capSegs; i++) {
                        int currC2;
                        if (i == capSegs) {
                            currC2 = 3;
                        } else {
                            currC2 = vIdx++;
                            double a = (lineAngle + Math.PI / 2) - Math.PI * ((double) i / capSegs);
                            writeVertex(vertexBuffer, currC2, (float) (x2 + radius * Math.cos(a)), (float) (y2 + radius * Math.sin(a)));
                        }
                        indicesBuffer[iIdx++] = c2Center;
                        indicesBuffer[iIdx++] = prevC2;
                        indicesBuffer[iIdx++] = currC2;
                        prevC2 = currC2;
                    }
                }

                SDL3Library.INSTANCE.SDL_RenderGeometry(
                        sdlTarget.getRenderer(),
                        null,
                        vertexBuffer,
                        vIdx,
                        indicesBuffer,
                        iIdx
                );
            } else {
                if (rounding) {
                    int r = Math.round(effThickness / 2f);
                    int d = Math.round(effThickness);
                    fillArc(x1 - r, y1 - r, d, d, 0, 360);
                } else {
                    SDL3Library.INSTANCE.SDL_RenderLine(sdlTarget.getRenderer(), x1, y1, x2, y2);
                }
            }
        }

        if (markSkeleton && drawThickness && thickness * 1000 / zoomOut > 8) {
            int prevCol = getColor();
            setColor(0xff0000);
            SDL3Library.INSTANCE.SDL_RenderLine(sdlTarget.getRenderer(), x1, y1, x2, y2);
            setColor(prevCol);
        }
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints, int thickness, int zoomOut, boolean drawThickness, boolean zoomThickness, boolean roundCaps) {
        if (nPoints < 2) {
            return;
        }

        float effThickness = 1f;
        if (drawThickness) {
            if (zoomOut < 0) {
                zoomOut = -zoomOut;
            }
            effThickness = thickness * (zoomThickness ? (1000f / zoomOut) : 1f);
        }

        if (effThickness <= 1f) {
            int count = Math.min(nPoints, MAX_VERTICES);
            for (int i = 0; i < count; i++) {
                sharedPointsBuffer.setFloat(i * 8L, xPoints[i]);
                sharedPointsBuffer.setFloat(i * 8L + 4, yPoints[i]);
            }
            SDL3Library.INSTANCE.SDL_RenderLines(sdlTarget.getRenderer(), sharedPointsBuffer, count);
            return;
        }

        float[] px = new float[nPoints];
        float[] py = new float[nPoints];
        int validPoints = 0;
        for (int i = 0; i < nPoints; i++) {
            if (validPoints > 0) {
                float dx = xPoints[i] - px[validPoints - 1];
                float dy = yPoints[i] - py[validPoints - 1];
                if (dx * dx + dy * dy < 0.25f) {
                    continue;
                }
            }
            px[validPoints] = xPoints[i];
            py[validPoints] = yPoints[i];
            validPoints++;
        }

        if (validPoints < 2) {
            if (validPoints == 1 && roundCaps) {
                int r = Math.round(effThickness / 2f);
                fillArc((int) px[0] - r, (int) py[0] - r, Math.round(effThickness), Math.round(effThickness), 0, 360);
            }
            return;
        }

        float radius = effThickness / 2.0f;
        int numSegs = Math.min(validPoints - 1, (MAX_VERTICES - 64) / 8);

        float[] nX = new float[numSegs];
        float[] nY = new float[numSegs];

        for (int i = 0; i < numSegs; i++) {
            float dx = px[i + 1] - px[i];
            float dy = py[i + 1] - py[i];
            float len = (float) Math.hypot(dx, dy);
            if (len > 0.0001f) {
                nX[i] = -dy / len * radius;
                nY[i] =  dx / len * radius;
            } else {
                nX[i] = 0;
                nY[i] = radius;
            }
        }

        int vIdx = 0;
        int iIdx = 0;
        int[] segVerts = new int[numSegs * 4];
        for (int i = 0; i < numSegs; i++) {
            int base = vIdx;
            segVerts[i * 4] = base;
            segVerts[i * 4 + 1] = base + 1;
            segVerts[i * 4 + 2] = base + 2;
            segVerts[i * 4 + 3] = base + 3;

            writeVertex(vertexBuffer, base, px[i] - nX[i], py[i] - nY[i]);
            writeVertex(vertexBuffer, base + 1, px[i] + nX[i], py[i] + nY[i]);
            writeVertex(vertexBuffer, base + 2, px[i + 1] + nX[i], py[i + 1] + nY[i]);
            writeVertex(vertexBuffer, base + 3, px[i + 1] - nX[i], py[i + 1] - nY[i]);
            vIdx += 4;

            indicesBuffer[iIdx++] = base;
            indicesBuffer[iIdx++] = base + 1;
            indicesBuffer[iIdx++] = base + 2;

            indicesBuffer[iIdx++] = base;
            indicesBuffer[iIdx++] = base + 2;
            indicesBuffer[iIdx++] = base + 3;
        }

        float MITER_LIMIT = 2.5f;

        for (int i = 0; i < numSegs - 1; i++) {
            float dx1 = px[i + 1] - px[i];
            float dy1 = py[i + 1] - py[i];
            float dx2 = px[i + 2] - px[i + 1];
            float dy2 = py[i + 2] - py[i + 1];

            float cross = dx1 * dy2 - dy1 * dx2;

            if (Math.abs(cross) < 0.001f) {
                continue;
            }

            int centerV = vIdx++;
            writeVertex(vertexBuffer, centerV, px[i + 1], py[i + 1]);

            int startV, endV;
            float outX1, outY1, outX2, outY2;
            if (cross > 0) {
                startV = segVerts[i * 4 + 3];
                endV = segVerts[(i + 1) * 4];
                outX1 = -nX[i];
                outY1 = -nY[i];
                outX2 = -nX[i + 1];
                outY2 = -nY[i + 1];
            } else {
                startV = segVerts[i * 4 + 2];
                endV = segVerts[(i + 1) * 4 + 1];
                outX1 = nX[i];
                outY1 = nY[i];
                outX2 = nX[i + 1];
                outY2 = nY[i + 1];
            }

            float ax = (outX1 + outX2) / radius;
            float ay = (outY1 + outY2) / radius;
            float aLenSq = ax * ax + ay * ay;

            if (aLenSq > 0.001f) {
                float miterScale = 2.0f / aLenSq;
                if (miterScale <= MITER_LIMIT) {
                    float mx = ax * miterScale * radius;
                    float my = ay * miterScale * radius;

                    int miterV = vIdx++;
                    writeVertex(vertexBuffer, miterV, px[i + 1] + mx, py[i + 1] + my);

                    indicesBuffer[iIdx++] = centerV;
                    indicesBuffer[iIdx++] = startV;
                    indicesBuffer[iIdx++] = miterV;

                    indicesBuffer[iIdx++] = centerV;
                    indicesBuffer[iIdx++] = miterV;
                    indicesBuffer[iIdx++] = endV;
                    continue;
                }
            }

            indicesBuffer[iIdx++] = centerV;
            indicesBuffer[iIdx++] = startV;
            indicesBuffer[iIdx++] = endV;
        }

        if (roundCaps) {
            int capSegs = 6;

            float dx0 = px[1] - px[0];
            float dy0 = py[1] - py[0];
            double startAngle = Math.atan2(dy0, dx0);
            int startCapCenter = vIdx++;
            writeVertex(vertexBuffer, startCapCenter, px[0], py[0]);

            int prevStartCapV = segVerts[1];
            for (int k = 1; k <= capSegs; k++) {
                int currStartCapV = (k == capSegs) ? segVerts[0] : vIdx++;
                if (k < capSegs) {
                    double a = (startAngle + Math.PI / 2) + Math.PI * ((double) k / capSegs);
                    writeVertex(vertexBuffer, currStartCapV,
                            (float) (px[0] + radius * Math.cos(a)),
                            (float) (py[0] + radius * Math.sin(a)));
                }
                indicesBuffer[iIdx++] = startCapCenter;
                indicesBuffer[iIdx++] = prevStartCapV;
                indicesBuffer[iIdx++] = currStartCapV;
                prevStartCapV = currStartCapV;
            }

            int lastSeg = numSegs - 1;
            float dx1 = px[numSegs] - px[numSegs - 1];
            float dy1 = py[numSegs] - py[numSegs - 1];
            double endAngle = Math.atan2(dy1, dx1);
            int endCapCenter = vIdx++;
            writeVertex(vertexBuffer, endCapCenter, px[numSegs], py[numSegs]);

            int prevEndCapV = segVerts[lastSeg * 4 + 2];
            for (int k = 1; k <= capSegs; k++) {
                int currEndCapV = (k == capSegs) ? segVerts[lastSeg * 4 + 3] : vIdx++;
                if (k < capSegs) {
                    double a = (endAngle + Math.PI / 2) - Math.PI * ((double) k / capSegs);
                    writeVertex(vertexBuffer, currEndCapV,
                            (float) (px[numSegs] + radius * Math.cos(a)),
                            (float) (py[numSegs] + radius * Math.sin(a)));
                }
                indicesBuffer[iIdx++] = endCapCenter;
                indicesBuffer[iIdx++] = prevEndCapV;
                indicesBuffer[iIdx++] = currEndCapV;
                prevEndCapV = currEndCapV;
            }
        }

        SDL3Library.INSTANCE.SDL_RenderGeometry(
                sdlTarget.getRenderer(),
                null,
                vertexBuffer,
                vIdx,
                indicesBuffer,
                iIdx
        );
    }

    @Override
    public void drawRect(int x, int y, int width, int height) {
        sharedFRect.x = x;
        sharedFRect.y = y;
        sharedFRect.w = width;
        sharedFRect.h = height;
        SDL3Library.INSTANCE.SDL_RenderRect(sdlTarget.getRenderer(), sharedFRect);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        sharedFRect.x = x;
        sharedFRect.y = y;
        sharedFRect.w = width;
        sharedFRect.h = height;
        SDL3Library.INSTANCE.SDL_RenderFillRect(sdlTarget.getRenderer(), sharedFRect);
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        int rx = Math.min(arcWidth / 2, width / 2);
        int ry = Math.min(arcHeight / 2, height / 2);
        if (rx <= 0 || ry <= 0) {
            drawRect(x, y, width, height);
            return;
        }

        int cornerSegs = 8;
        int idx = 0;

        double cx = x + width - rx;
        double cy = y + ry;
        for (int i = 0; i <= cornerSegs; i++) {
            double rad = Math.toRadians(-270 - (90.0 * i / cornerSegs));
            sharedPointsBuffer.setFloat(idx * 8L, (float) (cx + rx * Math.cos(rad)));
            sharedPointsBuffer.setFloat(idx * 8L + 4, (float) (cy + ry * -Math.sin(rad)));
            idx++;
        }

        cx = x + width - rx;
        cy = y + height - ry;
        for (int i = 0; i <= cornerSegs; i++) {
            double rad = Math.toRadians(-0 - (90.0 * i / cornerSegs));
            sharedPointsBuffer.setFloat(idx * 8L, (float) (cx + rx * Math.cos(rad)));
            sharedPointsBuffer.setFloat(idx * 8L + 4, (float) (cy + ry * -Math.sin(rad)));
            idx++;
        }

        cx = x + rx;
        cy = y + height - ry;
        for (int i = 0; i <= cornerSegs; i++) {
            double rad = Math.toRadians(-90 - (90.0 * i / cornerSegs));
            sharedPointsBuffer.setFloat(idx * 8L, (float) (cx + rx * Math.cos(rad)));
            sharedPointsBuffer.setFloat(idx * 8L + 4, (float) (cy + ry * -Math.sin(rad)));
            idx++;
        }

        cx = x + rx;
        cy = y + ry;
        for (int i = 0; i <= cornerSegs; i++) {
            double rad = Math.toRadians(-180 - (90.0 * i / cornerSegs));
            sharedPointsBuffer.setFloat(idx * 8L, (float) (cx + rx * Math.cos(rad)));
            sharedPointsBuffer.setFloat(idx * 8L + 4, (float) (cy + ry * -Math.sin(rad)));
            idx++;
        }

        cx = x + width - rx;
        cy = y + ry;
        double rad0 = Math.toRadians(-270);
        sharedPointsBuffer.setFloat(idx * 8L, (float) (cx + rx * Math.cos(rad0)));
        sharedPointsBuffer.setFloat(idx * 8L + 4, (float) (cy + ry * -Math.sin(rad0)));
        idx++;

        SDL3Library.INSTANCE.SDL_RenderLines(sdlTarget.getRenderer(), sharedPointsBuffer, idx);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        int rx = Math.min(arcWidth / 2, width / 2);
        int ry = Math.min(arcHeight / 2, height / 2);
        if (rx <= 0 || ry <= 0) {
            fillRect(x, y, width, height);
            return;
        }

        int cornerSegs = 6;
        int vIdx = 0;

        float centerX = x + width / 2.0f;
        float centerY = y + height / 2.0f;
        writeVertex(vertexBuffer, vIdx++, centerX, centerY);

        double cx = x + width - rx;
        double cy = y + ry;
        for (int i = 0; i <= cornerSegs; i++) {
            double rad = Math.toRadians(-270 - (90.0 * i / cornerSegs));
            writeVertex(vertexBuffer, vIdx++, (float) (cx + rx * Math.cos(rad)), (float) (cy + ry * -Math.sin(rad)));
        }

        cx = x + width - rx;
        cy = y + height - ry;
        for (int i = 0; i <= cornerSegs; i++) {
            double rad = Math.toRadians(-0 - (90.0 * i / cornerSegs));
            writeVertex(vertexBuffer, vIdx++, (float) (cx + rx * Math.cos(rad)), (float) (cy + ry * -Math.sin(rad)));
        }

        cx = x + rx;
        cy = y + height - ry;
        for (int i = 0; i <= cornerSegs; i++) {
            double rad = Math.toRadians(-90 - (90.0 * i / cornerSegs));
            writeVertex(vertexBuffer, vIdx++, (float) (cx + rx * Math.cos(rad)), (float) (cy + ry * -Math.sin(rad)));
        }

        cx = x + rx;
        cy = y + ry;
        for (int i = 0; i <= cornerSegs; i++) {
            double rad = Math.toRadians(-180 - (90.0 * i / cornerSegs));
            writeVertex(vertexBuffer, vIdx++, (float) (cx + rx * Math.cos(rad)), (float) (cy + ry * -Math.sin(rad)));
        }

        int boundaryPoints = vIdx - 1;
        int iIdx = 0;
        for (int i = 1; i <= boundaryPoints; i++) {
            int next = (i == boundaryPoints) ? 1 : (i + 1);
            indicesBuffer[iIdx++] = 0;
            indicesBuffer[iIdx++] = i;
            indicesBuffer[iIdx++] = next;
        }

        SDL3Library.INSTANCE.SDL_RenderGeometry(
                sdlTarget.getRenderer(),
                null,
                vertexBuffer,
                vIdx,
                indicesBuffer,
                iIdx
        );
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        drawArc(x, y, width, height, startAngle, arcAngle, 1, 1000, false, false, false);
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle, int thickness, int zoomOut, boolean drawThickness, boolean zoomThickness, boolean rounding) {
        float effThickness = 1f;
        if (drawThickness) {
            if (zoomOut < 0) {
                zoomOut = -zoomOut;
            }
            effThickness = thickness * (zoomThickness ? (1000f / zoomOut) : 1f);
        }

        int segments = Math.min((MAX_VERTICES / 2) - 1, Math.max(12, Math.abs(arcAngle) / 6));
        double rx = width / 2.0;
        double ry = height / 2.0;
        double cx = x + rx;
        double cy = y + ry;

        if (effThickness <= 1.0f && !rounding) {
            for (int i = 0; i <= segments; i++) {
                double angleDeg = -startAngle - (arcAngle * (double) i / segments);
                double rad = Math.toRadians(angleDeg);
                float px = (float) (cx + rx * Math.cos(rad));
                float py = (float) (cy + ry * -Math.sin(rad));

                sharedPointsBuffer.setFloat(i * 8L, px);
                sharedPointsBuffer.setFloat(i * 8L + 4, py);
            }
            SDL3Library.INSTANCE.SDL_RenderLines(sdlTarget.getRenderer(), sharedPointsBuffer, segments + 1);
            return;
        }

        float halfThick = effThickness / 2.0f;
        double rxOut = rx + halfThick;
        double ryOut = ry + halfThick;
        double rxIn  = Math.max(0, rx - halfThick);
        double ryIn  = Math.max(0, ry - halfThick);

        int numVertices = (segments + 1) * 2;

        for (int i = 0; i <= segments; i++) {
            double angleDeg = -startAngle - (arcAngle * (double) i / segments);
            double rad = Math.toRadians(angleDeg);
            double cos = Math.cos(rad);
            double sin = -Math.sin(rad);

            float xOut = (float) (cx + rxOut * cos);
            float yOut = (float) (cy + ryOut * sin);
            float xIn  = (float) (cx + rxIn  * cos);
            float yIn  = (float) (cy + ryIn  * sin);

            writeVertex(vertexBuffer, i * 2, xOut, yOut);
            writeVertex(vertexBuffer, i * 2 + 1, xIn, yIn);
        }

        int numIndices = segments * 6;
        for (int i = 0; i < segments; i++) {
            int baseVert = i * 2;
            int baseIndex = i * 6;

            indicesBuffer[baseIndex] = baseVert;
            indicesBuffer[baseIndex + 1] = baseVert + 1;
            indicesBuffer[baseIndex + 2] = baseVert + 2;

            indicesBuffer[baseIndex + 3] = baseVert + 1;
            indicesBuffer[baseIndex + 4] = baseVert + 2;
            indicesBuffer[baseIndex + 5] = baseVert + 3;
        }

        SDL3Library.INSTANCE.SDL_RenderGeometry(
                sdlTarget.getRenderer(),
                null,
                vertexBuffer,
                numVertices,
                indicesBuffer,
                numIndices
        );

        if (rounding) {
            int r = Math.round(halfThick);
            int d = Math.round(effThickness);

            double startRad = Math.toRadians(-startAngle);
            int startX = (int) Math.round(cx + rx * Math.cos(startRad));
            int startY = (int) Math.round(cy + ry * -Math.sin(startRad));

            double endRad = Math.toRadians(-startAngle - arcAngle);
            int endX = (int) Math.round(cx + rx * Math.cos(endRad));
            int endY = (int) Math.round(cy + ry * -Math.sin(endRad));

            fillArc(startX - r, startY - r, d, d, 0, 360);
            fillArc(endX - r, endY - r, d, d, 0, 360);
        }
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        int segments = Math.min(MAX_VERTICES - 2, Math.max(16, Math.abs(arcAngle) / 6));
        double rx = width / 2.0;
        double ry = height / 2.0;
        double cx = x + rx;
        double cy = y + ry;

        int numVertices = segments + 2;

        writeVertex(vertexBuffer, 0, (float) cx, (float) cy);

        for (int i = 0; i <= segments; i++) {
            double angleDeg = -startAngle - (arcAngle * (double) i / segments);
            double rad = Math.toRadians(angleDeg);
            writeVertex(vertexBuffer, i + 1, (float) (cx + rx * Math.cos(rad)), (float) (cy + ry * -Math.sin(rad)));
        }

        int numIndices = segments * 3;
        for (int i = 0; i < segments; i++) {
            indicesBuffer[i * 3] = 0;
            indicesBuffer[i * 3 + 1] = i + 1;
            indicesBuffer[i * 3 + 2] = i + 2;
        }

        SDL3Library.INSTANCE.SDL_RenderGeometry(sdlTarget.getRenderer(), null, vertexBuffer, numVertices, indicesBuffer, numIndices);
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
    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        writeVertex(vertexBuffer, 0, x1, y1);
        writeVertex(vertexBuffer, 1, x2, y2);
        writeVertex(vertexBuffer, 2, x3, y3);

        SDL3Library.INSTANCE.SDL_RenderGeometry(sdlTarget.getRenderer(), null, vertexBuffer, 3, TRIANGLE_INDICES, 3);
    }

    @Override
    public void drawArrow(int x1, int y1, int x2, int y2, int thickness, int zoomOut, boolean fill) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int arrowX = (x2 * 5 + x1) / 6;
        int arrowY = (y2 * 5 + y1) / 6;
        int arrowSideVecX = dy / 8;
        int arrowSideVecY = -dx / 8;
        drawLine(x1, y1, arrowX, arrowY, thickness, zoomOut, true, true, false, false);
        drawTriangle(x2, y2, arrowX + arrowSideVecX, arrowY + arrowSideVecY, arrowX - arrowSideVecX, arrowY - arrowSideVecY, fill);
    }

    @Override
    public void drawString(String str, int x, int y, int anchor) {
        if (str != null && !str.isEmpty()) {
            drawSubstring(str, 0, str.length(), x, y, anchor);
        }
    }

    @Override
    public void drawSubstring(String str, int offset, int len, int x, int y, int anchor) {
        if (str == null || len <= 0) {
            return;
        }
        String sub = str.substring(offset, offset + len);
        int w = substringWidth(str, offset, len);
        int h = getFontHeight();

        if ((anchor & HCENTER) != 0) x -= w / 2;
        else if ((anchor & RIGHT) != 0) x -= w;

        if ((anchor & VCENTER) != 0) y -= h / 2;
        else if ((anchor & BOTTOM) != 0) y -= h;

        TextTextureCacheSDL3.Entry entry = textCache.getTextTexture(sdlTarget.getRenderer(), sub, currentFont, currentColorRGB);
        if (entry != null && entry.texture != null) {
            sharedFRect.x = x;
            sharedFRect.y = y;
            sharedFRect.w = entry.width;
            sharedFRect.h = entry.height;
            SDL3Library.INSTANCE.SDL_RenderTexture(sdlTarget.getRenderer(), entry.texture, null, sharedFRect);
        }
    }

    @Override
    public void drawImage(Image img, int x, int y, int anchor) {
        if (img == null) return;
        int w = img.getWidth();
        int h = img.getHeight();

        if ((anchor & HCENTER) != 0) x -= w / 2;
        else if ((anchor & RIGHT) != 0) x -= w;

        if ((anchor & VCENTER) != 0) y -= h / 2;
        else if ((anchor & BOTTOM) != 0) y -= h;

        Pointer texture = img.getSdl3Texture(sdlTarget.getRenderer());
        if (texture != null) {
            sharedFRect.x = x;
            sharedFRect.y = y;
            sharedFRect.w = w;
            sharedFRect.h = h;
            SDL3Library.INSTANCE.SDL_RenderTexture(sdlTarget.getRenderer(), texture, null, sharedFRect);
        }
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        sharedRect.x = x;
        sharedRect.y = y;
        sharedRect.w = width;
        sharedRect.h = height;
        SDL3Library.INSTANCE.SDL_SetRenderClipRect(sdlTarget.getRenderer(), sharedRect);
        sdlTarget.setClipBounds(new Rectangle(x, y, width, height));
    }

    @Override
    public int getClipX() { return sdlTarget.getClipBounds().x; }
    @Override
    public int getClipY() { return sdlTarget.getClipBounds().y; }
    @Override
    public int getClipWidth() { return sdlTarget.getClipBounds().width; }
    @Override
    public int getClipHeight() { return sdlTarget.getClipBounds().height; }

    @Override
    public Font getFont() { return currentFont; }
    @Override
    public void setFont(Font font) { this.currentFont = (font != null) ? font : Font.getDefaultFont(); }
    @Override
    public void setFont(int face, int style, int size) { setFont(Font.getFont(face, style, size)); }
    @Override
    public int getFontFace() { return currentFont.getFace(); }
    @Override
    public int getFontStyle() { return currentFont.getStyle(); }
    @Override
    public int getFontSize() { return currentFont.getSize(); }
    @Override
    public void setFontSize(int size) { setFont(Font.getFont(getFontFace(), getFontStyle(), size)); }
    @Override
    public int getFontHeight() { return currentFont.getHeight(); }
    @Override
    public int getFontHeight(int face, int style, int size) { return Font.getFont(face, style, size).getHeight(); }
    @Override
    public int stringWidth(String str) { return currentFont.stringWidth(str); }
    @Override
    public int substringWidth(String str, int offset, int len) { return currentFont.substringWidth(str, offset, len); }

    private void writeVertex(Memory mem, int index, float x, float y) {
        long offset = index * 32L;
        mem.setFloat(offset, x);
        mem.setFloat(offset + 4, y);
        mem.setFloat(offset + 8, currentR);
        mem.setFloat(offset + 12, currentG);
        mem.setFloat(offset + 16, currentB);
        mem.setFloat(offset + 20, currentA);
        mem.setFloat(offset + 24, 0f);
        mem.setFloat(offset + 28, 0f);
    }
}