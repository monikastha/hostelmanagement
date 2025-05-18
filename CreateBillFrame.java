package Admin;

import java.awt.Font;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;

import javax.swing.JOptionPane;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.File;

public class CreateBillFrame extends JFrame {

    private JComboBox<String> comboStudentName;
    private JTextField txtBillNo;
    private JTextField txtBillDate;
    private JTextField txtRoomNo;
    private JTextField txtRoomFloorNo;
    private JTextField txtStdHostelFee;
    private JTextField txtFCredit;
    private JTextField txtFRemaining;
    private JButton btnCreateBill, btnDownloadReceiptPdf, btnBack; // Added btnBack
    private JTextArea receiptTextArea;
    private JPanel formPanel;

    private AdminFeeManager parentFeeManager;
    private Map<String, String> nameToIdMap;
    private Vector<String> studentNames;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static AtomicInteger billNumberCounter = new AtomicInteger(0);

    // Colors
    private Color backgroundColor = new Color(89, 91, 94);
    private Color formPanelColor = Color.WHITE;
    private Color labelColor = Color.BLACK;
    private Color textFieldBackgroundColor = Color.WHITE;
    private Color buttonColor = new Color(0, 216, 92);
    private Color buttonTextColor = Color.GRAY;
    private Color errorColor = new Color(255, 69, 0);
    private Color titleColor = new Color(0, 128, 128);

    // Fonts
    private Font labelFont = new Font("Tahoma", Font.PLAIN, 16);
    private Font textFont = new Font("Tahoma", Font.PLAIN, 16);
    private Font buttonFont = new Font("Tahoma", Font.BOLD, 16);
    private Font titleFont = new Font("Tahoma", Font.BOLD, 18);
    private Font receiptFont = new Font("Monospaced", Font.PLAIN, 12);

    // iTextPDF Fonts
    private static com.itextpdf.text.Font pdfTitleFont;
    private static com.itextpdf.text.Font pdfRegularFont;

    public CreateBillFrame(AdminFeeManager parent) {
        this.parentFeeManager = parent;
        this.nameToIdMap = new HashMap<>();
        this.studentNames = loadStudentNames();

        // Initialize iTextPDF fonts
        try {
            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            pdfTitleFont = new com.itextpdf.text.Font(baseFont, 18, com.itextpdf.text.Font.BOLD);
            BaseFont regularBaseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            pdfRegularFont = new com.itextpdf.text.Font(regularBaseFont, 12);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading fonts for PDF generation. PDF download may not work correctly.", "Font Error", JOptionPane.ERROR_MESSAGE);
        }

        setTitle("Create New Hostel Bill");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(backgroundColor);
        add(mainPanel);

        formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(formPanelColor);

        addFormComponents();

        mainPanel.add(formPanel, BorderLayout.CENTER);

        receiptTextArea = new JTextArea();
        receiptTextArea.setFont(receiptFont);
        receiptTextArea.setEditable(false);
        JScrollPane receiptScrollPane = new JScrollPane(receiptTextArea);
        receiptScrollPane.setPreferredSize(new Dimension(350, 250));
        mainPanel.add(receiptScrollPane, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(backgroundColor);

        btnCreateBill = new JButton("Create Bill");
        btnCreateBill.setFont(buttonFont);
        btnCreateBill.setBackground(buttonColor);
        btnCreateBill.setForeground(buttonTextColor);

        btnDownloadReceiptPdf = new JButton("Download Receipt as PDF");
        btnDownloadReceiptPdf.setFont(buttonFont);
        btnDownloadReceiptPdf.setBackground(buttonColor);
        btnDownloadReceiptPdf.setForeground(buttonTextColor);

        btnBack = new JButton("Back"); // added back button
        btnBack.setFont(buttonFont);
        btnBack.setBackground(buttonColor);
        btnBack.setForeground(buttonTextColor);

        buttonPanel.add(btnBack); // added back button
        buttonPanel.add(btnCreateBill);
        buttonPanel.add(btnDownloadReceiptPdf);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getNextBillNumber();
        comboStudentName.addActionListener(e -> handleStudentNameSelection());
        txtFCredit.addActionListener(e -> calculateRemainingAmount());
        btnCreateBill.addActionListener(e -> {
            if (createBill()) { // changed createBill to return boolean
                generateReceipt();
            }
            // removed generateReceipt(); from here
        });
        btnDownloadReceiptPdf.addActionListener(e -> downloadReceiptAsPdf());
        btnBack.addActionListener(e -> dispose()); // added action listener for back button

        if (!studentNames.isEmpty()) {
            loadStudentDetails(nameToIdMap.get(studentNames.get(0)));
            comboStudentName.setSelectedIndex(0);
        }
        setVisible(true);
    }

    private void addFormComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridwidth = 1;

        JLabel lblStudentName = createLabel("Student Name:");
        JLabel lblBillNo = createLabel("Bill Number:");
        JLabel lblBillDate = createLabel("Bill Date (YYYY-MM-DD):");
        JLabel lblRoomNo = createLabel("Room No:");
        JLabel lblRoomFloorNo = createLabel("Room Floor No:");
        JLabel lblStdHostelFee = createLabel("Hostel Fee (Rs):");
        JLabel lblFCredit = createLabel("Paid Amount (Rs):");
        JLabel lblFRemaining = createLabel("Remaining (Rs):");

        comboStudentName = new JComboBox<>(studentNames);
        comboStudentName.setFont(textFont);
        comboStudentName.setBackground(textFieldBackgroundColor);
        txtBillNo = createTextField(false);
        txtBillDate = createTextField();
        txtRoomNo = createTextField(false);
        txtRoomFloorNo = createTextField(false);
        txtStdHostelFee = createTextField(false);
        txtFCredit = createTextField(false);  // Paid amount is not editable.
        txtFRemaining = createTextField(false);

        addComponent(formPanel, lblStudentName, 0, 0, gbc);
        addComponent(formPanel, comboStudentName, 1, 0, gbc);

        addComponent(formPanel, lblBillNo, 0, 1, gbc);
        addComponent(formPanel, txtBillNo, 1, 1, gbc);

        addComponent(formPanel, lblBillDate, 0, 2, gbc);
        addComponent(formPanel, txtBillDate, 1, 2, gbc);

        addComponent(formPanel, lblRoomNo, 0, 3, gbc);
        addComponent(formPanel, txtRoomNo, 1, 3, gbc);

        addComponent(formPanel, lblRoomFloorNo, 0, 4, gbc);
        addComponent(formPanel, txtRoomFloorNo, 1, 4, gbc);

        addComponent(formPanel, lblStdHostelFee, 0, 5, gbc);
        addComponent(formPanel, txtStdHostelFee, 1, 5, gbc);

        addComponent(formPanel, lblFCredit, 0, 6, gbc);
        addComponent(formPanel, txtFCredit, 1, 6, gbc);

        addComponent(formPanel, lblFRemaining, 0, 7, gbc);
        addComponent(formPanel, txtFRemaining, 1, 7, gbc);

    }

