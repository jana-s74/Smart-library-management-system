package dao;

import database.DatabaseConnection;
import model.BorrowHistory;
import utils.FineCalculator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowDAO {

    public boolean issueBook(int studentId, int bookId, int loanDays) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        Timestamp borrowDate = new Timestamp(System.currentTimeMillis());
        Timestamp dueDate = new Timestamp(System.currentTimeMillis() + (loanDays * 24L * 60L * 60L * 1000L));

        String query = "INSERT INTO BorrowHistory (student_id, book_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, 'BORROWED')";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, bookId);
                pstmt.setTimestamp(3, borrowDate);
                pstmt.setTimestamp(4, dueDate);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    // Update book available copies
                    try (PreparedStatement pBook = conn.prepareStatement("UPDATE Books SET available_copies = available_copies - 1 WHERE book_id = ?")) {
                        pBook.setInt(1, bookId);
                        pBook.executeUpdate();
                    }
                    // Update student current borrowed
                    try (PreparedStatement pStu = conn.prepareStatement("UPDATE Students SET current_borrowed = current_borrowed + 1 WHERE student_id = ?")) {
                        pStu.setInt(1, studentId);
                        pStu.executeUpdate();
                    }

                    conn.commit();
                    return true;
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean returnBook(int borrowId, int studentId, int bookId, Timestamp dueDate) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        Timestamp returnDate = new Timestamp(System.currentTimeMillis());
        double fineAmount = FineCalculator.calculateFine(dueDate, returnDate);
        String status = (fineAmount > 0) ? "OVERDUE" : "RETURNED";

        String query = "UPDATE BorrowHistory SET return_date = ?, status = ?, fine_amount = ? WHERE borrow_id = ?";

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setTimestamp(1, returnDate);
                pstmt.setString(2, status);
                pstmt.setDouble(3, fineAmount);
                pstmt.setInt(4, borrowId);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    // Increment available copies
                    try (PreparedStatement pBook = conn.prepareStatement("UPDATE Books SET available_copies = available_copies + 1 WHERE book_id = ?")) {
                        pBook.setInt(1, bookId);
                        pBook.executeUpdate();
                    }
                    // Decrement student current borrowed
                    try (PreparedStatement pStu = conn.prepareStatement("UPDATE Students SET current_borrowed = CASE WHEN current_borrowed > 0 THEN current_borrowed - 1 ELSE 0 END WHERE student_id = ?")) {
                        pStu.setInt(1, studentId);
                        pStu.executeUpdate();
                    }

                    // If fine exists, record in FineHistory & update Student total fine
                    if (fineAmount > 0) {
                        try (PreparedStatement pFine = conn.prepareStatement("INSERT INTO FineHistory (student_id, borrow_id, amount, reason) VALUES (?, ?, ?, 'Late Return Overdue Fine')")) {
                            pFine.setInt(1, studentId);
                            pFine.setInt(2, borrowId);
                            pFine.setDouble(3, fineAmount);
                            pFine.executeUpdate();
                        }
                        try (PreparedStatement pStuFine = conn.prepareStatement("UPDATE Students SET total_fines_owed = total_fines_owed + ? WHERE student_id = ?")) {
                            pStuFine.setDouble(1, fineAmount);
                            pStuFine.setInt(2, studentId);
                            pStuFine.executeUpdate();
                        }
                    }

                    conn.commit();
                    return true;
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<BorrowHistory> getAllBorrowHistory() {
        List<BorrowHistory> list = new ArrayList<>();
        String query = "SELECT bh.*, s.full_name as student_name, s.student_code, b.title as book_title, b.isbn " +
                "FROM BorrowHistory bh " +
                "JOIN Students s ON bh.student_id = s.student_id " +
                "JOIN Books b ON bh.book_id = b.book_id " +
                "ORDER BY bh.borrow_id DESC";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return list;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapBorrow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BorrowHistory> getBorrowHistoryByStudent(int studentId) {
        List<BorrowHistory> list = new ArrayList<>();
        String query = "SELECT bh.*, s.full_name as student_name, s.student_code, b.title as book_title, b.isbn " +
                "FROM BorrowHistory bh " +
                "JOIN Students s ON bh.student_id = s.student_id " +
                "JOIN Books b ON bh.book_id = b.book_id " +
                "WHERE bh.student_id = ? " +
                "ORDER BY bh.borrow_id DESC";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return list;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapBorrow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private BorrowHistory mapBorrow(ResultSet rs) throws SQLException {
        return new BorrowHistory(
                rs.getInt("borrow_id"),
                rs.getInt("student_id"),
                rs.getString("student_name"),
                rs.getString("student_code"),
                rs.getInt("book_id"),
                rs.getString("book_title"),
                rs.getString("isbn"),
                rs.getTimestamp("borrow_date"),
                rs.getTimestamp("due_date"),
                rs.getTimestamp("return_date"),
                rs.getString("status"),
                rs.getDouble("fine_amount"),
                rs.getBoolean("fine_paid")
        );
    }
}
