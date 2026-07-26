package ui.components;

import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TopHeaderBar extends JPanel {

    private final JTextField txtSearch;
    private Runnable onSearchAction;

    public TopHeaderBar(String userName) {
        setOpaque(false);
        setLayout(new BorderLayout(16, 0));
        setPreferredSize(new Dimension(0, 56));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        // 1. Left Search Bar (Pill with purple search button)
        JPanel searchBox = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF4, 0xF0, 0xFA));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.dispose();
            }
        };
        searchBox.setOpaque(false);
        searchBox.setPreferredSize(new Dimension(340, 38));
        searchBox.setBorder(new EmptyBorder(0, 16, 0, 4));

        txtSearch = new JTextField("Search your book...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setForeground(ThemeManager.TEXT_MUTED);
        txtSearch.setOpaque(false);
        txtSearch.setBorder(null);

        // Clear placeholder on focus
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().equals("Search your book...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(ThemeManager.TEXT_DARK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Search your book...");
                    txtSearch.setForeground(ThemeManager.TEXT_MUTED);
                }
            }
        });

        // Purple Magnifying Search Button
        ModernButton btnSearch = new ModernButton("🔍", ModernButton.ButtonStyle.PURPLE_PILL);
        btnSearch.setPreferredSize(new Dimension(30, 30));
        btnSearch.setCornerRadius(15);
        btnSearch.addActionListener(e -> {
            if (onSearchAction != null) onSearchAction.run();
        });

        searchBox.add(txtSearch, BorderLayout.CENTER);
        searchBox.add(btnSearch, BorderLayout.EAST);

        add(searchBox, BorderLayout.WEST);

        // 2. Right Accessories (Language, Bell, Profile Badge)
        JPanel rightAccessories = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightAccessories.setOpaque(false);

        // Language toggle
        JLabel lblLang = new JLabel("EN 🇬🇧");
        lblLang.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLang.setForeground(ThemeManager.TEXT_DARK);

        // Notification Bell Icon Button
        ModernButton btnBell = new ModernButton("🔔", ModernButton.ButtonStyle.SECONDARY);
        btnBell.setPreferredSize(new Dimension(36, 36));
        btnBell.setCornerRadius(18);

        // User Profile Badge
        JPanel profilePill = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(ThemeManager.BORDER_GRAY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        profilePill.setOpaque(false);
        profilePill.setPreferredSize(new Dimension(170, 38));
        profilePill.setBorder(new EmptyBorder(0, 4, 0, 8));

        // Avatar
        JLabel lblAvatar = new JLabel("👤");
        lblAvatar.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JLabel lblName = new JLabel(userName != null ? userName : "Entesar Alnahari");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(ThemeManager.TEXT_DARK);

        JLabel lblArrow = new JLabel("∨");
        lblArrow.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblArrow.setForeground(ThemeManager.TEXT_MUTED);

        profilePill.add(lblAvatar);
        profilePill.add(lblName);
        profilePill.add(lblArrow);

        rightAccessories.add(lblLang);
        rightAccessories.add(btnBell);
        rightAccessories.add(profilePill);

        add(rightAccessories, BorderLayout.EAST);
    }

    public String getSearchQuery() {
        String q = txtSearch.getText().trim();
        return q.equals("Search your book...") ? "" : q;
    }

    public void setOnSearchAction(Runnable action) {
        this.onSearchAction = action;
    }
}
