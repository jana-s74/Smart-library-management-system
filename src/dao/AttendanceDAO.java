package dao;

import database.DatabaseConnection;
import model.AttendanceLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class AttendanceDAO {

    public boolean checkIn(int studentId, Timestamp checkInTime) {
        String sql = "INSERT INTO Attendance (student_id, check_in_time, status) VALUES (?, ?, 'IN')";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setTimestamp(2, checkInTime);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkOut(int attendanceId, Timestamp checkOutTime) {
        String sql = "UPDATE Attendance SET check_out_time = ?, status = 'OUT' WHERE attendance_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, checkOutTime);
            pstmt.setInt(2, attendanceId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public AttendanceLog getActiveAttendance(int studentId) {
        String sql = "SELECT a.*, s.student_code, s.full_name, s.department " +
                     "FROM Attendance a " +
                     "JOIN Students s ON a.student_id = s.student_id " +
                     "WHERE a.student_id = ? AND a.status = 'IN' AND a.check_out_time IS NULL LIMIT 1";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new AttendanceLog(
                            rs.getInt("attendance_id"),
                            rs.getInt("student_id"),
                            rs.getString("student_code"),
                            rs.getString("full_name"),
                            rs.getString("department"),
                            rs.getTimestamp("check_in_time"),
                            rs.getTimestamp("check_out_time"),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<AttendanceLog> getAttendanceLogs() {
        List<AttendanceLog> logs = new ArrayList<>();
        String sql = "SELECT a.*, s.student_code, s.full_name, s.department " +
                     "FROM Attendance a " +
                     "JOIN Students s ON a.student_id = s.student_id " +
                     "ORDER BY a.check_in_time DESC";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return logs;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new AttendanceLog(
                        rs.getInt("attendance_id"),
                        rs.getInt("student_id"),
                        rs.getString("student_code"),
                        rs.getString("full_name"),
                        rs.getString("department"),
                        rs.getTimestamp("check_in_time"),
                        rs.getTimestamp("check_out_time"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    public int getCurrentlyInsideCount() {
        String sql = "SELECT COUNT(*) FROM Attendance WHERE status = 'IN' AND check_out_time IS NULL";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return 0;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTodayTotalVisits() {
        String sql = "SELECT COUNT(*) FROM Attendance WHERE DATE(check_in_time) = CURRENT_DATE";
        if (DatabaseConnection.isSQLite()) {
            sql = "SELECT COUNT(*) FROM Attendance WHERE date(check_in_time) = date('now')";
        }
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return 0;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Map<String, Object>> getTopLibraryVisitors(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql;
        if (DatabaseConnection.isSQLite()) {
            sql = "SELECT s.student_id, s.student_code, s.full_name, s.department, " +
                  "SUM(strftime('%s', COALESCE(a.check_out_time, datetime('now', 'localtime'))) - strftime('%s', a.check_in_time)) as total_seconds " +
                  "FROM Attendance a " +
                  "JOIN Students s ON a.student_id = s.student_id " +
                  "GROUP BY s.student_id, s.student_code, s.full_name, s.department " +
                  "ORDER BY total_seconds DESC LIMIT ?";
        } else {
            sql = "SELECT s.student_id, s.student_code, s.full_name, s.department, " +
                  "SUM(TIMESTAMPDIFF(SECOND, a.check_in_time, COALESCE(a.check_out_time, NOW()))) as total_seconds " +
                  "FROM Attendance a " +
                  "JOIN Students s ON a.student_id = s.student_id " +
                  "GROUP BY s.student_id, s.student_code, s.full_name, s.department " +
                  "ORDER BY total_seconds DESC LIMIT ?";
        }
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return list;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("studentId", rs.getInt("student_id"));
                    map.put("studentCode", rs.getString("student_code"));
                    map.put("fullName", rs.getString("full_name"));
                    map.put("department", rs.getString("department"));
                    map.put("totalSeconds", rs.getLong("total_seconds"));
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
