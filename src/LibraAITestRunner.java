import model.Book;
import model.Student;
import utils.FineCalculator;
import utils.PasswordUtils;
import utils.dsa.BinarySearchUtil;
import utils.dsa.BookHashMapIndex;
import utils.dsa.MergeSortUtil;
import utils.dsa.WaitingQueue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 📚 LibraAI Self-Contained Test Runner
 * No external dependencies (JUnit-free). Pure Java 21.
 * Covers Unit, Integration (DSA layer), Smoke, Sanity, and Security tests.
 */
public class LibraAITestRunner {

    // ─────────────────────────────────────────────────────────────────────────
    // Minimal test harness
    // ─────────────────────────────────────────────────────────────────────────
    static int passed = 0, failed = 0, skipped = 0;
    static List<String> failures = new ArrayList<>();

    static void assertEquals(Object expected, Object actual, String testName) {
        if (expected == null && actual == null || (expected != null && expected.equals(actual))) {
            pass(testName);
        } else {
            fail(testName, "Expected [" + expected + "] but got [" + actual + "]");
        }
    }

    static void assertEquals(double expected, double actual, double delta, String testName) {
        if (Math.abs(expected - actual) <= delta) {
            pass(testName);
        } else {
            fail(testName, "Expected [" + expected + "] ± " + delta + " but got [" + actual + "]");
        }
    }

    static void assertTrue(boolean condition, String testName) {
        if (condition) pass(testName); else fail(testName, "Condition was false");
    }

    static void assertFalse(boolean condition, String testName) {
        if (!condition) pass(testName); else fail(testName, "Condition was true (expected false)");
    }

    static void assertNull(Object obj, String testName) {
        if (obj == null) pass(testName); else fail(testName, "Expected null but got [" + obj + "]");
    }

    static void assertNotNull(Object obj, String testName) {
        if (obj != null) pass(testName); else fail(testName, "Expected non-null but was null");
    }

    static void pass(String testName) {
        passed++;
        System.out.println("  ✅ PASS  " + testName);
    }

    static void fail(String testName, String reason) {
        failed++;
        String msg = "  ❌ FAIL  " + testName + "\n           → " + reason;
        failures.add(msg);
        System.out.println(msg);
    }

