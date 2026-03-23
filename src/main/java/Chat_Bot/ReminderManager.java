package Chat_Bot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

import javax.swing.JOptionPane;

import Connect.Connect;

public class ReminderManager {
    private final Timer timer;

    public ReminderManager() {
        this.timer = new Timer(true); // Daemon timer

        // Ensure the trigger is created
        //TriggerManager.createCheckPastRemindersTrigger();

        pollTriggeredReminders(); // Start polling for triggered reminders
    }

    public void addReminder(String message, String date, String time, String username) {
        try {
            LocalDate reminderDate = LocalDate.parse(date);
            LocalTime reminderTime = LocalTime.parse(time);

            // Save reminder to the database
            saveReminderToDatabase(username, reminderDate, reminderTime, message);

            long delay = java.sql.Timestamp.valueOf(reminderDate.atTime(reminderTime)).getTime() - System.currentTimeMillis();

            if (delay > 0) {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // Display notification
                        JOptionPane.showMessageDialog(null, "Reminder for " + username + ": " + message, "Reminder", JOptionPane.INFORMATION_MESSAGE);
                        System.out.println("Reminder for " + username + ": " + message);
                    }
                }, delay);
            }
        } catch (SQLException e) {
            // Handle the error raised by the trigger
            if ("45000".equals(e.getSQLState())) {
                String errorMessage = "Reminder not set: " + e.getMessage();
                System.out.println(errorMessage);
                JOptionPane.showMessageDialog(null, errorMessage, "Trigger Error", JOptionPane.ERROR_MESSAGE);
            } else {
                System.out.println("Failed to set reminder: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Failed to set reminder: " + e.getMessage());
        }
    }

    private void saveReminderToDatabase(String username, LocalDate date, LocalTime time, String content) throws SQLException {
        try (Connection conn = Connect.getConnection()) {
            if (conn == null) {
                throw new SQLException("Database connection failed.");
            }

            // Get user_id from username
            String userIdQuery = "SELECT user_id FROM Users WHERE username = ?";
            int userId;
            try (PreparedStatement userStmt = conn.prepareStatement(userIdQuery)) {
                userStmt.setString(1, username);
                var rs = userStmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                } else {
                    throw new SQLException("User not found: " + username);
                }
            }

            // Insert reminder into the database
            String insertQuery = """
                INSERT INTO reminder (user_id, reminder_details, reminder_time, reminder_date)
                VALUES (?, ?, ?, ?)
            """;
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setInt(1, userId);
                stmt.setString(2, content);
                stmt.setTime(3, java.sql.Time.valueOf(time));
                stmt.setDate(4, java.sql.Date.valueOf(date));
                stmt.executeUpdate();
            }
        }
    }

    public void getRemindersForUser(String username) {
        try (Connection conn = Connect.getConnection()) {
            if (conn == null) {
                System.out.println("Database connection failed.");
                return;
            }

            // Get user_id from username
            String userIdQuery = "SELECT user_id FROM Users WHERE username = ?";
            int userId;
            try (PreparedStatement userStmt = conn.prepareStatement(userIdQuery)) {
                userStmt.setString(1, username);
                var rs = userStmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                } else {
                    System.out.println("User not found: " + username);
                    return;
                }
            }

            // Retrieve reminders for the user
            String selectQuery = """
                SELECT reminder_details, reminder_time, reminder_date
                FROM reminder
                WHERE user_id = ?
            """;
            try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                stmt.setInt(1, userId);
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    String details = rs.getString("reminder_details");
                    LocalTime time = rs.getTime("reminder_time").toLocalTime();
                    LocalDate date = rs.getDate("reminder_date").toLocalDate();
                    System.out.println("Reminder: " + details + " at " + time + " on " + date);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to retrieve reminders: " + e.getMessage());
        }
    }

    public void startBudgetMonitor(Supplier<Double> totalExpensesSupplier, Supplier<Double> budgetSupplier) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double totalExpenses = totalExpensesSupplier.get();
                double budget = budgetSupplier.get();

                if (totalExpenses > budget) {
                    System.out.println("Warning: You have exceeded your budget!");
                }
            }
        }, 0, 24 * 60 * 60 * 1000); // Check daily
    }

    public void pollTriggeredReminders() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try (Connection conn = Connect.getConnection()) {
                    if (conn == null) {
                        System.out.println("Database connection failed.");
                        return;
                    }

                    // Retrieve triggered reminders
                    String selectQuery = """
                        SELECT reminder_id, reminder_details, username
                        FROM reminder r
                        JOIN Users u ON r.user_id = u.user_id
                        WHERE r.triggered = 1
                    """;
                    try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
                        var rs = stmt.executeQuery();
                        while (rs.next()) {
                            int reminderId = rs.getInt("reminder_id");
                            String details = rs.getString("reminder_details");
                            String username = rs.getString("username");

                            // Display notification
                            JOptionPane.showMessageDialog(null, "Reminder for " + username + ": " + details, "Reminder (Triggered)", JOptionPane.INFORMATION_MESSAGE);
                            System.out.println("Reminder for " + username + ": " + details);

                            // Mark reminder as processed
                            markReminderAsProcessed(conn, reminderId);
                        }
                    }
                } catch (SQLException e) {
                    System.out.println("Failed to poll triggered reminders: " + e.getMessage());
                }
            }
        }, 0, 60 * 1000); // Check every minute
    }

    private void markReminderAsProcessed(Connection conn, int reminderId) throws SQLException {
        String updateQuery = "UPDATE reminder SET triggered = 2 WHERE reminder_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setInt(1, reminderId);
            stmt.executeUpdate();
        }
    }
}