package utils.dsa;

import model.Book;
import java.util.List;

/**
 * DSA Implementation: Binary Search for fast O(log N) searching on sorted lists of Books.
 */
public class BinarySearchUtil {

    /**
     * Binary search by Title (requires list to be sorted by Title ascending).
     */
    public static Book searchByTitle(List<Book> books, String targetTitle) {
        if (books == null || targetTitle == null || books.isEmpty()) return null;

        int low = 0;
        int high = books.size() - 1;
        String search = targetTitle.trim().toLowerCase();

        while (low <= high) {
            int mid = low + (high - low) / 2;
            Book midBook = books.get(mid);
            String midTitle = midBook.getTitle().trim().toLowerCase();

            int cmp = midTitle.compareTo(search);
            if (cmp == 0) {
                return midBook;
            } else if (cmp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return null;
    }

    /**
     * Binary search by ISBN (requires list to be sorted by ISBN ascending).
     */
    public static Book searchByIsbn(List<Book> books, String targetIsbn) {
        if (books == null || targetIsbn == null || books.isEmpty()) return null;

        int low = 0;
        int high = books.size() - 1;
        String search = targetIsbn.trim().toLowerCase();

        while (low <= high) {
            int mid = low + (high - low) / 2;
            Book midBook = books.get(mid);
            String midIsbn = midBook.getIsbn().trim().toLowerCase();

            int cmp = midIsbn.compareTo(search);
            if (cmp == 0) {
                return midBook;
            } else if (cmp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return null;
    }
}
