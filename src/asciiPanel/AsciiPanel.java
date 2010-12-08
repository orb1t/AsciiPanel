package asciiPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * This simulates a code page 437 ASCII terminal display.
 * @author Trystan Spangler
 */
public class AsciiPanel extends JPanel {

    /**
     * The color black (pure black).
     */
    public static Color black = new Color(0, 0, 0);

    /**
     * The color red.
     */
    public static Color red = new Color(128, 0, 0);

    /**
     * The color green.
     */
    public static Color green = new Color(0, 128, 0);

    /**
     * The color yellow.
     */
    public static Color yellow = new Color(128, 128, 0);

    /**
     * The color blue.
     */
    public static Color blue = new Color(0, 0, 128);

    /**
     * The color magenta.
     */
    public static Color magenta = new Color(128, 0, 128);

    /**
     * The color cyan.
     */
    public static Color cyan = new Color(0, 128, 128);

    /**
     * The color white (light gray).
     */
    public static Color white = new Color(192, 192, 192);

    /**
     * A brighter black (dark gray).
     */
    public static Color brightBlack = new Color(128, 128, 128);

    /**
     * A brighter red.
     */
    public static Color brightRed = new Color(255, 0, 0);

    /**
     * A brighter green.
     */
    public static Color brightGreen = new Color(0, 255, 0);

    /**
     * A brighter yellow.
     */
    public static Color brightYellow = new Color(255, 255, 0);

    /**
     * A brighter blue.
     */
    public static Color brightBlue = new Color(0, 0, 255);

    /**
     * A brighter magenta.
     */
    public static Color brightMagenta = new Color(255, 0, 255);

    /**
     * A brighter cyan.
     */
    public static Color brightCyan = new Color(0, 255, 255);
    
    /**
     * A brighter white (pure white).
     */
    public static Color brightWhite = new Color(255, 255, 255);
    
    private int widthInCharacters;
    private int heightInCharacters;
    private int charWidth = 9;
    private int charHeight = 16;
    private Color defaultBackgroundColor;
    private Color defaultForegroundColor;
    private int cursorX;
    private int cursorY;
    private BufferedImage glyphSprite;
    private BufferedImage[] glyphs;
    private char[][] chars;
    private Color[][] backgroundColors;
    private Color[][] foregroundColors;

    /**
     * Gets the height, in pixels, of a character.
     * @return
     */
    public int getCharHeight() {
        return charHeight;
    }

    /**
     * Gets the width, in pixels, of a character.
     * @return
     */
    public int getCharWidth() {
        return charWidth;
    }

    /**
     * Gets the height in characters.
     * A standard terminal is 24 characters high.
     * @return
     */
    public int getHeightInCharacters() {
        return heightInCharacters;
    }

    /**
     * Gets the width in characters.
     * A standard terminal is 80 characters wide.
     * @return
     */
    public int getWidthInCharacters() {
        return widthInCharacters;
    }

    /**
     * Gets the distance from the left new text will be written to.
     * @return
     */
    public int getCursorX() {
        return cursorX;
    }

    /**
     * Sets the distance from the left new text will be written to.
     * This should be equal to or greater than 0 and less than the the width in characters.
     * @param cursorX the distance from the left new text should be written to
     */
    public void setCursorX(int cursorX) {
        this.cursorX = cursorX;
    }

    /**
     * Gets the distance from the top new text will be written to.
     * @return
     */
    public int getCursorY() {
        return cursorY;
    }

    /**
     * Sets the distance from the top new text will be written to.
     * This should be equal to or greater than 0 and less than the the height in characters.
     * @param cursorY the distance from the top new text should be written to
     */
    public void setCursorY(int cursorY) {
        this.cursorY = cursorY;
    }

    /**
     * Sets the x and y position of where new text will be written to. The origin (0,0) is the upper left corner.
     * The x should be equal to or greater than 0 and less than the the width in characters.
     * The y should be equal to or greater than 0 and less than the the height in characters.
     * @param x the distance from the left new text should be written to
     * @param y the distance from the top new text should be written to
     */
    public void setCursorPosition(int x, int y) {
        setCursorX(x);
        setCursorY(y);
    }

    /**
     * Gets the default background color that is used when writing new text.
     * @return
     */
    public Color getDefaultBackgroundColor() {
        return defaultBackgroundColor;
    }

    /**
     * Sets the default background color that is used when writing new text.
     * @param defaultBackgroundColor
     */
    public void setDefaultBackgroundColor(Color defaultBackgroundColor) {
        this.defaultBackgroundColor = defaultBackgroundColor;
    }

    /**
     * Gets the default foreground color that is used when writing new text.
     * @return
     */
    public Color getDefaultForegroundColor() {
        return defaultForegroundColor;
    }

