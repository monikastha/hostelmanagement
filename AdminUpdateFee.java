package Admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class AdminUpdateFee extends JFrame {
    private int feeId;
    private JTextField txtStdName, txtCredit, txtRemaining, txtDateMonth;
    private JButton btnUpdate, btnCancel;
    private AdminFeeManager parentPanel; // Reference to the parent panel to refresh data
    private Map<String, String> nameToIdMap; // Map student name to ID (not strictly needed here anymore but kept for consistency if you extend this class)
    private String currentStudentName; // Store the initial student name

    public AdminUpdateFee(int feeId, String currentStudentName, AdminFeeManager parentPanel) {
        this.feeId = feeId;
        this.parentPanel = parentPanel;
        this.currentStudentName = currentStudentName;
        this.nameToIdMap = new HashMap<>(); // Initialize even if not directly used in this version

        setTitle("Update Fee Payment");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblStdName = new JLabel("Student Name:");
        lblStdName.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(lblStdName, gbc);

        txtStdName = new JTextField();
        txtStdName.setFont(new Font("Tahoma", Font.PLAIN, 16));
        txtStdName.setPreferredSize(new Dimension(200, 30));
        txtStdName.setEditable(false); // Make the text field non-editable
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(txtStdName, gbc);

        JLabel lblCredit = new JLabel("Paid Amount:");
        lblCredit.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(lblCredit, gbc);

        txtCredit = new JTextField();
        txtCredit.setFont(new Font("Tahoma", Font.PLAIN, 16));
        txtCredit.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(txtCredit, gbc);

        JLabel lblRemaining = new JLabel("Remaining Amount:");
        lblRemaining.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(lblRemaining, gbc);

        txtRemaining = new JTextField();
        txtRemaining.setFont(new Font("Tahoma", Font.PLAIN, 16));
        txtRemaining.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(txtRemaining, gbc);

        JLabel lblDateMonth = new JLabel("Date/Month:");
        lblDateMonth.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(lblDateMonth, gbc);

        txtDateMonth = new JTextField();
        txtDateMonth.setFont(new Font("Tahoma", Font.PLAIN, 16));
        txtDateMonth.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 3;
        add(txtDateMonth, gbc);

        btnUpdate = new JButton("Update Payment");
        btnUpdate.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnUpdate.setBackground(new Color(30, 144, 255));
        btnUpdate.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(btnUpdate, gbc);

        btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnCancel.setBackground(new Color(220, 20, 60));
        btnCancel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        add(btnCancel, gbc);

        // Load current data of fee record
        loadFeeData();

        // Button Actions
        btnUpdate.addActionListener(e -> updateFee());
        btnCancel.addActionListener(e -> dispose());

        setVisible(true);
    }

    private void loadFeeData() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
             PreparedStatement pst = con.prepareStatement("SELECT s.std_name, f.f_credit, f.f_remaining, f.f_date_month, f.std_id FROM fee f JOIN student s ON f.std_id = s.std_id WHERE f.f_id = ?")) {

            pst.setInt(1, feeId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                txtStdName.setText(rs.getString("std_name"));
                txtCredit.setText(rs.getBigDecimal("f_credit").toString());
                txtRemaining.setText(rs.getBigDecimal("f_remaining").toString());
                txtDateMonth.setText(rs.getString("f_date_month"));
                // We might still need the student ID for the update query
                nameToIdMap.put(rs.getString("std_name"), rs.getString("std_id"));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading fee data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFee() {
        String studentName = txtStdName.getText(); // Get the student name from the non-editable field
        String credit = txtCredit.getText();
        String remaining = txtRemaining.getText();
        String dateMonth = txtDateMonth.getText();

        // Validation
        if (studentName == null || credit.isEmpty() || remaining.isEmpty() || dateMonth.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        // We need the student ID to update. Since the txtStdName is non-editable,
        // we should have fetched the ID in loadFeeData and stored it (or refetch it).
        // For simplicity, let's assume we refetch it based on the name.
        String stdId = null;
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
             PreparedStatement pst = con.prepareStatement("SELECT std_id FROM student WHERE std_name = ?")) {
            pst.setString(1, studentName);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                stdId = rs.getString("std_id");
            } else {
                JOptionPane.showMessageDialog(this, "Error: Could not retrieve Student ID.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (stdId == null) {
            return; // Already showed an error message
        }

        try {
            //  Use BigDecimal for precision
            BigDecimal creditDecimal = new BigDecimal(credit);
            BigDecimal remainingDecimal = new BigDecimal(remaining);
            String statusUpdate = remainingDecimal.compareTo(BigDecimal.ZERO) > 0 ? "Pending" : "Paid";

            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
                 PreparedStatement pst = con.prepareStatement("UPDATE fee SET std_id = ?, f_credit = ?, f_remaining = ?, f_date_month = ?, f_status = ? WHERE f_id = ?")) {

                pst.setString(1, stdId); // Set student ID
                pst.setBigDecimal(2, creditDecimal);
                pst.setBigDecimal(3, remainingDecimal);
                pst.setString(4, dateMonth);
                pst.setString(5, statusUpdate); // Set the updated status
                pst.setInt(6, feeId);

                System.out.println("Remaining Decimal: " + remainingDecimal);
                System.out.println("Status Update: " + statusUpdate);

                int rowsUpdated = pst.executeUpdate();

                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, "Payment Updated Successfully!");
                    if (parentPanel != null) {
                        parentPanel.viewPayments(false); // Refresh the table in the parent panel
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Error updating payment. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format for Credit or Remaining amounts.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating payment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Example: Open Update Fee window with a specific fee ID (e.g., 1), current student name "John Doe", and a null parent panel for standalone testing
        new AdminUpdateFee(1, "John Doe", null);
    }
}