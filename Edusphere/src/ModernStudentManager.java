import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ModernStudentManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/college_management";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "200030";

    private JFrame frame;
    private JTable studentTable;
    private DefaultTableModel tableModel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ModernStudentManager::new);
    }

    public ModernStudentManager() {
        setupGUI();
        loadStudents();
    }

    private void setupGUI() {
        frame = new JFrame("Student Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Gradient background panel
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(173, 216, 230);
                Color color2 = new Color(240, 248, 255);
                g2d.setPaint(new GradientPaint(0, 0, color1, 0, getHeight(), color2));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "DOB", "Course", "Year"}, 0);
        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(25);
        studentTable.setSelectionBackground(new Color(135, 206, 250));
        studentTable.setSelectionForeground(Color.WHITE);
        studentTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JTableHeader header = studentTable.getTableHeader();
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(studentTable);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton addButton = createRoundedButton("Add Student", new Color(50, 205, 50));
        JButton updateButton = createRoundedButton("Update Student", new Color(255, 165, 0));
        JButton deleteButton = createRoundedButton("Delete Student", new Color(255, 69, 0));

        addButton.addActionListener(e -> openAddStudentDialog());
        updateButton.addActionListener(e -> openUpdateStudentDialog());
        deleteButton.addActionListener(e -> deleteStudent());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // Add components to the main panel
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private JButton createRoundedButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(button.getBackground());
                g2d.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), 30, 30);
                super.paint(g2d, c);
                g2d.dispose();
            }
        });
        return button;
    }

    private void loadStudents() {
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM Students");
            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("ID"),
                        rs.getString("Name"),
                        rs.getDate("DOB"),
                        rs.getString("Course"),
                        rs.getInt("Year")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading students: " + e.getMessage());
        }
    }

    private void openAddStudentDialog() {
        JDialog dialog = new JDialog(frame, "Add Student", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(5, 2));

        JTextField nameField = new JTextField();
        JTextField dobField = new JTextField("YYYY-MM-DD");
        JTextField courseField = new JTextField();
        JTextField yearField = new JTextField();

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("DOB:"));
        dialog.add(dobField);
        dialog.add(new JLabel("Course:"));
        dialog.add(courseField);
        dialog.add(new JLabel("Year:"));
        dialog.add(yearField);

        JButton saveButton = createRoundedButton("Save", new Color(30, 144, 255));
        saveButton.addActionListener(e -> {
            String name = nameField.getText();
            String dob = dobField.getText();
            String course = courseField.getText();
            int year = Integer.parseInt(yearField.getText());

            addStudent(name, dob, course, year);
            dialog.dispose();
        });

        dialog.add(new JLabel()); // Empty space
        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    private void addStudent(String name, String dob, String course, int year) {
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO Students (Name, DOB, Course, Year) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, name);
            ps.setDate(2, Date.valueOf(dob));
            ps.setString(3, course);
            ps.setInt(4, year);
            ps.executeUpdate();
            loadStudents();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error adding student: " + e.getMessage());
        }
    }

    private void openUpdateStudentDialog() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a student to update.");
            return;
        }

        int studentID = (int) tableModel.getValueAt(selectedRow, 0);
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentDOB = tableModel.getValueAt(selectedRow, 2).toString();
        String currentCourse = (String) tableModel.getValueAt(selectedRow, 3);
        int currentYear = (int) tableModel.getValueAt(selectedRow, 4);

        JDialog dialog = new JDialog(frame, "Update Student", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(5, 2));

        JTextField nameField = new JTextField(currentName);
        JTextField dobField = new JTextField(currentDOB);
        JTextField courseField = new JTextField(currentCourse);
        JTextField yearField = new JTextField(String.valueOf(currentYear));

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("DOB:"));
        dialog.add(dobField);
        dialog.add(new JLabel("Course:"));
        dialog.add(courseField);
        dialog.add(new JLabel("Year:"));
        dialog.add(yearField);

        JButton saveButton = createRoundedButton("Save", new Color(30, 144, 255));
        saveButton.addActionListener(e -> {
            String name = nameField.getText();
            String dob = dobField.getText();
            String course = courseField.getText();
            int year = Integer.parseInt(yearField.getText());

            updateStudent(studentID, name, dob, course, year);
            dialog.dispose();
        });

        dialog.add(new JLabel()); // Empty space
        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    private void updateStudent(int id, String name, String dob, String course, int year) {
        try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE Students SET Name = ?, DOB = ?, Course = ?, Year = ? WHERE ID = ?")) {
            ps.setString(1, name);
            ps.setDate(2, Date.valueOf(dob));
            ps.setString(3, course);
            ps.setInt(4, year);
            ps.setInt(5, id);
            ps.executeUpdate();
            loadStudents();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error updating student: " + e.getMessage());
        }
    }

    private void deleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a student to delete.");
            return;
        }

        int studentID = (int) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this student?",
                "Delete Student", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement ps = con.prepareStatement("DELETE FROM Students WHERE ID = ?")) {
                ps.setInt(1, studentID);
                ps.executeUpdate();
                loadStudents();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error deleting student: " + e.getMessage());
            }
        }
    }
}