    /**
     * Sets the default foreground color that is used when writing new text.
     * @param defaultForegroundColor
     */
    public void setDefaultForegroundColor(Color defaultForegroundColor) {
        this.defaultForegroundColor = defaultForegroundColor;
    }

    /**
     * Class constructor.
     */
    public AsciiPanel() {
        this(80, 24);
    }

    /**
     * Class constructor specifying the width and height in characters.
     * @param width
     * @param height
     */
    public AsciiPanel(int width, int height) {
        super();

        widthInCharacters = width;
        heightInCharacters = height;
        setPreferredSize(new Dimension(charWidth * widthInCharacters, charHeight * heightInCharacters));

        defaultBackgroundColor = black;
        defaultForegroundColor = white;

        chars = new char[widthInCharacters][heightInCharacters];
        backgroundColors = new Color[widthInCharacters][heightInCharacters];
        foregroundColors = new Color[widthInCharacters][heightInCharacters];

        AsciiPanel.this.clear();

        loadGlyphs();
    }

    /**
     *
     * @param g
     */
    @Override
    public void paintComponent(Graphics g) {
        for (int x = 0; x < widthInCharacters; x++) {
            for (int y = 0; y < heightInCharacters; y++) {
                Color bg = backgroundColors[x][y];
                Color fg = foregroundColors[x][y];

                LookupOp op = setColors(bg, fg);
                BufferedImage img = op.filter(glyphs[chars[x][y]], null);
                g.drawImage(img, x * charWidth, y * charHeight, null);
            }
        }
    }