    private void handleStudentNameSelection() {
        String selectedStudentName = (String) comboStudentName.getSelectedItem();
        if (selectedStudentName != null) {
            loadStudentDetails(nameToIdMap.get(selectedStudentName));
        } else {
            clearDisplayFields();
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(labelFont);
        label.setForeground(labelColor);
        return label;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(textFont);
        textField.setBackground(textFieldBackgroundColor);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                new EmptyBorder(5, 10, 5, 10)));
        return textField;
    }

    private JTextField createTextField(boolean editable) {
        JTextField textField = new JTextField();
        textField.setFont(textFont);
        textField.setEditable(editable);
        textField.setBackground(editable ? textFieldBackgroundColor : Color.LIGHT_GRAY);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                new EmptyBorder(5, 10, 5, 10)));
        return textField;
    }

    private void addComponent(JPanel panel, Component component, int gridx, int gridy, GridBagConstraints gbc) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        panel.add(component, gbc);
    }

    private Connection getDatabaseConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
    }

    private Vector<String> loadStudentNames() {
        Vector<String> names = new Vector<>();
        try (Connection conn = getDatabaseConnection()) {
            String query = "SELECT std_id, std_name FROM student ORDER BY std_name";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String id = rs.getString("std_id");
                String name = rs.getString("std_name");
                names.add(name);
                nameToIdMap.put(name, id);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading student names: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return names;
    }

    private void loadStudentDetails(String studentId) {
        if (studentId != null) {
            try (Connection conn = getDatabaseConnection()) {
                String query = "SELECT r.room_no, r.room_floor_no, s.std_hostel_fee, f.f_credit "
                        + "FROM student s "
                        + "JOIN room r ON s.room_id = r.room_id "
                        + "LEFT JOIN fee f ON s.std_id = f.std_id "
                        + "WHERE s.std_id = ?";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, studentId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String roomNo = rs.getString("room_no");
                    String roomFloorNo = rs.getString("room_floor_no");
                    float stdHostelFee = rs.getFloat("std_hostel_fee");
                    float fCredit = rs.getFloat("f_credit");

                    txtRoomNo.setText(roomNo);
                    txtRoomFloorNo.setText(roomFloorNo);
                    txtStdHostelFee.setText(String.valueOf(stdHostelFee));
                    txtFCredit.setText(String.valueOf(fCredit));
                    calculateRemainingAmount();
                } else {
                    clearDisplayFields();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading student details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            clearDisplayFields();
        }
    }

    private void calculateRemainingAmount() {
        try {
            float hostelFee = Float.parseFloat(txtStdHostelFee.getText().isEmpty() ? "0" : txtStdHostelFee.getText());
            float paidAmount = Float.parseFloat(txtFCredit.getText().isEmpty() ? "0" : txtFCredit.getText());
            float remainingAmount = hostelFee - paidAmount;
            txtFRemaining.setText(String.valueOf(remainingAmount));
        } catch (NumberFormatException e) {
            txtFRemaining.setText("Error");
        }
    }

    private void clearDisplayFields() {
        txtRoomNo.setText("");
        txtRoomFloorNo.setText("");
        txtStdHostelFee.setText("");
        txtFCredit.setText("");
        txtFRemaining.setText("");
    }

    private boolean createBill() { // changed return type to boolean
        String studentName = (String) comboStudentName.getSelectedItem();
        String billNo = txtBillNo.getText();
        String billDateStr = txtBillDate.getText();
        String studentId = nameToIdMap.get(studentName);
        String paidAmountStr = txtFCredit.getText().trim();

        if (studentName == null || billNo.isEmpty() || billDateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all essential fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return false; // added return false
        }

        if (!billDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use-MM-DD", "Error", JOptionPane.ERROR_MESSAGE);
            return false; // added return false
        }

        try {
            java.util.Date billDate = dateFormat.parse(billDateStr);
            java.sql.Date sqlBillDate = new java.sql.Date(billDate.getTime());
            float paidAmount = Float.parseFloat(paidAmountStr.isEmpty() ? "0" : paidAmountStr);

            try (Connection conn = getDatabaseConnection()) {
                String checkQuery = "SELECT bill_no FROM bill WHERE bill_no = ?";
                PreparedStatement checkPs = conn.prepareStatement(checkQuery);
                checkPs.setString(1, billNo);
                ResultSet checkRs = checkPs.executeQuery();
                if (checkRs.next()) {
                    JOptionPane.showMessageDialog(this, "Bill number already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    getNextBillNumber();
                    return false; // added return false
                }

                String insertBillQuery = "INSERT INTO bill (bill_no, bill_date, std_id, f_id, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, NOW(), NOW())";
                PreparedStatement insertBillPs = conn.prepareStatement(insertBillQuery);
                insertBillPs.setString(1, billNo);
                insertBillPs.setDate(2, sqlBillDate);
                insertBillPs.setString(3, studentId);

                String getFIdQuery = "SELECT f_id FROM fee WHERE std_id = ?";
                PreparedStatement getFIdPs = conn.prepareStatement(getFIdQuery);
                getFIdPs.setString(1, studentId);
                ResultSet fIdRs = getFIdPs.executeQuery();
                int fId = 0;
                if (fIdRs.next()) {
                    fId = fIdRs.getInt("f_id");
                }
                insertBillPs.setInt(4, fId);

                int billResult = insertBillPs.executeUpdate();

                if (billResult > 0) {
                    String updateFeeQuery = "UPDATE fee SET f_credit = ?, f_remaining = ? WHERE std_id = ?";
                    PreparedStatement updateFeePs = conn.prepareStatement(updateFeeQuery);
                    updateFeePs.setFloat(1, paidAmount);
                    updateFeePs.setFloat(2, Float.parseFloat(txtFRemaining.getText()));
                    updateFeePs.setString(3, studentId);
                    int feeResult = updateFeePs.executeUpdate();

                    if (feeResult >= 0) {
                        JOptionPane.showMessageDialog(this, "Bill created and fee details updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        return true; // added return true
                    } else {
                        JOptionPane.showMessageDialog(this, "Bill created, but failed to update fee details.", "Warning", JOptionPane.WARNING_MESSAGE);
                        return false; // added return false
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create bill.", "Error", JOptionPane.ERROR_MESSAGE);
                    return false; // added return false
                }
            }
        } catch (ParseException | SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false; // added return false
        }
    }

    private void clearBillForm() {
        txtBillNo.setText("");
        txtBillDate.setText("");
        comboStudentName.setSelectedIndex(-1);
        clearDisplayFields();
        getNextBillNumber();
    }

    private void getNextBillNumber() {
        try (Connection conn = getDatabaseConnection()) {
            String query = "SELECT bill_no FROM bill ORDER BY bill_no DESC LIMIT 1";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String lastBillNo = rs.getString("bill_no");
                // Extract the numeric part of the bill number
                String numericPart = lastBillNo.substring(3); // Assuming "HS-001" format
                int billNumber = Integer.parseInt(numericPart);
                billNumber++; // Increment the bill number

                // Format the new bill number
                String newBillNo = String.format("HS-%03d", billNumber);
                txtBillNo.setText(newBillNo);
            } else {
                txtBillNo.setText("HS-001");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error getting next bill number: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            txtBillNo.setText("HS-001");
        }
    }

    private void generateReceipt() {
        String studentName = (String) comboStudentName.getSelectedItem();
        String billNo = txtBillNo.getText();
        String billDate = txtBillDate.getText();
        String roomNo = txtRoomNo.getText();
        String roomFloorNo = txtRoomFloorNo.getText();
        String stdHostelFee = txtStdHostelFee.getText();
        String fCredit = txtFCredit.getText();
        String fRemaining = txtFRemaining.getText();

        StringBuilder receipt = new StringBuilder();
        receipt.append("\n======================== Hostel Bill Receipt =======================\n");
        receipt.append("  Bill Number             : ").append(String.format("%-10s", billNo)).append("\n");
        receipt.append("  Bill Date               : ").append(String.format("%-10s", billDate)).append("\n");
        receipt.append("  Student Name            : ").append(String.format("%-10s", studentName)).append("\n");
        receipt.append("  Room No                 : ").append(String.format("%-10s", roomNo)).append("\n");
        receipt.append("  Room Floor No           : ").append(String.format("%-10s", roomFloorNo)).append("\n");
        receipt.append("  Hostel Fee (Rs)         : ").append(String.format("%-10s", stdHostelFee)).append("\n");
        receipt.append("  Paid Amount (Rs)        : ").append(String.format("%-10s", fCredit)).append("\n");
        receipt.append("  Remaining Amount (Rs)   : ").append(String.format("%-10s", fRemaining)).append("\n");
        receipt.append("===================================================================\n");

        receiptTextArea.setText(receipt.toString());
    }

    private void downloadReceiptAsPdf() {
        String studentName = (String) comboStudentName.getSelectedItem();
        String billNo = txtBillNo.getText();
        String billDate = txtBillDate.getText();
        String roomNo = txtRoomNo.getText();
        String roomFloorNo = txtRoomFloorNo.getText();
        String stdHostelFee = txtStdHostelFee.getText();
        String fCredit = txtFCredit.getText();
        String fRemaining = txtFRemaining.getText();
        if (studentName == null || billNo.isEmpty() || billDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all essential fields before downloading.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Receipt as PDF");

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".pdf")) {
                filePath += ".pdf";
            }

            Document document = new Document();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(filePath));
                document.open();

                // Add metadata (optional)
                document.addTitle("Hostel Bill Receipt");
                document.addSubject("Hostel Fee Bill");
                document.addAuthor("Hostel Management System");

                // Add content
                Paragraph title = new Paragraph("Hostel Bill Receipt\n\n", pdfTitleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);

                Paragraph content = new Paragraph();
                content.add(new Chunk("Bill Number: ", pdfRegularFont));
                content.add(new Chunk(billNo + "\n", pdfRegularFont));
                content.add(new Chunk("Bill Date: ", pdfRegularFont));
                content.add(new Chunk(billDate + "\n", pdfRegularFont));
                content.add(new Chunk("Student Name: ", pdfRegularFont));
                content.add(new Chunk(studentName + "\n", pdfRegularFont));
                content.add(new Chunk("Room No: ", pdfRegularFont));
                content.add(new Chunk(roomNo + "\n", pdfRegularFont));
                content.add(new Chunk("Room Floor No: ", pdfRegularFont));
                content.add(new Chunk(roomFloorNo + "\n", pdfRegularFont));
                content.add(new Chunk("Hostel Fee (Rs): ", pdfRegularFont));
                content.add(new Chunk(stdHostelFee + "\n", pdfRegularFont));
                content.add(new Chunk("Paid Amount (Rs): ", pdfRegularFont));
                content.add(new Chunk(fCredit + "\n", pdfRegularFont));
                content.add(new Chunk("Remaining Amount (Rs): ", pdfRegularFont));
                content.add(new Chunk(fRemaining + "\n\n", pdfRegularFont));
                content.setAlignment(Element.ALIGN_LEFT);
                document.add(content);

                Paragraph footer = new Paragraph("===================================================================", pdfRegularFont);
                footer.setAlignment(Element.ALIGN_CENTER);
                document.add(footer);

                document.close();
                JOptionPane.showMessageDialog(this, "Receipt saved as PDF successfully to " + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (DocumentException | IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving receipt as PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                AdminFeeManager parent = new AdminFeeManager();
                CreateBillFrame frame = new CreateBillFrame(parent);
                frame.setVisible(true);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                     UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
        });
    }
}
