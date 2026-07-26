package dao;

import database.DatabaseConnection;
import model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String query = "SELECT b.*, c.category_name FROM Books b " +
                "LEFT JOIN Categories c ON b.category_id = c.category_id " +
                "ORDER BY b.book_id DESC";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return books;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                books.add(mapBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public Book getBookById(int bookId) {
        String query = "SELECT b.*, c.category_name FROM Books b " +
                "LEFT JOIN Categories c ON b.category_id = c.category_id " +
                "WHERE b.book_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapBook(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addBook(Book book) {
        String query = "INSERT INTO Books (isbn, title, author, publisher, category_id, language, edition, description, shelf_number, rack_number, floor_number, total_copies, available_copies, qr_code_path) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getPublisher());
            pstmt.setInt(5, book.getCategoryId());
            pstmt.setString(6, book.getLanguage());
            pstmt.setString(7, book.getEdition());
            pstmt.setString(8, book.getDescription());
            pstmt.setString(9, book.getShelfNumber());
            pstmt.setString(10, book.getRackNumber());
            pstmt.setInt(11, book.getFloorNumber());
            pstmt.setInt(12, book.getTotalCopies());
            pstmt.setInt(13, book.getAvailableCopies());
            pstmt.setString(14, book.getQrCodePath());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBook(Book book) {
        String query = "UPDATE Books SET isbn=?, title=?, author=?, publisher=?, category_id=?, language=?, edition=?, description=?, shelf_number=?, rack_number=?, floor_number=?, total_copies=?, available_copies=? WHERE book_id=?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getPublisher());
            pstmt.setInt(5, book.getCategoryId());
            pstmt.setString(6, book.getLanguage());
            pstmt.setString(7, book.getEdition());
            pstmt.setString(8, book.getDescription());
            pstmt.setString(9, book.getShelfNumber());
            pstmt.setString(10, book.getRackNumber());
            pstmt.setInt(11, book.getFloorNumber());
            pstmt.setInt(12, book.getTotalCopies());
            pstmt.setInt(13, book.getAvailableCopies());
            pstmt.setInt(14, book.getBookId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBook(int bookId) {
        String query = "DELETE FROM Books WHERE book_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, bookId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAvailableCopies(int bookId, int delta) {
        String query = "UPDATE Books SET available_copies = available_copies + ? WHERE book_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, delta);
            pstmt.setInt(2, bookId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("book_id"),
                rs.getString("isbn"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("publisher"),
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("language"),
                rs.getString("edition"),
                rs.getString("description"),
                rs.getString("shelf_number"),
                rs.getString("rack_number"),
                rs.getInt("floor_number"),
                rs.getInt("total_copies"),
                rs.getInt("available_copies"),
                rs.getString("cover_image_path"),
                rs.getString("qr_code_path"),
                rs.getTimestamp("created_at")
        );
    }
}
