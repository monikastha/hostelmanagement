package Admin;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class AdminFeeManager extends JPanel {
    private JButton btnAdd;
    private JButton btnClear;
    private JButton btnCreateBill;
    private JButton btnDelete;
    private JButton btnEdit;
    private JButton btnDownloadPayments; // Added button
    JComboBox<String> comboStudentName;
    private JLabel lblCredit;
    private JLabel lblDateMonth;
    private JLabel lblRemaining;
    private JLabel lblStudentName;
    private Map<String, String> nameToIdMap;
    DefaultTableModel model;
    int selectedRow = -1;
    JTable table;
    JTextField txtCredit, txtDateMonth, txtRemaining;
    private Vector<String> studentNames;
    private Map<String, Integer> studentHostelFees = new HashMap<>();
    private String studentName; // Added to store student name for PDF title
    private final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy");
    private final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Add a variable to store the student's hostel fee
    private int studentHostelFee = 0;

    public AdminFeeManager() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        nameToIdMap = new HashMap<>();
        studentNames = loadStudentNames();

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(225, 227, 228));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Fee Payment"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblCredit = new JLabel("Paid Amount:");
        lblCredit.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(lblCredit, gbc);

        lblDateMonth = new JLabel("Date/Month (YYYY-MM-DD):");
        lblDateMonth.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(lblDateMonth, gbc);

        lblRemaining = new JLabel("Remaining Amount:");  // Label is now "Remaining Amount"
        lblRemaining.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(lblRemaining, gbc);

        lblStudentName = new JLabel("Student Name:");
        lblStudentName.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblStudentName, gbc);

        comboStudentName = new JComboBox<>(studentNames);
        comboStudentName.setFont(new Font("Tahoma", Font.PLAIN, 16));
        comboStudentName.addActionListener(new ActionListener() {  // Added Action Listener
            @Override
            public void actionPerformed(ActionEvent e) {
                // Update the studentHostelFee when the student name is selected.
                String selectedStudentName = (String) comboStudentName.getSelectedItem();
                if (selectedStudentName != null) {
                    updateStudentHostelFee(nameToIdMap.get(selectedStudentName));
                    calculateAndDisplayBalance();
                }
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(comboStudentName, gbc);

        txtCredit = new JTextField();
        txtCredit.setFont(new Font("Tahoma", Font.PLAIN, 16));
        txtCredit.setEditable(true); // changed to true
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(txtCredit, gbc);

        txtDateMonth = new JTextField();
        txtDateMonth.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(txtDateMonth, gbc);

        txtRemaining = new JTextField();
        txtRemaining.setFont(new Font("Tahoma", Font.PLAIN, 16));
        txtRemaining.setEditable(false); // Make it non-editable.
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(txtRemaining, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        buttonPanel.setBackground(Color.WHITE);

        btnAdd = new JButton("Add Payment");
        btnAdd.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnAdd.setBackground(new Color(30, 144, 255));
        btnAdd.setForeground(Color.WHITE);

        btnClear = new JButton("Clear");
        btnClear.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnClear.setBackground(new Color(220, 20, 60));
        btnClear.setForeground(Color.WHITE);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnClear);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Fee ID", "Student Name", "Paid", "Balance", "Date/Month", "Status"}, 0); // Changed Header.  "Balance" is the remaining balance.
        table = new JTable(model);
        table.setFont(new Font("Tahoma", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 11));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("View Payments"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(Color.WHITE);

        btnCreateBill = new JButton("Create Bill");
        btnCreateBill.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnCreateBill.setBackground(new Color(100, 149, 237));
        btnCreateBill.setForeground(Color.WHITE);

        btnDelete = new JButton("Delete Payment");
        btnDelete.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnDelete.setBackground(new Color(220, 20, 60));
        btnDelete.setForeground(Color.WHITE);

        btnEdit = new JButton("Edit Payment");
        btnEdit.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnEdit.setBackground(new Color(255, 140, 0));
        btnEdit.setForeground(Color.WHITE);

        btnDownloadPayments = new JButton("Download Payments"); // Added Download button
        btnDownloadPayments.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnDownloadPayments.setBackground(new Color(46, 139, 87)); // Green color
        btnDownloadPayments.setForeground(Color.WHITE);
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnCreateBill);
        bottomPanel.add(btnDownloadPayments); // Add the button to the panel

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> addPayment());
        btnClear.addActionListener(e -> clearForm());
        btnCreateBill.addActionListener(_ -> createBill());
        btnDelete.addActionListener(_ -> deletePayment());
        btnEdit.addActionListener(e -> updatePayment());
        btnDownloadPayments.addActionListener(e -> downloadPaymentsPDF()); // Add listener to the new button

        viewPayments(false);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedRow = table.getSelectedRow();
            }
        });

        add(mainPanel);
    }

    // Method to update studentHostelFee
    private void updateStudentHostelFee(String studentId) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "")) {
            String query = "SELECT std_hostel_fee FROM student WHERE std_id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                studentHostelFee = rs.getInt("std_hostel_fee");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading student hostel fee: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            studentHostelFee = 0; // Reset to 0 in case of error
        }
    }

    void addPayment() {
        String studentName = (String) comboStudentName.getSelectedItem();
        String creditStr = txtCredit.getText();
        String dateMonth = txtDateMonth.getText();

        if (studentName == null || creditStr.isEmpty() || dateMonth.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!dateMonth.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Date must be in-MM-DD format", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String studentId = nameToIdMap.get(studentName);

        if (studentId == null) {
            JOptionPane.showMessageDialog(this, "Selected student name not found in mapping", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            float paidAmount = Float.parseFloat(creditStr);
            if (paidAmount < 0) {
                JOptionPane.showMessageDialog(this, "Paid amount cannot be negative", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "")) {
                PreparedStatement ps = conn.prepareStatement("SELECT std_hostel_fee, std_balance, std_name FROM student WHERE std_id = ?");
                ps.setString(1, studentId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Selected student not found in database", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                float studentHostelFee = rs.getFloat("std_hostel_fee");
                float remainingAmount = rs.getFloat("std_balance"); // Get remaining from student table.
                this.studentName = rs.getString("std_name");  // Get student name for PDF
                String status = "Pending"; // Default status
                if (remainingAmount <= 0) {
                    status = "Paid";
                }

                String insertQuery = "INSERT INTO fee (std_id, f_credit, f_remaining, f_date_month, f_status) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertPs = conn.prepareStatement(insertQuery);
                insertPs.setString(1, studentId);
                insertPs.setFloat(2, paidAmount);
                insertPs.setFloat(3, remainingAmount); // Use the remaining amount from student table
                insertPs.setString(4, dateMonth);
                insertPs.setString(5, status);
                int result = insertPs.executeUpdate();

                if (result > 0) {
                    //update student table
                    String updateStudentQuery = "UPDATE student SET std_balance = ? WHERE std_id = ?";
                    PreparedStatement updateStudentPs = conn.prepareStatement(updateStudentQuery);
                    updateStudentPs.setFloat(1, remainingAmount); // Update with the remaining amount
                    updateStudentPs.setString(2, studentId);
                    updateStudentPs.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Payment added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    viewPayments(false);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add payment", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for Paid Amount", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void clearForm() {
        comboStudentName.setSelectedIndex(-1);
        txtCredit.setText("");
        txtDateMonth.setText("");
        txtRemaining.setText("");
        studentHostelFee = 0; // Reset
    }

    private void createBill() {
        // Open CreateBillFrame.  Pass the parent JFrame.
        CreateBillFrame billFrame = new CreateBillFrame(this);
        billFrame.setVisible(true);
    }

    void deletePayment() {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a payment to delete", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int feeId = (int) model.getValueAt(selectedRow, 0);
        String studentName = (String) model.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the payment for student: " + studentName + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION); // Modified confirmation message
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "")) {
                conn.setAutoCommit(false); // Start a transaction

                try {
                    String deleteBillsQuery = "DELETE FROM bill WHERE f_id = ?";
                    PreparedStatement deleteBillsPs = conn.prepareStatement(deleteBillsQuery);
                    deleteBillsPs.setInt(1, feeId);
                    int billsDeleted = deleteBillsPs.executeUpdate();
                    System.out.println(billsDeleted + " bills deleted for fee ID " + feeId);
                    // 2. Delete the fee
                    String deleteFeeQuery = "DELETE FROM fee WHERE f_id = ?";
                    PreparedStatement deleteFeePs = conn.prepareStatement(deleteFeeQuery);
                    deleteFeePs.setInt(1, feeId);
                    int feesDeleted = deleteFeePs.executeUpdate();

                    if (feesDeleted > 0) {
                        conn.commit();
                        JOptionPane.showMessageDialog(this, "Payment deleted successfully: ", "Success", JOptionPane.INFORMATION_MESSAGE); // Modified success message
                        viewPayments(false);
                        selectedRow = -1;
                    } else {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "Failed to delete payment  ", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException e) {
                    conn.rollback();
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage() + "  Transaction rolled back.", "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database connection error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Vector<String> loadStudentNames() {
        Vector<String> names = new Vector<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "")) {
            String query = "SELECT std_id, std_name FROM student";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String id = rs.getString("std_id");
                String name = rs.getString("std_name");
                names.add(name);
                nameToIdMap.put(name, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading student names: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return names;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Admin Fee Manager");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            AdminFeeManager feeManager = new AdminFeeManager();
            frame.add(feeManager);
            frame.setVisible(true);
        });
    }

    public void viewPayments(boolean filter) {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "")) {
            String query = "SELECT f.f_id, s.std_name, f.f_credit, f.f_remaining, f.f_date_month, f.f_status FROM fee f JOIN student s ON f.std_id = s.std_id";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String rawDate = rs.getString("f_date_month");
                String formattedDate;
                try {
                    LocalDate dateMonth = LocalDate.parse(rawDate, inputFormatter);
                    formattedDate = outputFormatter.format(dateMonth);
                } catch (DateTimeParseException e) {
                    formattedDate = rawDate;
                    System.err.println("Error parsing date: " + rawDate + " - " + e.getMessage());
                }
                model.addRow(new Object[]{
                        rs.getInt("f_id"),
                        rs.getString("std_name"),
                        rs.getFloat("f_credit"),
                        rs.getFloat("f_remaining"),
                        formattedDate,
                        rs.getString("f_status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePayment() {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a payment to edit", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int feeId = (int) model.getValueAt(selectedRow, 0);
        String currentStudentName = (String) model.getValueAt(selectedRow, 1);
        AdminUpdateFee updateForm = new AdminUpdateFee(feeId, currentStudentName, this);
    }

    private void calculateAndDisplayBalance() {
        String studentName = (String) comboStudentName.getSelectedItem();
        if (studentName == null) {
            txtRemaining.setText("");
            txtCredit.setText("");
            return;
        }
        String studentId = nameToIdMap.get(studentName);

        if (studentId == null) {
            txtRemaining.setText("");
            txtCredit.setText("");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "")) {
            PreparedStatement ps = conn.prepareStatement("SELECT std_hostel_fee, std_balance, std_name FROM student WHERE std_id = ?");
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                float studentHostelFee = rs.getFloat("std_hostel_fee");
                float currentBalance = rs.getFloat("std_balance");
                this.studentName = rs.getString("std_name"); // Get student name for PDF
                float paidAmount = studentHostelFee - currentBalance;
                txtCredit.setText(String.valueOf(paidAmount));
                txtRemaining.setText(String.valueOf(currentBalance));
            } else {
                txtRemaining.setText("");
                txtCredit.setText("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving student balance: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            txtRemaining.setText("");
            txtCredit.setText("");
        }
    }

    private void downloadPaymentsPDF() {
        // Get the desired file name from the user
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF File");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filename = fileToSave.getAbsolutePath();
            if (!filename.toLowerCase().endsWith(".pdf")) {
                filename += ".pdf";
            }
            System.out.println("Saving to: " + filename);

            Document document = null;
            try {
                // 1. Create document
                document = new Document(PageSize.A4.rotate());

                // 2. Create PdfWriter
                PdfWriter.getInstance(document, new FileOutputStream(filename));

                // 3. Open the document
                document.open();

                // Add title "Payment History of <Student Name>"
                Paragraph title = new Paragraph("Payment History of Student", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD));
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);

                // Add some space after the title
                document.add(new Paragraph("\n"));

                // Create table
                PdfPTable pdfTable = new PdfPTable(5); // Changed to 5 columns
                pdfTable.setWidthPercentage(100);
                pdfTable.setSpacingBefore(10f);
                pdfTable.setSpacingAfter(10f);

                // Add table headers
                addTableHeader(pdfTable);

                // Add table data from the model
                for (int i = 0; i < model.getRowCount(); i++) {
                    String formattedDate;
                    String rawDate = model.getValueAt(i, 4).toString();
                    try {
                        LocalDate dateMonth = LocalDate.parse(rawDate, outputFormatter);
                        formattedDate = outputFormatter.format(dateMonth);
                    } catch (DateTimeParseException e) {
                        formattedDate = rawDate;
                        System.err.println("Error parsing date for PDF: " + rawDate + " - " + e.getMessage());
                    }
                    addTableCell(pdfTable, model.getValueAt(i, 1).toString()); // Student Name
                    addTableCell(pdfTable, formattedDate);
                    addTableCell(pdfTable, String.format("%,.2f", Float.parseFloat(model.getValueAt(i, 2).toString())));
                    addTableCell(pdfTable, String.format("%,.2f", Float.parseFloat(model.getValueAt(i, 3).toString())));
                    addTableCell(pdfTable, model.getValueAt(i, 5).toString());
                }

                // Add the table to the document
                document.add(pdfTable);

                JOptionPane.showMessageDialog(this, "Payment history downloaded to " + filename);

            } catch (DocumentException | IOException ex) {
                JOptionPane.showMessageDialog(this, "Error creating PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                // 7. Close the document
                if (document != null) {
                    document.close();
                }
            }
        } else if (userSelection == JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(this, "Download cancelled by user.");
        }
    }

    private void addTableHeader(PdfPTable table) {
        com.itextpdf.text.Font font = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
        addTableCell(table, "Student Name", font);
        addTableCell(table, "Date", font);
        addTableCell(table, "Credit (Rs.)", font);
        addTableCell(table, "Remaining (Rs.)", font);
        addTableCell(table, "Status", font);
    }

    private void addTableCell(PdfPTable table, String text) {
        addTableCell(table, text, null);
    }

    private void addTableCell(PdfPTable table, String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text));
        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        if (font != null) {
            cell.setPhrase(new Paragraph(text, font));
        }
        table.addCell(cell);
    }
}

