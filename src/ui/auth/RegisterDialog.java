package ui.auth;

import controller.LibraryController;
import model.Student;
import ui.components.ModernButton;
import ui.components.ModernTextField;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterDialog extends JDialog {

    private final LibraryController controller;
    private ModernTextField txtCode, txtName, txtEmail, txtPhone, txtDepartment;
    private JPasswordField txtPassword;
    private JComboBox<Integer> cbYear;

    public RegisterDialog(JFrame owner, LibraryController controller) {
        super(owner, "Register Student Account", true);
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        setSize(480, 560);
        setLocationRelativeTo(getOwner());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(24, 32, 24, 32));
        panel.setBackground(ThemeManager.LIGHT_BG);

        JLabel lblTitle = new JLabel("🎓 Student Registration");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(ThemeManager.PRIMARY_ORANGE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtCode = new ModernTextField("Student Code (e.g. STU-2026-003)");
        txtName = new ModernTextField("Full Name");
        txtEmail = new ModernTextField("Email Address");
        txtPhone = new ModernTextField("Phone Number");
        txtDepartment = new ModernTextField("Department (e.g. Computer Science)");

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_GRAY, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        cbYear = new JComboBox<>(new Integer[]{1, 2, 3, 4});

        ModernButton btnSubmit = new ModernButton("Create Account", ModernButton.ButtonStyle.PRIMARY);
        btnSubmit.setMaximumSize(new Dimension(400, 42));
        btnSubmit.addActionListener(e -> handleRegister());

        panel.add(lblTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));
        panel.add(txtCode);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(txtName);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(txtEmail);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(txtPhone);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(txtDepartment);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(txtPassword);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(cbYear);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(btnSubmit);

        add(panel);
    }

    private void handleRegister() {
        String code = txtCode.getText().trim();
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String dept = txtDepartment.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();
        int year = (Integer) cbYear.getSelectedItem();

        if (code.isEmpty() || name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student student = new Student();
        student.setStudentCode(code);
        student.setFullName(name);
        student.setEmail(email);
        student.setPhone(phone);
        student.setDepartment(dept);
        student.setYearOfStudy(year);

        boolean success = controller.registerStudent(student, pass);
        if (success) {
            JOptionPane.showMessageDialog(this, "Student registered successfully! You can now log in.", "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to register. Student Code or Email might already exist.", "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
