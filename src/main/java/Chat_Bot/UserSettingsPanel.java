package Chat_Bot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class UserSettingsPanel extends JPanel {

    public UserSettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 45));

        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());
        container.setBackground(new Color(45, 45, 45));
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(40, 40, 40));
        topPanel.setPreferredSize(new Dimension(400, 50));

        JLabel titleLabel = new JLabel("Settings", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        topPanel.add(titleLabel, BorderLayout.CENTER);

        JPanel settingsList = new JPanel();
        settingsList.setLayout(new GridLayout(4, 1, 1, 1));
        settingsList.setBackground(new Color(35, 35, 35));

        settingsList.add(createSettingsButton("Profile", "👤", new ProfilePanel()));
        settingsList.add(createSettingsButton("Preferences", "⚙️", new PreferencesPanel()));
        settingsList.add(createSettingsButton("Notifications", "🔔", new NotificationsPanel()));
        settingsList.add(createSettingsButton("Privacy Settings", "🔒", new PrivacySettingsPanel()));

        container.add(topPanel, BorderLayout.NORTH);
        container.add(settingsList, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(container, BorderLayout.CENTER);
    }

    private JButton createSettingsButton(String text, String icon, JPanel targetPanel) {
        JButton button = new JButton(icon + "  " + text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(50, 50, 50));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(70, 70, 70)));
        button.setFocusPainted(false);

        button.addActionListener(e -> {
            JFrame frame = new JFrame(text);
            frame.setSize(400, 500);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(targetPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        return button;
    }

    // ✅ Profile Panel (Fetch & Update User Data)
    class ProfilePanel extends JPanel {
        JTextField usernameField, emailField, phoneField, dobField;
        JButton editButton, saveButton;

        public ProfilePanel() {
            setLayout(new GridLayout(5, 2, 10, 10));
            setBackground(new Color(35, 35, 35));
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            usernameField = createTextField();
            emailField = createTextField();
            phoneField = createTextField();
            dobField = createTextField();

            editButton = new JButton("Edit");
            saveButton = new JButton("Save");
            saveButton.setEnabled(false);

            editButton.addActionListener(e -> enableEditing(true));
            saveButton.addActionListener(e -> {
                enableEditing(false);
                updateUserDetails();
            });

            add(createStyledLabel("Username:")); add(usernameField);
            add(createStyledLabel("Email:")); add(emailField);
            add(createStyledLabel("Phone:")); add(phoneField);
            add(createStyledLabel("Date of Birth:")); add(dobField);
            add(editButton); add(saveButton);

            loadUserData();
        }

        private JTextField createTextField() {
            JTextField field = new JTextField();
            field.setEditable(false);
            return field;
        }

        private JLabel createStyledLabel(String text) {
            JLabel label = new JLabel(text);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            return label;
        }

        private void enableEditing(boolean enable) {
            emailField.setEditable(enable);
            phoneField.setEditable(enable);
            dobField.setEditable(enable);
            saveButton.setEnabled(enable);
        }

        private void loadUserData() {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
                stmt.setString(1, LoggedInUser.getUsername());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    usernameField.setText(rs.getString("username"));
                    emailField.setText(rs.getString("email"));
                    phoneField.setText(rs.getString("phone"));
                    dobField.setText(rs.getString("dob"));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        private void updateUserDetails() {
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE users SET email=?, phone=?, dob=? WHERE username=?")) {
                stmt.setString(1, emailField.getText());
                stmt.setString(2, phoneField.getText());
                stmt.setString(3, dobField.getText());
                stmt.setString(4, LoggedInUser.getUsername());
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Profile updated successfully!");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    class PreferencesPanel extends JPanel {
        public PreferencesPanel() {
            setLayout(new GridLayout(2, 2, 10, 10));
            setBackground(new Color(35, 35, 35));
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JComboBox<String> languageBox = new JComboBox<>(new String[]{"English", "Spanish", "French"});
            JComboBox<String> currencyBox = new JComboBox<>(new String[]{"USD", "EUR", "GBP"});

            add(createStyledLabel("Language:"));
            add(languageBox);
            add(createStyledLabel("Currency:"));
            add(currencyBox);
        }
    }

    class NotificationsPanel extends JPanel {
        public NotificationsPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(35, 35, 35));
            JLabel label = new JLabel("Manage Notifications", SwingConstants.CENTER);
            add(label, BorderLayout.CENTER);
        }
    }

    class PrivacySettingsPanel extends JPanel {
        public PrivacySettingsPanel() {
            setLayout(new GridLayout(2, 2, 10, 10));
            setBackground(new Color(35, 35, 35));

            JButton changeUsernameButton = new JButton("Change Username");
            JButton changePasswordButton = new JButton("Change Password");

            changeUsernameButton.addActionListener(e -> showChangeUsernameDialog());
            changePasswordButton.addActionListener(e -> showChangePasswordDialog());

            add(createStyledLabel("Username:"));
            add(changeUsernameButton);
            add(createStyledLabel("Password:"));
            add(changePasswordButton);
        }

        private void showChangeUsernameDialog() {
            JTextField newUsername = new JTextField();
            int option = JOptionPane.showConfirmDialog(null, new Object[]{
                    "New Username:", newUsername
            }, "Change Username", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                try (Connection conn = DatabaseHelper.getConnection()) {
                    String updateQuery = "UPDATE users SET username=? WHERE username=?";
                    PreparedStatement stmt = conn.prepareStatement(updateQuery);
                    stmt.setString(1, newUsername.getText());
                    stmt.setString(2, LoggedInUser.getUsername());
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Username updated successfully!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void showChangePasswordDialog() {
            JOptionPane.showMessageDialog(null, "Password change feature coming soon!");
        }
    }
}
