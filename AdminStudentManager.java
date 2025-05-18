package Admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class AdminStudentManager extends JPanel {

    JTextField txtName, txtUsername, txtAddress, txtGuardianName, txtGuardianNo, txtHostelFee, txtBalance;
    // Removed txtRoomNo
    JTable table;
    DefaultTableModel model;
    JButton btnCheckIn, btnCheckOut;
    private Map<String, Integer> roomNameToIdMap; // Map to store room number to room ID
    private JComboBox<String> comboRoomNo;  // JComboBox for room numbers
    private Vector<String> roomNumbers;

    public AdminStudentManager() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(225, 227, 228));

        // --- Top Panel: Add Student Form ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Student"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblName = new JLabel("Name:");
        lblName.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblName, gbc);

        txtName = new JTextField();
        txtName.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(txtName, gbc);

        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(lblUsername, gbc);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(txtUsername, gbc);

        JLabel lblAddress = new JLabel("Address:");
        lblAddress.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(lblAddress, gbc);

        txtAddress = new JTextField();
        txtAddress.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(txtAddress, gbc);

        JLabel lblGuardianName = new JLabel("Guardian Name:");
        lblGuardianName.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(lblGuardianName, gbc);

        txtGuardianName = new JTextField();
        txtGuardianName.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(txtGuardianName, gbc);

        JLabel lblGuardianNo = new JLabel("Guardian No:");
        lblGuardianNo.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(lblGuardianNo, gbc);

        txtGuardianNo = new JTextField();
        txtGuardianNo.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 4;
        formPanel.add(txtGuardianNo, gbc);

        JLabel lblHostelFee = new JLabel("Hostel Fee:");
        lblHostelFee.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(lblHostelFee, gbc);

        txtHostelFee = new JTextField();
        txtHostelFee.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 5;
        formPanel.add(txtHostelFee, gbc);

        JLabel lblBalance = new JLabel("Remaining Fee:");
        lblBalance.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(lblBalance, gbc);

        txtBalance = new JTextField();
        txtBalance.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 6;
        formPanel.add(txtBalance, gbc);

        JLabel lblRoomNo = new JLabel("Room No:");
        lblRoomNo.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(lblRoomNo, gbc);

        // txtRoomNo = new JTextField();  Removed txtRoomNo
        // txtRoomNo.setFont(new Font("Tahoma", Font.PLAIN, 16));
        // gbc.gridx = 1;
        // gbc.gridy = 7;
        // formPanel.add(txtRoomNo, gbc);

        roomNumbers = loadRoomNumbers();
        comboRoomNo = new JComboBox<>(roomNumbers);
        comboRoomNo.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 7;
        formPanel.add(comboRoomNo, gbc);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnAdd = new JButton("Add Student");
        btnAdd.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnAdd.setBackground(new Color(30, 144, 255));
        btnAdd.setForeground(Color.WHITE);

        JButton btnClear = new JButton("Clear");
        btnClear.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnClear.setBackground(new Color(220, 20, 60));
        btnClear.setForeground(Color.WHITE);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnClear);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        // --- Center Panel: Table ---

        model = new DefaultTableModel(new String[]{"ID", "Name", "Username", "Address", "Guardian Name", "Guardian No", "Hostel Fee", "Remaining Fee", "Hostel Status", "Room No"}, 0);
        table = new JTable(model);
        table.setFont(new Font("Tahoma", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 11));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("View Students"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Bottom Panel: Edit/Delete and Check-in/Check-out Buttons ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnEdit = new JButton("Edit Student");
        btnEdit.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnEdit.setBackground(new Color(255, 140, 0));
        btnEdit.setForeground(Color.WHITE);

        JButton btnDelete = new JButton("Delete Student");
        btnDelete.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnDelete.setBackground(new Color(220, 20, 60));
        btnDelete.setForeground(Color.WHITE);

        btnCheckIn = new JButton("Check-in");
        btnCheckIn.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnCheckIn.setBackground(new Color(0, 128, 0));
        btnCheckIn.setForeground(Color.WHITE);

        btnCheckOut = new JButton("Check-out");
        btnCheckOut.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnCheckOut.setBackground(new Color(178, 34, 34));
        btnCheckOut.setForeground(Color.WHITE);

        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnCheckIn);
        bottomPanel.add(btnCheckOut);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Initialize the roomNameToIdMap
        roomNameToIdMap = new HashMap<>();
        loadRoomData(); // Load room data from the database

        // Button Actions
        btnAdd.addActionListener(e -> addStudent());
        btnClear.addActionListener(e -> clearForm());
        btnEdit.addActionListener(e -> editStudent());
        btnDelete.addActionListener(e -> deleteStudent());
        btnCheckIn.addActionListener(e -> checkInStudent());
        btnCheckOut.addActionListener(e -> checkOutStudent());

        viewStudent();
        setVisible(true);
    }

    private void loadRoomData() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT room_id, room_no FROM room")) {

            while (rs.next()) {
                int roomId = rs.getInt("room_id");
                String roomNo = rs.getString("room_no");
                roomNameToIdMap.put(roomNo, roomId);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading room data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Vector<String> loadRoomNumbers() {
        Vector<String> numbers = new Vector<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT room_no FROM room ORDER BY room_no ASC")) { //added order by
            while (rs.next()) {
                String roomNo = rs.getString("room_no");
                numbers.add(roomNo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading room numbers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return numbers;
    }

    void addStudent() {
        String name = txtName.getText();
        String username = txtUsername.getText();
        String address = txtAddress.getText();
        String guardianName = txtGuardianName.getText();
        String guardianNo = txtGuardianNo.getText();
        String hostelFee = txtHostelFee.getText();
        String balance = txtBalance.getText();
        //String roomNo = txtRoomNo.getText();  Removed
        String roomNo = (String) comboRoomNo.getSelectedItem();

        if (name.isEmpty() || username.isEmpty() || hostelFee.isEmpty() || balance.isEmpty() || roomNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields including Room No!");
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
             PreparedStatement pstInsert = con.prepareStatement("INSERT INTO student ( std_name, std_username, std_address, std_guardian_name, std_guardian_no, std_hostel_fee, std_balance, std_hostel_status, room_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            Integer roomId = roomNameToIdMap.get(roomNo); // Get room ID from the map
            if (roomId != null) {
                pstInsert.setString(1, name);
                pstInsert.setString(2, username);
                pstInsert.setString(3, address);
                pstInsert.setString(4, guardianName);
                pstInsert.setString(5, guardianNo);
                pstInsert.setBigDecimal(6, new java.math.BigDecimal(hostelFee));
                pstInsert.setBigDecimal(7, new java.math.BigDecimal(balance));
                pstInsert.setString(8, "Residing");
                pstInsert.setInt(9, roomId);
                pstInsert.executeUpdate();
                JOptionPane.showMessageDialog(this, "Student Added Successfully!");
                clearForm();
                viewStudent();
            } else {
                JOptionPane.showMessageDialog(this, "Room No '" + roomNo + "' does not exist!");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Add Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void clearForm() {
        txtName.setText("");
        txtUsername.setText("");
        txtAddress.setText("");
        txtGuardianName.setText("");
        txtGuardianNo.setText("");
        txtHostelFee.setText("");
        txtBalance.setText("");
        //txtRoomNo.setText("");  Removed
        comboRoomNo.setSelectedIndex(-1);
    }

    void viewStudent() {
        model.setRowCount(0);
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT s.std_id, s.std_name, s.std_username, s.std_address, s.std_guardian_name, s.std_guardian_no, s.std_hostel_fee, s.std_balance, s.std_hostel_status, r.room_no " +
                     "FROM student s JOIN room r ON s.room_id = r.room_id")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("std_id"),
                        rs.getString("std_name"),
                        rs.getString("std_username"),
                        rs.getString("std_address"),
                        rs.getString("std_guardian_name"),
                        rs.getString("std_guardian_no"),
                        rs.getBigDecimal("std_hostel_fee"),
                        rs.getBigDecimal("std_balance"),
                        rs.getString("std_hostel_status"),
                        rs.getString("room_no")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Load Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void editStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int studentId = (int) model.getValueAt(selectedRow, 0);
            AdminUpdateStudent updateDialog = new AdminUpdateStudent(studentId);
            updateDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    viewStudent();
                }
            });
            updateDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a student to edit!", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    void deleteStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int studentId = (int) model.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this student?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
                 PreparedStatement pst = con.prepareStatement("DELETE FROM student WHERE std_id=?")) {

                pst.setInt(1, studentId);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Student Deleted Successfully!");
                viewStudent();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Delete Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a student to delete!");
        }
    }

    void checkInStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int studentId = (int) model.getValueAt(selectedRow, 0);
            updateStudentHostelStatus(studentId, "Residing");
            AdminHostelLogManager.logActivity(String.valueOf(studentId), "Check-in");
            viewStudent();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a student to check in.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    void checkOutStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int studentId = (int) model.getValueAt(selectedRow, 0);
            updateStudentHostelStatus(studentId, "Left");
            AdminHostelLogManager.logActivity(String.valueOf(studentId), "Check-out");
            viewStudent();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a student to check out.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateStudentHostelStatus(int studentId, String status) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
             PreparedStatement pst = con.prepareStatement("UPDATE student SET std_hostel_status = ? WHERE std_id = ?")) {
            pst.setString(1, status);
            pst.setInt(2, studentId);
            pst.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating hostel status: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new AdminStudentManager();
    }
}

