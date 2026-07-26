package ui.components;

import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ModernTextField extends JTextField {

    private String placeholder;
    private int cornerRadius = 10;

    public ModernTextField(String placeholder) {
        this.placeholder = placeholder;
        setOpaque(false);
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setForeground(ThemeManager.TEXT_DARK);
        setCaretColor(ThemeManager.PRIMARY_ORANGE);
        setBorder(new EmptyBorder(10, 14, 10, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        // Border
        if (hasFocus()) {
            g2.setColor(ThemeManager.PRIMARY_ORANGE);
            g2.setStroke(new BasicStroke(1.5f));
        } else {
            g2.setColor(ThemeManager.BORDER_GRAY);
        }
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        super.paintComponent(g);

        // Placeholder text
        if (getText().isEmpty() && !hasFocus()) {
            g2.setColor(ThemeManager.TEXT_MUTED);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, 14, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
        }

        g2.dispose();
    }
}
