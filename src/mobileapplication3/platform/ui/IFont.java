package mobileapplication3.platform.ui;

public interface IFont {

    int STYLE_PLAIN = 0, STYLE_BOLD = 1, STYLE_ITALIC = 2, STYLE_UNDERLINED = 4,
            SIZE_SMALL = 8, SIZE_MEDIUM = 0, SIZE_LARGE = 16,
            FACE_SYSTEM = 0, FACE_MONOSPACE = 32, FACE_PROPORTIONAL = 64,
            FONT_STATIC_TEXT = 0, FONT_INPUT_TEXT = 1;

    int getFace();

    int getStyle();

    int getSize();

    int getHeight();

    int stringWidth(String str);

    int substringWidth(String str, int offset, int len);

    int[][] getLineBounds(String text, int w, int padding);
}
