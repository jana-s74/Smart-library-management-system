package model;

import java.sql.Timestamp;

public class AttendanceLog {
    private int attendanceId;
    private int studentId;
    private String studentCode;
    private String studentName;
    private String department;
    private Timestamp checkInTime;
    private Timestamp checkOutTime;
    private String status;

    public AttendanceLog() {}

    public AttendanceLog(int attendanceId, int studentId, String studentCode, String studentName, String department, Timestamp checkInTime, Timestamp checkOutTime, String status) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.department = department;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
    }

    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Timestamp getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Timestamp checkInTime) { this.checkInTime = checkInTime; }

    public Timestamp getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(Timestamp checkOutTime) { this.checkOutTime = checkOutTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
