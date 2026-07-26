package ui.components;

import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ModernSidebar extends JPanel {

    public interface SidebarSelectionListener {
        void onItemSelected(String itemKey);
    }

    private final List<SidebarMenuItem> menuItems = new ArrayList<>();
    private String activeKey = "";
    private SidebarSelectionListener selectionListener;

    public ModernSidebar(String userName, String userRole) {
        setPreferredSize(new Dimension(220, 0));
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.BORDER_GRAY));

        // Header / Brand Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 20));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 10));

        // Purple Logo Badge
        JPanel logoBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.PRIMARY_PURPLE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                g2.drawString("🎓", 8, 24);
                g2.dispose();
            }
        };
        logoBadge.setPreferredSize(new Dimension(36, 36));

        JLabel lblBrand = new JLabel("LibraAI");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBrand.setForeground(ThemeManager.TEXT_DARK);

        headerPanel.add(logoBadge);
        headerPanel.add(lblBrand);

        add(headerPanel, BorderLayout.NORTH);

        // Center Menu Panel
        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setOpaque(false);
        menuContainer.setBorder(new EmptyBorder(10, 16, 10, 16));

        add(menuContainer, BorderLayout.CENTER);

        // Bottom Illustration Panel
        JPanel illustrationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Person stick/vector illustration
                g2.setColor(new Color(0x6C, 0x3B, 0xB8)); // Purple shirt
                g2.fillOval(20, h - 75, 14, 14); // Head
                g2.fillRoundRect(17, h - 58, 20, 30, 8, 8); // Body

                // Legs
                g2.setColor(new Color(0x33, 0x33, 0x44));
                g2.fillRect(20, h - 28, 6, 25);
                g2.fillRect(29, h - 28, 6, 25);

                // Book Stand & Open Book
                g2.setColor(new Color(0xE0, 0xDE, 0xE8));
                g2.drawRoundRect(55, h - 65, 80, 50, 6, 6);
                g2.setColor(new Color(0x73, 0x3D, 0xD9));
                g2.setStroke(new BasicStroke(2f));
                g2.drawArc(65, h - 50, 30, 20, 0, 180);
                g2.drawArc(95, h - 50, 30, 20, 0, 180);

                // Text lines in book
                g2.setColor(new Color(0xAA, 0xA8, 0xBF));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(70, h - 42, 90, h - 42);
                g2.drawLine(70, h - 36, 90, h - 36);
                g2.drawLine(100, h - 42, 120, h - 42);
                g2.drawLine(100, h - 36, 120, h - 36);

                g2.dispose();
            }
        };
        illustrationPanel.setOpaque(false);
        illustrationPanel.setPreferredSize(new Dimension(220, 130));

        add(illustrationPanel, BorderLayout.SOUTH);
    }

    public void addMenuItem(String key, String title, String icon) {
        SidebarMenuItem item = new SidebarMenuItem(key, title, icon);
        menuItems.add(item);

        JPanel menuContainer = (JPanel) getComponent(1);
        menuContainer.add(item);
        menuContainer.add(Box.createRigidArea(new Dimension(0, 8)));

        if (menuItems.size() == 1) {
            setActiveItem(key);
        }
    }

    public void setActiveItem(String key) {
        this.activeKey = key;
        for (SidebarMenuItem item : menuItems) {
            item.setActive(item.getKey().equals(key));
        }
        if (selectionListener != null) {
            selectionListener.onItemSelected(key);
        }
    }

    public void setSelectionListener(SidebarSelectionListener listener) {
        this.selectionListener = listener;
    }

    private class SidebarMenuItem extends JPanel {
        private final String key;
        private boolean isActive = false;
        private boolean isHovered = false;
        private final JLabel label;

        public SidebarMenuItem(String key, String title, String icon) {
            this.key = key;
            setOpaque(false);
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(188, 42));
            setMaximumSize(new Dimension(188, 42));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(0, 16, 0, 16));

            label = new JLabel(icon + "   " + title);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            label.setForeground(new Color(0x70, 0x73, 0x7C));

            add(label, BorderLayout.WEST);

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
                    setActiveItem(key);
                }
            });
        }

        public String getKey() {
            return key;
        }

        public void setActive(boolean active) {
            this.isActive = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isActive) {
                // Active Pill with Vibrant Green
                g2.setColor(ThemeManager.ACCENT_GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                label.setForeground(Color.WHITE);
            } else if (isHovered) {
                g2.setColor(new Color(0xF4, 0xF0, 0xFA));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                label.setForeground(ThemeManager.PRIMARY_PURPLE);
            } else {
                label.setForeground(new Color(0x70, 0x73, 0x7C));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
