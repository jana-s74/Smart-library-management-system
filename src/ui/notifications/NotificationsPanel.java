package ui.notifications;

import controller.LibraryController;
import dao.NotificationDAO;
import model.Notification;
import ui.components.ModernCard;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class NotificationsPanel extends JPanel {

    private final LibraryController controller;
    private final NotificationDAO notificationDAO;
    private final JPanel listContainer;

    public NotificationsPanel(LibraryController controller) {
        this.controller = controller;
        this.notificationDAO = new NotificationDAO();

        setLayout(new BorderLayout(16, 16));
        setBackground(ThemeManager.LIGHT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel lblHeader = new JLabel("🔔 System Notifications");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(ThemeManager.TEXT_DARK);

        add(lblHeader, BorderLayout.NORTH);

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeManager.LIGHT_BG);

        add(scrollPane, BorderLayout.CENTER);

        loadNotifications();
    }

    public void loadNotifications() {
        listContainer.removeAll();
        List<Notification> list = notificationDAO.getNotificationsForUser("ADMIN", 1);
        if (list.isEmpty()) {
            JLabel lblEmpty = new JLabel("No active notifications.");
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lblEmpty.setForeground(ThemeManager.TEXT_MUTED);
            listContainer.add(lblEmpty);
        } else {
            for (Notification n : list) {
                ModernCard card = new ModernCard();
                card.setLayout(new BorderLayout(8, 8));
                card.setBorder(new EmptyBorder(14, 16, 14, 16));
                card.setMaximumSize(new Dimension(800, 70));

                JLabel lblTitle = new JLabel("📌 " + n.getTitle());
                lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lblTitle.setForeground(ThemeManager.PRIMARY_ORANGE);

                JLabel lblMsg = new JLabel(n.getMessage());
                lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblMsg.setForeground(ThemeManager.TEXT_DARK);

                card.add(lblTitle, BorderLayout.NORTH);
                card.add(lblMsg, BorderLayout.CENTER);

                listContainer.add(card);
                listContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        listContainer.revalidate();
        listContainer.repaint();
    }
}
