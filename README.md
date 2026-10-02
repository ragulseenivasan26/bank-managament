# 🏦 SmartBank — Bank Management System

An educational **Bank Management System** developed in Java, covering core Object-Oriented Programming (OOP) concepts, JDBC MySQL database persistence, a JavaFX Desktop GUI, and a modern Localhost Web Dashboard.

---

## 🏗️ Technology Stack

| Layer | Technology | Purpose |
| :--- | :--- | :--- |
| **Logic** | Java 17 / 21 (OOPs) | Core business logic, balance validation & rules |
| **Desktop UI** | JavaFX 21 | Native interactive desktop application |
| **Web UI** | HTML5 / CSS3 / Vanilla JS | Modern glassmorphic browser dashboard (`http://localhost:8080`) |
| **Database** | JDBC + MySQL 8.0 | Permanent storage for accounts & transaction ledger |
| **Build Tool** | Apache Maven | Dependency management and build lifecycle |

---

## 📚 OOPs Concepts Demonstrated

1. **Encapsulation**:
   - The `balance` field in `Account.java` is kept `private` and only modified through validated mutator methods (`deposit()`, `withdraw()`).
2. **Inheritance**:
   - `SavingsAccount` and `CurrentAccount` extend the abstract `Account` base class.
3. **Polymorphism**:
   - **Runtime Polymorphism (Method Overriding)**: `withdraw()` behaves differently:
     - `SavingsAccount`: Zero minimum balance, prevents overdrafts.
     - `CurrentAccount`: Permits up to ₹10,000 overdraft limit.
   - **Compile-time Polymorphism (Method Overloading)**: `deposit(double amount)` and `deposit(double amount, String note)`.
4. **Abstraction & Interfaces**:
   - `Account` is an abstract base class.
   - `Transactable` interface defines contract for all financial accounts.
5. **Exception Handling**:
   - Custom checked `InsufficientBalanceException` thrown whenever a withdrawal violates business rules.

---

## 📁 Project Structure

```
Bank_Management_System/
├── src/main/java/com/smartbank/
│   ├── Account.java                      # Abstract base class
│   ├── Transactable.java                 # Transaction interface
│   ├── SavingsAccount.java               # Savings logic (no overdraft)
│   ├── CurrentAccount.java               # Current logic (₹10,000 overdraft)
│   ├── InsufficientBalanceException.java # Custom checked exception
│   ├── DBConnection.java                 # JDBC MySQL connection manager
│   ├── BankApp.java                      # JavaFX GUI Application
│   ├── Main.java                         # Direct launcher (prevents JavaFX runtime missing error)
│   └── WebServer.java                    # Native Java HTTP Server (REST API + Web UI)
├── src/main/resources/web/
│   └── index.html                        # Glassmorphic web dashboard
├── database/
│   ├── schema.sql                        # MySQL schema & sample records
│   └── test_queries.sql                  # Verification queries
├── run.bat                               # 1-Click launcher for JavaFX Desktop GUI
├── run_localhost.bat                     # 1-Click launcher for Localhost Web App
├── pom.xml                               # Maven build configuration
└── README.md                             # Documentation
```

---

## 🚀 Quick Start Guide

### 1. Database Setup
1. Open MySQL Workbench or command line.
2. Execute the schema script:
   ```sql
   source database/schema.sql;
   ```
3. Set your MySQL root password in `src/main/java/com/smartbank/DBConnection.java`:
   ```java
   private static final String PASSWORD = "your_mysql_password";
   ```

### 2. Running the Application

#### Option A: Localhost Web Dashboard (Browser)
Double-click **`run_localhost.bat`** or run:
```cmd
java -cp "target/classes;..." com.smartbank.WebServer
```
Open **`http://localhost:8080`** in Chrome / Edge.

#### Option B: Desktop JavaFX Application
Double-click **`run.bat`** or run via Maven:
```cmd
mvn javafx:run
```

---

## 📊 Sample Preloaded Accounts

| Account No | Customer Name | Account Type | Initial Balance | Overdraft Limit |
| :---: | :--- | :---: | :---: | :---: |
| **100001** | Ragul S | Savings | ₹25,000.00 | ₹0.00 |
| **100002** | Arun Kumar | Current | ₹50,000.00 | ₹10,000.00 |
| **100003** | Priya S | Savings | ₹15,000.00 | ₹0.00 |
| **100004** | Karthik M | Current | ₹32,500.00 | ₹10,000.00 |

---

## 👨‍💻 Author
- **Ragul Seenivasan** ([@ragulseenivasan26](https://github.com/ragulseenivasan26))
