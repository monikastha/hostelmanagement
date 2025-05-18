package Admin;

import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class AdminRoomManager extends JPanel {

    JTextField txtRoomNo, txtFloorNo, txtNoOfStudents;
    JComboBox<String> cmbRoomStatus, cmbRoomType;
    JTable table;
    DefaultTableModel model;

    public AdminRoomManager() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(225, 227, 228)); // Light bluish background

        // --- Top Panel: Add Room Form ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Room"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblRoomNo = new JLabel("Room No:");
        lblRoomNo.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(lblRoomNo, gbc);

        txtRoomNo = new JTextField();
        txtRoomNo.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1; gbc.gridy = 0;
        formPanel.add(txtRoomNo, gbc);

        JLabel lblFloorNo = new JLabel("Floor No:");
        lblFloorNo.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(lblFloorNo, gbc);

        txtFloorNo = new JTextField();
        txtFloorNo.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(txtFloorNo, gbc);

        JLabel lblRoomStatus = new JLabel("Room Status:");
        lblRoomStatus.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(lblRoomStatus, gbc);

        cmbRoomStatus = new JComboBox<>(new String[]{"Empty", "Occupied"});
        cmbRoomStatus.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(cmbRoomStatus, gbc);

        JLabel lblNoOfStudents = new JLabel("No. of Students:");
        lblNoOfStudents.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(lblNoOfStudents, gbc);

        txtNoOfStudents = new JTextField();
        txtNoOfStudents.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(txtNoOfStudents, gbc);

        JLabel lblRoomType = new JLabel("Room Type:");
        lblRoomType.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(lblRoomType, gbc);

        cmbRoomType = new JComboBox<>(new String[]{ "AC - Single", "AC - Double", "AC - Triple",
                "Non-AC - Single", "Non-AC - Double", "Non-AC - Triple",
                "AC - Dormitory", "Non-AC - Dormitory"});
        cmbRoomType.setFont(new Font("Tahoma", Font.PLAIN, 16));
        gbc.gridx = 1; gbc.gridy = 4;
        formPanel.add(cmbRoomType, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnAddRoom = new JButton("Add Room");
        btnAddRoom.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnAddRoom.setBackground(new Color(30, 144, 255));
        btnAddRoom.setForeground(Color.WHITE);

        JButton btnClear = new JButton("Clear");
        btnClear.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnClear.setBackground(new Color(220, 20, 60));
        btnClear.setForeground(Color.WHITE);

        buttonPanel.add(btnAddRoom);
        buttonPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        // --- Center Panel: View Table ---
        model = new DefaultTableModel(new String[]{"Room ID", "Room No", "Floor No", "Room Status", "No. of Students", "Room Type"}, 0);
        table = new JTable(model);
        table.setFont(new Font("Tahoma", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 15));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("View Rooms"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Bottom Panel: Edit/Delete Buttons ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnEdit = new JButton("Edit Room");
        btnEdit.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnEdit.setBackground(new Color(255, 140, 0));
        btnEdit.setForeground(Color.WHITE);

        JButton btnDelete = new JButton("Delete Room");
        btnDelete.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnDelete.setBackground(new Color(220, 20, 60));
        btnDelete.setForeground(Color.WHITE);

        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Button Actions
        btnAddRoom.addActionListener(e -> addRoom());
        btnClear.addActionListener(e -> clearForm());
        btnEdit.addActionListener(e -> editRoom());
        btnDelete.addActionListener(e -> deleteRoom());
        // Load Table
        viewRoom();

        setVisible(true);
    }

    void addRoom() {
        String roomNo = txtRoomNo.getText();
        String floorNo = txtFloorNo.getText();
        String status = (String) cmbRoomStatus.getSelectedItem();
        String noOfStudents = txtNoOfStudents.getText();
        String roomType = (String) cmbRoomType.getSelectedItem();

        if (roomNo.isEmpty() || floorNo.isEmpty() || noOfStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
             PreparedStatement pst = con.prepareStatement("INSERT INTO room (room_no, room_floor_no, room_status, room_noof_std, room_type) VALUES (?, ?, ?, ?, ?)")) {

            pst.setString(1, roomNo);
            pst.setInt(2, Integer.parseInt(floorNo));
            pst.setString(3, status);
            pst.setInt(4, Integer.parseInt(noOfStudents));
            pst.setString(5, roomType);

            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Room Added Successfully!");

            clearForm();
            viewRoom();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Add Error: " + ex.getMessage());
        }
    }

    void clearForm() {
        txtRoomNo.setText("");
        txtFloorNo.setText("");
        txtNoOfStudents.setText("");
        cmbRoomStatus.setSelectedIndex(0);
        cmbRoomType.setSelectedIndex(0);
    }

    void viewRoom() {
        model.setRowCount(0);
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM room")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("room_id"),
                        rs.getString("room_no"),
                        rs.getInt("room_floor_no"),
                        rs.getString("room_status"),
                        rs.getInt("room_noof_std"),
                        rs.getString("room_type")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load Error: " + ex.getMessage());
        }
    }

    void editRoom() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int roomId = (int) model.getValueAt(selectedRow, 0);
            AdminEditRoom editDialog = new AdminEditRoom(roomId);
            editDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    viewRoom(); // This is the crucial line that refreshes the table
                }
            });
            editDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a room to edit!");
        }
    }


    void deleteRoom() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int roomId = (int) model.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this room?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
                 PreparedStatement pst = con.prepareStatement("DELETE FROM room WHERE room_id=?")) {

                pst.setInt(1, roomId);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Room Deleted Successfully!");
                viewRoom();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Delete Error: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a room to delete!");
        }
    }

    public static void main(String[] args) {
        new AdminRoomManager();
    }
}
