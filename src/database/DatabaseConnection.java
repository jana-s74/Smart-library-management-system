package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/libraai_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASS = "1123";

    private static final String SQLITE_URL = "jdbc:sqlite:libraai.db";

    private static Connection connection = null;
    private static boolean isSQLiteMode = false;

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = createConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private static Connection createConnection() {
        // 1. Try MySQL JDBC Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
            System.out.println(" Connected successfully to MySQL Database!");
            isSQLiteMode = false;
            return conn;
        } catch (Exception e) {
            // MySQL driver or connection not present
        }

        // 2. Try SQLite Driver
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(SQLITE_URL);
            System.out.println(" Connected successfully to SQLite Database (libraai.db)!");
            isSQLiteMode = true;
            return conn;
        } catch (Exception e) {
            // SQLite driver not present
        }

        // 3. Fallback to embedded memory H2 / SQLite if available in runtime
        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:libraai;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "");
            System.out.println(" Connected to Embedded H2 In-Memory Database!");
            return conn;
        } catch (Exception e) {
            // No external JDBC driver jar in classpath
        }

        System.out.println("ℹ️ Operating with Standalone In-Memory Data Store (Add mysql-connector-j.jar or sqlite-jdbc.jar to lib/ for persistent DB storage).");
        return InMemoryDatabaseDriver.createInMemoryConnection();
    }

    public static boolean isSQLite() {
        return isSQLiteMode;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
