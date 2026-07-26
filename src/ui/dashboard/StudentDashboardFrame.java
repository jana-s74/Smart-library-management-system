package ui.dashboard;

import controller.LibraryController;
import model.Book;
import model.BorrowHistory;
import model.Student;
import ui.auth.LoginFrame;
import ui.components.*;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentDashboardFrame extends JFrame {

    private final LibraryController controller;
    private final Student student;

    private ModernSidebar sidebar;
    private TopHeaderBar headerBar;
    private JPanel contentCardPanel;
    private CardLayout cardLayout;

    private DefaultTableModel catalogModel;
    private DefaultTableModel historyModel;

    public StudentDashboardFrame(LibraryController controller, Student student) {
        this.controller = controller;
        this.student = student;
        initUI();
    }

    private void initUI() {
        setTitle("📚 LibraAI – Smart Library Portal");
        setSize(1240, 800);
        setMinimumSize(new Dimension(1100, 720));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Outer Soft Lavender Background Canvas
        JPanel bgCanvasPanel = new JPanel(new BorderLayout());
        bgCanvasPanel.setBackground(ThemeManager.LIGHT_BG);
        bgCanvasPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Main White Rounded Window Card Container
        JPanel windowCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
            }
        };
        windowCard.setOpaque(false);

        // 1. Sidebar (Left)
        sidebar = new ModernSidebar(student.getFullName(), "Student");
        sidebar.addMenuItem("home", "Home", "🏠");
        sidebar.addMenuItem("catalog", "Categories", "🗂️");
        sidebar.addMenuItem("favorite", "Favorite", "❤️");
        sidebar.addMenuItem("history", "Your Library", "📁");
        sidebar.addMenuItem("setting", "Setting", "⚙️");
        sidebar.addMenuItem("logout", "Log out", "🚪");

        sidebar.setSelectionListener(key -> {
            if (key.equals("logout")) {
                controller.logout();
                new LoginFrame().setVisible(true);
                dispose();
            } else {
                cardLayout.show(contentCardPanel, key);
            }
        });

        windowCard.add(sidebar, BorderLayout.WEST);

        // 2. Right Center Stack (Header + Workspace Content + Right Panel)
        JPanel rightArea = new JPanel(new BorderLayout());
        rightArea.setOpaque(false);

        // Top Header
        headerBar = new TopHeaderBar(student.getFullName());
        rightArea.add(headerBar, BorderLayout.NORTH);

        // Center Content Stack
        cardLayout = new CardLayout();
        contentCardPanel = new JPanel(cardLayout);
        contentCardPanel.setOpaque(false);

        contentCardPanel.add(createHomeDashboardPanel(), "home");
        contentCardPanel.add(createCatalogPanel(), "catalog");
        contentCardPanel.add(createHistoryPanel(), "history");
        contentCardPanel.add(createProfilePanel(), "setting");
        contentCardPanel.add(createHomeDashboardPanel(), "favorite");

        rightArea.add(contentCardPanel, BorderLayout.CENTER);
        windowCard.add(rightArea, BorderLayout.CENTER);

        bgCanvasPanel.add(windowCard);
        add(bgCanvasPanel);
    }

    private JPanel createHomeDashboardPanel() {
        JPanel homeRoot = new JPanel(new BorderLayout());
        homeRoot.setOpaque(false);

        // Scrollable Center Main Workspace
        JPanel centerBody = new JPanel();
        centerBody.setLayout(new BoxLayout(centerBody, BoxLayout.Y_AXIS));
        centerBody.setOpaque(false);
        centerBody.setBorder(new EmptyBorder(10, 20, 20, 20));

        // 1. Hero Banner Card
        HeroBannerCard heroCard = new HeroBannerCard();
        heroCard.setOnViewNowAction(() -> cardLayout.show(contentCardPanel, "catalog"));
        heroCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerBody.add(heroCard);
        centerBody.add(Box.createRigidArea(new Dimension(0, 20)));

        // 2. Popular Section Header
        JPanel popularHeader = createSectionHeader("Popular", e -> cardLayout.show(contentCardPanel, "catalog"));
        popularHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerBody.add(popularHeader);
        centerBody.add(Box.createRigidArea(new Dimension(0, 10)));

        // Popular Books Grid Row
        JPanel popularGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        popularGrid.setOpaque(false);
        popularGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<Book> books = controller.getAllBooks();
        String[] years = {"2000", "2020", "2016", "2002", "1997", "2014", "2018", "2022", "2015", "2017", "1988", "2011"};
        String[] ratings = {"4.8/5", "4.9/5", "4.7/5", "4.9/5", "5.0/5", "4.9/5", "4.8/5", "4.9/5", "4.7/5", "4.8/5", "4.9/5", "4.6/5"};

        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            String y = i < years.length ? years[i] : "2024";
            String r = i < ratings.length ? ratings[i] : "4.8/5";

            popularGrid.add(new PopularBookCard(b, y, r, () -> {
                JOptionPane.showMessageDialog(this,
                        "Book Details:\nTitle: " + b.getTitle() + "\nAuthor: " + b.getAuthor() + "\nISBN: " + b.getIsbn() + "\nAvailable Copies: " + b.getAvailableCopies(),
                        "Book Details", JOptionPane.INFORMATION_MESSAGE);
            }));
        }
        centerBody.add(popularGrid);
        centerBody.add(Box.createRigidArea(new Dimension(0, 20)));

        // 3. Top Authors Section Header
        JPanel authorsHeader = createSectionHeader("Top Authors", null);
        authorsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerBody.add(authorsHeader);
        centerBody.add(Box.createRigidArea(new Dimension(0, 10)));

        // Top Authors Row
        JPanel authorsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        authorsRow.setOpaque(false);
        authorsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] authors = {"Steve Krug", "Jake Knapp", "John Green", "Yuval Harari", "J.K. Rowling", "R. Kiyosaki", "K. Simpson"};
        Color[] badgeColors = {new Color(0x1F, 0x24, 0x21), new Color(0x02, 0x84, 0xC7), new Color(0xD9, 0x77, 0x06), new Color(0x73, 0x3D, 0xD9), new Color(0xDC, 0x26, 0x26), new Color(0x25, 0x63, 0xEB), new Color(0x16, 0xA3, 0x4A)};

        for (int i = 0; i < authors.length; i++) {
            authorsRow.add(new AuthorAvatarCard(authors[i], badgeColors[i]));
        }
        centerBody.add(authorsRow);
        centerBody.add(Box.createRigidArea(new Dimension(0, 20)));

        // 4. Stats Summary Row
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        statsRow.add(new StatPillCard("16", "Read books", "📚", new Color(0xED, 0xE9, 0xFE)));
        statsRow.add(new StatPillCard("19", "Authors", "👥", new Color(0xDC, 0xFC, 0xE7)));
        statsRow.add(new StatPillCard("02", "Reading books", "📖", new Color(0xFE, 0xF3, 0xC7)));

        centerBody.add(statsRow);

        JScrollPane scrollPane = new JScrollPane(centerBody);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        homeRoot.add(scrollPane, BorderLayout.CENTER);

        // Right Widgets Sidebar Panel
        RightWidgetsPanel rightPanel = new RightWidgetsPanel();
        homeRoot.add(rightPanel, BorderLayout.EAST);

        return homeRoot;
    }

    private JPanel createSectionHeader(String title, java.awt.event.ActionListener onViewAll) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(680, 26));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(ThemeManager.TEXT_DARK);

        header.add(lblTitle, BorderLayout.WEST);

        if (onViewAll != null) {
            JLabel lblViewAll = new JLabel("View all");
            lblViewAll.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblViewAll.setForeground(ThemeManager.ACCENT_GREEN);
            lblViewAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lblViewAll.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    onViewAll.actionPerformed(null);
                }
            });
            header.add(lblViewAll, BorderLayout.EAST);
        }

        return header;
    }

    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 24, 24, 24));

        JLabel lbl = new JLabel("📖 Available Book Library");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbl.setForeground(ThemeManager.TEXT_DARK);
        panel.add(lbl, BorderLayout.NORTH);

        String[] columns = {"Book ID", "ISBN", "Title", "Author", "Category", "Available", "Location"};
        catalogModel = new DefaultTableModel(columns, 0);
        ModernTable table = new ModernTable(catalogModel);

        loadCatalog();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottomAction = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        bottomAction.setOpaque(false);

        ModernButton btnBorrow = new ModernButton("Borrow Selected Book", ModernButton.ButtonStyle.GREEN_PILL);
        btnBorrow.setPreferredSize(new Dimension(200, 40));
        btnBorrow.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a book to borrow.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int bookId = (Integer) catalogModel.getValueAt(row, 0);
            if (controller.issueBook(student.getId(), bookId, 14)) {
                JOptionPane.showMessageDialog(this, "Book borrowed successfully for 14 days!", "Borrow Successful", JOptionPane.INFORMATION_MESSAGE);
                loadCatalog();
                loadHistory();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to borrow book. It might be out of stock.", "Borrow Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomAction.add(btnBorrow);
        panel.add(bottomAction, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 24, 24, 24));

        JLabel lbl = new JLabel("🔄 My Borrowed Books & Returns");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbl.setForeground(ThemeManager.TEXT_DARK);
        panel.add(lbl, BorderLayout.NORTH);

        String[] columns = {"Borrow ID", "Book Title", "ISBN", "Borrow Date", "Due Date", "Status", "Fine ($)"};
        historyModel = new DefaultTableModel(columns, 0);
        ModernTable table = new ModernTable(historyModel);

        loadHistory();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottomAction = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        bottomAction.setOpaque(false);

        ModernButton btnReturn = new ModernButton("Return Selected Book", ModernButton.ButtonStyle.PURPLE_PILL);
        btnReturn.setPreferredSize(new Dimension(200, 40));
        btnReturn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a borrowed record to return.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int borrowId = (Integer) historyModel.getValueAt(row, 0);
            List<BorrowHistory> list = controller.getStudentBorrowHistory(student.getId());
            BorrowHistory bh = list.stream().filter(b -> b.getBorrowId() == borrowId).findFirst().orElse(null);

            if (bh != null) {
                if (controller.returnBook(borrowId, student.getId(), bh.getBookId(), bh.getDueDate())) {
                    JOptionPane.showMessageDialog(this, "Book returned successfully!", "Return Complete", JOptionPane.INFORMATION_MESSAGE);
                    loadCatalog();
                    loadHistory();
                }
            }
        });

        bottomAction.add(btnReturn);
        panel.add(bottomAction, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 24, 24, 24));

        ModernCard card = new ModernCard();
        card.setPreferredSize(new Dimension(500, 320));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));
        card.setLayout(new GridLayout(6, 1, 10, 10));

        card.add(new JLabel("👤 Student Code: " + student.getStudentCode()));
        card.add(new JLabel("Full Name: " + student.getFullName()));
        card.add(new JLabel("Email: " + student.getEmail()));
        card.add(new JLabel("Department: " + student.getDepartment()));
        card.add(new JLabel("Year of Study: Year " + student.getYearOfStudy()));
        card.add(new JLabel("Total Fines Owed: $" + String.format("%.2f", student.getTotalFinesOwed())));

        panel.add(card);
        return panel;
    }

    private void loadCatalog() {
        if (catalogModel == null) return;
        catalogModel.setRowCount(0);
        List<Book> books = controller.getAllBooks();
        for (Book b : books) {
            catalogModel.addRow(new Object[]{
                    b.getBookId(),
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getCategoryName() != null ? b.getCategoryName() : "General",
                    b.getAvailableCopies() + " / " + b.getTotalCopies(),
                    "Shelf " + b.getShelfNumber() + " (Fl " + b.getFloorNumber() + ")"
            });
        }
    }

    private void loadHistory() {
        if (historyModel == null) return;
        historyModel.setRowCount(0);
        List<BorrowHistory> list = controller.getStudentBorrowHistory(student.getId());
        for (BorrowHistory bh : list) {
            historyModel.addRow(new Object[]{
                    bh.getBorrowId(),
                    bh.getBookTitle(),
                    bh.getIsbn(),
                    bh.getBorrowDate() != null ? bh.getBorrowDate().toString().substring(0, 10) : "",
                    bh.getDueDate() != null ? bh.getDueDate().toString().substring(0, 10) : "",
                    bh.getStatus(),
                    String.format("%.2f", bh.getFineAmount())
            });
        }
    }
}
