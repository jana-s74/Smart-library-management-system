package model;

import java.sql.Timestamp;

public class Reservation {
    private int reservationId;
    private int studentId;
    private String studentName;
    private int bookId;
    private String bookTitle;
    private Timestamp reservationDate;
    private String status; // PENDING, FULFILLED, CANCELLED
    private int queuePosition;

    public Reservation() {}

    public Reservation(int reservationId, int studentId, String studentName, int bookId, String bookTitle, Timestamp reservationDate, String status, int queuePosition) {
        this.reservationId = reservationId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.reservationDate = reservationDate;
        this.status = status;
        this.queuePosition = queuePosition;
    }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public Timestamp getReservationDate() { return reservationDate; }
    public void setReservationDate(Timestamp reservationDate) { this.reservationDate = reservationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getQueuePosition() { return queuePosition; }
    public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; }
}
