package ui.components;

import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatCard extends ModernCard {

    public StatCard(String title, String value, String subtitle, Color accentColor, String emojiIcon) {
        super();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(ThemeManager.TEXT_MUTED);

        JLabel lblIcon = new JLabel(emojiIcon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        topRow.add(lblTitle, BorderLayout.WEST);
        topRow.add(lblIcon, BorderLayout.EAST);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        centerPanel.setOpaque(false);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(accentColor != null ? accentColor : ThemeManager.TEXT_DARK);
        centerPanel.add(lblValue);

        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(ThemeManager.TEXT_MUTED);

        add(topRow, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(lblSubtitle, BorderLayout.SOUTH);
    }
}
