package ui.students;

import controller.LibraryController;
import model.Student;
import ui.components.ModernButton;
import ui.components.ModernTable;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentManagementPanel extends JPanel {

    private final LibraryController controller;
    private ModernTable table;
    private DefaultTableModel tableModel;

    public StudentManagementPanel(LibraryController controller) {
        this.controller = controller;
        initUI();
        refreshTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(16, 16));
        setBackground(ThemeManager.LIGHT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel lblHeader = new JLabel("🎓 Student Directory & Accounts");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(ThemeManager.TEXT_DARK);

        add(lblHeader, BorderLayout.NORTH);

        String[] columns = {"ID", "Code", "Full Name", "Email", "Phone", "Department", "Year", "Borrowed", "Fines Owed ($)", "Status"};
        tableModel = new DefaultTableModel(columns, 0);

        table = new ModernTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomBar.setOpaque(false);

        ModernButton btnToggleStatus = new ModernButton("Toggle Account Status", ModernButton.ButtonStyle.UPDATE);
        btnToggleStatus.addActionListener(e -> handleToggleStatus());

        bottomBar.add(btnToggleStatus);

        add(bottomBar, BorderLayout.SOUTH);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Student> students = controller.getAllStudents();
        for (Student s : students) {
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getStudentCode(),
                    s.getFullName(),
                    s.getEmail(),
                    s.getPhone(),
                    s.getDepartment(),
                    s.getYearOfStudy(),
                    s.getCurrentBorrowed(),
                    String.format("%.2f", s.getTotalFinesOwed()),
                    s.getStatus()
            });
        }
    }

    private void handleToggleStatus() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int studentId = (Integer) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 9);
        String newStatus = "ACTIVE".equalsIgnoreCase(currentStatus) ? "SUSPENDED" : "ACTIVE";

        if (controller.updateStudentStatus(studentId, newStatus)) {
            refreshTable();
            JOptionPane.showMessageDialog(this, "Student status updated to " + newStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
