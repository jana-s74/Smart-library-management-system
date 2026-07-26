package model;

import java.sql.Timestamp;

public class FineHistory {
    private int fineId;
    private int studentId;
    private String studentName;
    private int borrowId;
    private String bookTitle;
    private double amount;
    private String reason;
    private String paymentStatus; // UNPAID, PAID
    private Timestamp paidAt;

    public FineHistory() {}

    public FineHistory(int fineId, int studentId, String studentName, int borrowId, String bookTitle, double amount, String reason, String paymentStatus, Timestamp paidAt) {
        this.fineId = fineId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.borrowId = borrowId;
        this.bookTitle = bookTitle;
        this.amount = amount;
        this.reason = reason;
        this.paymentStatus = paymentStatus;
        this.paidAt = paidAt;
    }

    public int getFineId() { return fineId; }
    public void setFineId(int fineId) { this.fineId = fineId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public int getBorrowId() { return borrowId; }
    public void setBorrowId(int borrowId) { this.borrowId = borrowId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Timestamp getPaidAt() { return paidAt; }
    public void setPaidAt(Timestamp paidAt) { this.paidAt = paidAt; }
}
