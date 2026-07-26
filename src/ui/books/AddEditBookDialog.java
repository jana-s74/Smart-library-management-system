package ui.books;

import controller.LibraryController;
import model.Book;
import ui.components.ModernButton;
import ui.components.ModernTextField;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddEditBookDialog extends JDialog {

    private final LibraryController controller;
    private final Book bookToEdit;
    private final Runnable onSaveCallback;

    private ModernTextField txtIsbn, txtTitle, txtAuthor, txtPublisher, txtShelf, txtRack, txtCopies;
    private JComboBox<Integer> cbFloor;
    private JComboBox<Integer> cbCategory;

    public AddEditBookDialog(JFrame owner, LibraryController controller, Book bookToEdit, Runnable onSaveCallback) {
        super(owner, bookToEdit == null ? "Add New Book" : "Edit Book Information", true);
        this.controller = controller;
        this.bookToEdit = bookToEdit;
        this.onSaveCallback = onSaveCallback;
        initUI();
    }

    private void initUI() {
        setSize(480, 560);
        setLocationRelativeTo(getOwner());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(24, 32, 24, 32));
        panel.setBackground(ThemeManager.LIGHT_BG);

        JLabel lblTitle = new JLabel(bookToEdit == null ? "📚 Add New Book" : "✏️ Edit Book");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(ThemeManager.PRIMARY_ORANGE);

        txtIsbn = new ModernTextField("ISBN (e.g. 978-0134685991)");
        txtTitle = new ModernTextField("Book Title");
        txtAuthor = new ModernTextField("Author Name");
        txtPublisher = new ModernTextField("Publisher");
        txtShelf = new ModernTextField("Shelf Number (e.g. CS-101)");
        txtRack = new ModernTextField("Rack Number (e.g. R-01)");
        txtCopies = new ModernTextField("Total Copies (e.g. 5)");

        cbFloor = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        cbCategory = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});

        if (bookToEdit != null) {
            txtIsbn.setText(bookToEdit.getIsbn());
            txtTitle.setText(bookToEdit.getTitle());
            txtAuthor.setText(bookToEdit.getAuthor());
            txtPublisher.setText(bookToEdit.getPublisher());
            txtShelf.setText(bookToEdit.getShelfNumber());
            txtRack.setText(bookToEdit.getRackNumber());
            txtCopies.setText(String.valueOf(bookToEdit.getTotalCopies()));
            cbFloor.setSelectedItem(bookToEdit.getFloorNumber());
            cbCategory.setSelectedItem(bookToEdit.getCategoryId());
        }

        ModernButton btnSave = new ModernButton("Save Book", ModernButton.ButtonStyle.PRIMARY);
        btnSave.setMaximumSize(new Dimension(400, 42));
        btnSave.addActionListener(e -> handleSave());

        panel.add(lblTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 14)));
        panel.add(txtIsbn);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(txtTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(txtAuthor);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(txtPublisher);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(txtShelf);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(txtRack);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(txtCopies);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        panel.add(cbFloor);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));
        panel.add(btnSave);

        add(panel);
    }

    private void handleSave() {
        try {
            String isbn = txtIsbn.getText().trim();
            String title = txtTitle.getText().trim();
            String author = txtAuthor.getText().trim();
            String publisher = txtPublisher.getText().trim();
            String shelf = txtShelf.getText().trim();
            String rack = txtRack.getText().trim();
            int copies = Integer.parseInt(txtCopies.getText().trim());
            int floor = (Integer) cbFloor.getSelectedItem();

            if (isbn.isEmpty() || title.isEmpty() || author.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ISBN, Title, and Author are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Book book = bookToEdit != null ? bookToEdit : new Book();
            book.setIsbn(isbn);
            book.setTitle(title);
            book.setAuthor(author);
            book.setPublisher(publisher);
            book.setShelfNumber(shelf);
            book.setRackNumber(rack);
            book.setFloorNumber(floor);
            book.setTotalCopies(copies);
            if (bookToEdit == null) {
                book.setAvailableCopies(copies);
                book.setCategoryId(1);
            }

            boolean success = bookToEdit == null ? controller.addBook(book) : controller.updateBook(book);
            if (success) {
                if (onSaveCallback != null) onSaveCallback.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for copies.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }
}
