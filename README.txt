SMARTBANK - BANK MANAGEMENT SYSTEM
=====================================

Comprehensive educational project covering Core Java + OOPs + JavaFX + JDBC + MySQL + Web Localhost.

TECHNOLOGIES
------------
- Java 17 / 21
- JavaFX 21 (Desktop GUI)
- JDBC + MySQL 8.0 (Database Persistence)
- Maven (Dependency Management & Build)
- Java HTTP REST Server (Localhost Web Application: http://localhost:8080)
- HTML5 / CSS3 / Vanilla JS (Modern Glassmorphic Web Dashboard)

PROJECT STRUCTURE
-----------------
src/main/java/com/smartbank/
  ├── Account.java                      -> Abstract base class (Encapsulation + Abstraction + Method Overloading)
  ├── Transactable.java                 -> Interface contract (Abstraction)
  ├── SavingsAccount.java               -> Subclass with zero-balance rule (Inheritance + Polymorphism)
  ├── CurrentAccount.java               -> Subclass with Rs.10,000 overdraft limit (Inheritance + Polymorphism)
  ├── InsufficientBalanceException.java -> Custom checked exception (Exception Handling)
  ├── DBConnection.java                 -> JDBC MySQL connection helper
  ├── BankApp.java                      -> JavaFX Desktop GUI Application
  ├── Main.java                         -> Direct launcher class (prevents JavaFX runtime missing error in IDEs)
  └── WebServer.java                    -> Java HTTP Server serving REST API & Web Dashboard

database/
  ├── schema.sql                        -> Complete schema (database, tables, sample accounts & transactions)
  └── test_queries.sql                  -> Verification queries

src/main/resources/web/
  └── index.html                        -> Ultra-modern glassmorphic web dashboard (for browser demo)

BATCH RUNNERS
-------------
- run.bat                               -> 1-Click launcher for JavaFX Desktop GUI
- run_localhost.bat                     -> 1-Click launcher for Localhost Web App (http://localhost:8080)

SAMPLE ACCOUNTS LOADED IN MYSQL
-------------------------------
100001 - Ragul S      - Savings - Rs. 25,000.00
100002 - Arun Kumar   - Current - Rs. 50,000.00 (Overdraft limit: Rs. 10,000)
100003 - Priya S      - Savings - Rs. 15,000.00
100004 - Karthik M    - Current - Rs. 32,500.00 (Overdraft limit: Rs. 10,000)

SAMPLE LOGIN
------------
Username: admin
Password: admin123

DATABASE SETUP INSTRUCTIONS
---------------------------
1. Start MySQL Server (Default port 3306).
2. Execute `database/schema.sql` in MySQL Workbench or mysql CLI:
   mysql -u root -p < database/schema.sql
3. Verify password in `src/main/java/com/smartbank/DBConnection.java`:
   private static final String PASSWORD = "your_mysql_password";

HOW TO RUN
----------
Method 1: Localhost Web Application (Recommended for quick browser demos)
  Double-click: run_localhost.bat
  Or open your browser at: http://localhost:8080

Method 2: Desktop JavaFX Application
  Double-click: run.bat
  Or run in IntelliJ IDEA: Right click Main.java -> Run 'Main.main()'

Method 3: Via Maven in Command Prompt
  mvn compile
  mvn javafx:run

OOP CONCEPTS DEMONSTRATED IN CODE
---------------------------------
1. Encapsulation:
   `balance` is private in `Account.java`, modified only via deposit/withdraw logic.
2. Inheritance:
   `SavingsAccount` and `CurrentAccount` inherit fields and methods from abstract `Account`.
3. Polymorphism:
   - Runtime Polymorphism (Method Overriding): `withdraw()` behaves differently for Savings vs Current (with overdraft limit).
   - Compile-time Polymorphism (Method Overloading): `deposit(double amount)` and `deposit(double amount, String note)` in `Account.java`.
4. Abstraction:
   `Account` is an abstract class with abstract `withdraw()`. `Transactable` is an interface.
5. Exception Handling:
   Custom `InsufficientBalanceException` thrown when withdrawal exceeds permissible limit.
