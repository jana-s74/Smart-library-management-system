package ui.components;

import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatPillCard extends JPanel {

    public StatPillCard(String countText, String labelText, String icon, Color iconBg) {
        setPreferredSize(new Dimension(170, 56));
        setOpaque(false);
        setLayout(new BorderLayout(10, 0));
        setBorder(new EmptyBorder(8, 12, 8, 12));

        // Icon Box
        JPanel iconBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg != null ? iconBg : new Color(0xEE, 0xE7, 0xFA));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                g2.drawString(icon, 8, 24);
                g2.dispose();
            }
        };
        iconBox.setPreferredSize(new Dimension(36, 36));
        iconBox.setOpaque(false);

        // Text stack
        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);

        JLabel lblCount = new JLabel(countText);
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCount.setForeground(ThemeManager.TEXT_DARK);

        JLabel lblTitle = new JLabel(labelText);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblTitle.setForeground(ThemeManager.TEXT_MUTED);

        textStack.add(lblCount);
        textStack.add(lblTitle);

        add(iconBox, BorderLayout.WEST);
        add(textStack, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // White card container with rounded pill border
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

        g2.setColor(ThemeManager.BORDER_GRAY);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

        g2.dispose();
        super.paintComponent(g);
    }
}
