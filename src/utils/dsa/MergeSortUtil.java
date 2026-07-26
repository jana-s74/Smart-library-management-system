package utils.dsa;

import model.Book;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * DSA Implementation: Custom Merge Sort Algorithm O(N log N) for sorting Book collections.
 */
public class MergeSortUtil {

    public static List<Book> sort(List<Book> books, Comparator<Book> comparator) {
        if (books == null || books.size() <= 1) {
            return books != null ? new ArrayList<>(books) : new ArrayList<>();
        }
        List<Book> list = new ArrayList<>(books);
        mergeSort(list, 0, list.size() - 1, comparator);
        return list;
    }

    private static void mergeSort(List<Book> list, int left, int right, Comparator<Book> comparator) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(list, left, mid, comparator);
            mergeSort(list, mid + 1, right, comparator);
            merge(list, left, mid, right, comparator);
        }
    }

    private static void merge(List<Book> list, int left, int mid, int right, Comparator<Book> comparator) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        List<Book> leftList = new ArrayList<>(n1);
        List<Book> rightList = new ArrayList<>(n2);

        for (int i = 0; i < n1; i++) leftList.add(list.get(left + i));
        for (int j = 0; j < n2; j++) rightList.add(list.get(mid + 1 + j));

        int i = 0, j = 0, k = left;

        while (i < n1 && j < n2) {
            if (comparator.compare(leftList.get(i), rightList.get(j)) <= 0) {
                list.set(k, leftList.get(i));
                i++;
            } else {
                list.set(k, rightList.get(j));
                j++;
            }
            k++;
        }

        while (i < n1) {
            list.set(k, leftList.get(i));
            i++;
            k++;
        }

        while (j < n2) {
            list.set(k, rightList.get(j));
            j++;
            k++;
        }
    }

    // Predefined comparators
    public static final Comparator<Book> TITLE_COMPARATOR = (b1, b2) ->
            b1.getTitle().compareToIgnoreCase(b2.getTitle());

    public static final Comparator<Book> AUTHOR_COMPARATOR = (b1, b2) ->
            b1.getAuthor().compareToIgnoreCase(b2.getAuthor());

    public static final Comparator<Book> COPIES_COMPARATOR = (b1, b2) ->
            Integer.compare(b2.getAvailableCopies(), b1.getAvailableCopies());
}
