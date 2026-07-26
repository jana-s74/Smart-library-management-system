package ui.components;

import model.Book;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PopularBookCard extends JPanel {

    private final Book book;
    private boolean isHovered = false;
    private final Runnable onClickAction;

    public PopularBookCard(Book book, String year, String rating, Runnable onClickAction) {
        this.book = book;
        this.onClickAction = onClickAction;

        setPreferredSize(new Dimension(105, 175));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setLayout(new BorderLayout());

        // Cover Panel
        JPanel coverPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Drop shadow
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(3, 4, w - 6, h - 4, 10, 10);

                // Book Cover Base Background (Color depends on Category)
                Color coverBg = getCoverColor(book.getCategoryId());
                g2.setColor(coverBg);
                g2.fillRoundRect(0, 0, w - 6, h - 6, 8, 8);

                // Spine Shadow
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRect(0, 0, 5, h - 6);

                // Cover Title text on Graphic Cover
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));

                String displayTitle = book.getTitle();
                if (displayTitle.length() > 18) {
                    displayTitle = displayTitle.substring(0, 15) + "...";
                }
                FontMetrics fm = g2.getFontMetrics();
                int tx = 10;
                int ty = 25;
                g2.drawString(displayTitle, tx, ty);

                // Author snippet on cover
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                g2.setColor(new Color(255, 255, 255, 200));
                g2.drawString(book.getAuthor(), 10, ty + 15);

                // Decorative Cover Circle / Icon
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillOval(w / 2 - 16, h / 2 - 10, 28, 28);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString("📖", w / 2 - 10, h / 2 + 8);

                g2.dispose();
            }
        };
        coverPanel.setPreferredSize(new Dimension(105, 115));
        coverPanel.setOpaque(false);

        // Info Panel below cover
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(4, 2, 0, 2));

        JLabel lblTitle = new JLabel(truncate(book.getTitle(), 14));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(ThemeManager.TEXT_DARK);

        JLabel lblAuthor = new JLabel(truncate(book.getAuthor(), 12) + ", " + year);
        lblAuthor.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblAuthor.setForeground(ThemeManager.TEXT_MUTED);

        JLabel lblRating = new JLabel("⭐ " + rating);
        lblRating.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblRating.setForeground(ThemeManager.TEXT_MUTED);

        infoPanel.add(lblTitle);
        infoPanel.add(lblAuthor);
        infoPanel.add(lblRating);

        add(coverPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);

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

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClickAction != null) onClickAction.run();
            }
        });
    }

    private Color getCoverColor(int categoryId) {
        switch (categoryId % 5) {
            case 1: return new Color(0x1F, 0x24, 0x21); // Charcoal Black
            case 2: return new Color(0x73, 0x3D, 0xD9); // Purple
            case 3: return new Color(0x02, 0x84, 0xC7); // Blue
            case 4: return new Color(0xDC, 0x26, 0x26); // Red
            default: return new Color(0xD9, 0x77, 0x06); // Amber
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 2) + "..";
    }
}
