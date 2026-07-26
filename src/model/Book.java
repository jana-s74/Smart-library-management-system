package model;

import java.sql.Timestamp;

public class Book {
    private int bookId;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private int categoryId;
    private String categoryName;
    private String language;
    private String edition;
    private String description;
    private String shelfNumber;
    private String rackNumber;
    private int floorNumber;
    private int totalCopies;
    private int availableCopies;
    private String coverImagePath;
    private String qrCodePath;
    private Timestamp createdAt;

    public Book() {}

    public Book(int bookId, String isbn, String title, String author, String publisher, int categoryId, String categoryName, String language, String edition, String description, String shelfNumber, String rackNumber, int floorNumber, int totalCopies, int availableCopies, String coverImagePath, String qrCodePath, Timestamp createdAt) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.language = language;
        this.edition = edition;
        this.description = description;
        this.shelfNumber = shelfNumber;
        this.rackNumber = rackNumber;
        this.floorNumber = floorNumber;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.coverImagePath = coverImagePath;
        this.qrCodePath = qrCodePath;
        this.createdAt = createdAt;
    }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getShelfNumber() { return shelfNumber; }
    public void setShelfNumber(String shelfNumber) { this.shelfNumber = shelfNumber; }

    public String getRackNumber() { return rackNumber; }
    public void setRackNumber(String rackNumber) { this.rackNumber = rackNumber; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public String getCoverImagePath() { return coverImagePath; }
    public void setCoverImagePath(String coverImagePath) { this.coverImagePath = coverImagePath; }

    public String getQrCodePath() { return qrCodePath; }
    public void setQrCodePath(String qrCodePath) { this.qrCodePath = qrCodePath; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return title + " - " + author + " (" + isbn + ")";
    }
}
