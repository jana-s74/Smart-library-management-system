package dao;

import database.DatabaseConnection;
import model.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    public boolean addReview(Review review) {
        String query = "INSERT INTO Reviews (book_id, student_id, rating, review_text) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, review.getBookId());
            pstmt.setInt(2, review.getStudentId());
            pstmt.setInt(3, review.getRating());
            pstmt.setString(4, review.getReviewText());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Review> getReviewsByBook(int bookId) {
        List<Review> list = new ArrayList<>();
        String query = "SELECT r.*, s.full_name as student_name FROM Reviews r " +
                "JOIN Students s ON r.student_id = s.student_id " +
                "WHERE r.book_id = ? ORDER BY r.review_id DESC";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return list;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Review(
                        rs.getInt("review_id"),
                        rs.getInt("book_id"),
                        rs.getInt("student_id"),
                        rs.getString("student_name"),
                        rs.getInt("rating"),
                        rs.getString("review_text"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
