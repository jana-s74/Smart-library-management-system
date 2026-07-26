package model;

import java.sql.Timestamp;

public class Review {
    private int reviewId;
    private int bookId;
    private int studentId;
    private String studentName;
    private int rating;
    private String reviewText;
    private Timestamp createdAt;

    public Review() {}

    public Review(int reviewId, int bookId, int studentId, String studentName, int rating, String reviewText, Timestamp createdAt) {
        this.reviewId = reviewId;
        this.bookId = bookId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.rating = rating;
        this.reviewText = reviewText;
        this.createdAt = createdAt;
    }

    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
