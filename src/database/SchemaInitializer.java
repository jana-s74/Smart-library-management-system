package database;

import utils.PasswordUtils;

import java.sql.*;

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

            // 12. Attendance Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Attendance (" +
                    "attendance_id INTEGER PRIMARY KEY " + autoInc + ", " +
                    "student_id INT NOT NULL, " +
                    "check_in_time TIMESTAMP NULL, " +
                    "check_out_time TIMESTAMP NULL, " +
                    "status VARCHAR(10) NOT NULL DEFAULT 'IN'" +
                    ")");

            System.out.println(" Database tables verified / created.");

            // Seed Initial Data if empty
            seedInitialData(conn, stmt);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Connection conn, Statement stmt) throws SQLException {
        // Seed Admin 1
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Admins WHERE LOWER(username) = 'janaselvarasu7@gmail.com'");
        if (rs.next() && rs.getInt(1) == 0) {
            String defaultPasswordHash = PasswordUtils.hashPassword("janaSK@1123");
            stmt.executeUpdate("INSERT INTO Admins (username, password_hash, full_name, email, phone) " +
                    "VALUES ('janaselvarasu7@gmail.com', '" + defaultPasswordHash + "', 'Jana Selvarasu', 'janaselvarasu7@gmail.com', '+91 9000000000')");
            System.out.println(" Seeded Admin 1: janaselvarasu7@gmail.com / janaSK@1123");
        }

        // Seed Admin 2
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Admins WHERE LOWER(username) = 'jj6773286@gmail.com'");
        if (rs.next() && rs.getInt(1) == 0) {
            String defaultPasswordHash2 = PasswordUtils.hashPassword("jana@1234");
            stmt.executeUpdate("INSERT INTO Admins (username, password_hash, full_name, email, phone) " +
                    "VALUES ('jj6773286@gmail.com', '" + defaultPasswordHash2 + "', 'JJ Admin', 'jj6773286@gmail.com', '+91 9000000001')");
            System.out.println(" Seeded Admin 2: jj6773286@gmail.com / jana@1234");
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

        // Seed Sample Students
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Students");
        if (rs.next() && rs.getInt(1) < 180) {
            stmt.executeUpdate("DELETE FROM Students");
            String studentPassHash = PasswordUtils.hashPassword("student123");
            java.util.List<String[]> seedStudents = new java.util.ArrayList<>();
            
            String[][] aidsStudents = {
                {"721424243001", "AARISH A R", "AI & DS"},
                {"721424243002", "ABITH GODSON T A", "AI & DS"},
                {"721424243003", "ADARSH K", "AI & DS"},
                {"721424243004", "ADARSH R", "AI & DS"},
                {"721424243005", "AKSHAYA M", "AI & DS"},
                {"721424243006", "AKSHITHVYAN S", "AI & DS"},
                {"721424243007", "AMARNATHAN S", "AI & DS"},
                {"721424243008", "ANSREE R", "AI & DS"},
                {"721424243009", "ANUDEEP S", "AI & DS"},
                {"721424243010", "ARCHANA B KRISHNA", "AI & DS"},
                {"721424243011", "ARJUN DEVADAS", "AI & DS"},
                {"721424243012", "ARJUN S", "AI & DS"},
                {"721424243013", "ARUMBUNATHAN S", "AI & DS"},
                {"721424243014", "ATHULRAG P P", "AI & DS"},
                {"721424243015", "AZWAD A", "AI & DS"},
                {"721424243016", "BADRA S", "AI & DS"},
                {"721424243017", "BAINTHAMIZHAN PV", "AI & DS"},
                {"721424243018", "BALA SANKAR G", "AI & DS"},
                {"721424243019", "BALAJI A S", "AI & DS"},
                {"721424243020", "BALAMURUGAN S", "AI & DS"},
                {"721424243021", "BARANIDHARAN P", "AI & DS"},
                {"721424243022", "BARATHVARSHAN D", "AI & DS"},
                {"721424243023", "CHANDRU P", "AI & DS"},
                {"721424243024", "DEEPAK A", "AI & DS"},
                {"721424243025", "DELNA DENNIS", "AI & DS"},
                {"721424243026", "DEVANATHAN G", "AI & DS"},
                {"721424243027", "DHANU SRI R", "AI & DS"},
                {"721424243028", "DHANUJA M", "AI & DS"},
                {"721424243029", "DHATCHINAMOORTHI R", "AI & DS"},
                {"721424243030", "DHEETCHAN MANIK A", "AI & DS"},
                {"721424243031", "DINESH KARTHIK A", "AI & DS"},
                {"721424243032", "DINESH P S", "AI & DS"},
                {"721424243033", "DINESH V", "AI & DS"},
                {"721424243034", "DIVYA SRI S", "AI & DS"},
                {"721424243035", "EARICK ANTO S", "AI & DS"},
                {"721424243036", "ESWARA DHANALAKSHMI M", "AI & DS"},
                {"721424243037", "GANESH MOORTHY A", "AI & DS"},
                {"721424243038", "GANESH V", "AI & DS"},
                {"721424243039", "Ganeshkrishnan N", "AI & DS"},
                {"721424243040", "GOUTHAM PRABHAKAR M", "AI & DS"},
                {"721424243041", "GUGANRAJ S", "AI & DS"},
                {"721424243042", "GURUDEVAN. A", "AI & DS"},
                {"721424243043", "HAREESWAR R", "AI & DS"},
                {"721424243044", "HARIPRASATH S", "AI & DS"},
                {"721424243045", "HARISH S K", "AI & DS"},
                {"721424243046", "HARSHINI S", "AI & DS"},
                {"721424243047", "HARSHIT R", "AI & DS"},
                {"721424243048", "HEPSHIBA A", "AI & DS"},
                {"721424243049", "INDHUMATHI J", "AI & DS"},
                {"721424243050", "ISMATH BATCHA S", "AI & DS"},
                {"721424243051", "JAI AADHITHYA S", "AI & DS"},
                {"721424243052", "JANA S", "AI & DS"},
                {"721424243053", "JANANI U S", "AI & DS"},
                {"721424243054", "JAYACHANDIRAN K", "AI & DS"},
                {"721424243055", "JEEVA M", "AI & DS"},
                {"721424243056", "JEGAN M", "AI & DS"},
                {"721424243057", "JIDTHESH V", "AI & DS"},
                {"721424243058", "JOSEPH D", "AI & DS"},
                {"721424243059", "KAILASH R", "AI & DS"},
                {"721424243060", "KALYANI JITH", "AI & DS"},
                {"721424243061", "KARTHICK N", "AI & DS"},
                {"721424243062", "KARTHIKA B", "AI & DS"},
                {"721424243063", "KAVIBALAN R", "AI & DS"},
                {"721424243304", "SYIAM D", "AI & DS"}
            };
            for (String[] stu : aidsStudents) {
                seedStudents.add(stu);
            }

            String[] firstNames = {
                "ARAVIND", "DEEPAK", "GOKUL", "HARIHARAN", "KARTHIK", "MANOJ", "ABHISHEK", "AKASH", "AMIT", "ANAND",
                "BALAJI", "BHARATH", "DINESH", "GUGAN", "HARISH", "JAI", "KAVIN", "LOGESH", "MITHUN", "NAVEEN",
                "PRANAV", "RAHUL", "SANJAY", "THARUN", "VARUN", "VIJAY", "YOGESH", "ANIRUDH", "CHARAN", "DIVYESH",
                "ELANGO", "GIRISH", "HEMANTH", "INDRAJIT", "JEEVA", "KISHORE", "MANISH", "NITHIN", "POOVARASAN", "RAJESH",
                "SARAVANAN", "SUDHAKAR", "VIGNESH", "YESHWANTH", "ROHIT", "SABARI", "SACHIN", "SATHISH", "SURYA", "VASANTH",
                "ARUN", "KUMAR", "MOHAN", "RAGHU", "RAMESH", "SIVA", "SURESH", "VENKAT", "VIKRAM", "KIRAN"
            };
            String[] lastNames = {
                "S", "R", "K", "M", "A", "P", "V", "T", "G", "N", "J", "B", "C", "D", "E"
            };

            for (int i = 1; i <= 60; i++) {
                String code = String.format("721424105%03d", i);
                String name = firstNames[(i - 1) % firstNames.length] + " " + lastNames[(i - 1) % lastNames.length];
                seedStudents.add(new String[]{code, name, "EEE"});
            }

            for (int i = 1; i <= 60; i++) {
                String code = String.format("721424114%03d", i);
                String name = firstNames[(i - 1) % firstNames.length] + " " + lastNames[(i - 1) % lastNames.length];
                seedStudents.add(new String[]{code, name, "Mechanical"});
            }

            String insertSQL = "INSERT INTO Students (student_code, full_name, email, password_hash, phone, department, year_of_study, max_borrow_limit, current_borrowed) VALUES (?, ?, ?, ?, ?, ?, ?, 5, 0)";
            try (PreparedStatement pStmt = conn.prepareStatement(insertSQL)) {
                for (String[] stu : seedStudents) {
                    String code = stu[0];
                    String name = stu[1];
                    String dept = stu[2];
                    
                    String cleanName = name.toLowerCase().replaceAll("[^a-z]", "");
                    String last4 = code.substring(code.length() - 4);
                    String email = cleanName + last4 + ".ai24@gmail.com";

                    pStmt.setString(1, code);
                    pStmt.setString(2, name);
                    pStmt.setString(3, email);
                    pStmt.setString(4, studentPassHash);
                    pStmt.setString(5, "+91 9000000000");
                    pStmt.setString(6, dept);
                    pStmt.setInt(7, 3); // 3rd year
                    pStmt.addBatch();
                }
                pStmt.executeBatch();
                System.out.println(" Seeded " + seedStudents.size() + " students successfully (AI & DS, EEE, Mechanical) with custom emails.");
            }
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
