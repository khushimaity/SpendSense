package Chat_Bot;

import Connect.*;
import java.awt.*;
import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class RegisterPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField gmailField;
    private JTextField phoneField;
    private JTextField dobField;

    public RegisterPanel(JFrame frame) {
        // Set layout and background
        setLayout(new GridBagLayout());
        setBackground(new Color(0xf0f0f0)); // Light grey background

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;

        // ======= LOGO AT THE TOP (OUTSIDE CARD) =======
        ImageIcon logoIcon = new ImageIcon("C:/Users/aleen/Pictures/Screenshots/logoname.png"); // Change path as needed
        Image img = logoIcon.getImage().getScaledInstance(170, 80, Image.SCALE_SMOOTH); // Resize logo
        JLabel logoLabel = new JLabel(new ImageIcon(img));

        gbc.gridx = 0;
        gbc.gridy = 0; // Logo at the very top
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 20, 0); // Extra bottom space to push card down
        add(logoLabel, gbc);

        // ======= CARD PANEL (Pushed Down) =======
        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(100, 50, 50, 50)); // Inner padding
        cardPanel.setBorder(BorderFactory.createLineBorder(new Color(0xdddddd), 2)); // Border around the card

        gbc.gridy = 1; // Push card panel below logo
        gbc.insets = new Insets(0, 20, 20, 20); // Adjust margin
        add(cardPanel, gbc);

        // Add components to the card
        addComponent1(cardPanel, gbc, "Username:");
        addComponent(cardPanel, gbc, "Username:", usernameField = new JTextField(20));
        addComponent(cardPanel, gbc, "Password:", passwordField = new JPasswordField(20));
        addComponent(cardPanel, gbc, "Confirm Password:", confirmPasswordField = new JPasswordField(20));
        addComponent(cardPanel, gbc, "Gmail:", gmailField = new JTextField(20));
        addComponent(cardPanel, gbc, "Phone Number:", phoneField = new JTextField(20));
        addComponent(cardPanel, gbc, "Date of Birth (YYYY-MM-DD):", dobField = new JTextField(20));

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(Color.WHITE);

        GridBagConstraints gbcBtn = new GridBagConstraints();
        gbcBtn.insets = new Insets(10, 10, 10, 10);
        gbcBtn.fill = GridBagConstraints.HORIZONTAL;
        gbcBtn.weightx = 1.0;
        gbcBtn.gridy = 0;

        // Back Button
        JButton backButton = createStyledButton("← Back");
        backButton.setPreferredSize(new Dimension(300, 50));
        backButton.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.add(new LoginPanel(frame));
            frame.revalidate();
            frame.repaint();
        });

        gbcBtn.gridx = 0;
        buttonPanel.add(backButton, gbcBtn);

        // Register Button
        JButton registerButton = createStyledButton("Register");
        registerButton.setPreferredSize(new Dimension(300, 50));
        registerButton.addActionListener(e -> handleRegister(frame));

        gbcBtn.gridx = 1;
        buttonPanel.add(registerButton, gbcBtn);

        // Add button panel to cardPanel
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        cardPanel.add(buttonPanel, gbc);
    }

    private void addComponent1(JPanel panel, GridBagConstraints gbc, String labelText) {
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 5));
        label.setForeground(new Color(0xFFFFFF));
        panel.add(label, gbc);
    }

    // Method to add components to the card with styling
    private void addComponent(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField) {
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(0x333333));
        panel.add(label, gbc);

        textField.setToolTipText("Enter your " + labelText.toLowerCase());
        textField.setBorder(BorderFactory.createLineBorder(new Color(0x00366E), 1));
        textField.setBackground(Color.WHITE);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        textField.setPreferredSize(new Dimension(200, 40));

        gbc.gridx = 1;
        panel.add(textField, gbc);
    }

    // Create a styled button
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0x00366E));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(0x001F3F));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(0x00366E));
            }
        });
        return button;
    }

    private boolean isValidPassword(String password) {
        // Must be at least 8 chars
        if (password.length() < 8)
            return false;
        // At least one uppercase
        if (!password.matches(".*[A-Z].*"))
            return false;
        // At least one lowercase
        if (!password.matches(".*[a-z].*"))
            return false;
        // At least one digit
        if (!password.matches(".*\\d.*"))
            return false;
        // At least one special char
        if (!password.matches(".*[!@#$%^&()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"))
            return false;
        return true;
    }

    private void handleRegister(JFrame frame) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String gmail = gmailField.getText();
        String phone = phoneField.getText();
        String dob = dobField.getText().trim();

        // Username length
        if (username.length() < 4) {
            showError("Username must be at least 4 characters long");
            return;
        }

        // Check password length first
        if (password.length() < 8) {
            showError("Password must be at least 8 characters long");
            return;
        }

        // Confirm password
        if (!password.equals(confirmPassword)) {
            showError("Passwords must match");
            return;
        }

        // Full password validation
        if (!isValidPassword(password)) {
            showError("Password must be at least 8 characters long and must contain uppercase letters, lowercase letters, numbers and special characters");
            return;
        }

        // Check email format
        if (!gmail.contains("@")) {
            showError("Please enter a valid Gmail address");
            return;
        }

        // Parse date with yyyy-MM-dd to match the prompt
        java.sql.Date sqlDate = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setLenient(false);
            java.util.Date parsedDate = dateFormat.parse(dob);
            sqlDate = new java.sql.Date(parsedDate.getTime());
        } catch (ParseException e) {
            showError("Invalid date format. Please use YYYY-MM-DD.");
            return;
        }

        // Insert into DB
        try (Connection conn = Connect.getConnection()) {
            if (conn == null) {
                showError("Failed to connect to database");
                return;
            }
            String query = "INSERT INTO Users (username, password, email, phone, dob) VALUES (?,?,?,?,?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, gmail);
                stmt.setString(4, phone);
                stmt.setDate(5, sqlDate);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Registration successful!");

                // Redirect to Login panel after registration
                frame.getContentPane().removeAll();
                frame.add(new LoginPanel(frame));
                frame.revalidate();
                frame.repaint();
            } catch (SQLException e) {
                showError("Error saving user: " + e.getMessage());
            }
        } catch (SQLException e) {
            showError("Database Error: " + e.getMessage());
        }
    }

    // Method to display error messages
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Registration Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Define darker gradient colors for the panel
        Color navy1 = new Color(0, 54, 110); // Dark navy
        Color navy4 = new Color(0, 31, 63); // Lighter navy

        // Create gradient from navy1 to navy4
        GradientPaint gradientPaint = new GradientPaint(0, 0, navy1, 0, getHeight(), navy4);
        g2d.setPaint(gradientPaint);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    // Main method to test the RegisterPanel
    public static void main(String[] args) {
        JFrame frame = new JFrame("Register Panel Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600); // Set preferred size
        frame.setLocationRelativeTo(null); // Center the frame on the screen
        frame.add(new RegisterPanel(frame)); // Change to Register
        frame.setVisible(true);
    }
}
