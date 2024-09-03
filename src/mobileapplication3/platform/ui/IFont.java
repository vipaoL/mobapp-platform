package mobileapplication3.platform.ui;

public interface IFont {
    int
            STYLE_PLAIN = 0;
    int STYLE_BOLD = 1;
    int STYLE_ITALIC = 2;
    int STYLE_UNDERLINED = 4;
    int SIZE_SMALL = 8;
    int SIZE_MEDIUM = 0;
    int SIZE_LARGE = 16;
    int FACE_SYSTEM = 0;
    int FACE_MONOSPACE = 32;
    int FACE_PROPORTIONAL = 64;
    int FONT_STATIC_TEXT = 0;
    int FONT_INPUT_TEXT = 1;

    int getFace();

    int getStyle();

    int getSize();

    int getHeight();

    int stringWidth(String str);

    int substringWidth(String str, int offset, int len);

    int[][] getLineBounds(String text, int w, int padding);
}
