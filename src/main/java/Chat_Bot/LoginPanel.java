package Chat_Bot;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;

import Connect.Connect;

public class LoginPanel extends JPanel {
    private static final Map<String, Integer> failedAttemptsMap = new HashMap<>();
    private static final Map<String, Instant> lastFailedAttemptMap = new HashMap<>();
    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCKOUT_MINUTES = 10;

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginPanel(JFrame frame) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        setBackground(Color.WHITE);

        // ======= LOGO AT THE TOP (OUTSIDE FORM) =======
        ImageIcon logoIcon = new ImageIcon("C:/Users/Dell/Desktop/chatbot/src/main/java/Chat_Bot/logo_upscaled.png"); // Update path as needed
        Image img = logoIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH); // Adjust size

        JLabel logoLabel = new JLabel(new ImageIcon(img));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span across both columns
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 30, 0); // Extra bottom margin to push the form down
        add(logoLabel, gbc);

        // ======= USERNAME LABEL & FIELD =======
        gbc.gridy = 1; // Move below logo
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 10, 10, 10); // Reset padding
        gbc.anchor = GridBagConstraints.WEST;

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 20));

        usernameLabel.setForeground(Color.WHITE); // Fix: Set text color to be visible
        add(usernameLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 20));
        usernameField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        add(usernameField, gbc);

        // ======= PASSWORD LABEL & FIELD =======
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 20));
        passwordLabel.setForeground(Color.WHITE);
        add(passwordLabel, gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 20));
        passwordField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        add(passwordField, gbc);

        // ======= BUTTONS (LOGIN & REGISTER) =======
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton loginButton = createStyledButton("Login");
        JButton registerButton = createStyledButton("Register");

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // Span both columns
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);


        loginButton.addActionListener(e -> handleLogin(frame));
        registerButton.addActionListener(e -> handleRegister(frame));

        addHoverEffect(loginButton);
        addHoverEffect(registerButton);
    }

    private void handleLogin(JFrame frame) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = Connect.getConnection()) {
            if (conn != null) {
                if (checkCredentials(conn, username, password)) {
                    resetFailedAttempts(conn, username);
                    LoggedInUser.setUsername(username);
                    JOptionPane.showMessageDialog(null, "Login successful!");
                    frame.dispose();
                    SwingUtilities.invokeLater(() -> new ModernDashboard());

                } else {
                    incrementFailedAttempts(conn, username);
                    JOptionPane.showMessageDialog(null, "Invalid username or password. Please try again.");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection error: " + e.getMessage());
        }
    }

    private boolean checkCredentials(Connection conn, String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, username);
            pst.setString(2, password);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void incrementFailedAttempts(Connection conn, String username) throws SQLException {
        String query = "INSERT INTO login_attempts (username, failed_attempts, last_failed_attempt) VALUES (?, 1, NOW()) " +
                "ON DUPLICATE KEY UPDATE failed_attempts = failed_attempts + 1, last_failed_attempt = NOW()";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, username);
            pst.executeUpdate();
        }
    }

    private void resetFailedAttempts(Connection conn, String username) throws SQLException {
        String query = "UPDATE login_attempts SET failed_attempts = 0 WHERE username = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, username);
            pst.executeUpdate();
        }
    }

    private void handleRegister(JFrame frame) {
        frame.getContentPane().removeAll();
        frame.add(new RegisterPanel(frame));
        frame.revalidate();
        frame.repaint();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(new Color(0, 31, 63));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        button.setPreferredSize(new Dimension(150, 50));
        return button;
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Define darker gradient colors for navy blue hues
        Color navy1 = new Color(0, 31, 63);  // Navy
        Color navy2 = new Color(0, 42, 85);  // Darker navy blue
        Color navy3 = new Color(0, 62, 128);   // Even darker navy blue
        Color navy4 = new Color(0, 83, 165);   // Lightest navy blue

        // Create gradient from navy1 to navy4
        GradientPaint gradientPaint = new GradientPaint(0, 0, navy1, 0, getHeight(), navy4);
        g2d.setPaint(gradientPaint);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    // Method to add hover effect to buttons
    private void addHoverEffect(JButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 54, 110));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 31, 63));
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Login Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(null);
            frame.add(new LoginPanel(frame));
            frame.setVisible(true);
        });
    }
}