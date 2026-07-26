package dao;

import database.DatabaseConnection;
import model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public boolean createNotification(String userType, int userId, String title, String message) {
        String query = "INSERT INTO Notifications (user_type, user_id, title, message) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userType);
            pstmt.setInt(2, userId);
            pstmt.setString(3, title);
            pstmt.setString(4, message);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Notification> getNotificationsForUser(String userType, int userId) {
        List<Notification> list = new ArrayList<>();
        String query = "SELECT * FROM Notifications WHERE user_type = ? AND (user_id = ? OR user_id = 0) ORDER BY notification_id DESC";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return list;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userType);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Notification(
                        rs.getInt("notification_id"),
                        rs.getString("user_type"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getBoolean("is_read"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
