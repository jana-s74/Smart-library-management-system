package ui.components;

import utils.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class ModernCard extends JPanel {

    private int cornerRadius = 16;
    private Color backgroundColor;

    public ModernCard() {
        this(ThemeManager.getCardBackgroundColor());
    }

    public ModernCard(Color bg) {
        this.backgroundColor = bg;
        setOpaque(false);
        setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Soft Drop Shadow
        g2.setColor(new Color(0, 0, 0, 12));
        g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, cornerRadius, cornerRadius);

        // Card Fill
        g2.setColor(backgroundColor != null ? backgroundColor : ThemeManager.getCardBackgroundColor());
        g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius);

        // Subtle Border
        g2.setColor(ThemeManager.BORDER_GRAY);
        g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}
