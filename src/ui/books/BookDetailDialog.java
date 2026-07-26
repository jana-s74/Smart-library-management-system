package ui.books;

import model.Book;
import ui.components.ModernButton;
import ui.components.ModernCard;
import utils.QRCodeGenerator;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BookDetailDialog extends JDialog {

    public BookDetailDialog(JFrame owner, Book book) {
        super(owner, "Book Specification & Location - " + book.getTitle(), true);
        initUI(book);
    }

    private void initUI(Book book) {
        setSize(580, 520);
        setLocationRelativeTo(getOwner());

        JPanel mainPanel = new JPanel(new BorderLayout(16, 16));
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        mainPanel.setBackground(ThemeManager.LIGHT_BG);

        // Header Title
        JLabel lblTitle = new JLabel("📖 " + book.getTitle());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(ThemeManager.PRIMARY_ORANGE);

        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Content Panel (Details + QR Code)
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 16, 16));
        contentPanel.setOpaque(false);

        // Left Details Card
        ModernCard infoCard = new ModernCard();
        infoCard.setBorder(new EmptyBorder(16, 16, 16, 16));
        infoCard.setLayout(new GridLayout(9, 1, 4, 4));

        infoCard.add(new JLabel("Author: " + book.getAuthor()));
        infoCard.add(new JLabel("Publisher: " + book.getPublisher()));
        infoCard.add(new JLabel("ISBN: " + book.getIsbn()));
        infoCard.add(new JLabel("Category: " + (book.getCategoryName() != null ? book.getCategoryName() : "General")));
        infoCard.add(new JLabel("Language: " + book.getLanguage()));
        infoCard.add(new JLabel("Edition: " + book.getEdition()));
        infoCard.add(new JLabel("Shelf Location: Shelf " + book.getShelfNumber() + ", Rack " + book.getRackNumber()));
        infoCard.add(new JLabel("Floor Level: Floor " + book.getFloorNumber()));
        infoCard.add(new JLabel("Copies: " + book.getAvailableCopies() + " available / " + book.getTotalCopies() + " total"));

        // Right QR Code Card
        ModernCard qrCard = new ModernCard();
        qrCard.setLayout(new BorderLayout());
        qrCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblQrHeader = new JLabel("Smart QR Shelf Code", SwingConstants.CENTER);
        lblQrHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Generate QR Image
        Image qrImg = QRCodeGenerator.generateQRCodeImage("BOOK:" + book.getIsbn() + ":" + book.getTitle(), 180, 180);
        JLabel lblQrImage = new JLabel(new ImageIcon(qrImg));
        lblQrImage.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblLoc = new JLabel("📍 Find on Floor " + book.getFloorNumber() + " -> Shelf " + book.getShelfNumber(), SwingConstants.CENTER);
        lblLoc.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblLoc.setForeground(ThemeManager.PRIMARY_ORANGE);

        qrCard.add(lblQrHeader, BorderLayout.NORTH);
        qrCard.add(lblQrImage, BorderLayout.CENTER);
        qrCard.add(lblLoc, BorderLayout.SOUTH);

        contentPanel.add(infoCard);
        contentPanel.add(qrCard);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Bottom Close Button
        ModernButton btnClose = new ModernButton("Close", ModernButton.ButtonStyle.SECONDARY);
        btnClose.addActionListener(e -> dispose());
        mainPanel.add(btnClose, BorderLayout.SOUTH);

        add(mainPanel);
    }
}
