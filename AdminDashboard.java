package Admin;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Create the content panel and CardLayout for dynamic content switching
        JPanel contentPanel = new JPanel();
        CardLayout cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);

        // Create the AdminRoomManager panel
        AdminRoomManager roomManagerPanel = new AdminRoomManager();
        contentPanel.add(roomManagerPanel, "rooms");

        // Create the AdminStudentManager panel
        AdminStudentManager studentManagerPanel = new AdminStudentManager();
        contentPanel.add(studentManagerPanel, "students");

        // Create the AdminFeeManager panel
        AdminFeeManager feeManagerPanel = new AdminFeeManager();
        contentPanel.add(feeManagerPanel, "fees");

        // Create the AdminHostelStatus panel
        //AdminHostelStatus hostelStatusPanel = new AdminHostelStatus();
        //contentPanel.add(hostelStatusPanel, "hostelStatus"); // Add the new panel

        // Add the sidebar to the frame, passing the CardLayout and content panel
        AdminSidebar sidebar = new AdminSidebar(cardLayout, contentPanel);
        add(sidebar, BorderLayout.WEST);

        // Add the content panel to the center of the frame
        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // Main method to run the application
    public static void main(String[] args) {
        // Run the dashboard in the event dispatch thread for thread-safety
        SwingUtilities.invokeLater(() -> {
            new AdminDashboard();
        });
    }
}