    private void loadGlyphs() {
        glyphs = new BufferedImage[255];

        try {
            glyphSprite = ImageIO.read(AsciiPanel.class.getResource("cp437.png"));
        } catch (IOException e) {
            System.err.println("loadGlyphs(): " + e.getMessage());
        }

        for (int i = 0; i < 255; i++) {
            int sx = (i % 32) * charWidth + 8;
            int sy = (i / 32) * charHeight + 8;

            glyphs[i] = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB);
            glyphs[i].getGraphics().drawImage(glyphSprite, 0, 0, charWidth, charHeight, sx, sy, sx + charWidth, sy + charHeight, null);
        }
    }

    private LookupOp setColors(Color bgColor, Color fgColor) {
        byte[] a = new byte[256];
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];

        byte bgr = (byte) (bgColor.getRed());
        byte bgg = (byte) (bgColor.getGreen());
        byte bgb = (byte) (bgColor.getBlue());

        byte fgr = (byte) (fgColor.getRed());
        byte fgg = (byte) (fgColor.getGreen());
        byte fgb = (byte) (fgColor.getBlue());

        for (int i = 0; i < 256; i++) {
            if (i == 0) {
                a[i] = (byte) 255;
                r[i] = bgr;
                g[i] = bgg;
                b[i] = bgb;
            } else {
                a[i] = (byte) 255;
                r[i] = fgr;
                g[i] = fgg;
                b[i] = fgb;
            }
        }

        byte[][] table = {r, g, b, a};
        return new LookupOp(new ByteLookupTable(0, table), null);
    }

    /**
     * Clear the entire screen to whatever the default background color is.
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel clear() {
        return clear(' ', 0, 0, widthInCharacters, heightInCharacters, defaultForegroundColor, defaultBackgroundColor);
    }

    /**
     * Clear the entire screen with the specified character and whatever the default foreground and background colors are.
     * @param character  the character to write
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel clear(char character) {
        return clear(character, 0, 0, widthInCharacters, heightInCharacters, defaultForegroundColor, defaultBackgroundColor);
    }

    /**
     * Clear the entire screen with the specified character and whatever the specified foreground and background colors are.
     * @param character  the character to write
     * @param foreground the foreground color or null to use the default
     * @param background the background color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel clear(char character, Color foreground, Color background) {
        return clear(character, 0, 0, widthInCharacters, heightInCharacters, foreground, background);
    }

    /**
     * Clear the section of the screen with the specified character and whatever the default foreground and background colors are.
     * @param character  the character to write
     * @param x          the distance from the left to begin writing from
     * @param y          the distance from the top to begin writing from
     * @param width      the height of the section to clear
     * @param height     the width of the section to clear
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel clear(char character, int x, int y, int width, int height) {
        return clear(character, x, y, width, height, defaultForegroundColor, defaultBackgroundColor);
    }

    /**
     * Clear the section of the screen with the specified character and whatever the specified foreground and background colors are.
     * @param character  the character to write
     * @param x          the distance from the left to begin writing from
     * @param y          the distance from the top to begin writing from
     * @param width      the height of the section to clear
     * @param height     the width of the section to clear
     * @param foreground the foreground color or null to use the default
     * @param background the background color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel clear(char character, int x, int y, int width, int height, Color foreground, Color background) {
        for (int xo = x; xo < x + width; xo++) {
            for (int yo = y; yo < y + height; yo++) {
                write(character, xo, yo, foreground, background);
            }
        }
        return this;
    }

    /**
     * Write a character to the cursor's position.
     * This updates the cursor's position.
     * @param character  the character to write
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(char character) {
        return write(character, cursorX, cursorY, defaultForegroundColor, defaultBackgroundColor);
    }

    /**
     * Write a character to the cursor's position with the specified foreground color.
     * This updates the cursor's position but not the default foreground color.
     * @param character  the character to write
     * @param foreground the foreground color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(char character, Color foreground) {
        return write(character, cursorX, cursorY, foreground, defaultBackgroundColor);
    }

    /**
     * Write a character to the cursor's position with the specified foreground and background colors.
     * This updates the cursor's position but not the default foreground or background colors.
     * @param character  the character to write
     * @param foreground the foreground color or null to use the default
     * @param background the background color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(char character, Color foreground, Color background) {
        return write(character, cursorX, cursorY, foreground, background);
    }

    /**
     * Write a character to the specified position.
     * This updates the cursor's position.
     * @param character  the character to write
     * @param x          the distance from the left to begin writing from
     * @param y          the distance from the top to begin writing from
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(char character, int x, int y) {
        return write(character, x, y, defaultForegroundColor, defaultBackgroundColor);
    }

    /**
     * Write a character to the specified position with the specified foreground color.
     * This updates the cursor's position but not the default foreground color.
     * @param character  the character to write
     * @param x          the distance from the left to begin writing from
     * @param y          the distance from the top to begin writing from
     * @param foreground the foreground color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(char character, int x, int y, Color foreground) {
        return write(character, x, y, foreground, defaultBackgroundColor);
    }

    /**
     * Write a character to the specified position with the specified foreground and background colors.
     * This updates the cursor's position but not the default foreground or background colors.
     * @param character  the character to write
     * @param x          the distance from the left to begin writing from
     * @param y          the distance from the top to begin writing from
     * @param foreground the foreground color or null to use the default
     * @param background the background color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(char character, int x, int y, Color foreground, Color background) {
        chars[x][y] = character;
        foregroundColors[x][y] = foreground;
        backgroundColors[x][y] = background;
        cursorX = x + 1;
        cursorY = y;
        return this;
    }

    /**
     * Write a string to the cursor's position.
     * This updates the cursor's position.
     * @param string     the string to write
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(String string) {
        return write(string, cursorX, cursorY, defaultForegroundColor, defaultBackgroundColor);
    }

    /**
     * Write a string to the cursor's position with the specified foreground color.
     * This updates the cursor's position but not the default foreground color.
     * @param string     the string to write
     * @param foreground the foreground color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(String string, Color foreground) {
        return write(string, cursorX, cursorY, foreground, defaultBackgroundColor);
    }

    /**
     * Write a string to the cursor's position with the specified foreground and background colors.
     * This updates the cursor's position but not the default foreground or background colors.
     * @param string     the string to write
     * @param foreground the foreground color or null to use the default
     * @param background the background color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(String string, Color foreground, Color background) {
        return write(string, cursorX, cursorY, foreground, background);
    }

    /**
     * Write a string to the specified position.
     * This updates the cursor's position.
     * @param string     the string to write
     * @param x          the distance from the left to begin writing from
     * @param y          the distance from the top to begin writing from
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(String string, int x, int y) {
        return write(string, x, y, defaultForegroundColor, defaultBackgroundColor);
    }

    /**
     * Write a string to the specified position with the specified foreground color.
     * This updates the cursor's position but not the default foreground color.
     * @param string     the string to write
     * @param x          the distance from the left to begin writing from
     * @param y          the distance from the top to begin writing from
     * @param foreground the foreground color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(String string, int x, int y, Color foreground) {
        return write(string, x, y, foreground, defaultBackgroundColor);
    }

    /**
     * Write a string to the specified position with the specified foreground and background colors.
     * This updates the cursor's position but not the default foreground or background colors.
     * @param string     the string to write
     * @param x          the distance from the left to begin writing from
     * @param y          the distance from the top to begin writing from
     * @param foreground the foreground color or null to use the default
     * @param background the background color or null to use the default
     * @return this for convenient chaining of method calls
     */
    public AsciiPanel write(String string, int x, int y, Color foreground, Color background) {
        if (foreground == null)
            foreground = defaultForegroundColor;

        if (background == null)
            background = defaultBackgroundColor;

        for (int i = 0; i < string.length(); i++) {
            write(string.charAt(i), x + i, y, foreground, background);
        }
        return this;
    }
}
