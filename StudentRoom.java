package Student;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

public class StudentRoom extends JPanel {

    String studentUsername;
    JLabel lblRoomDetails;
    JTable table;
    DefaultTableModel model;

    public StudentRoom(String username) {
        this.studentUsername = username;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 250, 255)); // Light background

        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel title = new JLabel("Student Room Information", SwingConstants.CENTER);
        title.setFont(new Font("Verdana", Font.BOLD, 24));
        title.setForeground(new Color(30, 60, 90)); // Dark blue
        add(title, BorderLayout.NORTH);

        // Room details panel
        JPanel roomDetailPanel = new JPanel(new BorderLayout());
        roomDetailPanel.setBackground(new Color(230, 240, 255));
        roomDetailPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 100, 180)),
                "Assigned Room Details",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Tahoma", Font.BOLD, 14),
                new Color(0, 70, 140)
        ));
        lblRoomDetails = new JLabel("Loading assigned room...", SwingConstants.CENTER);
        lblRoomDetails.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblRoomDetails.setForeground(Color.DARK_GRAY);
        roomDetailPanel.add(lblRoomDetails, BorderLayout.CENTER);
        add(roomDetailPanel, BorderLayout.CENTER);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(240, 248, 255));
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 120, 170)),
                "All Rooms and Availability", // Changed title
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Tahoma", Font.BOLD, 14),
                new Color(0, 70, 140)
        ));

        // Table model and table for all rooms
        model = new DefaultTableModel(new String[]{
                "Floor", "Room No", "Status", "No. of Students", "Type"
        }, 0);

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setForeground(Color.DARK_GRAY);
        table.setBackground(Color.WHITE);
        table.setEnabled(false); // read-only

        // Customize table header for all rooms
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.setBackground(new Color(0, 120, 180));
        header.setForeground(Color.WHITE);

        // Center align table cells for all rooms
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.SOUTH);

        // Load data
        viewAssignedRoomDetails();
        loadAllRoomsWithAvailability(); // Changed method name
    }

    private void viewAssignedRoomDetails() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
             PreparedStatement pst = con.prepareStatement(
                     "SELECT r.room_floor_no, r.room_no, r.room_status, r.room_noof_std, r.room_type " +
                             "FROM room r JOIN student s ON s.room_id = r.room_id " +
                             "WHERE s.std_username = ? AND s.room_id IS NOT NULL;")) {

            pst.setString(1, studentUsername);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                // If a room is assigned, show all the requested room details
                String details = "<html><b>Floor:</b> " + rs.getString("room_floor_no") +
                        " &nbsp;&nbsp; <b>Room No:</b> " + rs.getString("room_no") +
                        " &nbsp;&nbsp; <b>Status:</b> " + rs.getString("room_status") +
                        " &nbsp;&nbsp; <b>No. of Students:</b> " + rs.getInt("room_noof_std") +
                        " &nbsp;&nbsp; <b>Type:</b> " + rs.getString("room_type") + "</html>";
                lblRoomDetails.setText(details);
            } else {
                // If no room is assigned to the student
                lblRoomDetails.setText("No room assigned yet.");
            }

        } catch (Exception ex) {
            // Handle any exceptions and display the error message
            lblRoomDetails.setText("Error: " + ex.getMessage());
        }
    }

    private void loadAllRoomsWithAvailability() { // Changed method name
        model.setRowCount(0);
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT room_floor_no, room_no, room_status, room_noof_std, room_type FROM room")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("room_floor_no"),
                        rs.getString("room_no"),
                        rs.getString("room_status"),
                        rs.getInt("room_noof_std"),
                        rs.getString("room_type")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading all rooms: " + ex.getMessage());
        }
    }
}