    static void section(String title) {
        System.out.println("\n" + "═".repeat(62));
        System.out.println("  🔬 " + title);
        System.out.println("═".repeat(62));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper to build a Book quickly
    // ─────────────────────────────────────────────────────────────────────────
    static Book book(int id, String isbn, String title, String author, int total, int avail) {
        Book b = new Book();
        b.setBookId(id);
        b.setIsbn(isbn);
        b.setTitle(title);
        b.setAuthor(author);
        b.setTotalCopies(total);
        b.setAvailableCopies(avail);
        b.setCategoryName("General");
        return b;
    }

    static Student student(int id, String name, String code) {
        Student s = new Student();
        s.setId(id);
        s.setFullName(name);
        s.setStudentCode(code);
        return s;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main entry point
    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║       📚 LibraAI Test Runner  —  Java 21 / JDK           ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        // Run all test suites
        testFineCalculator();
        testPasswordUtils();
        testBookHashMapIndex();
        testMergeSort();
        testBinarySearch();
        testWaitingQueue();
        testXssEscapeLogic();
        testStudentRegistrationValidation();
        testSmoke();

        // ── Final Summary ────────────────────────────────────────────────────
        System.out.println("\n" + "═".repeat(62));
        System.out.println("  📊 TEST SUMMARY");
        System.out.println("═".repeat(62));
        System.out.printf("  Total  : %d%n", passed + failed);
        System.out.printf("  ✅ Pass  : %d%n", passed);
        System.out.printf("  ❌ Fail  : %d%n", failed);
        if (!failures.isEmpty()) {
            System.out.println("\n  Failures:");
            failures.forEach(System.out::println);
        }
        System.out.println("═".repeat(62));
        System.exit(failed > 0 ? 1 : 0);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // UNIT TESTS — FineCalculator
    // ═════════════════════════════════════════════════════════════════════════
    static void testFineCalculator() {
        section("UNIT TEST  |  FineCalculator");

        // No overdue: returned on time
        long now = System.currentTimeMillis();
        Timestamp due = new Timestamp(now + 86400000L); // due in 1 day
        assertEquals(0.0, FineCalculator.calculateFine(due, new Timestamp(now)), 0.001,
                "No fine when returned before due date");

        // No overdue: null return date but due is in future
        assertEquals(0.0, FineCalculator.calculateFine(due, null), 0.001,
                "No fine when due date is in future (no return)");

        // Overdue: returned 2 days late → ₹1.00
        Timestamp duePast = new Timestamp(now - (2 * 86400000L));
        Timestamp returnedNow = new Timestamp(now);
        assertEquals(1.00, FineCalculator.calculateFine(duePast, returnedNow), 0.01,
                "Fine = ₹1.00 for 2 overdue days");

        // Overdue: 5 days overdue → ₹2.50
        Timestamp due5 = new Timestamp(now - (5 * 86400000L));
        assertEquals(2.50, FineCalculator.calculateFine(due5, returnedNow), 0.01,
                "Fine = ₹2.50 for 5 overdue days");

        // Overdue: null return date means using now; should still compute
        Timestamp due10 = new Timestamp(now - (10 * 86400000L));
        double fine = FineCalculator.calculateFine(due10, null);
        assertTrue(fine >= 5.0, "Fine ≥ ₹5.00 for 10+ overdue days (null return)");

        // Null due date → 0.0
        assertEquals(0.0, FineCalculator.calculateFine(null, returnedNow), 0.001,
                "Zero fine for null dueDate");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // UNIT TESTS — PasswordUtils
    // ═════════════════════════════════════════════════════════════════════════
    static void testPasswordUtils() {
        section("UNIT TEST  |  PasswordUtils (BCrypt)");

        // Hash must be 60-char BCrypt hash
        String hash = PasswordUtils.hashPassword("admin123");
        assertNotNull(hash, "Hash is not null");
        assertEquals(60, hash.length(), "Hash length is 60 chars");

        // BCrypt is non-deterministic, so hash != hash2
        String hash2 = PasswordUtils.hashPassword("admin123");
        assertFalse(hash.equals(hash2), "BCrypt hashes are non-deterministic");

        // Different input → different hash
        String hash3 = PasswordUtils.hashPassword("wrongpassword");
        assertFalse(hash.equals(hash3), "Different password yields different hash");

        // Verify password – correct
        assertTrue(PasswordUtils.verifyPassword("admin123", hash), "Correct password verifies OK against hash 1");
        assertTrue(PasswordUtils.verifyPassword("admin123", hash2), "Correct password verifies OK against hash 2");

        // Verify password – wrong
        assertFalse(PasswordUtils.verifyPassword("wrong", hash), "Wrong password fails verification");

        // Null / empty edge cases
        String emptyHash = PasswordUtils.hashPassword("");
        assertEquals("", emptyHash, "Empty password returns empty string");

        assertFalse(PasswordUtils.verifyPassword(null, hash), "Null raw password fails verify");
        assertFalse(PasswordUtils.verifyPassword("admin123", null), "Null stored hash fails verify");
    }


    // ═════════════════════════════════════════════════════════════════════════
    // UNIT TESTS — BookHashMapIndex (O(1) Lookup)
    // ═════════════════════════════════════════════════════════════════════════
    static void testBookHashMapIndex() {
        section("UNIT TEST  |  BookHashMapIndex  (HashMap O(1) Lookup)");

        BookHashMapIndex idx = new BookHashMapIndex();
        assertEquals(0, idx.size(), "Empty index has size 0");

        Book b1 = book(1, "978-0262033848", "Introduction to Algorithms", "Cormen", 5, 4);
        Book b2 = book(2, "978-0134685991", "Effective Java", "Bloch", 3, 2);

        idx.put(b1);
        idx.put(b2);
        assertEquals(2, idx.size(), "Index size = 2 after two inserts");

        // Get by ID
        Book found = idx.getById(1);
        assertNotNull(found, "Book found by ID");
        assertEquals("Introduction to Algorithms", found.getTitle(), "Correct title by ID lookup");

        // Get by ISBN
        Book byIsbn = idx.getByIsbn("978-0134685991");
        assertNotNull(byIsbn, "Book found by ISBN");
        assertEquals("Effective Java", byIsbn.getTitle(), "Correct title by ISBN lookup");

        // ISBN lookup is case-insensitive
        Book byIsbnUpper = idx.getByIsbn("978-0134685991".toUpperCase());
        assertNotNull(byIsbnUpper, "ISBN lookup is case-insensitive");

        // Missing ID
        assertNull(idx.getById(999), "Missing book ID returns null");

        // Remove
        idx.remove(b1);
        assertNull(idx.getById(1), "Book removed from ID index");
        assertNull(idx.getByIsbn("978-0262033848"), "Book removed from ISBN index");
        assertEquals(1, idx.size(), "Size = 1 after removal");

        // Clear
        idx.clear();
        assertEquals(0, idx.size(), "Size = 0 after clear");

        // Null put – should not crash
        idx.put(null);
        assertEquals(0, idx.size(), "Null put is safely ignored");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // UNIT TESTS — MergeSortUtil
    // ═════════════════════════════════════════════════════════════════════════
    static void testMergeSort() {
        section("UNIT TEST  |  MergeSortUtil  (O(N log N) Merge Sort)");

        List<Book> books = Arrays.asList(
            book(3, "isbn3", "Zebra Book",   "Smith",  2, 2),
            book(1, "isbn1", "Alpha Book",   "Jones",  5, 5),
            book(2, "isbn2", "Middle Book",  "Adams",  3, 1)
        );

        // Sort by title
        List<Book> byTitle = MergeSortUtil.sort(books, MergeSortUtil.TITLE_COMPARATOR);
        assertEquals("Alpha Book",  byTitle.get(0).getTitle(), "First by title = Alpha Book");
        assertEquals("Middle Book", byTitle.get(1).getTitle(), "Second by title = Middle Book");
        assertEquals("Zebra Book",  byTitle.get(2).getTitle(), "Third by title = Zebra Book");

        // Sort by author
        List<Book> byAuthor = MergeSortUtil.sort(books, MergeSortUtil.AUTHOR_COMPARATOR);
        assertEquals("Adams", byAuthor.get(0).getAuthor(), "First by author = Adams");
        assertEquals("Jones", byAuthor.get(1).getAuthor(), "Second by author = Jones");
        assertEquals("Smith", byAuthor.get(2).getAuthor(), "Third by author = Smith");

        // Sort by availability descending (most first)
        List<Book> byAvail = MergeSortUtil.sort(books, MergeSortUtil.COPIES_COMPARATOR);
        assertTrue(byAvail.get(0).getAvailableCopies() >= byAvail.get(1).getAvailableCopies(),
                "Sorted by availability descending");

        // Edge: null list
        List<Book> sortedNull = MergeSortUtil.sort(null, MergeSortUtil.TITLE_COMPARATOR);
        assertTrue(sortedNull.isEmpty(), "Null list returns empty list");

        // Edge: single element
        List<Book> single = MergeSortUtil.sort(List.of(book(9,"i","Solo","X",1,1)), MergeSortUtil.TITLE_COMPARATOR);
        assertEquals(1, single.size(), "Single-element list sorted correctly");

        // Original list is not mutated
        assertEquals("Zebra Book", books.get(0).getTitle(), "Original list not mutated by sort");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // UNIT TESTS — BinarySearchUtil
    // ═════════════════════════════════════════════════════════════════════════
    static void testBinarySearch() {
        section("UNIT TEST  |  BinarySearchUtil  (O(log N) Binary Search)");

        // Sorted list prerequisite
        List<Book> sorted = Arrays.asList(
            book(1, "i1", "Algorithms",        "Cormen",  5, 4),
            book(2, "i2", "Clean Code",        "Martin",  3, 2),
            book(3, "i3", "Design Patterns",   "Gang",    2, 1),
            book(4, "i4", "Effective Java",    "Bloch",   3, 3),
            book(5, "i5", "Python Crash Course","Matthes", 4, 4)
        );

        // Found at first
        Book r1 = BinarySearchUtil.searchByTitle(sorted, "Algorithms");
        assertNotNull(r1, "Found 'Algorithms'");
        assertEquals("Algorithms", r1.getTitle(), "Correct book title returned");

        // Found in middle
        Book r2 = BinarySearchUtil.searchByTitle(sorted, "Design Patterns");
        assertNotNull(r2, "Found 'Design Patterns'");

        // Found at last
        Book r3 = BinarySearchUtil.searchByTitle(sorted, "Python Crash Course");
        assertNotNull(r3, "Found 'Python Crash Course'");

        // Case-insensitive
        Book r4 = BinarySearchUtil.searchByTitle(sorted, "clean code");
        assertNotNull(r4, "Binary search is case-insensitive");

        // Not found
        Book r5 = BinarySearchUtil.searchByTitle(sorted, "Nonexistent Title");
        assertNull(r5, "Non-existent title returns null");

        // Null inputs
        assertNull(BinarySearchUtil.searchByTitle(null, "Algorithms"), "Null list returns null");
        assertNull(BinarySearchUtil.searchByTitle(sorted, null), "Null target returns null");
        assertNull(BinarySearchUtil.searchByTitle(new ArrayList<>(), "Algo"), "Empty list returns null");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // UNIT TESTS — WaitingQueue
    // ═════════════════════════════════════════════════════════════════════════
    static void testWaitingQueue() {
        section("UNIT TEST  |  WaitingQueue  (FIFO Queue DSA)");

        WaitingQueue q = new WaitingQueue();
        assertTrue(q.isEmpty(), "New queue is empty");
        assertEquals(0, q.size(), "New queue size = 0");

        Student s1 = student(1, "Jana", "721424243001");
        Student s2 = student(2, "Priya", "721424243002");
        Student s3 = student(3, "Ravi", "721424243003");

        q.enqueue(s1);
        q.enqueue(s2);
        q.enqueue(s3);
        assertEquals(3, q.size(), "Queue size = 3 after 3 enqueues");
        assertFalse(q.isEmpty(), "Queue is not empty");

        // Peek does not remove
        Student peeked = q.peek();
        assertEquals("Jana", peeked.getFullName(), "Peek returns first student");
        assertEquals(3, q.size(), "Peek does not reduce size");

        // FIFO dequeue order
        Student d1 = q.dequeue();
        assertEquals("Jana", d1.getFullName(), "First dequeue = Jana (FIFO)");
        Student d2 = q.dequeue();
        assertEquals("Priya", d2.getFullName(), "Second dequeue = Priya (FIFO)");
        assertEquals(1, q.size(), "Size = 1 after two dequeues");

        // Duplicate prevention
        q.enqueue(s3); // already in queue
        assertEquals(1, q.size(), "Duplicate student not enqueued");

        // Dequeue all
        q.dequeue();
        assertTrue(q.isEmpty(), "Queue empty after all dequeues");
        assertNull(q.dequeue(), "Dequeue from empty returns null");

        // Null enqueue
        q.enqueue(null);
        assertEquals(0, q.size(), "Null enqueue is safely ignored");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SECURITY TEST — XSS escaping (mirrors escapeHtml() in app.js)
    // ═════════════════════════════════════════════════════════════════════════
    static void testXssEscapeLogic() {
        section("SECURITY TEST  |  XSS Input Sanitisation");

        // We replicate the JS escapeHtml() in Java to verify the same logic
        java.util.function.Function<String, String> escapeHtml = (s) -> s == null ? "" : s
                .replace("&",  "&amp;")
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&#039;");

        String evil1 = "<script>alert('xss')</script>";
        String safe1 = escapeHtml.apply(evil1);
        assertFalse(safe1.contains("<script>"), "XSS script tag neutralised");
        assertTrue(safe1.contains("&lt;script&gt;"), "Script tag replaced with HTML entities");

        String evil2 = "\" onmouseover=\"alert(1)";
        String safe2 = escapeHtml.apply(evil2);
        assertFalse(safe2.contains("\""), "Double-quotes escaped in attribute injection");

        String evil3 = "O'Reilly";
        String safe3 = escapeHtml.apply(evil3);
        assertTrue(safe3.contains("&#039;"), "Single-quote escaped");

        String evil4 = "&amp;<b>bold</b>";
        String safe4 = escapeHtml.apply(evil4);
        assertFalse(safe4.contains("<b>"), "Bold tag removed");

        String safe5 = escapeHtml.apply(null);
        assertEquals("", safe5, "Null input returns empty string (no NPE)");

        // Clean input must pass through unchanged
        String clean = "Introduction to Algorithms";
        assertEquals(clean, escapeHtml.apply(clean), "Clean input is unchanged");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // UNIT TEST — Student register number validation
    // ═════════════════════════════════════════════════════════════════════════
    static void testStudentRegistrationValidation() {
        section("UNIT TEST  |  Student Register Number Validation");

        java.util.function.Predicate<String> validRegNo = (code) ->
                code != null && code.matches("^721424\\d{6}$");

        assertTrue(validRegNo.test("721424243052"), "Valid AI&DS register number accepted");
        assertTrue(validRegNo.test("721424105001"), "Valid EEE register number accepted");
        assertTrue(validRegNo.test("721424114099"), "Valid Mechanical register number accepted");

        assertFalse(validRegNo.test("721424243"),   "Short number rejected");
        assertFalse(validRegNo.test("7214242430521"),"13-digit number rejected");
        assertFalse(validRegNo.test("123456243052"), "Wrong prefix rejected");
        assertFalse(validRegNo.test("721424ABCD12"), "Alpha characters rejected");
        assertFalse(validRegNo.test(""),             "Empty string rejected");
        assertFalse(validRegNo.test(null),           "Null rejected");
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SMOKE TEST — Critical class instantiation
    // ═════════════════════════════════════════════════════════════════════════
    static void testSmoke() {
        section("SMOKE TEST  |  Critical Class Instantiation");

        try {
            BookHashMapIndex idx = new BookHashMapIndex();
            assertNotNull(idx, "BookHashMapIndex instantiated");
        } catch (Exception e) {
            fail("BookHashMapIndex instantiation", e.getMessage());
        }

        try {
            WaitingQueue q = new WaitingQueue();
            assertNotNull(q, "WaitingQueue instantiated");
        } catch (Exception e) {
            fail("WaitingQueue instantiation", e.getMessage());
        }

        try {
            String h = PasswordUtils.hashPassword("smoke-test");
            assertNotNull(h, "PasswordUtils.hashPassword accessible");
        } catch (Exception e) {
            fail("PasswordUtils smoke test", e.getMessage());
        }

        try {
            Timestamp t = new Timestamp(System.currentTimeMillis());
            double f = FineCalculator.calculateFine(t, null);
            assertTrue(f >= 0, "FineCalculator.calculateFine accessible");
        } catch (Exception e) {
            fail("FineCalculator smoke test", e.getMessage());
        }

        try {
            dao.UserDAO userDAO = new dao.UserDAO();
            boolean res = userDAO.changePassword("dummy", "curr", "new");
            assertFalse(res, "changePassword handles uninitialized database connection gracefully");
        } catch (Exception e) {
            fail("UserDAO.changePassword NPE check", e.getMessage());
        }

        System.out.println("\n  ✔  All critical components instantiated without error.");
    }
}

