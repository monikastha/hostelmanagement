package Auth;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import Auth.LoginForm;

public class RegisterPage extends JFrame {
    private JTextField nameField, emailField, usernameField;
    private JPasswordField passwordField;
    private JButton registerButton, backButton;

    public RegisterPage() {
        setTitle("Register - Hostel Management");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        // Background Color
        getContentPane().setBackground(Color.lightGray); // Sets background to light gray

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Name Label & Field
        addComponent(new JLabel("Full Name:"), gbc, 0, 0);
        nameField = new JTextField(15);
        addComponent(nameField, gbc, 1, 0);

        // Email Label & Field
        addComponent(new JLabel("Email:"), gbc, 0, 1);
        emailField = new JTextField(15);
        addComponent(emailField, gbc, 1, 1);

        // Username Label & Field
        addComponent(new JLabel("Username:"), gbc, 0, 2);
        usernameField = new JTextField(15);
        addComponent(usernameField, gbc, 1, 2);

        // Password Label & Field
        addComponent(new JLabel("Password:"), gbc, 0, 3);
        passwordField = new JPasswordField(15);
        addComponent(passwordField, gbc, 1, 3);

        // Register Button
        registerButton = new JButton("Register");
        styleButton(registerButton);
        registerButton.addActionListener(e -> registerUser());
        addComponent(registerButton, gbc, 0, 4, 2);

        // Back Button
        backButton = new JButton("Back to Login");
        styleButton(backButton);
        backButton.addActionListener(e -> {
            new LoginForm();
            dispose();
        });
        addComponent(backButton, gbc, 0, 5, 2);

        setVisible(true);
    }

    private void registerUser() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/hostelms", "root", "")) {
            // Check if username already exists
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM login WHERE log_username = ?")) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Username already taken!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Insert user data
            try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO login (log_username, log_password, log_role) VALUES (?, ?, 'Student')")) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);

                if (insertStmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "Registration Successful! You can now log in.");
                    new LoginForm();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Registration Failed!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database connection error!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addComponent(Component comp, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        add(comp, gbc);
    }

    private void addComponent(Component comp, GridBagConstraints gbc, int x, int y, int width) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        add(comp, gbc);
        gbc.gridwidth = 1;
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(50, 205, 50)); // Green
        button.setForeground(Color.WHITE); // White text
        button.setFocusPainted(false);
    }

    public static void main(String[] args) {
        new RegisterPage();
    }
}
