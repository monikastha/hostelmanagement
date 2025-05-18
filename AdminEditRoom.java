package Admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AdminEditRoom extends JFrame {

    JTextField txtFloorNo, txtRoomNo, txtNoOfStd;
    JComboBox<String> comboStatus, comboRoomType;
    JButton btnSave, btnBack;
    int roomId; // for updating specific room

    public AdminEditRoom(int roomId) {
        this.roomId = roomId; // Receive room id to edit
        setTitle("Edit Room");
        setSize(700, 530);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(new Color(245, 245, 245));
        formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        add(formPanel, BorderLayout.CENTER);

        JLabel lblHeading = new JLabel("Edit Room");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblHeading.setForeground(new Color(41, 128, 185));
        lblHeading.setHorizontalAlignment(JLabel.CENTER);
        lblHeading.setBounds(0, 20, 700, 50);
        formPanel.add(lblHeading);

        JLabel[] labels = {
                new JLabel("Floor No:"),
                new JLabel("Room No:"),
                new JLabel("Room Status:"),
                new JLabel("No. of Students:"),
                new JLabel("Room Type:")
        };

        txtFloorNo = new JTextField();
        txtRoomNo = new JTextField();
        txtNoOfStd = new JTextField();
        comboStatus = new JComboBox<>(new String[]{"Empty", "Occupied"});
        comboRoomType = new JComboBox<>(new String[]{
                "AC - Single", "AC - Double", "AC - Triple",
                "Non-AC - Single", "Non-AC - Double", "Non-AC - Triple",
                "AC - Dormitory", "Non-AC - Dormitory"
        });

        int y = 100;
        for (int i = 0; i < labels.length; i++) {
            labels[i].setFont(new Font("Segoe UI", Font.PLAIN, 16));
            labels[i].setBounds(250, y, 150, 30);
            formPanel.add(labels[i]);

            JComponent field;
            if (i == 2) {
                field = comboStatus;
            } else if (i == 4) {
                field = comboRoomType;
            } else if (i == 0) {
                field = txtFloorNo;
            } else if (i == 1) {
                field = txtRoomNo;
            } else {
                field = txtNoOfStd;
            }

            field.setBounds(400, y, 220, 30);
            formPanel.add(field);

            y += 50;
        }

        btnSave = new JButton("Update Room");
        btnSave.setBounds(400, y + 20, 180, 40);
        styleButton(btnSave, new Color(39, 174, 96));
        formPanel.add(btnSave);

        btnBack = new JButton("Back");
        btnBack.setBounds(200, y + 20, 150, 40);
        styleButton(btnBack, new Color(231, 76, 60));
        formPanel.add(btnBack);

        btnSave.addActionListener(e -> updateRoom());
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the current edit form
                // No need to create a new AdminRoomManager here.
                // The AdminRoomManager should handle refreshing its own table.
            }
        });
       ;
        loadRoomData();

        setVisible(true);
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadRoomData() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
             PreparedStatement pst = con.prepareStatement("SELECT * FROM room WHERE room_id = ?")) {
            pst.setInt(1, roomId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                txtFloorNo.setText(String.valueOf(rs.getInt("room_floor_no")));
                txtRoomNo.setText(rs.getString("room_no"));
                comboStatus.setSelectedItem(rs.getString("room_status"));
                txtNoOfStd.setText(String.valueOf(rs.getInt("room_noof_std")));
                comboRoomType.setSelectedItem(rs.getString("room_type"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Load Error: " + ex.getMessage());
        }
    }

    private void updateRoom() {
        try {
            // Validation
            if (txtFloorNo.getText().trim().isEmpty() || txtRoomNo.getText().trim().isEmpty() || txtNoOfStd.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int floorNo = Integer.parseInt(txtFloorNo.getText().trim());
            String roomNo = txtRoomNo.getText().trim();
            String status = (String) comboStatus.getSelectedItem();
            int noOfStd = Integer.parseInt(txtNoOfStd.getText().trim());
            String type = (String) comboRoomType.getSelectedItem();

            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
            String query = "UPDATE room SET room_floor_no=?, room_no=?, room_status=?, room_noof_std=?, room_type=? WHERE room_id=?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, floorNo);
            pst.setString(2, roomNo);
            pst.setString(3, status);
            pst.setInt(4, noOfStd);
            pst.setString(5, type);
            pst.setInt(6, roomId);

            int rows = pst.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Room updated successfully!");
                dispose();
            }

            con.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new AdminEditRoom(1); // Example room id 1 (you will pass actual selected id)
    }
}
