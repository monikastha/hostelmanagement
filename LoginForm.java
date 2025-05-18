package Auth;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton loginButton, registerButton;

    public LoginForm() {
        setTitle("Login - Hostel Management");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        // Set background color
        getContentPane().setBackground(Color.LIGHT_GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Username Label
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        usernameLabel.setForeground(new Color(0, 51, 102)); // Dark Blue
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(usernameLabel, gbc);

        // Username Field
        usernameField = new JTextField(20);
        gbc.gridx = 1;
        add(usernameField, gbc);

        // Password Label
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordLabel.setForeground(new Color(0, 51, 102)); // Dark Blue
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);

        // Password Field
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Role Label
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        roleLabel.setForeground(new Color(0, 51, 102)); // Dark Blue
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(roleLabel, gbc);

        // Role ComboBox
        roleComboBox = new JComboBox<>(new String[]{"Admin", "Student"});
        gbc.gridx = 1;
        roleComboBox.setPreferredSize(new Dimension(200, 30));
        add(roleComboBox, gbc);

        // Login Button
        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(30, 144, 255)); // Blue
        loginButton.setForeground(Color.WHITE); // White Text
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(loginButton, gbc);

        // Register Button
        registerButton = new JButton("Register");
        registerButton.setBackground(new Color(50, 205, 50)); // Green
        registerButton.setForeground(Color.WHITE); // White Text
        gbc.gridy = 4;
        add(registerButton, gbc);

        // Button Actions
        loginButton.addActionListener(e -> loginUser());
        registerButton.addActionListener(e -> {
            new RegisterPage();
            dispose();
        });

        setVisible(true);
    }

    private void loginUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String selectedRole = roleComboBox.getSelectedItem().toString(); // get selected role

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "");
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM login WHERE log_username=? AND log_password=? AND log_role=?")) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, selectedRole);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login Successful!");

                if (selectedRole.equals("Admin")) {
                    new Admin.AdminDashboard();
                } else if (selectedRole.equals("Student")) {
                    new Student.StudentDashboard(username);
                }

                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username, password, or role!", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection error!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new LoginForm();
    }
}
