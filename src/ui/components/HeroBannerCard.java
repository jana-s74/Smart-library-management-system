package ui.components;

import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HeroBannerCard extends JPanel {

    private Runnable onViewNowAction;

    public HeroBannerCard() {
        setPreferredSize(new Dimension(680, 160));
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(22, 28, 20, 20));

        JLabel lblTitle = new JLabel("MOST READ BOOKS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblTitle2 = new JLabel("THESE MONTHS");
        lblTitle2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle2.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("view to trending books in this months");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(230, 220, 255, 220));

        ModernButton btnViewNow = new ModernButton("VIEW NOW", ModernButton.ButtonStyle.GREEN_PILL);
        btnViewNow.setPreferredSize(new Dimension(120, 36));
        btnViewNow.setMaximumSize(new Dimension(120, 36));
        btnViewNow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnViewNow.addActionListener(e -> {
            if (onViewNowAction != null) onViewNowAction.run();
        });

        textPanel.add(lblTitle);
        textPanel.add(lblTitle2);
        textPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        textPanel.add(lblSub);
        textPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        textPanel.add(btnViewNow);

        add(textPanel, BorderLayout.WEST);
    }

    public void setOnViewNowAction(Runnable action) {
        this.onViewNowAction = action;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Purple Gradient Card Background
        GradientPaint gp = new GradientPaint(0, 0, new Color(0x73, 0x3D, 0xD9), w, h, new Color(0x54, 0x25, 0xAB));
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, w, h, 24, 24);

        // Soft Decorative Wave Shapes
        g2.setColor(new Color(255, 255, 255, 25));
        g2.fillArc(w - 260, -40, 280, 240, 180, 180);
        g2.fillArc(w - 320, 60, 300, 200, 0, 180);

        // Illustration on Right: Stand & Open Book & Person
        int rx = w - 190;
        int ry = h - 130;

        // Big Open Book
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(rx, ry + 20, 95, 65, 8, 8);
        g2.setColor(new Color(0x6C, 0xB3, 0x3F));
        g2.fillRect(rx + 45, ry + 20, 5, 65); // Book Spine

        // Page text lines
        g2.setColor(new Color(0x9E, 0xD4, 0x86));
        g2.setStroke(new BasicStroke(2f));
        for (int i = 0; i < 4; i++) {
            g2.drawLine(rx + 10, ry + 32 + (i * 10), rx + 40, ry + 32 + (i * 10));
            g2.drawLine(rx + 55, ry + 32 + (i * 10), rx + 85, ry + 32 + (i * 10));
        }

        // Person graphic
        g2.setColor(new Color(0x6C, 0xB3, 0x3F)); // Green shirt
        g2.fillOval(rx + 105, ry + 15, 14, 14); // Head
        g2.fillRect(rx + 107, ry + 30, 10, 35); // Body
        g2.setColor(new Color(0x33, 0x33, 0x44));
        g2.fillRect(rx + 107, ry + 65, 4, 40); // Left Leg
        g2.fillRect(rx + 113, ry + 65, 4, 40); // Right Leg

        g2.dispose();
        super.paintComponent(g);
    }
}
