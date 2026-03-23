package Connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        String url = "jdbc:mysql://localhost:3306/";
        String dbName = "chatbot";
        String user = "root";
        String password = "bijubhavan"; // Replace with your actual MySQL password

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            // 1. Create database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            stmt.execute("USE " + dbName);

            // 2. Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(100),
                    phone VARCHAR(15),
                    dob DATE
                )
            """);

            // 3. Budget table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Budget (
                    user_id INT PRIMARY KEY,
                    amount DOUBLE,
                    duration VARCHAR(20),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                )
            """);

            // 4. Category table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Category (
                    Category_ID INT AUTO_INCREMENT PRIMARY KEY,
                    Category_Name VARCHAR(100) NOT NULL,
                    User_ID INT NOT NULL,
                    Created_At TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (User_ID) REFERENCES users(User_ID) ON DELETE CASCADE
                )
            """);

            // 5. Expenses table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Expenses (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT,
                    category_id INT,
                    description VARCHAR(255),
                    amount DOUBLE,
                    date DATE,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (category_id) REFERENCES Category(Category_ID)
                )
            """);

            // 6. Income table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Income (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT,
                    source VARCHAR(100),
                    amount DOUBLE,
                    date DATE,
                    UNIQUE (user_id, source),
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                )
            """);

            // 7. Deal_Tracking table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Deal_Tracking (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT,
                    product_name VARCHAR(100),
                    category VARCHAR(50),
                    url TEXT,
                    tracked_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                )
            """);

            // 8. Reminder table (corrected)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reminder (
                    reminder_id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    reminder_details TEXT,
                    reminder_time TIME,
                    reminder_date DATE,
                    CONSTRAINT fk_reminder_user
                    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                )
            """);

            // 9. Login_Attempt table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS login_attempts (
                    username VARCHAR(255) PRIMARY KEY,
                    failed_attempts INT DEFAULT 0,
                    last_failed_attempt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            System.out.println("✅ All tables created successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ Error initializing database: " + e.getMessage());
        }
    }
}