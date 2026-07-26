package ui.components;

import utils.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {

    public enum ButtonStyle {
        PRIMARY, SECONDARY, DANGER, SUCCESS, UPDATE, GREEN_PILL, PURPLE_PILL
    }

    private ButtonStyle style;
    private boolean isHovered = false;
    private int cornerRadius = 20;

    public ModernButton(String text) {
        this(text, ButtonStyle.PRIMARY);
    }

    public ModernButton(String text, ButtonStyle style) {
        super(text);
        this.style = style;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (style == ButtonStyle.GREEN_PILL || style == ButtonStyle.PURPLE_PILL) {
            this.cornerRadius = 24;
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color baseColor;
        Color textColor = Color.WHITE;

        switch (style) {
            case SECONDARY:
                baseColor = ThemeManager.CARD_BG_LIGHT;
                textColor = ThemeManager.TEXT_DARK;
                break;
            case DANGER:
                baseColor = ThemeManager.DANGER_RED;
                break;
            case SUCCESS:
            case GREEN_PILL:
                baseColor = ThemeManager.ACCENT_GREEN;
                break;
            case UPDATE:
                baseColor = new Color(0x3B, 0x82, 0xF6); // Blue
                break;
            case PURPLE_PILL:
            case PRIMARY:
            default:
                baseColor = ThemeManager.PRIMARY_PURPLE;
                break;
        }

        if (isHovered) {
            if (style == ButtonStyle.GREEN_PILL || style == ButtonStyle.SUCCESS) {
                baseColor = ThemeManager.HOVER_GREEN;
            } else if (style == ButtonStyle.PRIMARY || style == ButtonStyle.PURPLE_PILL) {
                baseColor = ThemeManager.HOVER_PURPLE;
            } else {
                baseColor = baseColor.darker();
            }
        }

        // Draw rounded background
        g2.setColor(baseColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Draw border for secondary button
        if (style == ButtonStyle.SECONDARY) {
            g2.setColor(ThemeManager.BORDER_GRAY);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }

        // Text
        g2.setColor(textColor);
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), x, y);

        g2.dispose();
    }
}
