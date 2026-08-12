# 📚 LibraAI — Smart Library Management System

> A modern, full-stack library management platform built with **Java 21** and a **web-based dashboard UI**, featuring AI-inspired workflows, QR-code attendance, fine management, book reservations, analytics, and more.

---

## 🚀 Features

### 🔐 Authentication & Security
- Secure **Admin and Student login** with hashed passwords (BCrypt)
- OTP-based verification via **Email (Nodemailer / Gmail SMTP)**
- Password change with re-authentication guard

### 📖 Book Management
- Add, edit, delete and search books with full metadata (ISBN, author, publisher, category, edition, shelf/rack/floor location)
- Upload and display **book cover images**
- Auto-generate **QR codes** per book
- Multi-copy tracking (total vs. available copies)
- Category-based filtering

### 👩‍🎓 Student Management
- Student registration with department, year-of-study, and borrow limits
- Account status control (`ACTIVE` / `SUSPENDED`)
- Student profile with borrowing history and fine balance
- Achievement / badge system for reading milestones

### 📦 Borrow & Return System
- Issue books to students with configurable due dates
- One-click return flow with automatic fine calculation
- Overdue detection and tracking
- Full borrow history per student and per book

### 📋 Reservations & Waiting Queue
- Students can **reserve** unavailable books
- FIFO **waiting queue** (backed by a DSA Queue implementation)
- Queue position tracking; auto-promoted on book return

### 💰 Fine Management
- Automatic daily fine accumulation for overdue books
- Fine payment recording and payment history
- Per-student outstanding balance tracking

### 📊 Analytics & Reports
- Dashboard stats: total books, students, borrows, overdue count
- **Top visitors** leaderboard
- Borrow trend charts
- Exportable reports (HTML template)

### 📡 QR Attendance System
- QR-code-based student **attendance scan**
- Attendance log with timestamps
- Top-visitor analytics

### 🔔 Notifications
- In-app notifications for admins and students
- Read/unread status tracking

### 🌐 Web Dashboard
- Single-page application served by a built-in **JDK HttpServer** (no external servlet container needed)
- Responsive UI with dark/light theme support
- Auto-opens in the default browser on startup

---

## 🛠️ Technology Stack

| Layer | Technology |
|---|---|
| **Language** | Java 21 |
| **Backend Server** | JDK `com.sun.net.httpserver.HttpServer` (port 8080 / 8085 fallback) |
| **Database** | MySQL **or** SQLite (dual-dialect SQL schema) |
| **OTP Email Server** | Node.js · Express · Nodemailer |
| **Frontend** | HTML5 · Vanilla CSS · Vanilla JavaScript |
| **QR Code** | `qrcode.min.js` (generation) · `jsQR.min.js` (scanning) |
| **Password Hashing** | BCrypt (custom `PasswordUtils`) |
| **DSA Layer** | Custom implementations (MergeSort, BinarySearch, HashMap Index, Queue, Action Stack) |
| **Build** | Compiled to `bin/` — no Maven/Gradle required |

---

## 🗂️ Project Structure

```
Library management system/
├── src/
│   ├── Main.java                   # Application entry point
│   ├── LibraAITestRunner.java      # Self-contained test suite (JUnit-free)
│   ├── controller/
│   │   └── LibraryController.java  # Core business-logic orchestrator
│   ├── dao/                        # Data-Access Layer
│   │   ├── UserDAO.java
│   │   ├── BookDAO.java
│   │   ├── BorrowDAO.java
│   │   ├── AttendanceDAO.java
│   │   ├── CategoryDAO.java
│   │   ├── NotificationDAO.java
│   │   ├── ReservationDAO.java
│   │   └── ReviewDAO.java
│   ├── service/                    # Business-Service Layer
│   │   ├── AuthService.java
│   │   ├── BookService.java
│   │   ├── BorrowService.java
│   │   ├── StudentService.java
│   │   └── AnalyticsService.java
│   ├── model/                      # Domain Models (POJOs)
│   │   ├── Book.java
│   │   ├── Student.java
│   │   ├── User.java
│   │   ├── BorrowHistory.java
│   │   ├── Reservation.java
│   │   ├── FineHistory.java
│   │   ├── Notification.java
│   │   ├── Review.java
│   │   ├── AttendanceLog.java
│   │   ├── Category.java
│   │   └── Achievement.java
│   ├── database/
│   │   ├── DatabaseConnection.java
│   │   ├── InMemoryDatabaseDriver.java
│   │   ├── SchemaInitializer.java  # Auto-creates tables & seeds demo data
│   │   └── schema.sql
│   ├── utils/
│   │   ├── FineCalculator.java
│   │   ├── PasswordUtils.java
│   │   ├── QRCodeGenerator.java
│   │   ├── ReportExporter.java
│   │   ├── ThemeManager.java
│   │   └── dsa/
│   │       ├── BinarySearchUtil.java
│   │       ├── BookHashMapIndex.java
│   │       ├── MergeSortUtil.java
│   │       ├── WaitingQueue.java
│   │       └── ActionStack.java
│   └── web/
│       ├── server/
│       │   └── LibraWebServer.java # HTTP API + static file server
│       ├── index.html              # Main SPA dashboard
│       ├── html/
│       │   └── report_template.html
│       ├── css/                    # Stylesheets
│       └── js/
│           ├── app.js              # Frontend application logic
│           ├── report_chart.js
│           ├── qrcode.min.js
│           └── jsQR.min.js
├── otp-server.js                   # Node.js OTP email microservice
├── package.json
├── .env.example                    # Environment variable template
└── bin/                            # Compiled Java class files
```

