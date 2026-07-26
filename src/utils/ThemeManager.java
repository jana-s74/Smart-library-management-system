package utils;

import java.awt.Color;

public class ThemeManager {

    private static boolean isDarkMode = false;

    // Vibrant Modern Palette inspired by UI design
    public static final Color PRIMARY_PURPLE = new Color(0x73, 0x3D, 0xD9); // #733DD9 Deep Purple
    public static final Color ACCENT_GREEN = new Color(0x6C, 0xB3, 0x3F);   // #6CB33F Vibrant Green
    public static final Color LIGHT_BG = new Color(0xEF, 0xEB, 0xF6);       // #EFEBF6 Soft Lavender Canvas
    public static final Color CARD_BG_LIGHT = new Color(0xFF, 0xFF, 0xFF);   // Pure White Card
    public static final Color TEXT_DARK = new Color(0x23, 0x22, 0x2A);       // #23222A Deep Charcoal
    public static final Color TEXT_MUTED = new Color(0x8E, 0x8D, 0xAA);      // #8E8DAA Muted Purple Gray
    public static final Color BORDER_GRAY = new Color(0xEB, 0xE7, 0xF2);     // #EBE7F2 Soft Border
    public static final Color HOVER_PURPLE = new Color(0x87, 0x50, 0xED);    // #8750ED Hover State
    public static final Color HOVER_GREEN = new Color(0x7B, 0xC4, 0x4D);     // Hover Green

    // Legacy Aliases for seamless backward compatibility
    public static final Color PRIMARY_ORANGE = PRIMARY_PURPLE;
    public static final Color SECONDARY_WHITE = CARD_BG_LIGHT;
    public static final Color SIDEBAR_ORANGE = CARD_BG_LIGHT;
    public static final Color HOVER_ORANGE = HOVER_PURPLE;

    public static final Color SUCCESS_GREEN = ACCENT_GREEN;
    public static final Color DANGER_RED = new Color(0xEF, 0x44, 0x44);      // #EF4444
    public static final Color WARNING_AMBER = new Color(0xF5, 0x9E, 0x0B);   // #F59E0B
    public static final Color REPORT_PURPLE = PRIMARY_PURPLE;

    // Dark Mode Palette
    public static final Color DARK_BG = new Color(0x18, 0x17, 0x24);
    public static final Color DARK_CARD_BG = new Color(0x23, 0x21, 0x33);
    public static final Color DARK_TEXT = new Color(0xF9, 0xFA, 0xFB);

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
    }

    public static Color getBackgroundColor() {
        return isDarkMode ? DARK_BG : LIGHT_BG;
    }

    public static Color getCardBackgroundColor() {
        return isDarkMode ? DARK_CARD_BG : CARD_BG_LIGHT;
    }

    public static Color getTextColor() {
        return isDarkMode ? DARK_TEXT : TEXT_DARK;
    }
}
