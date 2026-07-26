package ui.auth;

import controller.LibraryController;
import model.Student;
import model.User;
import ui.components.ModernButton;
import ui.components.ModernCard;
import ui.components.ModernTextField;
import ui.dashboard.AdminDashboardFrame;
import ui.dashboard.StudentDashboardFrame;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final LibraryController controller;
    private ModernTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkRememberMe;
    private JRadioButton radAdmin;
    private JRadioButton radStudent;

    public LoginFrame() {
        this.controller = new LibraryController();
        initUI();
    }

    private void initUI() {
        setTitle("📚 LibraAI – Login");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(ThemeManager.LIGHT_BG);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(ThemeManager.LIGHT_BG);

        // Center Login Box Card
        ModernCard card = new ModernCard();
        card.setPreferredSize(new Dimension(440, 520));
        card.setBorder(new EmptyBorder(32, 36, 32, 36));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Header Title
        JLabel lblTitle = new JLabel("🎓 Welcome to LibraAI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(ThemeManager.PRIMARY_PURPLE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Smart Library Management System");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeManager.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Role Switcher Radio buttons
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        rolePanel.setOpaque(false);

        radAdmin = new JRadioButton("Admin", true);
        radStudent = new JRadioButton("Student", false);
        radAdmin.setOpaque(false);
        radStudent.setOpaque(false);
        radAdmin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        radStudent.setFont(new Font("Segoe UI", Font.BOLD, 13));

        ButtonGroup group = new ButtonGroup();
        group.add(radAdmin);
        group.add(radStudent);
        rolePanel.add(radAdmin);
        rolePanel.add(radStudent);

        // Input Fields
        txtUsername = new ModernTextField("Username or Email (e.g. admin or STU-2026-001)");
        txtUsername.setMaximumSize(new Dimension(360, 42));

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setMaximumSize(new Dimension(360, 42));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_GRAY, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // Options Row (Remember me / Forgot Password)
        JPanel optPanel = new JPanel(new BorderLayout());
        optPanel.setOpaque(false);
        optPanel.setMaximumSize(new Dimension(360, 30));

        chkRememberMe = new JCheckBox("Remember Me");
        chkRememberMe.setOpaque(false);
        chkRememberMe.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel lblForgot = new JLabel("Forgot Password?");
        lblForgot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblForgot.setForeground(ThemeManager.PRIMARY_PURPLE);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));

        optPanel.add(chkRememberMe, BorderLayout.WEST);
        optPanel.add(lblForgot, BorderLayout.EAST);

        // Buttons
        ModernButton btnLogin = new ModernButton("Sign In", ModernButton.ButtonStyle.GREEN_PILL);
        btnLogin.setMaximumSize(new Dimension(360, 44));
        btnLogin.addActionListener(e -> handleLogin());

        ModernButton btnRegister = new ModernButton("Register Student Account", ModernButton.ButtonStyle.SECONDARY);
        btnRegister.setMaximumSize(new Dimension(360, 40));
        btnRegister.addActionListener(e -> new RegisterDialog(this, controller).setVisible(true));

        // Assembly
        card.add(lblTitle);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(lblSub);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(rolePanel);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(txtUsername);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(txtPassword);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(optPanel);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(btnLogin);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(btnRegister);

        mainPanel.add(card);
        add(mainPanel);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (radAdmin.isSelected()) {
            User admin = controller.loginAdmin(username, password);
            if (admin != null) {
                new AdminDashboardFrame(controller, admin).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Admin credentials! (Default: admin / admin123)", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            Student student = controller.loginStudent(username, password);
            if (student != null) {
                new StudentDashboardFrame(controller, student).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Student credentials! (Default: STU-2026-001 / student123)", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