---

## ⚙️ Setup & Installation

### Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | 21 or higher |
| Node.js | 18+ (for OTP email server) |
| MySQL | 8+ **or** SQLite (auto-selected) |

---

### 1️⃣ Clone the Repository

```bash
git clone <repository-url>
cd "Library management system"
```

---

### 2️⃣ Configure the OTP Email Server

```bash
# Copy the example env file
copy .env.example .env
```

Edit `.env` and fill in your Gmail credentials:

```env
MAIL_USER=your_email@gmail.com
MAIL_PASS=your_app_password       # Gmail App Password (not your real password)
ADMIN_EMAIL=admin@example.com
OTP_PORT=3001
```

> **How to generate a Gmail App Password:**
> Google Account → Security → 2-Step Verification → App Passwords

---

### 3️⃣ Start the OTP Email Server

```bash
npm install
node otp-server.js
```

The OTP server will start on **port 3001** (configurable via `OTP_PORT`).

---

### 4️⃣ Compile the Java Application

```bash
javac -d bin -sourcepath src src/Main.java
```

---

### 5️⃣ Run the Application

```bash
java -cp bin Main
```

The application will:
1. ✅ Auto-initialize the database schema and seed demo data
2. ✅ Start the web backend on **http://localhost:8080**
3. ✅ Automatically open the dashboard in your default browser

---

## 🌐 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/stats` | Dashboard summary statistics |
| `POST` | `/api/auth/login` | Admin / Student login |
| `POST` | `/api/auth/register` | Register a new student |
| `POST` | `/api/auth/change-password` | Change account password |
| `GET/POST/DELETE` | `/api/books` | Book CRUD operations |
| `POST` | `/api/borrow/issue` | Issue a book to a student |
| `POST` | `/api/borrow/return` | Return a borrowed book |
| `GET` | `/api/borrow/history` | Borrowing history |
| `GET` | `/api/borrow/overdue` | List overdue borrows |
| `GET/POST` | `/api/students` | Student management |
| `POST` | `/api/students/status` | Update student status |
| `POST` | `/api/attendance/scan` | QR attendance scan |
| `GET` | `/api/attendance/logs` | Attendance log records |
| `GET` | `/api/analytics/top-visitors` | Top-visitor leaderboard |
| `GET` | `/api/health` | Server health check |

---

## 🗄️ Database Schema

The system supports **MySQL** and **SQLite** (auto-detected at startup). Tables are created automatically via `SchemaInitializer`.

| Table | Purpose |
|---|---|
| `Admins` | Admin accounts |
| `Students` | Student accounts & profiles |
| `Categories` | Book categories |
| `Books` | Book catalog with location info |
| `BorrowHistory` | All borrow/return transactions |
| `Reservations` | Book reservation records |
| `WaitingQueue` | FIFO queue for unavailable books |
| `Reviews` | Student book reviews & ratings |
| `Notifications` | In-app notification feed |
| `FineHistory` | Fine records and payment status |
| `Achievements` | Student reading badges |
| `AttendanceLogs` | QR scan attendance records |

---

## 🧪 Running Tests

The project includes a **self-contained test suite** (`LibraAITestRunner`) with no external dependencies — pure Java 21.

```bash
# Compile
javac -d bin -sourcepath src src/LibraAITestRunner.java

# Run
java -cp bin LibraAITestRunner
```

**Test coverage includes:**
- ✅ Unit tests — FineCalculator, PasswordUtils, DSA utilities
- ✅ Integration tests — BinarySearch, MergeSort, HashMap Index, WaitingQueue
- ✅ Smoke tests — application startup checks
- ✅ Sanity tests — model integrity verification
- ✅ Security tests — password hashing, SQL injection guards

---

## 🔣 DSA Implementations

The project includes custom Data Structures & Algorithm implementations used in core library operations:

| Class | Algorithm / Structure | Usage |
|---|---|---|
| `MergeSortUtil` | Merge Sort | Sorting book lists |
| `BinarySearchUtil` | Binary Search | Fast book lookup |
| `BookHashMapIndex` | Hash Map | O(1) book index |
| `WaitingQueue` | FIFO Queue | Book reservation queue |
| `ActionStack` | Stack | Undo action history |

---

## 🔒 Security Highlights

- Passwords stored as **BCrypt hashes** — never in plain text
- OTP-based email verification for sensitive operations
- Input sanitization in DAO layer
- CORS-enabled API server for safe cross-origin access

---

## 📄 License

This project is intended for **educational purposes**. Feel free to fork, modify, and use it as a learning reference for full-stack Java development.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push and open a Pull Request

---

> Built with ❤️ using Java 21 · JDK HttpServer · Vanilla JS · MySQL/SQLite
