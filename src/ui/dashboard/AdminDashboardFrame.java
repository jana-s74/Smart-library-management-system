package ui.dashboard;

import controller.LibraryController;
import model.User;
import ui.auth.LoginFrame;
import ui.books.BookManagementPanel;
import ui.components.*;
import ui.notifications.NotificationsPanel;
import ui.reports.ReportsPanel;
import ui.students.StudentManagementPanel;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class AdminDashboardFrame extends JFrame {

    private final LibraryController controller;
    private final User adminUser;

    private ModernSidebar sidebar;
    private JPanel contentCardPanel;
    private CardLayout cardLayout;

    // Sub-panels
    private JPanel overviewPanel;
    private BookManagementPanel bookPanel;
    private StudentManagementPanel studentPanel;
    private ReportsPanel reportsPanel;
    private NotificationsPanel notificationsPanel;

    public AdminDashboardFrame(LibraryController controller, User adminUser) {
        this.controller = controller;
        this.adminUser = adminUser;
        initUI();
    }

    private void initUI() {
        setTitle("📚 LibraAI – System Administrator Portal (" + adminUser.getFullName() + ")");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(ThemeManager.LIGHT_BG);

        // Sidebar
        sidebar = new ModernSidebar(adminUser.getFullName(), "System Admin");
        sidebar.addMenuItem("dashboard", "Dashboard", "📊");
        sidebar.addMenuItem("books", "Books Catalog", "📚");
        sidebar.addMenuItem("students", "Student Directory", "🎓");
        sidebar.addMenuItem("reports", "Reports & Audit", "📈");
        sidebar.addMenuItem("notifications", "Notifications", "🔔");

        sidebar.setSelectionListener(key -> cardLayout.show(contentCardPanel, key));

        rootPanel.add(sidebar, BorderLayout.WEST);

        // Right Main Section
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setOpaque(false);

        // Header Bar
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);
        headerBar.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel lblWelcome = new JLabel("Welcome back, " + adminUser.getFullName() + " 👋");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblWelcome.setForeground(ThemeManager.TEXT_DARK);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        headerActions.setOpaque(false);

        JToggleButton btnThemeToggle = new JToggleButton("🌙 Dark Mode");
        btnThemeToggle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThemeToggle.addActionListener(e -> {
            boolean dark = btnThemeToggle.isSelected();
            ThemeManager.setDarkMode(dark);
            btnThemeToggle.setText(dark ? "☀️ Light Mode" : "🌙 Dark Mode");
            repaint();
        });

        ModernButton btnLogout = new ModernButton("Logout", ModernButton.ButtonStyle.SECONDARY);
        btnLogout.setPreferredSize(new Dimension(90, 34));
        btnLogout.addActionListener(e -> {
            controller.logout();
            new LoginFrame().setVisible(true);
            dispose();
        });

        headerActions.add(btnThemeToggle);
        headerActions.add(btnLogout);

        headerBar.add(lblWelcome, BorderLayout.WEST);
        headerBar.add(headerActions, BorderLayout.EAST);

        rightContainer.add(headerBar, BorderLayout.NORTH);

        // Cards Stack
        cardLayout = new CardLayout();
        contentCardPanel = new JPanel(cardLayout);
        contentCardPanel.setOpaque(false);

        overviewPanel = createOverviewPanel();
        bookPanel = new BookManagementPanel(controller);
        studentPanel = new StudentManagementPanel(controller);
        reportsPanel = new ReportsPanel(controller);
        notificationsPanel = new NotificationsPanel(controller);

        contentCardPanel.add(overviewPanel, "dashboard");
        contentCardPanel.add(bookPanel, "books");
        contentCardPanel.add(studentPanel, "students");
        contentCardPanel.add(reportsPanel, "reports");
        contentCardPanel.add(notificationsPanel, "notifications");

        rightContainer.add(contentCardPanel, BorderLayout.CENTER);
        rootPanel.add(rightContainer, BorderLayout.CENTER);

        add(rootPanel);
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 24, 24, 24));

        Map<String, Object> stats = controller.getDashboardStats();

        // Top KPI Stat Cards Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 16, 16));
        statsGrid.setOpaque(false);

        StatCard card1 = new StatCard("Total Book Copies", String.valueOf(stats.getOrDefault("totalBookCopies", 0)), "Across all shelves", ThemeManager.PRIMARY_ORANGE, "📚");
        StatCard card2 = new StatCard("Available Copies", String.valueOf(stats.getOrDefault("availableCopies", 0)), "Ready to borrow", ThemeManager.SUCCESS_GREEN, "✅");
        StatCard card3 = new StatCard("Active Students", String.valueOf(stats.getOrDefault("totalStudents", 0)), "Registered accounts", new Color(0x3B, 0x82, 0xF6), "🎓");
        StatCard card4 = new StatCard("Total Fines Owed", String.format("$%.2f", stats.getOrDefault("totalFinesOwed", 0.0)), "Pending collection", ThemeManager.DANGER_RED, "💰");

        statsGrid.add(card1);
        statsGrid.add(card2);
        statsGrid.add(card3);
        statsGrid.add(card4);

        panel.add(statsGrid, BorderLayout.NORTH);

        // Center Visual Analytics Chart Panel
        SimpleChartPanel chartPanel = new SimpleChartPanel("📊 Library Category Distribution");
        chartPanel.addData("Computer Sci", 18);
        chartPanel.addData("Data Science", 14);
        chartPanel.addData("Physics", 9);
        chartPanel.addData("Mathematics", 12);
        chartPanel.addData("Literature", 7);

        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }
}
