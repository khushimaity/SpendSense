# SpendSense – Finance Management Chatbot

SpendSense is a personal finance management application that helps users track expenses, manage reminders, and analyze spending patterns.  
The system combines a desktop GUI with a chatbot interface to make financial tracking simple and interactive.

---

## Features

- User registration and login system
- Expense tracking and management
- Reminder system for financial tasks
- Spending analysis and dashboard
- Chatbot interface for interacting with the system
- User settings and profile management
- Secure database connectivity

---

## Tech Stack

### Programming Language
- Java

### GUI
- Java Swing

### Database
- MySQL

### Build Tool
- Maven

### Libraries
- JDBC
- JFreeChart (for financial charts)

---

## Project Structure

```
SpendSense
│
├── pom.xml
├── src
│   └── main
│       └── java
│           ├── Chat_Bot
│           │   ├── ChatBotApp.java
│           │   ├── ExpenseManager.java
│           │   ├── ReminderManager.java
│           │   └── UserSettingsPanel.java
│           │
│           └── Connect
│               ├── Connect.java
│               └── DatabaseInitializer.java
```

---

## How to Run

### 1 Install Dependencies

Make sure you have installed:

- Java (JDK)
- Maven
- MySQL

### 2 Configure Database

Create a MySQL database and update the connection details in:

```
DatabaseHelper.java
```

### 3 Run the Application

Using Maven:

```
mvn clean install
mvn exec:java
```

Or run the main class:

```
ChatBotApp.java
```

---

## Future Improvements

- AI-powered financial insights
- Automatic expense categorization
- Mobile application integration
- Real-time financial notifications

---

## Author

Khushi Maity  
B.Tech Computer Science and Engineering  
TKM College of Engineering  

GitHub: https://github.com/khushimaity
