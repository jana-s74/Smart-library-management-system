package database;

import utils.PasswordUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInitializer {

    public static void initializeDatabase() {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("❌ Database Connection Failed! Skipping Schema Initialization.");
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            boolean isSqlite = DatabaseConnection.isSQLite();
            String autoInc = isSqlite ? "AUTOINCREMENT" : "AUTO_INCREMENT";

            // 1. Admins Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Admins (" +
                    "admin_id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password_hash VARCHAR(256) NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL UNIQUE, " +
                    "phone VARCHAR(20), " +
                    "profile_pic_path VARCHAR(255), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // 2. Students Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Students (" +
                    "student_id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "student_code VARCHAR(20) NOT NULL UNIQUE, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL UNIQUE, " +
                    "password_hash VARCHAR(256) NOT NULL, " +
                    "phone VARCHAR(20), " +
                    "department VARCHAR(50), " +
                    "year_of_study INT DEFAULT 1, " +
                    "max_borrow_limit INT DEFAULT 5, " +
                    "current_borrowed INT DEFAULT 0, " +
                    "total_fines_owed DOUBLE DEFAULT 0.0, " +
                    "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                    "profile_pic_path VARCHAR(255), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // 3. Categories Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Categories (" +
                    "category_id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "category_name VARCHAR(50) NOT NULL UNIQUE, " +
                    "description TEXT, " +
                    "icon_name VARCHAR(50) DEFAULT 'book'" +
                    ")");

            // 4. Books Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Books (" +
                    "book_id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "isbn VARCHAR(20) NOT NULL UNIQUE, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "author VARCHAR(150) NOT NULL, " +
                    "publisher VARCHAR(150), " +
                    "category_id INT, " +
                    "language VARCHAR(30) DEFAULT 'English', " +
                    "edition VARCHAR(30) DEFAULT '1st Edition', " +
                    "description TEXT, " +
                    "shelf_number VARCHAR(20), " +
                    "rack_number VARCHAR(20), " +
                    "floor_number INT DEFAULT 1, " +
                    "total_copies INT NOT NULL DEFAULT 1, " +
                    "available_copies INT NOT NULL DEFAULT 1, " +
                    "cover_image_path VARCHAR(255), " +
                    "qr_code_path VARCHAR(255), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // 5. BorrowHistory Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS BorrowHistory (" +
                    "borrow_id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "student_id INT NOT NULL, " +
                    "book_id INT NOT NULL, " +
                    "borrow_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "due_date TIMESTAMP NOT NULL, " +
                    "return_date TIMESTAMP NULL, " +
                    "status VARCHAR(20) DEFAULT 'BORROWED', " +
                    "fine_amount DOUBLE DEFAULT 0.0, " +
                    "fine_paid BOOLEAN DEFAULT 0" +
                    ")");

            // 6. Reservations Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Reservations (" +
                    "reservation_id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "student_id INT NOT NULL, " +
                    "book_id INT NOT NULL, " +
                    "reservation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "status VARCHAR(20) DEFAULT 'PENDING', " +
                    "queue_position INT DEFAULT 1" +
                    ")");

            // 7. WaitingQueue Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS WaitingQueue (" +
                    "queue_id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "book_id INT NOT NULL, " +
                    "student_id INT NOT NULL, " +
                    "request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "status VARCHAR(20) DEFAULT 'WAITING'" +
                    ")");

            // 8. Reviews Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Reviews (" +
                    "review_id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "book_id INT NOT NULL, " +
                    "student_id INT NOT NULL, " +
                    "rating INT CHECK (rating >= 1 AND rating <= 5), " +
                    "review_text TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // 9. Notifications Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Notifications (" +
                    "notification_id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "user_type VARCHAR(10) NOT NULL, " +
                    "user_id INT NOT NULL, " +
                    "title VARCHAR(150) NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "is_read BOOLEAN DEFAULT 0, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // 10. FineHistory Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS FineHistory (" +
                    "fine_id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "student_id INT NOT NULL, " +
                    "borrow_id INT NOT NULL, " +
                    "amount DOUBLE NOT NULL, " +
                    "reason VARCHAR(255), " +
                    "payment_status VARCHAR(20) DEFAULT 'UNPAID', " +
                    "paid_at TIMESTAMP NULL" +
                    ")");

            // 11. Achievements Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Achievements (" +
                    "achievement_id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "student_id INT NOT NULL, " +
                    "title VARCHAR(100) NOT NULL, " +
                    "badge_type VARCHAR(50) NOT NULL, " +
                    "unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            System.out.println(" Database tables verified / created.");

            // Seed Initial Data if empty
            seedInitialData(conn, stmt);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Connection conn, Statement stmt) throws SQLException {
        // Seed Admin user
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Admins");
        if (rs.next() && rs.getInt(1) == 0) {
            String defaultPasswordHash = PasswordUtils.hashPassword("admin123");
            stmt.executeUpdate("INSERT INTO Admins (username, password_hash, full_name, email, phone) " +
                    "VALUES ('admin', '" + defaultPasswordHash + "', 'System Administrator', 'admin@libraai.org', '+1 800-555-0199')");
            System.out.println(" Seeded default Admin user: admin / admin123");
        }

        // Seed Categories
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Categories");
        if (rs.next() && rs.getInt(1) == 0) {
            stmt.executeUpdate("INSERT INTO Categories (category_name, description, icon_name) VALUES " +
                    "('Computer Science', 'Software engineering, algorithms, and AI', 'cpu'), " +
                    "('Data Science', 'Data analysis, statistics, and machine learning', 'bar-chart'), " +
                    "('Physics & Engineering', 'Quantum mechanics, electronics, robotics', 'zap'), " +
                    "('Mathematics', 'Calculus, linear algebra, discrete math', 'calculator'), " +
                    "('Literature & Arts', 'Classic novels, world poetry, design history', 'book-open')");
            System.out.println(" Seeded default Categories.");
        }

        // Seed Books
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Books");
        if (rs.next() && rs.getInt(1) == 0) {
            stmt.executeUpdate("INSERT INTO Books (isbn, title, author, publisher, category_id, language, edition, description, shelf_number, rack_number, floor_number, total_copies, available_copies) VALUES " +
                    "('978-0321965516', 'Don''t Make Me Think', 'Steve Krug', 'New Riders', 1, 'English', '3rd Edition', 'A Common Sense Approach to Web Usability.', 'CS-100', 'R-01', 1, 6, 5), " +
                    "('978-1720043997', 'The Road to React', 'Robin Wieruch', 'Independent', 1, 'English', '2024 Edition', 'Your journey to master plain JavaScript to React.', 'CS-101', 'R-01', 1, 5, 4), " +
                    "('978-1501121746', 'Sprint: Solve Big Problems', 'Jake Knapp', 'Simon & Schuster', 2, 'English', '1st Edition', 'How to test new ideas in just five days.', 'DS-201', 'R-02', 2, 4, 3), " +
                    "('978-0590353427', 'Harry Potter & Sorcerer''s Stone', 'J.K. Rowling', 'Scholastic', 5, 'English', 'Special Edition', 'The classic fantasy masterpiece.', 'LIT-501', 'R-09', 3, 8, 7), " +
                    "('978-1612680194', 'Rich Dad Poor Dad', 'Robert T. Kiyosaki', 'Plata Publishing', 5, 'English', '25th Anniversary', 'What the rich teach their kids about money.', 'LIT-502', 'R-09', 3, 10, 8), " +
                    "('978-1449335588', 'You Don''t Know JS: Scope', 'Kyle Simpson', 'O''Reilly Media', 1, 'English', '1st Edition', 'Deep dive into JavaScript core mechanisms.', 'CS-104', 'R-03', 1, 5, 4), " +
                    "('978-0134685991', 'Effective Java', 'Joshua Bloch', 'Addison-Wesley', 1, 'English', '3rd Edition', 'Best practices for the Java platform.', 'CS-105', 'R-01', 2, 5, 4), " +
                    "('978-0262035613', 'Introduction to Algorithms', 'Thomas H. Cormen', 'MIT Press', 1, 'English', '4th Edition', 'Comprehensive guide to algorithms and data structures.', 'CS-106', 'R-01', 2, 4, 3), " +
                    "('978-0596007126', 'Head First Design Patterns', 'Eric Freeman', 'O''Reilly Media', 1, 'English', '2nd Edition', 'A brain-friendly guide to design patterns.', 'CS-107', 'R-02', 2, 6, 6), " +
                    "('978-1491957660', 'Python Data Science Handbook', 'Jake VanderPlas', 'O''Reilly Media', 2, 'English', '1st Edition', 'Essential tools for working with data in Python.', 'DS-202', 'R-05', 3, 4, 2), " +
                    "('978-0131103627', 'The C Programming Language', 'Brian W. Kernighan', 'Prentice Hall', 1, 'English', '2nd Edition', 'The definitive authority on C programming.', 'CS-108', 'R-03', 2, 3, 1), " +
                    "('978-0321573513', 'Algorithms in Java', 'Robert Sedgewick', 'Addison-Wesley', 1, 'English', '4th Edition', 'Fundamental data structures and algorithms.', 'CS-109', 'R-03', 2, 5, 5)");
            System.out.println(" Seeded default Books (12 books total).");
        }

        // Seed Sample Student
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Students");
        if (rs.next() && rs.getInt(1) == 0) {
            String studentPassHash = PasswordUtils.hashPassword("student123");
            stmt.executeUpdate("INSERT INTO Students (student_code, full_name, email, password_hash, phone, department, year_of_study, max_borrow_limit, current_borrowed) VALUES " +
                    "('STU-2026-001', 'Alex Mercer', 'alex.mercer@university.edu', '" + studentPassHash + "', '+1 555-0144', 'Computer Science', 3, 5, 2), " +
                    "('STU-2026-002', 'Sophia Chen', 'sophia.chen@university.edu', '" + studentPassHash + "', '+1 555-0188', 'Data Analytics', 2, 5, 1)");
            System.out.println(" Seeded sample Students: STU-2026-001 / student123");
        }

        // Seed Sample Notifications
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Notifications");
        if (rs.next() && rs.getInt(1) == 0) {
            stmt.executeUpdate("INSERT INTO Notifications (user_type, user_id, title, message) VALUES " +
                    "('STUDENT', 1, 'Welcome to LibraAI!', 'Explore thousands of physical and digital books seamlessly.'), " +
                    "('ADMIN', 1, 'System Initialization', 'LibraAI Database and Security Engine successfully launched.')");
        }
    }
}
