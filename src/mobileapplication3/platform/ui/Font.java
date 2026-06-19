// SPDX-License-Identifier: LGPL-2.1-only

package mobileapplication3.platform.ui;

import org.robovm.apple.foundation.NSString;
import org.robovm.apple.uikit.NSAttributedStringAttributes;
import org.robovm.apple.uikit.UIFont;
import org.robovm.apple.uikit.UIFontDescriptor;
import org.robovm.apple.uikit.UIFontDescriptorSymbolicTraits;
import org.robovm.apple.uikit.UIScreen;

import java.util.Hashtable;
import java.util.Vector;

public class Font implements IFont {
    private static final int BASE_FONT_SIZE = 23;
    private static final Hashtable nativeCache = new Hashtable();

    private UIFont uiFont;
    private int face;
    private int style;
    private int size;
    private NSAttributedStringAttributes attributes;

    public Font(int face, int style, int size) {
        this.face = face;
        this.style = style;
        this.size = size;
        initFont();
    }

    private void initFont() {
        String key = face + "_" + style + "_" + size;

        synchronized (nativeCache) {
            CachedFontData cached = (CachedFontData) nativeCache.get(key);
            if (cached != null) {
                this.uiFont = cached.uiFont;
                this.attributes = cached.attributes;
                return;
            }
        }

        double scale = UIScreen.getMainScreen().getScale();
        float realSize = (float) (BASE_FONT_SIZE * scale);
        switch (size) {
            case SIZE_SMALL:
                realSize = (float) (BASE_FONT_SIZE * scale * 3 / 4);
                break;
            case SIZE_MEDIUM:
                realSize = (float) (BASE_FONT_SIZE * scale);
                break;
            case SIZE_LARGE:
                realSize = (float) (BASE_FONT_SIZE * scale * 3 / 2);
                break;
        }

        int trait = 0;
        if ((style & STYLE_BOLD) != 0) {
            trait |= UIFontDescriptorSymbolicTraits.TraitBold.value();
        }
        if ((style & STYLE_ITALIC) != 0) {
            trait |= UIFontDescriptorSymbolicTraits.TraitItalic.value();
        }

        UIFont fontObj = null;
        if (trait != 0) {
            UIFontDescriptor descriptor = UIFont.getSystemFont(realSize).getFontDescriptor().newWithSymbolicTraits(new UIFontDescriptorSymbolicTraits(trait));
            if (descriptor != null) {
                fontObj = UIFont.getFont(descriptor, realSize);
            }
        }

        if (fontObj == null) {
            fontObj = UIFont.getSystemFont(realSize);
        }

        NSAttributedStringAttributes attrObj = new NSAttributedStringAttributes().setFont(fontObj);

        this.uiFont = fontObj;
        this.attributes = attrObj;

        synchronized (nativeCache) {
            nativeCache.put(key, new CachedFontData(fontObj, attrObj));
        }
    }

    public Font() {
        this(SIZE_MEDIUM);
    }

    public Font(int size) {
        this(FACE_SYSTEM, STYLE_PLAIN, size);
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
        return (int) Math.ceil(uiFont.getLineHeight());
    }

    NSAttributedStringAttributes getAttributes() {
        return attributes;
    }

    public int stringWidth(String str) {
        return substringWidth(str, 0, str.length());
    }

    public int substringWidth(String str, int offset, int len) {
        if (str == null || len <= 0) {
            return 0;
        }

        NSString nsStr = NSStringCache.get(str, offset, len);

        return (int) Math.ceil(nsStr.getSize(attributes).getWidth());
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

    private static class CachedFontData {
        final UIFont uiFont;
        final NSAttributedStringAttributes attributes;
        CachedFontData(UIFont f, NSAttributedStringAttributes a) {
            this.uiFont = f;
            this.attributes = a;
        }
    }
}
