package Student;

import javax.swing.*;
import java.awt.*;

public class StudentDashboard extends JFrame {

    String loggedInUsername;

    public StudentDashboard(String username) {
        this.loggedInUsername = username;

        setTitle("Student Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Create the content panel and CardLayout for dynamic content switching
        JPanel contentPanel = new JPanel();
        CardLayout cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);

        // Create the StudentRoom panel
        StudentRoom roomPanel = new StudentRoom(loggedInUsername);
        contentPanel.add(roomPanel, "room");
        System.out.println("StudentRoom panel added with card name: room"); // Debug print

        // Create the StudentFees panel
        StudentFees feeManagerPanel = new StudentFees(loggedInUsername);
        contentPanel.add(feeManagerPanel, "fees");
        System.out.println("StudentFees panel added with card name: fees"); // Debug print

        // Create the sidebar for navigation
        StudentSidebar sidebar = new StudentSidebar(cardLayout, contentPanel);
        add(sidebar, BorderLayout.WEST);

        // Add the content panel to the center
        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // Main method to run the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Example username passed for testing
            new StudentDashboard("jeena123"); // Replace with actual login username
        });
    }
}