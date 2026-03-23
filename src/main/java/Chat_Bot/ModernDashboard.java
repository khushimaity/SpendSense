package Chat_Bot;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.json.JSONArray;
import org.json.JSONObject;

import Connect.Connect;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ModernDashboard extends JFrame {

    private final JPanel mainPanel;
    private JTextArea chatArea;
    private JTextField chatInput;
    private final CardLayout cardLayout;
    private boolean isFirstUse = true;
    private String currentContext = "MainMenu";
    private ExpenseManager expenseManager;
    private String selectedCategory;
    private String productName;
    String loggedInUsername = LoggedInUser.getUsername(); // Get username
    private ReminderManager reminderManager;
    private ChartPanel monthlyLineChartPanel;
    private ChartPanel yearlyLineChartPanel;
    private ChartPanel barChartPanel;
    private ChartPanel pieChartPanel;
    private JPanel chartGridPanel;


    public ModernDashboard() {
        setTitle("Spend Sense");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        this.expenseManager = new ExpenseManager();
        this.reminderManager = new ReminderManager();

        JPanel sidePanel = createSidePanel();
        add(sidePanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // ✅ Add panels in the correct order
        mainPanel.add(createDashboardPanel(), "Dashboard");  // Home Page
        mainPanel.add(createChatbotPanel(), "Chatbot");
        mainPanel.add(new UserSettingsPanel(), "Settings");

        add(mainPanel, BorderLayout.CENTER);

        // ✅ Force "Dashboard" to be the first page shown
        cardLayout.show(mainPanel, "Dashboard");

        setVisible(true);
    }

    private JPanel createSidePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 1));
        panel.setBackground(new Color(0x001f3f));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JButton homeButton = createSideButton("Home", "C:\\Users\\Dell\\Desktop\\home.png");
        JButton chatbotButton = createSideButton("Chat", "C:\\Users\\Dell\\Desktop\\chats.png");
        JButton settingsButton = createSideButton("Settings", "C:\\Users\\Dell\\Desktop\\settings.png");

        homeButton.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        chatbotButton.addActionListener(e -> cardLayout.show(mainPanel, "Chatbot"));
        settingsButton.addActionListener(e -> cardLayout.show(mainPanel, "Settings"));

        panel.add(homeButton);
        panel.add(chatbotButton);
        panel.add(settingsButton);

        return panel;
    }

    private JButton createSideButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setForeground(new Color(0xf8f9fa));
        button.setBackground(new Color(0x001f3f));
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setIcon(new ImageIcon(iconPath));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setBorderPainted(false);

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x003366), 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(0x003366));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0x001f3f));
            }
        });

        return button;
    }

    private Connection getConnection() throws Exception {
        String url = "jdbc:mysql://localhost:3306/chatbot"; // use your DB name
        String user = "root";
        String password = "bijubhavan"; // change this
        return DriverManager.getConnection(url, user, password);
    }

    private JPanel createDashboardPanel() {
        JPanel dashboard = new JPanel(new BorderLayout());

        // ======= WELCOME MESSAGE =======
        JLabel welcomeLabel = new JLabel("WELCOME, " + loggedInUsername + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(0x001F3F));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeLabel1 = new JLabel("Here is your expense overview and analysis.", SwingConstants.CENTER);
        welcomeLabel1.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel1.setForeground(new Color(0x001F3F));
        welcomeLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ======= REFRESH BUTTON =======
        JButton refreshButton = new JButton("⟳ Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshButton.setBackground(new Color(0x001F3F));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);

        // ======= HEADER PANEL WITH WELCOME TEXT AND REFRESH =======
        JPanel welcomePanel = new JPanel(new BorderLayout());
        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));
        labelsPanel.setBackground(Color.WHITE);
        labelsPanel.add(welcomeLabel);
        labelsPanel.add(welcomeLabel1);

        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshPanel.setBackground(Color.WHITE);
        refreshPanel.add(refreshButton);

        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.add(labelsPanel, BorderLayout.CENTER);
        welcomePanel.add(refreshPanel, BorderLayout.EAST);

        // ======= CHART PANEL (MODIFIABLE CONTENT) =======
        JPanel contentPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));

        // Add initial charts
        contentPanel.add(createChartPanel(createMonthlyLineChart(), "Monthly spend"));
        contentPanel.add(createChartPanel(createYearlyLineChart(), "Yearly spend"));
        contentPanel.add(createChartPanel(createPieChart(), "Category-wise Spending"));
        contentPanel.add(createChartPanel(createBarChart(), "Bar Chart"));

        // Refresh Button Action: Clear and re-add charts
        refreshButton.addActionListener(e -> {
            contentPanel.removeAll();
            contentPanel.add(createChartPanel(createMonthlyLineChart(), "Monthly spend"));
            contentPanel.add(createChartPanel(createYearlyLineChart(), "Yearly spend"));
            contentPanel.add(createChartPanel(createPieChart(), "Category-wise Spending"));
            contentPanel.add(createChartPanel(createBarChart(), "Bar Chart"));
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        dashboard.add(welcomePanel, BorderLayout.NORTH);
        dashboard.add(contentPanel, BorderLayout.CENTER);

        return dashboard;
    }


    private JPanel createChartPanel(JFreeChart chart, String title) {
        ChartPanel panel = new ChartPanel(chart);
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    public JPanel createChartPanelWithRefresh() {
        JPanel outerPanel = new JPanel(new BorderLayout());

        // Create the chart panel
        JPanel chartPanel = createChartPanel(createMonthlyLineChart(), "Monthly Expenses");
        outerPanel.add(chartPanel, BorderLayout.CENTER);

        // Create refresh button
        JButton refreshButton = new JButton("⟳ Refresh");
        refreshButton.setFocusable(false);

        // Align refresh button to the right
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(refreshButton);
        outerPanel.add(topPanel, BorderLayout.NORTH);

        // Add action listener
        refreshButton.addActionListener(e -> {
            // Remove old chart and add updated one
            outerPanel.remove(chartPanel);
            JPanel updatedChart = createChartPanel(createMonthlyLineChart(), "Monthly Expenses");
            outerPanel.add(updatedChart, BorderLayout.CENTER);
            outerPanel.revalidate();
            outerPanel.repaint();
        });

        return outerPanel;
    }



    private JFreeChart createMonthlyLineChart() {
        XYSeries series = new XYSeries("Monthly Expenses");

        // Get current year and month
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        try (Connection conn = getConnection()) {
            String query = """
            SELECT DAY(date) AS day, SUM(amount) AS total
            FROM Expenses
            WHERE YEAR(date) = ? AND MONTH(date) = ?
            GROUP BY DAY(date)
            ORDER BY DAY(date)
        """;

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, year);
                stmt.setInt(2, month);

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int day = rs.getInt("day");
                    double total = rs.getDouble("total");
                    series.add(day, total);  // X: day of month, Y: total expense
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        return ChartFactory.createXYLineChart(
                "Expenses in " + year + "-" + String.format("%02d", month),
                "Day of Month",
                "Total Spent",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }

    private JFreeChart createYearlyLineChart() {
        XYSeries series = new XYSeries("Total Monthly Expenses");
        int year = LocalDate.now().getYear();

        try (Connection conn = getConnection()) {
            String query = """
            SELECT MONTH(date) AS month, SUM(amount) AS total
            FROM Expenses
            WHERE YEAR(date) = ?
            GROUP BY MONTH(date)
            ORDER BY MONTH(date)
        """;

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, year);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int month = rs.getInt("month");
                    double total = rs.getDouble("total");
                    series.add(month, total); // X: month (1-12), Y: total expenses
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);

        return ChartFactory.createXYLineChart(
                "Monthly Expenses for " + year,
                "Month",
                "Total Expenses",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }



    private JFreeChart createPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<String, Double> categoryTotals = new LinkedHashMap<>();

        try (Connection conn = getConnection()) {
            int userId = getUserIdFromUsername(conn, loggedInUsername); // ✅ Get current user's ID

            String query = """
            SELECT c.Category_Name, SUM(e.amount) AS total
            FROM Expenses e
            JOIN Category c ON e.category_id = c.Category_ID
            WHERE e.user_id = ? AND c.User_ID = ?
            GROUP BY c.Category_Name
        """;

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, userId); // ✅ Filter by user
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String category = rs.getString("Category_Name");
                    double total = rs.getDouble("total");
                    categoryTotals.put(category, total);
                    dataset.setValue(category, total);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Expenses by Category", dataset, true, true, false);

        PiePlot plot = (PiePlot) chart.getPlot();

        // 🎨 List of distinct blue shades
        Color[] colors = {
                new Color(0x0000FF),  // Blue
                new Color(0x000080),  // Navy Blue
                new Color(0x6495ED),  // Cornflower Blue
                new Color(0x0F52BA),  // Sapphire Blue
                new Color(0x1E90FF),  // Dodger Blue
                new Color(0x4682B4),  // Steel Blue
                new Color(0x191970),  // Midnight Blue
                new Color(0x001f3f),
                new Color(0x003366)
        };

        int i = 0;
        for (String category : categoryTotals.keySet()) {
            plot.setSectionPaint(category, colors[i % colors.length]);
            i++;
        }

        return chart;
    }


    // Based on category totals
    private JFreeChart createBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int currentYear = LocalDate.now().getYear();

        // Step 1: Create month map with default values
        Map<String, Double> monthTotals = new LinkedHashMap<>();
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        for (String month : months) {
            monthTotals.put(month, 0.0);
        }

        // Step 2: Populate actual totals from DB for the current user
        try (Connection conn = getConnection()) {
            int userId = getUserIdFromUsername(conn, loggedInUsername); // ✅ get user ID

            String query = """
            SELECT MONTH(date) AS month_num, MONTHNAME(date) AS month_name, SUM(amount) AS total
            FROM Expenses
            WHERE YEAR(date) = ? AND user_id = ?
            GROUP BY MONTH(date), MONTHNAME(date)
            ORDER BY month_num
        """;

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, currentYear);
                stmt.setInt(2, userId); // ✅ filter by user
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String month = rs.getString("month_name");
                    double total = rs.getDouble("total");
                    monthTotals.put(month, total); // update from DB
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Step 3: Fill dataset
        for (Map.Entry<String, Double> entry : monthTotals.entrySet()) {
            dataset.addValue(entry.getValue(), "Expenses", entry.getKey());
        }

        // Step 4: Create chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Monthly Expenses (" + currentYear + ")",
                "Month",
                "Total Amount (₹)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // Step 5: Customize bar colors (each month = a different blue shade)
        Color[] colors = {
                new Color(0x0000FF),  // Blue
                new Color(0x000080),  // Navy Blue
                new Color(0x6495ED),  // Cornflower Blue
                new Color(0x0F52BA),  // Sapphire Blue
                new Color(0x1E90FF),  // Dodger Blue
                new Color(0x4682B4),  // Steel Blue
                new Color(0x191970),  // Midnight Blue
                new Color(0x001f3f),
                new Color(0x003366),
                new Color(0x4169E1),
                new Color(0x87CEFA),
                new Color(0x5F9EA0)
        };

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = new BarRenderer();

        // Step 6: Color each bar distinctly
        for (int i = 0; i < months.length; i++) {
            renderer.setSeriesPaint(0, colors[i % colors.length]); // Same series, use index 0
        }

        plot.setRenderer(renderer);

        return chart;
    }





    private JPanel createChatbotPanel() {
        JPanel chatbotPanel = new JPanel(new BorderLayout());
        chatbotPanel.setBackground(new Color(0xf8f9fa));
        chatbotPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Chatbot", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0x003366));
        chatbotPanel.add(titleLabel, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(0xf8f9fa));
        chatArea.setForeground(new Color(0x001f3f));
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        chatbotPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(0xf8f9fa));

        chatInput = new JTextField();
        chatInput.setBackground(new Color(0xf8f9fa));
        chatInput.setForeground(new Color(0x001f3f));
        chatInput.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JButton sendButton = new JButton("Send");
        sendButton.setBackground(new Color(0x003366));
        sendButton.setForeground(new Color(0xf8f9fa));
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sendButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        if (isFirstUse) {
            chatArea.append("Bot: Welcome! How can I help you today?\n" + getMainMenu());
            isFirstUse = false;
        }

        sendButton.addActionListener(e -> {
            String message = chatInput.getText().trim();
            if (!message.isEmpty()) {
                chatArea.append("\nYou: " + message + "\n\n");
                chatInput.setText("");
                handleUserInput(message);
            }
        });

        chatInput.addActionListener(e -> {
            String message = chatInput.getText().trim();
            if (!message.isEmpty()) {
                chatArea.append("\nYou: " + message + "\n\n");
                chatInput.setText("");
                handleUserInput(message);
            }
        });

        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatbotPanel.add(inputPanel, BorderLayout.SOUTH);

        return chatbotPanel;
    }

    private void handleUserInput(String message) {
        switch (currentContext) {
            case "MainMenu": handleMainMenu(message); break;
            case "AddExpense": handleAddExpense(message); break;
            case "SetBudget": handleSetBudget(message); break;
            case "ViewExpensesMenu": handleViewExpenses(message); break;
            case "EditOrDeleteExpenses": handleEditOrDeleteExpenses(message); break;
            case "SavingsMenu": handleSavingsMenu(message); break;
            case "SetSavings": handleSetSavings(message); break;
            case "AddSavings": handleAddSavings(message); break;
            case "DeductSavings": handleDeductSavings(message); break;
            case "TrackDeals": handleTrackDeals(message); break;
            case "TrackDealsCategory": handleTrackDealsCategory(message); break;
            case "FAQ": handleFAQ(message); break;
            case "AddReminder": handleAddReminder(message); break;
            default:
                chatArea.append("Bot: Invalid context. Returning to main menu.\n");
                resetToMainMenu();
        }
    }

    private void handleMainMenu(String message) {
        switch (message) {
            case "1":
                chatArea.append("Bot: Enter expense details (Description, Category, Amount):\n");
                currentContext = "AddExpense";
                break;
            case "2":
                chatArea.append(Expenses_Menu);
                currentContext = "ViewExpensesMenu";
                break;
            case "3":
                chatArea.append("Bot: Savings Menu:\n"
                        + "1. Set Savings\n"
                        + "2. Add Savings\n"
                        + "3. Deduct Savings\n"
                        + "4. View current Savings\n"
                        + "5. Back to Main Menu\n"
                        + "Choose an option: \n");
                currentContext = "SavingsMenu";
                break;
            case "4":
                chatArea.append("Bot: To track deals, please select a category:\n"
                        + "1. Mobiles\n2. Laptops\n3. Refrigerators\n");
                currentContext = "TrackDealsCategory";
                break;
            case "5":
                chatArea.append("Bot: Enter the budget amount:\n");
                currentContext = "SetBudget";
                break;
            case "6":
                chatArea.append("Bot: Enter the reminder details (Message, Date (YYYY-MM-DD), Time (HH:mm)):\n");
                currentContext = "AddReminder";
                break;
            case "7":
                chatArea.append("Bot: Here is the support/FAQ information:\n"
                        + "1. How to add an expense?\n"
                        + "2. How to set a budget?\n"
                        + "3. How to track expenses?\n"
                        + "4. How to view my savings?\n"
                        + "5. How to delete an expense?\n"
                        + "6. What should I do if I exceed my budget?\n"
                        + "7. None of the above, contact us directly.\n"
                        + "Please enter the number of your question: \n");
                currentContext = "FAQ";
                break;
            case "8":
                handleViewReminders();
                break;
            default:
                chatArea.append("Bot: Invalid option. Please select a valid number from the menu.\n");
        }
    }

    private void handleAddExpense(String message) {
        double currentSavings = expenseManager.getSavingsFromDB(loggedInUsername);

        if (currentSavings <= 0) {
            chatArea.append("Bot: Savings not set or insufficient. Please set your savings first.\n");
            resetToMainMenu();
            return;
        }

        String[] parts = message.split(",");
        if (parts.length != 3) {
            chatArea.append("Bot: Invalid format. Use: Description, Category, Amount\n");
            return;
        }

        try {
            String description = parts[0].trim();
            String category = parts[1].trim();
            double amount = Double.parseDouble(parts[2].trim());

            if (expenseManager.getSavingsFromDB(loggedInUsername) >= amount) {
                try (Connection conn = Connect.getConnection()) {
                    int userId = getUserIdFromUsername(conn, loggedInUsername);

                    // ✅ 1. Insert category if it doesn't exist
                    String checkCategory = "SELECT * FROM Category WHERE Category_Name = ? AND User_ID = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkCategory)) {
                        checkStmt.setString(1, category);
                        checkStmt.setInt(2, userId);
                        ResultSet rs = checkStmt.executeQuery();

                        if (!rs.next()) {
                            String insertCategory = "INSERT INTO Category (Category_Name, User_ID) VALUES (?, ?)";
                            try (PreparedStatement insertStmt = conn.prepareStatement(insertCategory)) {
                                insertStmt.setString(1, category);
                                insertStmt.setInt(2, userId);
                                insertStmt.executeUpdate();
                                System.out.println("✅ New category inserted: " + category);
                            }
                        }
                    }

                    // ✅ 2. Add expense to ExpenseManager and local logic
                    expenseManager.addExpense(description, category, amount, LocalDate.now(), loggedInUsername);
                    double updatedSavings = expenseManager.getSavingsFromDB(loggedInUsername);
                    chatArea.append("Bot: Expense added! Remaining savings: ₹" + updatedSavings + "\n");


                } catch (SQLException e) {
                    chatArea.append("Bot: Failed to record expense: " + e.getMessage() + "\n");
                }

            } else {
                chatArea.append("Bot: Not enough savings.\n");
            }
        } catch (NumberFormatException e) {
            chatArea.append("Bot: Invalid amount.\n");
        }

        resetToMainMenu();
    }

    private int getUserIdFromUsername(Connection conn, String username) throws SQLException {
        String query = "SELECT user_id FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        }
        return -1;
    }

    private void handleViewExpenses(String message) {
        String username = loggedInUsername; // Make sure this is globally available

        switch (message) {
            case "1":
                chatArea.append("Bot: Edit or Delete Menu:\n1. Edit \n2. Delete \n3.Back\n");
                break;

            case "2":
                chatArea.append(expenseManager.displayExpenses(username));
                chatArea.append("\n");
                break;

            case "3":
                chatArea.append(expenseManager.showCategoryWiseExpenses(username));
                chatArea.append("\n");
                break;

            case "4":
                chatArea.append(expenseManager.showBiggestExpense(username));
                chatArea.append("\n");
                break;

            case "5":
                chatArea.append(expenseManager.showMostRecurringExpense(username));
                chatArea.append("\n");
                break;

            case "6":
                chatArea.append("Bot: ENTER THE DATE (YYYY-MM-DD):\n");
                String dateInput = JOptionPane.showInputDialog("Enter Date (YYYY-MM-DD):");
                if (dateInput != null && !dateInput.trim().isEmpty()) {
                    chatArea.append("You: " + dateInput + "\n");
                    LocalDate date = LocalDate.parse(dateInput);
                    String result = expenseManager.showDailyExpenses(date, username);
                    chatArea.append("Bot: " + result + "\n");
                } else {
                    chatArea.append("Bot: Invalid date input. Try again.\n");
                }
                break;

            case "7":
                chatArea.append("Bot: ENTER THE YEAR AND MONTH (YYYY-MM):\n");
                String yearMonthInput = JOptionPane.showInputDialog("Enter Year and Month (YYYY-MM):");
                if (yearMonthInput != null && yearMonthInput.matches("\\d{4}-\\d{2}")) {
                    try {
                        String[] parts = yearMonthInput.split("-");
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        if (month >= 1 && month <= 12) {
                            chatArea.append("You: " + yearMonthInput + "\n");
                            String result = expenseManager.showMonthlyExpenses(year, month, username);
                            chatArea.append("Bot: " + result + "\n");
                        } else {
                            chatArea.append("Bot: Invalid month. Please enter a valid month (01-12).\n");
                        }
                    } catch (NumberFormatException e) {
                        chatArea.append("Bot: Invalid input format. Please enter in YYYY-MM format.\n");
                    }
                } else {
                    chatArea.append("Bot: Invalid format. Please enter in YYYY-MM format.\n");
                }
                break;

            case "8":
                chatArea.append("Bot: ENTER THE YEAR (YYYY):\n");
                String yearInput = JOptionPane.showInputDialog("Enter Year (YYYY):");
                if (yearInput != null && !yearInput.trim().isEmpty()) {
                    chatArea.append("You: " + yearInput + "\n");
                    int year = Integer.parseInt(yearInput);
                    String result = expenseManager.showYearlyExpenses(year, username);
                    chatArea.append("Bot: " + result + "\n");
                } else {
                    chatArea.append("Bot: Invalid year input. Try again.\n");
                }
                break;

            case "9":
                resetToMainMenu();
                return;

            default:
                chatArea.append("Bot: Invalid option. Please select a valid option.\n");
        }

        chatArea.append("Bot: Returning to View or Edit Expense menu. Enter another number or 9 to return to the main menu.\n");
    }


    private void handleSavingsMenu(String message) {
        switch (message) {
            case "1":
                chatArea.append("Bot: Please enter the target savings amount to set:\n");
                currentContext = "SetSavings";
                break;
            case "2":
                chatArea.append("Bot: Please enter the amount to add to your savings:\n");
                currentContext = "AddSavings";
                break;
            case "3":
                chatArea.append("Bot: Please enter the amount to deduct from your savings:\n");
                currentContext = "DeductSavings";
                break;
            case "4":
                handleViewSavings();
                break;
            case "5":
                resetToMainMenu();
                return;
            default:
                chatArea.append("Bot: Invalid choice. Please select a valid option.\n");
                resetToSavingsMenu();
        }
    }

    private void handleSetSavings(String message) {
        try {
            double savingsAmount = Double.parseDouble(message.trim());

            try (Connection conn = Connect.getConnection()) {
                int userId = getUserIdFromUsername(conn, loggedInUsername);

                String query = """
                INSERT INTO Income (user_id, source, amount, date)
                VALUES (?, 'Savings', ?, CURDATE())
                ON DUPLICATE KEY UPDATE amount = VALUES(amount)
            """;

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, userId);
                    stmt.setDouble(2, savingsAmount);
                    stmt.executeUpdate();
                    chatArea.append("Bot: Savings set to ₹" + savingsAmount + "\n");
                }
            } catch (SQLException e) {
                chatArea.append("Bot: Failed to set savings: " + e.getMessage() + "\n");
            }

        } catch (NumberFormatException e) {
            chatArea.append("Bot: Invalid amount. Please enter a valid number.\n");
        }

        resetToSavingsMenu();
    }



    private void handleAddSavings(String message) {
        try {
            double amountToAdd = Double.parseDouble(message.trim());

            try (Connection conn = Connect.getConnection()) {
                int userId = getUserIdFromUsername(conn, loggedInUsername);

                double current = getCurrentSavings(conn, userId);
                double updated = current + amountToAdd;

                String updateQuery = """
                INSERT INTO Income (user_id, source, amount, date)
                VALUES (?, 'Savings', ?, CURDATE())
                ON DUPLICATE KEY UPDATE amount = VALUES(amount)
            """;

                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setInt(1, userId);
                    stmt.setDouble(2, updated);
                    stmt.executeUpdate();
                    chatArea.append("Bot: ₹" + amountToAdd + " added to savings.\n");
                }
            } catch (SQLException e) {
                chatArea.append("Bot: Failed to add savings: " + e.getMessage() + "\n");
            }

        } catch (NumberFormatException e) {
            chatArea.append("Bot: Invalid amount. Please enter a valid number.\n");
        }

        resetToSavingsMenu();
    }



    private void handleDeductSavings(String message) {
        try {
            double amountToDeduct = Double.parseDouble(message.trim());

            try (Connection conn = Connect.getConnection()) {
                int userId = getUserIdFromUsername(conn, loggedInUsername);
                double current = getCurrentSavings(conn, userId);

                if (amountToDeduct > current) {
                    chatArea.append("Bot: Not enough savings to deduct that amount.\n");
                } else {
                    double updated = current - amountToDeduct;

                    String updateQuery = "UPDATE Income SET amount = ?, date = CURDATE() WHERE user_id = ? AND source = 'Savings'";
                    try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                        stmt.setDouble(1, updated);
                        stmt.setInt(2, userId);
                        stmt.executeUpdate();
                        chatArea.append("Bot: ₹" + amountToDeduct + " deducted from savings.\n");
                    }
                }
            } catch (SQLException e) {
                chatArea.append("Bot: Failed to deduct savings: " + e.getMessage() + "\n");
            }

        } catch (NumberFormatException e) {
            chatArea.append("Bot: Invalid amount. Please enter a valid number.\n");
        }

        resetToSavingsMenu();
    }


    private void handleViewSavings() {
        try (Connection conn = Connect.getConnection()) {
            int userId = getUserIdFromUsername(conn, loggedInUsername);
            double current = getCurrentSavings(conn, userId);

            chatArea.append("Bot: Your current savings balance is: ₹" + current + "\n");

        } catch (SQLException e) {
            chatArea.append("Bot: Failed to fetch savings: " + e.getMessage() + "\n");
        }

        resetToSavingsMenu();
    }

    private double getCurrentSavings(Connection conn, int userId) throws SQLException {
        String query = "SELECT amount FROM Income WHERE user_id = ? AND source = 'Savings'";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("amount");
            }
        }
        return 0.0;
    }


    private void handleEditOrDeleteExpenses(String message) {
        switch (message) {
            case "1":
                chatArea.append("Bot: Enter the expense to edit:\n");
                break;
            case "2":
                chatArea.append("Bot: Enter the expense to delete:\n");
                break;
            case "3":
                chatArea.append("Bot: \n");
                currentContext = "ViewExpensesMenu";
                break;
            default:
                chatArea.append("Bot: Invalid option. Please select a valid number from the menu or press 3 to go back.\n");
        }
    }

    private void handleSetBudget(String message) {
        try {
            double budgetAmount = Double.parseDouble(message.trim());

            try (Connection conn = Connect.getConnection()) {
                if (conn == null) {
                    chatArea.append("Bot: Database connection failed.\n");
                    resetToMainMenu();
                    return;
                }

                // Step 1: Get user_id from username
                int userId = getUserIdFromUsername(conn, loggedInUsername);


                // Step 2: Insert or update budget using user_id
                String query = """
                INSERT INTO Budget (user_id, amount, duration)
                VALUES (?, ?, 'monthly')
                ON DUPLICATE KEY UPDATE amount = VALUES(amount)
            """;

                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, userId);
                    stmt.setDouble(2, budgetAmount);
                    stmt.executeUpdate();
                    chatArea.append("Bot: Budget set to $" + budgetAmount + "\n");
                }

            } catch (SQLException e) {
                chatArea.append("Bot: Failed to save budget: " + e.getMessage() + "\n");
            }

        } catch (NumberFormatException e) {
            chatArea.append("Bot: Invalid budget amount.\n");
        }

        resetToMainMenu();
    }




    // ===== NEW: Price Comparison API Integration =====
    // Minimal change in fetchProductSearch: use the pricer endpoint + headers.
    private void fetchProductSearch(String productTitle) {
        OkHttpClient client = new OkHttpClient();
        try {
            // URL-encode the product title
            String encodedTitle = URLEncoder.encode(productTitle, StandardCharsets.UTF_8.toString());
            // Use the new pricer endpoint (uncomment country filter if API supports it)
            // String url = "https://pricer.p.rapidapi.com/str?q=" + encodedTitle + "&country_code=IN";
            String url = "https://pricer.p.rapidapi.com/str?q=" + encodedTitle;

            // Build the request with the updated headers
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("x-rapidapi-key", "671e15ac68msha13256e8d29a7d7p1f16a9jsn84a0239d4b59")
                    .addHeader("x-rapidapi-host", "pricer.p.rapidapi.com")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JSONArray products = new JSONArray(responseData);

                    if (products.length() > 0) {
                        // Create a currency formatter for India
                        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                        double conversionRate = 82.0; // Example conversion rate: 1 USD = 82 INR

                        // Helper class for storing product info and parsed price
                        class ProductEntry {
                            JSONObject product;
                            double priceValue;
                            ProductEntry(JSONObject product, double priceValue) {
                                this.product = product;
                                this.priceValue = priceValue;
                            }
                        }
                        List<ProductEntry> productList = new ArrayList<>();

                        // Process products and parse their prices
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject product = products.getJSONObject(i);
                            String priceStr = product.optString("price", "N/A");
                            double priceValue = 0.0;
                            try {
                                String numericPrice = priceStr.replaceAll("[$,]", "").split(" ")[0];
                                priceValue = Double.parseDouble(numericPrice);
                            } catch(Exception e) {
                                continue;
                            }
                            if (priceValue > 0) {
                                productList.add(new ProductEntry(product, priceValue));
                            }
                        }

                        // Sort products by price (lowest first)
                        productList.sort(Comparator.comparingDouble(pe -> pe.priceValue));

                        // Limit to top 5 results
                        int limit = Math.min(5, productList.size());
                        for (int i = 0; i < limit; i++) {
                            ProductEntry entry = productList.get(i);
                            JSONObject product = entry.product;
                            String title = product.optString("title", "N/A");
                            String shop = product.optString("shop", "N/A");
                            String shipping = product.optString("shipping", "N/A");

                            double priceInINR = entry.priceValue * conversionRate;
                            String formattedPrice = formatter.format(priceInINR);

                            chatArea.append("Bot: Title: " + title + "\n");
                            chatArea.append("Bot: Price: " + formattedPrice + "\n");
                            chatArea.append("Bot: Shop: " + shop + "\n");
                            chatArea.append("Bot: Shipping: " + shipping + "\n");
                            chatArea.append("Bot: ----------------------\n\n");
                        }
                        if(productList.isEmpty()){
                            chatArea.append("Bot: No products with valid prices found for " + productTitle + ".\n");
                        }
                    } else {
                        chatArea.append("Bot: No products found for " + productTitle + ".\n");
                    }
                } else {
                    chatArea.append("Bot: Request failed with status code: " + response.code() + "\n");
                }
            }
        } catch (Exception e) {
            chatArea.append("Bot: Error fetching product search data: " + e.getMessage() + "\n");
        }
    }

    private void handleTrackDeals(String message) {
        if (selectedCategory == null) {
            chatArea.append("Bot: Select a category to track deals:\n");
            chatArea.append("1. Mobiles\n2. Laptops\n3. Refrigerators\n");
            currentContext = "TrackDealsCategory";
        } else if (productName == null) {
            productName = message.trim();
            chatArea.append("Bot: Searching for best prices for " + productName + "...\n");
            fetchProductSearch(productName);
            // Reset state so further searches work correctly
            productName = null;
            selectedCategory = null;
            resetToMainMenu();
        }
    }

    private void handleTrackDealsCategory(String message) {
        switch (message.trim()) {
            case "1":
                selectedCategory = "mobiles";
                break;
            case "2":
                selectedCategory = "laptops";
                break;
            case "3":
                selectedCategory = "refrigerators";
                break;
            default:
                chatArea.append("Bot: Invalid choice. Please select a valid category (1, 2, or 3).\n");
                return;
        }
        chatArea.append("Bot: Enter the product name you want to track:\n");
        currentContext = "TrackDeals";
    }
    // ===== End Price Comparison API Integration =====

    private void handleFAQ(String message) {
        switch (message) {
            case "1":
                chatArea.append("Bot: To add an expense, go to 'Add Expense' from the main menu, enter the description, category, and amount.\n");
                resetToMainMenu();
                currentContext = "MainMenu";
                break;
            case "2":
                chatArea.append("Bot: To set a budget, choose 'Set Budget' from the main menu and enter your desired budget.\n");
                resetToMainMenu();
                currentContext = "MainMenu";
                break;
            case "3":
                chatArea.append("Bot: You can track expenses using 'View Expenses' to see daily, monthly, or category-wise reports.\n");
                resetToMainMenu();
                currentContext = "MainMenu";
                break;
            case "4":
                chatArea.append("Bot: To view your savings, select 'Savings' from the main menu, then choose 'View Current Savings'.\n");
                resetToMainMenu();
                currentContext = "MainMenu";
                break;
            case "5":
                chatArea.append("Bot: To delete an expense, go to 'Edit/Delete Expense' from the main menu, select the expense, and confirm deletion.\n");
                resetToMainMenu();
                currentContext = "MainMenu";
                break;
            case "6":
                chatArea.append("Bot: If you exceed your budget, consider reviewing your expenses and adjusting your budget.\n");
                resetToMainMenu();
                currentContext = "MainMenu";
                break;
            case "7":
                chatArea.append("Bot: For further assistance, contact us at:\n");
                chatArea.append("📧 Email: spendsense@gmail.com\n");
                chatArea.append("📞 Customer Support: 9073927299\n");
                resetToMainMenu();
                currentContext = "MainMenu";
                break;
            default:
                chatArea.append("Bot: Invalid option. Please select a valid number from the menu.\n");
        }
    }

    private void handleAddReminder(String message) {
        try {
            String[] parts = message.split(",");
            if (parts.length != 3) {
                chatArea.append("Bot: Invalid format. Use: Message, Date (YYYY-MM-DD), Time (HH:mm)\n");
                return;
            }

            String reminderMessage = parts[0].trim();
            String date = parts[1].trim();
            String time = parts[2].trim();

            try {
                reminderManager.addReminder(reminderMessage, date, time, loggedInUsername); // Pass username

            } catch (Exception e) { // Catch general exceptions
                if (e.getMessage().contains("Reminder not set")) { // Trigger error for past date
                    chatArea.append("Bot: Error: " + e.getMessage() + "\n");
                } else {
                    chatArea.append("Bot: Failed to add reminder: " + e.getMessage() + "\n");
                }
            }
        } catch (Exception e) {
            chatArea.append("Bot: Failed to add reminder: " + e.getMessage() + "\n");
        }

        resetToMainMenu();
    }

    private void handleViewReminders() {
        chatArea.append("Bot: Retrieving your reminders...\n");
        reminderManager.getRemindersForUser(loggedInUsername);
        resetToMainMenu();
    }

    String Expenses_Menu = ("Bot: View or Edit Expenses Menu:\n"
            + "1. Edit or Delete Expense\n"
            + "2. Show All Expenses\n"
            + "3. Show Category-wise Expenses\n"
            + "4. Show Biggest Expense\n"
            + "5. Show Most Recurring Expense\n"
            + "6. Show Daily Expenses\n"
            + "7. Show Monthly Expenses\n"
            + "8. Show Yearly Expenses\n"
            + "9. Back to Main Menu\n"
            + "Choose an option: ");

    private void resetToSavingsMenu() {
        chatArea.append("Bot: Savings Menu:\n"
                + "1. Set Savings\n"
                + "2. Add Savings\n"
                + "3. Deduct Savings\n"
                + "4. View current Savings\n"
                + "5. Back to Main Menu\n"
                + "Choose an option:\n");
        currentContext = "SavingsMenu";
    }

    private void resetToMainMenu() {
        chatArea.append("Bot: Returning to main menu.\n" + getMainMenu());
        currentContext = "MainMenu";
        // Reset state variables to allow a new search or operation
        productName = null;
        selectedCategory = null;
    }

    private String getMainMenu() {
        String botResponse = "Here's the main menu:\n"
                + "1. Add Expense\n"
                + "2. View or Edit Expenses\n"
                + "3. Savings\n"
                + "4. Track Deals\n"
                + "5. Set Budget\n"
                + "6. Add Reminder\n"
                + "7. Support/Help/FAQ\n"
                + "8. View Reminders\n";
        return botResponse;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ModernDashboard::new);

        // Example usage
        String reminderDate = "2023-10-01";
        String reminderTime = "10:00:00";
        String message = "Doctor's appointment";

        ReminderManager reminderManager = new ReminderManager(); // Create an instance
        reminderManager.addReminder(message, reminderDate, reminderTime, "testUser"); // Call on the instance
    }
}
