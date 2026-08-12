package web.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import controller.LibraryController;
import model.Book;
import model.BorrowHistory;
import model.Student;
import model.User;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.*;

public class LibraWebServer {

    private static final int PORT = 8080;
    private final LibraryController controller;
    private final dao.AttendanceDAO attendanceDAO;
    private final dao.UserDAO userDAO;
    private HttpServer server;

    public LibraWebServer() {
        this.controller = new LibraryController();
        this.attendanceDAO = new dao.AttendanceDAO();
        this.userDAO = new dao.UserDAO();
    }

    public static void main(String[] args) {
        database.SchemaInitializer.initializeDatabase();
        LibraWebServer webServer = new LibraWebServer();
        webServer.start();
    }


    public void start() {
        try {
            int portToUse = PORT;
            try {
                server = HttpServer.create(new InetSocketAddress(portToUse), 0);
            } catch (IOException e) {
                portToUse = 8085;
                server = HttpServer.create(new InetSocketAddress(portToUse), 0);
            }

            // Bind API handlers
            server.createContext("/api/stats", new StatsHandler());
            server.createContext("/api/auth/login", new LoginHandler());
            server.createContext("/api/auth/register", new RegisterHandler());
            server.createContext("/api/auth/change-password", new ChangePasswordHandler());
            server.createContext("/api/books", new BooksHandler());
            server.createContext("/api/borrow/issue", new IssueBookHandler());
            server.createContext("/api/borrow/return", new ReturnBookHandler());
            server.createContext("/api/borrow/history", new BorrowHistoryHandler());
            server.createContext("/api/borrow/overdue", new OverdueHandler());
            server.createContext("/api/students", new StudentsHandler());
            server.createContext("/api/students/status", new StudentStatusHandler());
            server.createContext("/api/attendance/logs", new AttendanceLogsHandler());
            server.createContext("/api/attendance/scan", new AttendanceScanHandler());
            server.createContext("/api/health", new HealthHandler());
            server.createContext("/api/analytics/top-visitors", new TopVisitorsHandler());


            // Static file handler
            server.createContext("/", new StaticFileHandler());

            server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
            server.start();
            System.out.println("==================================================");
            System.out.println("🌐 LibraAI Web Backend running at: http://localhost:" + portToUse);
            System.out.println("🚀 Web Dashboard UI available at: http://localhost:" + portToUse + "/index.html");
            System.out.println("==================================================");

            // Automatically open default browser
            openWebBrowser("http://localhost:" + portToUse + "/index.html");

        } catch (Exception e) {
            System.err.println("❌ Failed to start Web Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openWebBrowser(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } else {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec(new String[]{"open", url});
                } else if (os.contains("nix") || os.contains("nux")) {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", url});
                } else {
                    System.out.println("ℹ️ Please open your browser and navigate to: " + url);
                }
            }
        } catch (Exception e) {
            System.err.println("ℹ️ Could not open default web browser automatically: " + e.getMessage());
        }
    }


    // CORS & Response Helpers
    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        addCorsHeaders(exchange);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendOptionsResponse(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        exchange.sendResponseHeaders(204, -1);
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return baos.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                params.put(pair[0], URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            } else if (pair.length == 1) {
                params.put(pair[0], "");
            }
        }
        return params;
    }

    // Simple JSON Value Parser Helper
    private static Map<String, String> parseSimpleJson(String jsonStr) {
        Map<String, String> map = new HashMap<>();
        if (jsonStr == null || jsonStr.trim().isEmpty()) return map;
        String cleanStr = jsonStr.trim();
        if (cleanStr.startsWith("{")) cleanStr = cleanStr.substring(1);
        if (cleanStr.endsWith("}")) cleanStr = cleanStr.substring(0, cleanStr.length() - 1);

        String[] tokens = cleanStr.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String token : tokens) {
            String[] kv = token.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replaceAll("^\"|\"$", "");
                String value = kv[1].trim().replaceAll("^\"|\"$", "");
                map.put(key, value);
            }
        }
        return map;
    }

    private static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Handlers
    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptionsResponse(exchange);
                return;
            }
            String json = "{\"status\":\"UP\",\"system\":\"LibraAI Web API\",\"serverTime\":\"" + new Timestamp(System.currentTimeMillis()) + "\"}";
            sendJsonResponse(exchange, 200, json);
        }
    }

    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptionsResponse(exchange);
                return;
            }
            Map<String, Object> stats = controller.getDashboardStats();
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"totalBookCopies\":").append(stats.getOrDefault("totalBookCopies", 0)).append(",");
            sb.append("\"availableCopies\":").append(stats.getOrDefault("availableCopies", 0)).append(",");
            sb.append("\"totalBookTitles\":").append(stats.getOrDefault("totalBookTitles", 0)).append(",");
            sb.append("\"totalStudents\":").append(stats.getOrDefault("totalStudents", 0)).append(",");
            sb.append("\"activeBorrows\":").append(stats.getOrDefault("activeBorrows", 0)).append(",");
            sb.append("\"pendingReservations\":").append(stats.getOrDefault("pendingReservations", 0)).append(",");
            sb.append("\"totalFinesOwed\":").append(stats.getOrDefault("totalFinesOwed", 0.0));
            sb.append("}");
            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptionsResponse(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> json = parseSimpleJson(body);
            String role = json.getOrDefault("role", "STUDENT");
            String username = json.getOrDefault("username", "");
            String password = json.getOrDefault("password", "");

            if ("ADMIN".equalsIgnoreCase(role)) {
                User admin = controller.loginAdmin(username, password);
                if (admin != null) {
                    sendAdminSuccess(exchange, admin);
                    return;
                }
                Student student = controller.loginStudent(username, password);
                if (student != null) {
                    sendStudentSuccess(exchange, student);
                    return;
                }
            } else {
                Student student = controller.loginStudent(username, password);
                if (student != null) {
                    sendStudentSuccess(exchange, student);
                    return;
                }
                User admin = controller.loginAdmin(username, password);
                if (admin != null) {
                    sendAdminSuccess(exchange, admin);
                    return;
                }
            }
            sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"Invalid credentials. Please check your username/student code and password.\"}");
        }

        private void sendAdminSuccess(HttpExchange exchange, User admin) throws IOException {
            String userJson = String.format(
                    "{\"id\":%d,\"username\":\"%s\",\"fullName\":\"%s\",\"email\":\"%s\",\"role\":\"ADMIN\"}",
                    admin.getId(), escapeJson(admin.getUsername()), escapeJson(admin.getFullName()), escapeJson(admin.getEmail())
            );
            sendJsonResponse(exchange, 200, "{\"success\":true,\"user\":" + userJson + "}");
        }

        private void sendStudentSuccess(HttpExchange exchange, Student student) throws IOException {
            String studentJson = String.format(
                    "{\"id\":%d,\"studentCode\":\"%s\",\"fullName\":\"%s\",\"email\":\"%s\",\"department\":\"%s\",\"role\":\"STUDENT\",\"currentBorrowed\":%d,\"totalFinesOwed\":%.2f,\"status\":\"%s\"}",
                    student.getId(), escapeJson(student.getStudentCode()), escapeJson(student.getFullName()),
                    escapeJson(student.getEmail()), escapeJson(student.getDepartment()),
                    student.getCurrentBorrowed(), student.getTotalFinesOwed(), escapeJson(student.getStatus())
            );
            sendJsonResponse(exchange, 200, "{\"success\":true,\"user\":" + studentJson + "}");
        }
    }


    private class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptionsResponse(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> json = parseSimpleJson(body);
            Student s = new Student();
            s.setStudentCode(json.getOrDefault("studentCode", "STU" + System.currentTimeMillis() % 10000));
            s.setFullName(json.getOrDefault("fullName", ""));
            s.setEmail(json.getOrDefault("email", ""));
            s.setPhone(json.getOrDefault("phone", ""));
            s.setDepartment(json.getOrDefault("department", "General"));
            try {
                s.setYearOfStudy(Integer.parseInt(json.getOrDefault("yearOfStudy", "1")));
            } catch (Exception e) {
                s.setYearOfStudy(1);
            }
            s.setMaxBorrowLimit(3);
            s.setStatus("ACTIVE");

            String password = json.getOrDefault("password", "123456");
            boolean success = controller.registerStudent(s, password);

            if (success) {
                sendJsonResponse(exchange, 201, "{\"success\":true,\"message\":\"Student registered successfully!\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Registration failed. Student code or email might already exist.\"}");
            }
        }
    }

    private class ChangePasswordHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptionsResponse(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> json = parseSimpleJson(body);
            String username = json.getOrDefault("username", "");
            String currentPassword = json.getOrDefault("currentPassword", "");
            String newPassword = json.getOrDefault("newPassword", "");

            if (username.isEmpty() || currentPassword.isEmpty() || newPassword.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"All fields are required.\"}");
                return;
            }

            boolean success = controller.changePassword(username, currentPassword, newPassword);
            if (success) {
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Password updated successfully!\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to update password. Verify current password.\"}");
            }
        }
    }

    private class BooksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptionsResponse(exchange);
                return;
            }

            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
                String sort = query.getOrDefault("sort", "title");
                String search = query.getOrDefault("search", "");

                List<Book> books;
                if (!search.isEmpty()) {
                    Book match = controller.searchBookByTitleBinary(search);
                    books = new ArrayList<>();
                    if (match != null) {
                        books.add(match);
                    } else {
                        // Linear search fallback if binary exact match doesn't hit
                        for (Book b : controller.getAllBooks()) {
                            if (b.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                                b.getAuthor().toLowerCase().contains(search.toLowerCase()) ||
                                (b.getIsbn() != null && b.getIsbn().toLowerCase().contains(search.toLowerCase()))) {
                                books.add(b);
                            }
                        }
                    }
                } else {
                    books = controller.getSortedBooks(sort);
                }

                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < books.size(); i++) {
                    Book b = books.get(i);
                    sb.append(formatBookJson(b));
                    if (i < books.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());

            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> json = parseSimpleJson(body);
                Book b = new Book();
                b.setTitle(json.getOrDefault("title", ""));
                b.setAuthor(json.getOrDefault("author", ""));
                b.setIsbn(json.getOrDefault("isbn", ""));
                b.setPublisher(json.getOrDefault("publisher", ""));
                b.setCategoryName(json.getOrDefault("categoryName", "General"));
                b.setLanguage(json.getOrDefault("language", "English"));
                b.setEdition(json.getOrDefault("edition", "1st"));
                b.setDescription(json.getOrDefault("description", ""));
                b.setShelfNumber(json.getOrDefault("shelfNumber", "A-1"));
                b.setRackNumber(json.getOrDefault("rackNumber", "R1"));
                try {
                    b.setTotalCopies(Integer.parseInt(json.getOrDefault("totalCopies", "5")));
                    b.setAvailableCopies(Integer.parseInt(json.getOrDefault("availableCopies", json.getOrDefault("totalCopies", "5"))));
                } catch (Exception e) {
                    b.setTotalCopies(5);
                    b.setAvailableCopies(5);
                }

                boolean success = controller.addBook(b);
                if (success) {
                    sendJsonResponse(exchange, 201, "{\"success\":true,\"message\":\"Book added successfully!\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to add book.\"}");
                }

            } else if ("PUT".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                Map<String, String> json = parseSimpleJson(body);
                Book b = new Book();
                try {
                    b.setBookId(Integer.parseInt(json.get("bookId")));
                } catch (Exception e) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid Book ID\"}");
                    return;
                }
                b.setTitle(json.getOrDefault("title", ""));
                b.setAuthor(json.getOrDefault("author", ""));
                b.setIsbn(json.getOrDefault("isbn", ""));
                b.setPublisher(json.getOrDefault("publisher", ""));
                b.setCategoryName(json.getOrDefault("categoryName", "General"));
                b.setLanguage(json.getOrDefault("language", "English"));
                b.setDescription(json.getOrDefault("description", ""));
                b.setShelfNumber(json.getOrDefault("shelfNumber", "A-1"));
                try {
                    b.setTotalCopies(Integer.parseInt(json.getOrDefault("totalCopies", "5")));
                    b.setAvailableCopies(Integer.parseInt(json.getOrDefault("availableCopies", "5")));
                } catch (Exception e) {
                    b.setTotalCopies(5);
                    b.setAvailableCopies(5);
                }

                boolean success = controller.updateBook(b);
                if (success) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Book updated successfully!\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to update book.\"}");
                }

            } else if ("DELETE".equalsIgnoreCase(method)) {
                Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
                String idStr = query.get("id");
                if (idStr != null) {
                    try {
                        int bookId = Integer.parseInt(idStr);
                        boolean success = controller.deleteBook(bookId);
                        if (success) {
                            sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Book deleted successfully!\"}");
                            return;
                        }
                    } catch (Exception ignored) {}
                }
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Could not delete book.\"}");
            } else {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }

        private String formatBookJson(Book b) {
            return String.format(
                    "{\"bookId\":%d,\"isbn\":\"%s\",\"title\":\"%s\",\"author\":\"%s\",\"publisher\":\"%s\",\"categoryName\":\"%s\",\"language\":\"%s\",\"edition\":\"%s\",\"description\":\"%s\",\"shelfNumber\":\"%s\",\"rackNumber\":\"%s\",\"floorNumber\":%d,\"totalCopies\":%d,\"availableCopies\":%d,\"coverImagePath\":\"%s\",\"qrCodePath\":\"%s\"}",
                    b.getBookId(), escapeJson(b.getIsbn()), escapeJson(b.getTitle()), escapeJson(b.getAuthor()),
                    escapeJson(b.getPublisher()), escapeJson(b.getCategoryName()), escapeJson(b.getLanguage()),
                    escapeJson(b.getEdition()), escapeJson(b.getDescription()), escapeJson(b.getShelfNumber()),
                    escapeJson(b.getRackNumber()), b.getFloorNumber(), b.getTotalCopies(), b.getAvailableCopies(),
                    escapeJson(b.getCoverImagePath()), escapeJson(b.getQrCodePath())
            );
        }
    }

    private class IssueBookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptionsResponse(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> json = parseSimpleJson(body);
            try {
                int studentId = Integer.parseInt(json.get("studentId"));
                int bookId    = Integer.parseInt(json.get("bookId"));
                int loanDays  = Integer.parseInt(json.getOrDefault("loanDays", "14"));

                boolean success = controller.issueBook(studentId, bookId, loanDays);
                if (success) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Book issued successfully!\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to issue book. Check student borrow limit or book availability.\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid request parameter formats.\"}");
            }
        }
    }


    private class ReturnBookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptionsResponse(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> json = parseSimpleJson(body);
            try {
                int borrowId = Integer.parseInt(json.get("borrowId"));
                int studentId = Integer.parseInt(json.get("studentId"));
                int bookId = Integer.parseInt(json.get("bookId"));
                Timestamp dueDate = new Timestamp(System.currentTimeMillis());

                boolean success = controller.returnBook(borrowId, studentId, bookId, dueDate);
                if (success) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Book returned successfully!\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to return book.\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid request format.\"}");
            }
        }
    }

    private class BorrowHistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptionsResponse(exchange);
                return;
            }

            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
            String studentIdStr = query.get("studentId");

            List<BorrowHistory> history;
            if (studentIdStr != null && !studentIdStr.isEmpty()) {
                try {
                    int studentId = Integer.parseInt(studentIdStr);
                    history = controller.getStudentBorrowHistory(studentId);
                } catch (Exception e) {
                    history = controller.getAllBorrowHistory();
                }
            } else {
                history = controller.getAllBorrowHistory();
            }

            long now = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < history.size(); i++) {
                BorrowHistory bh = history.get(i);
                long overdueDays = 0;
                if (bh.getReturnDate() == null && bh.getDueDate() != null) {
                    long diff = now - bh.getDueDate().getTime();
                    if (diff > 0) overdueDays = diff / (1000L * 60 * 60 * 24);
                }
                sb.append(String.format(
                        "{\"borrowId\":%d,\"studentId\":%d,\"studentName\":\"%s\",\"studentCode\":\"%s\",\"bookId\":%d,\"bookTitle\":\"%s\",\"isbn\":\"%s\",\"borrowDate\":\"%s\",\"dueDate\":\"%s\",\"returnDate\":\"%s\",\"status\":\"%s\",\"fineAmount\":%.2f,\"finePaid\":%b,\"overdueDays\":%d}",
                        bh.getBorrowId(), bh.getStudentId(), escapeJson(bh.getStudentName()), escapeJson(bh.getStudentCode()),
                        bh.getBookId(), escapeJson(bh.getBookTitle()), escapeJson(bh.getIsbn()),
                        bh.getBorrowDate() != null ? bh.getBorrowDate().toString() : "",
                        bh.getDueDate() != null ? bh.getDueDate().toString() : "",
                        bh.getReturnDate() != null ? bh.getReturnDate().toString() : "",
                        escapeJson(bh.getStatus()), bh.getFineAmount(), bh.isFinePaid(), overdueDays
                ));
                if (i < history.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    private class OverdueHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptionsResponse(exchange);
                return;
            }
            List<BorrowHistory> allHistory = controller.getAllBorrowHistory();
            long now = System.currentTimeMillis();
            List<BorrowHistory> overdue = new ArrayList<>();
            for (BorrowHistory bh : allHistory) {
                if (bh.getReturnDate() == null && bh.getDueDate() != null && now > bh.getDueDate().getTime()) {
                    overdue.add(bh);
                }
            }
            StringBuilder sb = new StringBuilder("{\"overdueCount\":");
            sb.append(overdue.size()).append(",\"overdueStudents\":[");
            Set<String> seen = new java.util.LinkedHashSet<>();
            List<String> studentEntries = new ArrayList<>();
            for (BorrowHistory bh : overdue) {
                if (!seen.contains(bh.getStudentCode())) {
                    seen.add(bh.getStudentCode());
                    long days = (now - bh.getDueDate().getTime()) / (1000L * 60 * 60 * 24);
                    studentEntries.add(String.format(
                        "{\"studentId\":%d,\"studentName\":\"%s\",\"studentCode\":\"%s\",\"bookTitle\":\"%s\",\"daysOverdue\":%d,\"fineAmount\":%.2f}",
                        bh.getStudentId(), escapeJson(bh.getStudentName()), escapeJson(bh.getStudentCode()),
                        escapeJson(bh.getBookTitle()), days, bh.getFineAmount()
                    ));
                }
            }
            sb.append(String.join(",", studentEntries)).append("]}");
            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    private class StudentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptionsResponse(exchange);
                return;
            }

            List<Student> students = controller.getAllStudents();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                sb.append(String.format(
                        "{\"id\":%d,\"studentCode\":\"%s\",\"fullName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"department\":\"%s\",\"yearOfStudy\":%d,\"maxBorrowLimit\":%d,\"currentBorrowed\":%d,\"totalFinesOwed\":%.2f,\"status\":\"%s\",\"passwordHash\":\"%s\"}",
                        s.getId(), escapeJson(s.getStudentCode()), escapeJson(s.getFullName()), escapeJson(s.getEmail()),
                        escapeJson(s.getPhone()), escapeJson(s.getDepartment()), s.getYearOfStudy(),
                        s.getMaxBorrowLimit(), s.getCurrentBorrowed(), s.getTotalFinesOwed(), escapeJson(s.getStatus()),
                        escapeJson(s.getPasswordHash())
                ));
                if (i < students.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    private class StudentStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptionsResponse(exchange);
                return;
            }
            if (!"PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> json = parseSimpleJson(body);
            try {
                int studentId = Integer.parseInt(json.get("studentId"));
                String status = json.getOrDefault("status", "ACTIVE");
                boolean success = controller.updateStudentStatus(studentId, status);
                if (success) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Student status updated to " + status + "\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to update status.\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid payload.\"}");
            }
        }
    }

    // Static File Server
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().getPath();
            if (uri.equals("/")) {
                uri = "/index.html";
            }

            // Path security check
            if (uri.contains("..")) {
                sendJsonResponse(exchange, 403, "{\"error\":\"Access Denied\"}");
                return;
            }

            // Look in src/web
            File file = new File("src/web" + uri);
            if (!file.exists() || file.isDirectory()) {
                // Fallback attempt for html folder
                file = new File("src/web/html" + uri);
            }

            if (!file.exists() || file.isDirectory()) {
                String errorHtml = "<html><body><h1>404 Not Found</h1><p>The requested URL " + uri + " was not found on this server.</p></body></html>";
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                addCorsHeaders(exchange);
                byte[] b = errorHtml.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, b.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(b);
                }
                return;
            }

            String contentType = getContentType(file.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            addCorsHeaders(exchange);
            exchange.sendResponseHeaders(200, file.length());

            try (OutputStream os = exchange.getResponseBody();
                 FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fis.read(buffer)) >= 0) {
                    os.write(buffer, 0, count);
                }
            }
        }

        private String getContentType(String fileName) {
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
            if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
            if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".svg")) return "image/svg+xml";
            if (lower.endsWith(".ico")) return "image/x-icon";
            return "application/octet-stream";
        }
    }

    private class AttendanceLogsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptionsResponse(exchange);
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            List<model.AttendanceLog> logs = attendanceDAO.getAttendanceLogs();
            int inside = attendanceDAO.getCurrentlyInsideCount();
            int todayVisits = attendanceDAO.getTodayTotalVisits();

            StringBuilder sb = new StringBuilder("{");
            sb.append("\"success\":true,");
            sb.append("\"currentlyInside\":").append(inside).append(",");
            sb.append("\"totalVisitsToday\":").append(todayVisits).append(",");
            sb.append("\"logs\":[");
            for (int i = 0; i < logs.size(); i++) {
                model.AttendanceLog log = logs.get(i);
                sb.append("{");
                sb.append("\"attendanceId\":").append(log.getAttendanceId()).append(",");
                sb.append("\"studentId\":").append(log.getStudentId()).append(",");
                sb.append("\"studentCode\":\"").append(escapeJson(log.getStudentCode())).append("\",");
                sb.append("\"studentName\":\"").append(escapeJson(log.getStudentName())).append("\",");
                sb.append("\"department\":\"").append(escapeJson(log.getDepartment())).append("\",");
                sb.append("\"checkInTime\":\"").append(log.getCheckInTime() != null ? log.getCheckInTime().toString() : "").append("\",");
                sb.append("\"checkOutTime\":\"").append(log.getCheckOutTime() != null ? log.getCheckOutTime().toString() : "").append("\",");
                sb.append("\"status\":\"").append(log.getStatus()).append("\"");
                sb.append("}");
                if (i < logs.size() - 1) sb.append(",");
            }
            sb.append("]");
            sb.append("}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    private class AttendanceScanHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptionsResponse(exchange);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> json = parseSimpleJson(body);
            String qrData = json.getOrDefault("qrData", "").trim();

            if (qrData.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Missing qrData parameter.\"}");
                return;
            }

            String studentCode = qrData;
            if (qrData.startsWith("libraai:student:")) {
                String[] parts = qrData.split(":");
                if (parts.length >= 3) {
                    studentCode = parts[2];
                }
            }

            model.Student student = userDAO.getStudentByCode(studentCode);
            if (student == null) {
                sendJsonResponse(exchange, 404, "{\"success\":false,\"message\":\"Student not found in database.\"}");
                return;
            }

            Timestamp now = new Timestamp(System.currentTimeMillis());
            model.AttendanceLog active = attendanceDAO.getActiveAttendance(student.getId());
            String action;
            boolean dbSuccess;

            if (active != null) {
                // Enforce 15-second minimum stay before allowing check-out
                long secondsInside = (now.getTime() - active.getCheckInTime().getTime()) / 1000L;
                if (secondsInside < 15) {
                    long remaining = 15 - secondsInside;
                    sendJsonResponse(exchange, 429,
                        String.format("{\"success\":false,\"message\":\"Too soon! Please wait %d more second%s before leaving.\",\"remainingSeconds\":%d}",
                            remaining, remaining == 1 ? "" : "s", remaining));
                    return;
                }
                dbSuccess = attendanceDAO.checkOut(active.getAttendanceId(), now);
                action = "CHECK_OUT";
            } else {
                dbSuccess = attendanceDAO.checkIn(student.getId(), now);
                action = "CHECK_IN";
            }

            if (dbSuccess) {
                int inside = attendanceDAO.getCurrentlyInsideCount();
                int todayVisits = attendanceDAO.getTodayTotalVisits();
                
                String responseJson = String.format(
                        "{\"success\":true,\"action\":\"%s\",\"studentName\":\"%s\",\"studentCode\":\"%s\",\"department\":\"%s\",\"timestamp\":\"%s\",\"currentlyInside\":%d,\"totalVisitsToday\":%d}",
                        action,
                        escapeJson(student.getFullName()),
                        escapeJson(student.getStudentCode()),
                        escapeJson(student.getDepartment()),
                        now.toString(),
                        inside,
                        todayVisits
                );
                sendJsonResponse(exchange, 200, responseJson);
            } else {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"message\":\"Database operation failed.\"}");
            }
        }
    }

    private class TopVisitorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendOptionsResponse(exchange);
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
            int limit = 10;
            try {
                if (query.containsKey("limit")) {
                    limit = Integer.parseInt(query.get("limit"));
                }
            } catch (Exception ignored) {}

            List<Map<String, Object>> visitors = attendanceDAO.getTopLibraryVisitors(limit);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < visitors.size(); i++) {
                Map<String, Object> visitor = visitors.get(i);
                sb.append("{");
                sb.append("\"studentId\":").append(visitor.get("studentId")).append(",");
                sb.append("\"studentCode\":\"").append(escapeJson((String) visitor.get("studentCode"))).append("\",");
                sb.append("\"fullName\":\"").append(escapeJson((String) visitor.get("fullName"))).append("\",");
                sb.append("\"department\":\"").append(escapeJson((String) visitor.get("department"))).append("\",");
                sb.append("\"totalSeconds\":").append(visitor.get("totalSeconds"));
                sb.append("}");
                if (i < visitors.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJsonResponse(exchange, 200, sb.toString());
        }
    }
}
