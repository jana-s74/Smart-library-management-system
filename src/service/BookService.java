package service;

import dao.BookDAO;
import model.Book;
import utils.QRCodeGenerator;
import utils.dsa.BinarySearchUtil;
import utils.dsa.BookHashMapIndex;
import utils.dsa.MergeSortUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BookService {

    private final BookDAO bookDAO;
    private final BookHashMapIndex bookIndex;

    public BookService() {
        this.bookDAO = new BookDAO();
        this.bookIndex = new BookHashMapIndex();
        refreshCache();
    }

    public void refreshCache() {
        bookIndex.clear();
        List<Book> books = bookDAO.getAllBooks();
        for (Book b : books) {
            bookIndex.put(b);
        }
    }

    public List<Book> getAllBooks() {
        return bookDAO.getAllBooks();
    }

    public Book getBookByIdFast(int bookId) {
        Book b = bookIndex.getById(bookId);
        if (b == null) {
            b = bookDAO.getBookById(bookId);
            if (b != null) bookIndex.put(b);
        }
        return b;
    }

    public Book getBookByIsbnFast(String isbn) {
        return bookIndex.getByIsbn(isbn);
    }

    public boolean addBook(Book book) {
        if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
            String qrPath = QRCodeGenerator.generateAndSave("BOOK:" + book.getIsbn() + ":" + book.getTitle(), "QR_" + book.getIsbn().replaceAll("[^a-zA-Z0-9]", ""));
            book.setQrCodePath(qrPath);
        }
        boolean success = bookDAO.addBook(book);
        if (success) refreshCache();
        return success;
    }

    public boolean updateBook(Book book) {
        boolean success = bookDAO.updateBook(book);
        if (success) refreshCache();
        return success;
    }

    public boolean deleteBook(int bookId) {
        boolean success = bookDAO.deleteBook(bookId);
        if (success) refreshCache();
        return success;
    }

    // DSA Merge Sort integration
    public List<Book> getBooksSortedByTitle() {
        List<Book> all = getAllBooks();
        return MergeSortUtil.sort(all, MergeSortUtil.TITLE_COMPARATOR);
    }

    public List<Book> getBooksSortedByAuthor() {
        List<Book> all = getAllBooks();
        return MergeSortUtil.sort(all, MergeSortUtil.AUTHOR_COMPARATOR);
    }

    public List<Book> getBooksSortedByAvailability() {
        List<Book> all = getAllBooks();
        return MergeSortUtil.sort(all, MergeSortUtil.COPIES_COMPARATOR);
    }

    // DSA Binary Search integration
    public Book binarySearchByTitle(String title) {
        List<Book> sortedBooks = getBooksSortedByTitle();
        return BinarySearchUtil.searchByTitle(sortedBooks, title);
    }
}
