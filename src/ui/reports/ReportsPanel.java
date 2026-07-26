package ui.reports;

import controller.LibraryController;
import model.Book;
import model.BorrowHistory;
import model.Student;
import ui.components.ModernButton;
import ui.components.ModernCard;
import utils.ReportExporter;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ReportsPanel extends JPanel {

    private final LibraryController controller;

    public ReportsPanel(LibraryController controller) {
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(16, 16));
        setBackground(ThemeManager.LIGHT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel lblHeader = new JLabel("📊 Reports & Data Exporter");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(ThemeManager.TEXT_DARK);

        add(lblHeader, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        gridPanel.setOpaque(false);

        // 1. Books Export Card
        ModernCard c1 = createExportCard("📚 Book Inventory Report", "Export full catalog including ISBNs, shelves, and copy counts.", e -> exportBooks());
        // 2. Students Export Card
        ModernCard c2 = createExportCard("🎓 Student Directory Report", "Export student details, current borrowed counts, and unpaid fines.", e -> exportStudents());
        // 3. Borrow History Export Card
        ModernCard c3 = createExportCard("🔄 Circulation & Borrow History", "Export complete issue, return, and overdue transaction history.", e -> exportBorrowHistory());
        // 4. HTML Styled Interactive Report
        ModernCard c4 = createExportCard("🌐 Modern HTML Interactive Report", "Generate a web report with embedded CSS styling.", e -> exportHTMLReport());

        gridPanel.add(c1);
        gridPanel.add(c2);
        gridPanel.add(c3);
        gridPanel.add(c4);

        add(gridPanel, BorderLayout.CENTER);
    }

    private ModernCard createExportCard(String title, String description, java.awt.event.ActionListener action) {
        ModernCard card = new ModernCard();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(ThemeManager.REPORT_PURPLE);

        JLabel lblDesc = new JLabel("<html>" + description + "</html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(ThemeManager.TEXT_MUTED);

        ModernButton btnExport = new ModernButton("Generate Export", ModernButton.ButtonStyle.PRIMARY);
        btnExport.addActionListener(action);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblDesc, BorderLayout.CENTER);
        card.add(btnExport, BorderLayout.SOUTH);

        return card;
    }

    private void exportBooks() {
        try {
            List<Book> books = controller.getAllBooks();
            File dir = new File("assets/reports");
            if (!dir.exists()) dir.mkdirs();
            String path = ReportExporter.exportBooksToCSV(books, "assets/reports/books_report.csv");
            JOptionPane.showMessageDialog(this, "Books report exported successfully:\n" + path, "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportStudents() {
        try {
            List<Student> students = controller.getAllStudents();
            File dir = new File("assets/reports");
            if (!dir.exists()) dir.mkdirs();
            String path = ReportExporter.exportStudentsToCSV(students, "assets/reports/students_report.csv");
            JOptionPane.showMessageDialog(this, "Students report exported successfully:\n" + path, "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportBorrowHistory() {
        try {
            List<BorrowHistory> history = controller.getAllBorrowHistory();
            File dir = new File("assets/reports");
            if (!dir.exists()) dir.mkdirs();
            String path = ReportExporter.exportBorrowHistoryToCSV(history, "assets/reports/borrow_history_report.csv");
            JOptionPane.showMessageDialog(this, "Borrow history exported successfully:\n" + path, "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportHTMLReport() {
        try {
            List<Book> books = controller.getAllBooks();
            StringBuilder rows = new StringBuilder();
            for (Book b : books) {
                rows.append("<tr>")
                        .append("<td>").append(b.getBookId()).append("</td>")
                        .append("<td>").append(b.getIsbn()).append("</td>")
                        .append("<td><strong>").append(b.getTitle()).append("</strong></td>")
                        .append("<td>").append(b.getAuthor()).append("</td>")
                        .append("<td>").append(b.getAvailableCopies()).append(" / ").append(b.getTotalCopies()).append("</td>")
                        .append("</tr>");
            }
            File dir = new File("assets/reports");
            if (!dir.exists()) dir.mkdirs();
            String path = ReportExporter.generateHTMLReport("Book Inventory Audit", "System status generated live",
                    "<tr><th>ID</th><th>ISBN</th><th>Title</th><th>Author</th><th>Available Copies</th></tr>",
                    rows.toString(), "assets/reports/inventory_audit.html");
            JOptionPane.showMessageDialog(this, "HTML report generated:\n" + path, "HTML Report Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "HTML Export failed: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
