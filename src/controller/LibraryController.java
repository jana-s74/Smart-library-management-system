package controller;

import model.Book;
import model.BorrowHistory;
import model.Category;
import model.Student;
import model.User;
import service.*;

import java.util.List;
import java.util.Map;

public class LibraryController {

    private final AuthService authService;
    private final BookService bookService;
    private final BorrowService borrowService;
    private final StudentService studentService;
    private final AnalyticsService analyticsService;

    public LibraryController() {
        this.authService = new AuthService();
        this.bookService = new BookService();
        this.borrowService = new BorrowService();
        this.studentService = new StudentService();
        this.analyticsService = new AnalyticsService();
    }

    // Auth
    public User loginAdmin(String username, String password) {
        return authService.loginAdmin(username, password);
    }

    public Student loginStudent(String identifier, String password) {
        return authService.loginStudent(identifier, password);
    }

    public boolean registerStudent(Student student, String password) {
        return authService.registerStudent(student, password);
    }

    public User getCurrentUser() {
        return authService.getCurrentUser();
    }

    public void logout() {
        authService.logout();
    }

    // Books
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    public Book getBookById(int bookId) {
        return bookService.getBookByIdFast(bookId);
    }

    public boolean addBook(Book book) {
        return bookService.addBook(book);
    }

    public boolean updateBook(Book book) {
        return bookService.updateBook(book);
    }

    public boolean deleteBook(int bookId) {
        return bookService.deleteBook(bookId);
    }

    public List<Book> getSortedBooks(String criteria) {
        switch (criteria.toLowerCase()) {
            case "author":
                return bookService.getBooksSortedByAuthor();
            case "availability":
                return bookService.getBooksSortedByAvailability();
            case "title":
            default:
                return bookService.getBooksSortedByTitle();
        }
    }

    public Book searchBookByTitleBinary(String title) {
        return bookService.binarySearchByTitle(title);
    }

    // Borrow / Return
    public boolean issueBook(int studentId, int bookId, int loanDays) {
        return borrowService.issueBook(studentId, bookId, loanDays);
    }

    public boolean returnBook(int borrowId, int studentId, int bookId, java.sql.Timestamp dueDate) {
        return borrowService.returnBook(borrowId, studentId, bookId, dueDate);
    }

    public List<BorrowHistory> getAllBorrowHistory() {
        return borrowService.getAllBorrowHistory();
    }

    public List<BorrowHistory> getStudentBorrowHistory(int studentId) {
        return borrowService.getBorrowHistoryByStudent(studentId);
    }

    // Students
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    public boolean updateStudentStatus(int studentId, String status) {
        return studentService.updateStatus(studentId, status);
    }

    public boolean changePassword(String usernameOrEmail, String currentPassword, String newPassword) {
        return authService.changePassword(usernameOrEmail, currentPassword, newPassword);
    }

    // Analytics
    public Map<String, Object> getDashboardStats() {
        return analyticsService.getDashboardStats();
    }
}

