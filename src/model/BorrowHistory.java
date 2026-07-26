package model;

import java.sql.Timestamp;

public class BorrowHistory {
    private int borrowId;
    private int studentId;
    private String studentName;
    private String studentCode;
    private int bookId;
    private String bookTitle;
    private String isbn;
    private Timestamp borrowDate;
    private Timestamp dueDate;
    private Timestamp returnDate;
    private String status; // BORROWED, RETURNED, OVERDUE
    private double fineAmount;
    private boolean finePaid;

    public BorrowHistory() {}

    public BorrowHistory(int borrowId, int studentId, String studentName, String studentCode, int bookId, String bookTitle, String isbn, Timestamp borrowDate, Timestamp dueDate, Timestamp returnDate, String status, double fineAmount, boolean finePaid) {
        this.borrowId = borrowId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentCode = studentCode;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.isbn = isbn;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.fineAmount = fineAmount;
        this.finePaid = finePaid;
    }

    public int getBorrowId() { return borrowId; }
    public void setBorrowId(int borrowId) { this.borrowId = borrowId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Timestamp getBorrowDate() { return borrowDate; }
    public void setBorrowDate(Timestamp borrowDate) { this.borrowDate = borrowDate; }

    public Timestamp getDueDate() { return dueDate; }
    public void setDueDate(Timestamp dueDate) { this.dueDate = dueDate; }

    public Timestamp getReturnDate() { return returnDate; }
    public void setReturnDate(Timestamp returnDate) { this.returnDate = returnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }

    public boolean isFinePaid() { return finePaid; }
    public void setFinePaid(boolean finePaid) { this.finePaid = finePaid; }
}
