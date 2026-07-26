package ui.components;

import utils.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class AuthorAvatarCard extends JPanel {

    private final String authorName;
    private final Color badgeColor;

    public AuthorAvatarCard(String authorName, Color badgeColor) {
        this.authorName = authorName;
        this.badgeColor = badgeColor != null ? badgeColor : ThemeManager.PRIMARY_PURPLE;

        setPreferredSize(new Dimension(75, 80));
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel avatarCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Outer Green Ring Outline
                g2.setColor(ThemeManager.ACCENT_GREEN);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(4, 4, w - 8, h - 8);

                // Inner Filled Circle
                g2.setColor(badgeColor);
                g2.fillOval(8, 8, w - 16, h - 16);

                // Initials
                String[] parts = authorName.split(" ");
                String initials = parts.length > 0 ? parts[0].substring(0, 1) : "A";
                if (parts.length > 1) {
                    initials += parts[parts.length - 1].substring(0, 1);
                }

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(initials)) / 2;
                int y = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initials, x, y);

                g2.dispose();
            }
        };
        avatarCircle.setPreferredSize(new Dimension(75, 55));
        avatarCircle.setOpaque(false);

        JLabel lblName = new JLabel(truncate(authorName, 12), SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblName.setForeground(ThemeManager.TEXT_DARK);

        add(avatarCircle, BorderLayout.CENTER);
        add(lblName, BorderLayout.SOUTH);
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 2) + "..";
    }
}
