import database.DatabaseConnection;
import java.sql.*;

public class VerifyDb {
    public static void main(String[] args) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.out.println("❌ Database Connection Failed!");
            return;
        }
        System.out.println("✅ Connection mode: " + (DatabaseConnection.isSQLite() ? "SQLite File" : "MySQL Database (Localhost)"));
        
        try (Statement stmt = conn.createStatement()) {
            // Locate physical directory
            try (ResultSet rs = stmt.executeQuery("SHOW VARIABLES LIKE 'datadir'")) {
                if (rs.next()) {
                    System.out.println("📂 MySQL Data Folder on Disk: " + rs.getString("Value"));
                    System.out.println("📂 Database Folder Name:       libraai_db");
                    System.out.println("📂 Full Path on Laptop:       " + rs.getString("Value") + "libraai_db");
                }
            }

            // 1. Admins
            System.out.println("\n=== 👥 Admins ===");
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Admins")) {
                if (rs.next()) System.out.println("Total Admins: " + rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT admin_id, username, full_name, email FROM Admins")) {
                while (rs.next()) {
                    System.out.println(" - ID: " + rs.getInt("admin_id") + " | " + rs.getString("full_name") + " (" + rs.getString("username") + ")");
                }
            }

            // 2. Categories
            System.out.println("\n=== 📂 Book Categories ===");
            try (ResultSet rs = stmt.executeQuery("SELECT category_id, category_name FROM Categories")) {
                while (rs.next()) {
                    System.out.println(" - ID: " + rs.getInt("category_id") + " | " + rs.getString("category_name"));
                }
            }

            // 3. Books
            System.out.println("\n=== 📚 Books Sample ===");
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Books")) {
                if (rs.next()) System.out.println("Total Books: " + rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT book_id, title, author, available_copies, total_copies FROM Books LIMIT 5")) {
                while (rs.next()) {
                    System.out.println(" - ID: " + rs.getInt("book_id") + " | \"" + rs.getString("title") + "\" by " + rs.getString("author") + 
                                       " (Available: " + rs.getInt("available_copies") + "/" + rs.getInt("total_copies") + ")");
                }
            }

            // 4. Students
            System.out.println("\n=== 🎓 Students Sample ===");
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Students")) {
                if (rs.next()) System.out.println("Total Students registered: " + rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT student_id, student_code, full_name, email, department FROM Students LIMIT 5")) {
                while (rs.next()) {
                    System.out.println(" - ID: " + rs.getInt("student_id") + " | Code: " + rs.getString("student_code") + 
                                       " | " + rs.getString("full_name") + " (" + rs.getString("department") + ")");
                }
            }

            // 5. Borrow History
            System.out.println("\n=== 🔄 Borrow Transactions ===");
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM BorrowHistory")) {
                if (rs.next()) System.out.println("Total Borrow records: " + rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM BorrowHistory WHERE status = 'BORROWED'")) {
                if (rs.next()) System.out.println("Active Borrows (Books currently out): " + rs.getInt(1));
            }

            // 6. Attendance Logs
            System.out.println("\n=== 🚪 Attendance Logs ===");
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Attendance")) {
                if (rs.next()) System.out.println("Total Check-in/Check-out events: " + rs.getInt(1));
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL Query Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
