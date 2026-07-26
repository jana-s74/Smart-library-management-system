package service;

import dao.UserDAO;
import model.Student;
import model.User;

public class AuthService {

    private final UserDAO userDAO;
    private User currentUser;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public User loginAdmin(String username, String password) {
        User admin = userDAO.authenticateAdmin(username, password);
        if (admin != null) {
            this.currentUser = admin;
        }
        return admin;
    }

    public Student loginStudent(String identifier, String password) {
        Student student = userDAO.authenticateStudent(identifier, password);
        if (student != null) {
            this.currentUser = student;
        }
        return student;
    }

    public boolean registerStudent(Student student, String password) {
        return userDAO.registerStudent(student, password);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }
}
