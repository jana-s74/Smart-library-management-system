-- ============================================================
-- 📚 LibraAI – Smart Library Management System SQL Schema
-- Supports both MySQL and SQLite database dialects
-- ============================================================

CREATE TABLE IF NOT EXISTS Admins (
    admin_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    profile_pic_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    student_code VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    phone VARCHAR(20),
    department VARCHAR(50),
    year_of_study INT DEFAULT 1,
    max_borrow_limit INT DEFAULT 5,
    current_borrowed INT DEFAULT 0,
    total_fines_owed DOUBLE DEFAULT 0.0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    profile_pic_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    icon_name VARCHAR(50) DEFAULT 'book'
);

CREATE TABLE IF NOT EXISTS Books (
    book_id INT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(150) NOT NULL,
    publisher VARCHAR(150),
    category_id INT,
    language VARCHAR(30) DEFAULT 'English',
    edition VARCHAR(30) DEFAULT '1st Edition',
    description TEXT,
    shelf_number VARCHAR(20),
    rack_number VARCHAR(20),
    floor_number INT DEFAULT 1,
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    cover_image_path VARCHAR(255),
    qr_code_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES Categories(category_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS BorrowHistory (
    borrow_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP NOT NULL,
    return_date TIMESTAMP NULL,
    status VARCHAR(20) DEFAULT 'BORROWED', -- BORROWED, RETURNED, OVERDUE
    fine_amount DOUBLE DEFAULT 0.0,
    fine_paid BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES Books(book_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Reservations (
    reservation_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    book_id INT NOT NULL,
    reservation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, FULFILLED, CANCELLED
    queue_position INT DEFAULT 1,
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES Books(book_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS WaitingQueue (
    queue_id INT PRIMARY KEY AUTO_INCREMENT,
    book_id INT NOT NULL,
    student_id INT NOT NULL,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'WAITING',
    FOREIGN KEY (book_id) REFERENCES Books(book_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Reviews (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    book_id INT NOT NULL,
    student_id INT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    review_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES Books(book_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    user_type VARCHAR(10) NOT NULL, -- ADMIN or STUDENT
    user_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS FineHistory (
    fine_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    borrow_id INT NOT NULL,
    amount DOUBLE NOT NULL,
    reason VARCHAR(255),
    payment_status VARCHAR(20) DEFAULT 'UNPAID', -- UNPAID, PAID
    paid_at TIMESTAMP NULL,
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (borrow_id) REFERENCES BorrowHistory(borrow_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Achievements (
    achievement_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    badge_type VARCHAR(50) NOT NULL,
    unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE
);
