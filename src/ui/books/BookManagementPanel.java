package ui.books;

import controller.LibraryController;
import model.Book;
import ui.components.ModernButton;
import ui.components.ModernTable;
import ui.components.ModernTextField;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BookManagementPanel extends JPanel {

    private final LibraryController controller;
    private ModernTable table;
    private DefaultTableModel tableModel;
    private ModernTextField txtSearch;
    private JComboBox<String> cbSort;

    public BookManagementPanel(LibraryController controller) {
        this.controller = controller;
        initUI();
        loadBooks(controller.getAllBooks());
    }

    private void initUI() {
        setLayout(new BorderLayout(16, 16));
        setBackground(ThemeManager.LIGHT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Top Toolbar Panel
        JPanel toolbar = new JPanel(new BorderLayout(12, 12));
        toolbar.setOpaque(false);

        JLabel lblHeader = new JLabel("📚 Book Inventory & Catalog");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(ThemeManager.TEXT_DARK);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        txtSearch = new ModernTextField("Search by Title or ISBN...");
        txtSearch.setPreferredSize(new Dimension(240, 38));

        ModernButton btnSearch = new ModernButton("Search (Binary)", ModernButton.ButtonStyle.SECONDARY);
        btnSearch.setPreferredSize(new Dimension(140, 38));
        btnSearch.addActionListener(e -> handleSearch());

        cbSort = new JComboBox<>(new String[]{"Sort by Title", "Sort by Author", "Sort by Availability"});
        cbSort.setPreferredSize(new Dimension(160, 38));
        cbSort.addActionListener(e -> handleSort());

        ModernButton btnAdd = new ModernButton("+ Add New Book", ModernButton.ButtonStyle.PRIMARY);
        btnAdd.setPreferredSize(new Dimension(150, 38));
        btnAdd.addActionListener(e -> new AddEditBookDialog((JFrame) SwingUtilities.getWindowAncestor(this), controller, null, this::refreshTable).setVisible(true));

        actionPanel.add(txtSearch);
        actionPanel.add(btnSearch);
        actionPanel.add(cbSort);
        actionPanel.add(btnAdd);

        toolbar.add(lblHeader, BorderLayout.WEST);
        toolbar.add(actionPanel, BorderLayout.EAST);

        add(toolbar, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"ID", "ISBN", "Title", "Author", "Category", "Shelf", "Rack", "Floor", "Total", "Available", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 10; // Only actions column
            }
        };

        table = new ModernTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomBar.setOpaque(false);

        ModernButton btnViewDetails = new ModernButton("View Book Details & QR Code", ModernButton.ButtonStyle.SECONDARY);
        btnViewDetails.addActionListener(e -> handleViewDetails());

        ModernButton btnDelete = new ModernButton("Delete Book", ModernButton.ButtonStyle.DANGER);
        btnDelete.addActionListener(e -> handleDelete());

        bottomBar.add(btnViewDetails);
        bottomBar.add(btnDelete);

        add(bottomBar, BorderLayout.SOUTH);
    }

    public void refreshTable() {
        loadBooks(controller.getAllBooks());
    }

    private void loadBooks(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getBookId(),
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getCategoryName() != null ? b.getCategoryName() : "General",
                    b.getShelfNumber(),
                    b.getRackNumber(),
                    b.getFloorNumber(),
                    b.getTotalCopies(),
                    b.getAvailableCopies(),
                    "Manage"
            });
        }
    }

    private void handleSearch() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            refreshTable();
            return;
        }

        // DSA Binary Search
        Book found = controller.searchBookByTitleBinary(query);
        if (found != null) {
            loadBooks(List.of(found));
        } else {
            JOptionPane.showMessageDialog(this, "No book found matching: " + query, "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleSort() {
        String selected = (String) cbSort.getSelectedItem();
        if (selected == null) return;

        List<Book> sorted;
        if (selected.contains("Author")) {
            sorted = controller.getSortedBooks("author");
        } else if (selected.contains("Availability")) {
            sorted = controller.getSortedBooks("availability");
        } else {
            sorted = controller.getSortedBooks("title");
        }
        loadBooks(sorted);
    }

    private void handleViewDetails() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int bookId = (Integer) tableModel.getValueAt(row, 0);
        Book b = controller.getBookById(bookId);
        if (b != null) {
            new BookDetailDialog((JFrame) SwingUtilities.getWindowAncestor(this), b).setVisible(true);
        }
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int bookId = (Integer) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete book ID " + bookId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.deleteBook(bookId)) {
                refreshTable();
                JOptionPane.showMessageDialog(this, "Book deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
