package service;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsService {

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return stats;

        try (Statement stmt = conn.createStatement()) {
            // Total Books & Available Copies
            ResultSet rs = stmt.executeQuery("SELECT SUM(total_copies), SUM(available_copies), COUNT(*) FROM Books");
            if (rs.next()) {
                stats.put("totalBookCopies", rs.getInt(1));
                stats.put("availableCopies", rs.getInt(2));
                stats.put("totalBookTitles", rs.getInt(3));
            }

            // Total Students
            rs = stmt.executeQuery("SELECT COUNT(*) FROM Students");
            if (rs.next()) {
                stats.put("totalStudents", rs.getInt(1));
            }

            // Currently Borrowed
            rs = stmt.executeQuery("SELECT COUNT(*) FROM BorrowHistory WHERE status = 'BORROWED' OR status = 'OVERDUE'");
            if (rs.next()) {
                stats.put("activeBorrows", rs.getInt(1));
            }

            // Total Pending Reservations
            rs = stmt.executeQuery("SELECT COUNT(*) FROM Reservations WHERE status = 'PENDING'");
            if (rs.next()) {
                stats.put("pendingReservations", rs.getInt(1));
            }

            // Total Fines Collected / Owed
            rs = stmt.executeQuery("SELECT SUM(total_fines_owed) FROM Students");
            if (rs.next()) {
                stats.put("totalFinesOwed", rs.getDouble(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}
