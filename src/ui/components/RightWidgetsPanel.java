package ui.components;

import model.Book;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RightWidgetsPanel extends JPanel {

    public RightWidgetsPanel() {
        setPreferredSize(new Dimension(240, 0));
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(16, 12, 16, 16));

        // 1. Subscribe Card
        JPanel subCard = createSubscribeCard();
        add(subCard);
        add(Box.createRigidArea(new Dimension(0, 16)));

        // 2. Join Community Banner
        JPanel joinBanner = createJoinCommunityBanner();
        add(joinBanner);
        add(Box.createRigidArea(new Dimension(0, 16)));

        // 3. Next Books Section
        JPanel nextBooksPanel = createNextBooksPanel();
        add(nextBooksPanel);
    }

    private JPanel createSubscribeCard() {
        JPanel card = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(ThemeManager.BORDER_GRAY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(212, 54));
        card.setMaximumSize(new Dimension(212, 54));
        card.setBorder(new EmptyBorder(10, 14, 10, 10));

        JLabel lblText = new JLabel("Subscribe to Our Blog");
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblText.setForeground(ThemeManager.TEXT_DARK);

        // Purple Envelope Icon Button
        ModernButton btnMail = new ModernButton("✉", ModernButton.ButtonStyle.PURPLE_PILL);
        btnMail.setPreferredSize(new Dimension(34, 34));
        btnMail.setCornerRadius(12);

        card.add(lblText, BorderLayout.WEST);
        card.add(btnMail, BorderLayout.EAST);

        return card;
    }

    private JPanel createJoinCommunityBanner() {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Gradient Background
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x73, 0x3D, 0xD9), w, h, new Color(0x59, 0x26, 0xB5));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 20, 20);

                // Wave overlay accent
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillArc(-30, h - 60, w + 60, 80, 0, 180);

                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setPreferredSize(new Dimension(212, 170));
        banner.setMaximumSize(new Dimension(212, 170));
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(new EmptyBorder(20, 16, 20, 16));

        JLabel lblMsg = new JLabel("<html><center>Join a community of over <b>5000 Book Lovers</b> here now</center></html>");
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMsg.setForeground(Color.WHITE);
        lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);

        ModernButton btnJoin = new ModernButton("Join Now", ModernButton.ButtonStyle.GREEN_PILL);
        btnJoin.setPreferredSize(new Dimension(120, 36));
        btnJoin.setMaximumSize(new Dimension(120, 36));
        btnJoin.setAlignmentX(Component.CENTER_ALIGNMENT);

        banner.add(lblMsg);
        banner.add(Box.createRigidArea(new Dimension(0, 16)));
        banner.add(btnJoin);

        return banner;
    }

    private JPanel createNextBooksPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lblHeader = new JLabel("Next Books");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeader.setForeground(ThemeManager.TEXT_DARK);
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblHeader);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createMiniBookRow("Fault In Our Stars", "John Green", "02:20:03", new Color(0x02, 0x84, 0xC7)));
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(createMiniBookRow("Never Eat Alone", "Keith Ferrazzi", "02:20:03", new Color(0xD9, 0x77, 0x06)));
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(createMiniBookRow("The Book Thief", "Markus Zusak", "02:20:03", new Color(0x1F, 0x24, 0x21)));

        return panel;
    }

    private JPanel createMiniBookRow(String title, String author, String timer, Color coverColor) {
        JPanel row = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF7, 0xF5, 0xFA));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(212, 46));
        row.setMaximumSize(new Dimension(212, 46));
        row.setBorder(new EmptyBorder(6, 8, 6, 8));

        // Thumbnail
        JPanel thumb = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(coverColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g2.drawString("📖", 6, 22);
                g2.dispose();
            }
        };
        thumb.setPreferredSize(new Dimension(26, 34));
        thumb.setOpaque(false);

        // Titles
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblT.setForeground(ThemeManager.TEXT_DARK);

        JLabel lblA = new JLabel(author);
        lblA.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblA.setForeground(ThemeManager.TEXT_MUTED);

        center.add(lblT);
        center.add(lblA);

        // Timer label
        JLabel lblTime = new JLabel(timer);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblTime.setForeground(ThemeManager.TEXT_MUTED);

        row.add(thumb, BorderLayout.WEST);
        row.add(center, BorderLayout.CENTER);
        row.add(lblTime, BorderLayout.EAST);

        return row;
    }
}
