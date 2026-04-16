// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import mobileapplication3.platform.PlatformSettings;

import java.awt.FontMetrics;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Font implements IFont {
    private final java.awt.Font font;
    private int face;
    private int style;
    private int size;

    public Font(int face, int style, int size) {
        this(face, style, size, PlatformSettings.getFontSize());
    }

    public Font() {
        this(SIZE_MEDIUM);
    }

    public Font(int size) {
        this(FACE_SYSTEM, STYLE_PLAIN, size);
    }

    protected Font(int size, int baseFontSize) {
        this(FACE_SYSTEM, STYLE_PLAIN, size, baseFontSize);
    }

    protected Font(int face, int style, int size, int baseFontSize) {
        this.face = face;
        this.style = style;
        this.size = size;

        int awtSize = size;
        switch (size) {
            case SIZE_SMALL:
                awtSize = baseFontSize * 3 / 4;
                break;
            case SIZE_MEDIUM:
                awtSize = baseFontSize;
                break;
            case SIZE_LARGE:
                awtSize = baseFontSize * 3 / 2;
                break;
        }

        String awtFace = java.awt.Font.DIALOG;
        if (face == FACE_MONOSPACE) {
            awtFace = java.awt.Font.MONOSPACED;
        } else if (face == FACE_PROPORTIONAL) {
            awtFace = java.awt.Font.SANS_SERIF;
        }

        int awtStyle = java.awt.Font.PLAIN;
        if ((style & STYLE_BOLD) != 0) {
            awtStyle |= java.awt.Font.BOLD;
        }
        if ((style & STYLE_ITALIC) != 0) {
            awtStyle |= java.awt.Font.ITALIC;
        }

        java.awt.Font baseFont = new java.awt.Font(awtFace, awtStyle, awtSize);

        if ((style & STYLE_UNDERLINED) != 0) {
            Map<TextAttribute, Object> attributes = new HashMap<>(baseFont.getAttributes());
            attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            this.font = baseFont.deriveFont(attributes);
        } else {
            this.font = baseFont;
        }
    }

    protected Font(java.awt.Font font) {
        this.font = font;
        this.face = FACE_SYSTEM;
        this.style = STYLE_PLAIN;
        this.size = SIZE_MEDIUM;
    }

    public java.awt.Font getAwtFont() {
        return font;
    }

    public static Font getFont(int face, int style, int size) {
        return new Font(face, style, size);
    }

    public static Font getDefaultFont() {
        return new Font(SIZE_MEDIUM);
    }

    public static int defaultFontStringWidth(String str) {
        return getDefaultFont().stringWidth(str);
    }

    public static int defaultFontSubstringWidth(String str, int offset, int len) {
        return getDefaultFont().substringWidth(str, offset, len);
    }

    public static int getDefaultFontHeight() {
        return getDefaultFont().getHeight();
    }

    public static int getDefaultFontSize() {
        return getDefaultFont().getSize();
    }

    public int getFace() {
        return face;
    }

    public int getStyle() {
        return style;
    }

    public int getSize() {
        return size;
    }

    public int getHeight() {
        java.awt.Graphics g = new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_BINARY).getGraphics();
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        return metrics.getHeight();
    }

    public int stringWidth(String str) {
        return substringWidth(str, 0, str.length());
    }

    public int substringWidth(String str, int offset, int len) {
        java.awt.Graphics g = new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_BINARY).getGraphics();
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        return metrics.stringWidth(str.substring(offset, offset + len));
    }

    public int[][] getLineBounds(String text, int w, int padding) {
        Vector lineBoundsVector = new Vector(text.length() / 5);
        int charOffset = 0;
        if (stringWidth(text) <= w - padding * 2 && text.indexOf('\n') == -1) {
            lineBoundsVector.addElement(new int[]{0, text.length()});
        } else {
            while (charOffset < text.length()) {
                int maxSymsInCurrLine = 1;
                boolean maxLineLengthReached = false;
                boolean lineBreakSymFound = false;
                for (int lineLength = 1; lineLength <= text.length() - charOffset; lineLength++) {
                    if (substringWidth(text, charOffset, lineLength) > w - padding * 2) {
                        maxLineLengthReached = true;
                        break;
                    }

                    maxSymsInCurrLine = lineLength;

                    if (charOffset + lineLength < text.length()) {
                        if (text.charAt(charOffset + lineLength) == '\n') {
                            lineBoundsVector.addElement(new int[]{charOffset, lineLength});
                            charOffset = charOffset + lineLength + 1;
                            lineBreakSymFound = true;
                            break;
                        }
                    }
                }

                if (lineBreakSymFound) {
                    continue;
                }


                boolean spaceFound = false;

                int maxRightBorder = charOffset + maxSymsInCurrLine;

                if (maxRightBorder >= text.length()) {
                    lineBoundsVector.addElement(new int[]{charOffset, maxSymsInCurrLine});
                    break;
                }

                if (!maxLineLengthReached) {
                    lineBoundsVector.addElement(new int[]{charOffset, maxSymsInCurrLine}); //
                    charOffset = maxRightBorder;
                } else {
                    for (int i = maxRightBorder; i > charOffset; i--) {
                        if (text.charAt(i) == ' ') {
                            lineBoundsVector.addElement(new int[]{charOffset, i - charOffset});
                            charOffset = i + 1;
                            spaceFound = true;
                            break;
                        }
                    }

                    if (!spaceFound) {
                        lineBoundsVector.addElement(new int[]{charOffset, maxRightBorder - charOffset});
                        charOffset = maxRightBorder;
                    }
                }
            }
        }

        int[][] lineBounds = new int[lineBoundsVector.size()][];
        for (int i = 0; i < lineBoundsVector.size(); i++) {
            lineBounds[i] = (int[]) lineBoundsVector.elementAt(i);
        }
        return lineBounds;
    }
}
