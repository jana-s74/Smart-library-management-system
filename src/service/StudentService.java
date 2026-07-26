package service;

import dao.UserDAO;
import model.Student;

import java.util.List;

public class StudentService {

    private final UserDAO userDAO;

    public StudentService() {
        this.userDAO = new UserDAO();
    }

    public List<Student> getAllStudents() {
        return userDAO.getAllStudents();
    }

    public Student getStudentById(int studentId) {
        return userDAO.getStudentById(studentId);
    }

    public boolean updateStatus(int studentId, String status) {
        return userDAO.updateStudentStatus(studentId, status);
    }
}
