package dao;

import database.DatabaseConnection;
import model.Student;
import model.User;
import utils.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticateAdmin(String username, String password) {
        String query = "SELECT * FROM Admins WHERE LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            String trimmed = username.trim();
            pstmt.setString(1, trimmed);
            pstmt.setString(2, trimmed);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (PasswordUtils.verifyPassword(password, storedHash)) {
                    return new User(
                            rs.getInt("admin_id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            "ADMIN",
                            rs.getString("profile_pic_path"),
                            rs.getTimestamp("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Student authenticateStudent(String identifier, String password) {
        String query = "SELECT * FROM Students WHERE (LOWER(student_code) = LOWER(?) OR LOWER(email) = LOWER(?) OR LOWER(full_name) = LOWER(?))";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            String trimmed = identifier.trim();
            pstmt.setString(1, trimmed);
            pstmt.setString(2, trimmed);
            pstmt.setString(3, trimmed);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (PasswordUtils.verifyPassword(password, storedHash)) {
                    return mapStudent(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean registerStudent(Student student, String rawPassword) {
        String query = "INSERT INTO Students (student_code, full_name, email, password_hash, phone, department, year_of_study) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, student.getStudentCode());
            pstmt.setString(2, student.getFullName());
            pstmt.setString(3, student.getEmail());
            pstmt.setString(4, PasswordUtils.hashPassword(rawPassword));
            pstmt.setString(5, student.getPhone());
            pstmt.setString(6, student.getDepartment());
            pstmt.setInt(7, student.getYearOfStudy());

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String query = "SELECT * FROM Students ORDER BY student_id DESC";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return list;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapStudent(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Student getStudentById(int studentId) {
        String query = "SELECT * FROM Students WHERE student_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapStudent(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStudentStatus(int studentId, String status) {
        String query = "UPDATE Students SET status = ? WHERE student_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("student_id"),
                rs.getString("student_code"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("department"),
                rs.getInt("year_of_study"),
                rs.getInt("max_borrow_limit"),
                rs.getInt("current_borrowed"),
                rs.getDouble("total_fines_owed"),
                rs.getString("status"),
                rs.getString("profile_pic_path"),
                rs.getTimestamp("created_at")
        );
    }
}
