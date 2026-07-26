package model;

import java.sql.Timestamp;

public class Student extends User {
    private String studentCode;
    private String department;
    private int yearOfStudy;
    private int maxBorrowLimit;
    private int currentBorrowed;
    private double totalFinesOwed;
    private String status; // ACTIVE, SUSPENDED, GRADUATED

    public Student() {
        super();
        setRole("STUDENT");
    }

    public Student(int id, String studentCode, String fullName, String email, String phone, String department, int yearOfStudy, int maxBorrowLimit, int currentBorrowed, double totalFinesOwed, String status, String profilePicPath, Timestamp createdAt) {
        super(id, studentCode, fullName, email, phone, "STUDENT", profilePicPath, createdAt);
        this.studentCode = studentCode;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
        this.maxBorrowLimit = maxBorrowLimit;
        this.currentBorrowed = currentBorrowed;
        this.totalFinesOwed = totalFinesOwed;
        this.status = status;
    }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(int yearOfStudy) { this.yearOfStudy = yearOfStudy; }

    public int getMaxBorrowLimit() { return maxBorrowLimit; }
    public void setMaxBorrowLimit(int maxBorrowLimit) { this.maxBorrowLimit = maxBorrowLimit; }

    public int getCurrentBorrowed() { return currentBorrowed; }
    public void setCurrentBorrowed(int currentBorrowed) { this.currentBorrowed = currentBorrowed; }

    public double getTotalFinesOwed() { return totalFinesOwed; }
    public void setTotalFinesOwed(double totalFinesOwed) { this.totalFinesOwed = totalFinesOwed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
