package utils.dsa;

import model.Book;

import java.util.HashMap;

/**
 * DSA Implementation: HashMap for O(1) Instant Book Lookups by ISBN and ID.
 */
public class BookHashMapIndex {

    private final HashMap<String, Book> isbnIndex;
    private final HashMap<Integer, Book> idIndex;

    public BookHashMapIndex() {
        this.isbnIndex = new HashMap<>();
        this.idIndex = new HashMap<>();
    }

    public void put(Book book) {
        if (book != null) {
            if (book.getIsbn() != null) {
                isbnIndex.put(book.getIsbn().toLowerCase().trim(), book);
            }
            idIndex.put(book.getBookId(), book);
        }
    }

    public Book getByIsbn(String isbn) {
        if (isbn == null) return null;
        return isbnIndex.get(isbn.toLowerCase().trim());
    }

    public Book getById(int bookId) {
        return idIndex.get(bookId);
    }

    public void remove(Book book) {
        if (book != null) {
            if (book.getIsbn() != null) {
                isbnIndex.remove(book.getIsbn().toLowerCase().trim());
            }
            idIndex.remove(book.getBookId());
        }
    }

    public void clear() {
        isbnIndex.clear();
        idIndex.clear();
    }

    public int size() {
        return idIndex.size();
    }
}
