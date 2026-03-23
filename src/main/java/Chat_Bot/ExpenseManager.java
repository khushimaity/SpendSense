package Chat_Bot;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import Connect.Connect;


public class ExpenseManager {

    public double getSavingsFromDB(String username) {
        double savings = 0.0;
        try (Connection conn = Connect.getConnection()) {
            String query = """
            SELECT SUM(amount) as total
            FROM Income
            WHERE user_id = (SELECT user_id FROM Users WHERE username = ?)
              AND source IN ('Savings', 'Expense Deduction')
        """;
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    savings = rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return savings;
    }


    private static class Expense {
        private String description;
        private String category;
        private double amount;
        private LocalDate date;

        public Expense(String description, String category, double amount, LocalDate date) {
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Description cannot be empty.");
            }
            if (category == null || category.trim().isEmpty()) {
                throw new IllegalArgumentException("Category cannot be empty.");
            }
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero.");
            }
            if (date == null || date.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Date cannot be in the future.");
            }
            this.description = description;
            this.category = category;
            this.amount = amount;
            this.date = date;
        }

        public String getCategory() {
            return category;
        }

        public double getAmount() {
            return amount;
        }

        public LocalDate getDate() {
            return date;
        }

        @Override
        public String toString() {
            return "Description: " + description + ", Category: " + category + ", Amount: $" + amount + ", Date: " + date;
        }
    }

    private static List<Expense> expenses;
    private static double savings;
    private double budget;

    public ExpenseManager() {
        this.expenses = new ArrayList<>();
        this.savings = 0.0;
        this.budget = 0.0;
    }

    public void addExpense(String description, String category, double amount, LocalDate date, String username) {
        try (Connection conn = Connect.getConnection()) {
            if (conn == null) {
                System.out.println("❌ Failed to connect to database.");
                return;
            }

            // 1. Get user ID
            int userId = getUserId(conn, username);
            if (userId == -1) return;

            // 2. Get category ID (assumes category already inserted in handleAddExpense)
            int categoryId = getCategoryId(conn, userId, category);

            // 3. Deduct from savings
            String deductQuery = """
            UPDATE Income 
            SET amount = amount - ?, date = CURDATE()
            WHERE user_id = ? AND source = 'Savings'
        """;
            try (PreparedStatement stmt = conn.prepareStatement(deductQuery)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }

            // ✅ 4. Insert into Expenses table
            String insertExpenseQuery = """
            INSERT INTO Expenses (user_id, category_id, description, amount, date)
            VALUES (?, ?, ?, ?, ?)
        """;
            try (PreparedStatement stmt = conn.prepareStatement(insertExpenseQuery)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, categoryId);
                stmt.setString(3, description);
                stmt.setDouble(4, amount);
                stmt.setDate(5, java.sql.Date.valueOf(date));
                stmt.executeUpdate();
            }

            System.out.println("✅ Expense added successfully to Expenses table.");
        } catch (SQLException e) {
            System.out.println("❌ Error while adding expense: " + e.getMessage());
        }
    }

    private int getCategoryId(Connection conn, int userId, String categoryName) throws SQLException {
        String query = "SELECT Category_ID FROM Category WHERE Category_Name = ? AND User_ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, categoryName);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("Category_ID");
            } else {
                throw new SQLException("❌ Category not found: " + categoryName);
            }
        }
    }

    // Helper to get user_id
    private static int getUserId(Connection conn, String username) throws SQLException {
        String query = "SELECT user_id FROM Users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
        }
        return -1;
    }

    public static String displayExpenses(String username) {
        StringBuilder result = new StringBuilder("Your Expenses:\n");

        try (Connection conn = Connect.getConnection()) {
            int userId = getUserId(conn, username);

            String query = """
            SELECT e.description, c.Category_Name, e.amount, e.date 
            FROM Expenses e
            JOIN Category c ON e.category_id = c.Category_ID
            WHERE e.user_id = ?
            ORDER BY e.date DESC
        """;

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (!rs.isBeforeFirst()) return "No expenses recorded yet.\n";

                while (rs.next()) {
                    result.append("Description: ").append(rs.getString("description"))
                            .append(", Category: ").append(rs.getString("Category_Name"))
                            .append(", Amount: ₹").append(rs.getDouble("amount"))
                            .append(", Date: ").append(rs.getDate("date"))
                            .append("\n");
                }
            }
        } catch (SQLException e) {
            return "Error fetching expenses: " + e.getMessage();
        }
        return result.toString();
    }


    public static String showCategoryWiseExpenses(String username) {
        StringBuilder result = new StringBuilder("Category-wise Expenses:\n");

        try (Connection conn = Connect.getConnection()) {
            int userId = getUserId(conn, username);

            String query = """
            SELECT c.Category_Name, SUM(e.amount) AS total
            FROM Expenses e
            JOIN Category c ON e.category_id = c.Category_ID
            WHERE e.user_id = ?
            GROUP BY c.Category_Name
        """;

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (!rs.isBeforeFirst()) return "No expenses recorded yet.\n";

                while (rs.next()) {
                    result.append("Category: ").append(rs.getString("Category_Name"))
                            .append(", Total: ₹").append(rs.getDouble("total"))
                            .append("\n");
                }
            }
        } catch (SQLException e) {
            return "Error fetching category-wise expenses: " + e.getMessage();
        }
        return result.toString();
    }


    public String showBiggestExpense(String username) {
        String query = """
        SELECT e.description, c.Category_Name, e.amount, e.date
        FROM Expenses e
        JOIN Category c ON e.category_id = c.Category_ID
        WHERE e.user_id = (SELECT user_id FROM Users WHERE username = ?)
        ORDER BY e.amount DESC
        LIMIT 1
    """;

        try (Connection conn = Connect.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return "Biggest Expense:\nDescription: " + rs.getString("description") +
                        ", Category: " + rs.getString("Category_Name") +
                        ", Amount: ₹" + rs.getDouble("amount") +
                        ", Date: " + rs.getDate("date") + "\n";
            }
        } catch (SQLException e) {
            return "❌ Error fetching biggest expense: " + e.getMessage();
        }

        return "No expenses recorded yet.";
    }


    public String showMostRecurringExpense(String username) {
        String query = """
        SELECT c.Category_Name, COUNT(*) AS frequency
        FROM Expenses e
        JOIN Category c ON e.category_id = c.Category_ID
        WHERE e.user_id = (SELECT user_id FROM Users WHERE username = ?)
        GROUP BY c.Category_Name
        ORDER BY frequency DESC
        LIMIT 1
    """;

        try (Connection conn = Connect.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return "Most Recurring Expense Category: " + rs.getString("Category_Name") +
                        " (" + rs.getInt("frequency") + " times)\n";
            }
        } catch (SQLException e) {
            return "❌ Error fetching most recurring expense: " + e.getMessage();
        }

        return "No expenses recorded yet.";
    }


    public static String showDailyExpenses(LocalDate date, String username) {
        StringBuilder result = new StringBuilder("Expenses for " + date + ":\n");

        try (Connection conn = Connect.getConnection()) {
            int userId = getUserId(conn, username);

            String query = """
            SELECT e.description, c.Category_Name, e.amount
            FROM Expenses e
            JOIN Category c ON e.category_id = c.Category_ID
            WHERE e.user_id = ? AND e.date = ?
        """;

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setDate(2, java.sql.Date.valueOf(date));
                ResultSet rs = stmt.executeQuery();

                if (!rs.isBeforeFirst()) return "No expenses recorded for " + date + ".\n";

                while (rs.next()) {
                    result.append("Description: ").append(rs.getString("description"))
                            .append(", Category: ").append(rs.getString("Category_Name"))
                            .append(", Amount: ₹").append(rs.getDouble("amount"))
                            .append("\n");
                }
            }
        } catch (SQLException e) {
            return "Error fetching daily expenses: " + e.getMessage();
        }
        return result.toString();
    }



    public String showMonthlyExpenses(int year, int month, String username) {
        StringBuilder result = new StringBuilder("Expenses for " + year + "-" + String.format("%02d", month) + ":\n");

        String query = """
        SELECT e.description, c.Category_Name, e.amount, e.date
        FROM Expenses e
        JOIN Category c ON e.category_id = c.Category_ID
        WHERE e.user_id = (SELECT user_id FROM Users WHERE username = ?)
          AND YEAR(e.date) = ? AND MONTH(e.date) = ?
    """;

        try (Connection conn = Connect.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setInt(2, year);
            stmt.setInt(3, month);
            ResultSet rs = stmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                result.append("Description: ").append(rs.getString("description"))
                        .append(", Category: ").append(rs.getString("Category_Name"))
                        .append(", Amount: ₹").append(rs.getDouble("amount"))
                        .append(", Date: ").append(rs.getDate("date")).append("\n");
            }

            if (!found) return "No expenses recorded for " + year + "-" + String.format("%02d", month) + ".\n";
        } catch (SQLException e) {
            return "❌ Error fetching monthly expenses: " + e.getMessage();
        }

        return result.toString();
    }


    public String showYearlyExpenses(int year, String username) {
        StringBuilder result = new StringBuilder("Expenses for the year " + year + ":\n");

        String query = """
        SELECT e.description, c.Category_Name, e.amount, e.date
        FROM Expenses e
        JOIN Category c ON e.category_id = c.Category_ID
        WHERE e.user_id = (SELECT user_id FROM Users WHERE username = ?)
          AND YEAR(e.date) = ?
    """;

        try (Connection conn = Connect.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setInt(2, year);
            ResultSet rs = stmt.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                result.append("Description: ").append(rs.getString("description"))
                        .append(", Category: ").append(rs.getString("Category_Name"))
                        .append(", Amount: ₹").append(rs.getDouble("amount"))
                        .append(", Date: ").append(rs.getDate("date")).append("\n");
            }

            if (!found) return "No expenses recorded for the year " + year + ".\n";
        } catch (SQLException e) {
            return "❌ Error fetching yearly expenses: " + e.getMessage();
        }

        return result.toString();
    }

    public void addReminder(String message, LocalDate date, String time, String username) {
        try (Connection conn = Connect.getConnection()) {
            if (conn == null) {
                System.out.println("❌ Failed to connect to database.");
                return;
            }

            // Insert reminder into the database
            String query = """
            INSERT INTO Reminders (user_id, message, date, time)
            VALUES ((SELECT user_id FROM Users WHERE username = ?), ?, ?, ?)
            """;
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, message);
                stmt.setDate(3, java.sql.Date.valueOf(date));
                stmt.setString(4, time);
                stmt.executeUpdate();
                System.out.println("✅ Reminder added successfully!");
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("45000")) {
                System.out.println("❌ Error: " + e.getMessage());
            } else {
                System.out.println("❌ Database error: " + e.getMessage());
            }
        }
    }

}




