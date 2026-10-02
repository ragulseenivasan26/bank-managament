package com.smartbank;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;

public class BankApp extends Application {
    private TableView<String[]> table = new TableView<>();
    private TextArea output = new TextArea();

    @Override
    public void start(Stage stage) {
        stage.setTitle("SmartBank - Bank Management System");

        Label title = new Label("SMARTBANK - Bank Management System");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField acc = new TextField();
        acc.setPromptText("Account No");
        TextField amount = new TextField();
        amount.setPromptText("Amount");

        Button view = new Button("View Account");
        Button deposit = new Button("Deposit");
        Button withdraw = new Button("Withdraw");
        Button all = new Button("Show All Accounts");

        view.setOnAction(e -> viewAccount(acc.getText()));
        deposit.setOnAction(e -> transaction(acc.getText(), amount.getText(), true));
        withdraw.setOnAction(e -> transaction(acc.getText(), amount.getText(), false));
        all.setOnAction(e -> showAll());

        HBox controls = new HBox(10, acc, amount, view, deposit, withdraw, all);
        controls.setPadding(new Insets(10));

        output.setEditable(false);
        output.setPrefRowCount(18);

        VBox root = new VBox(15, title, controls, output);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 1050, 550));
        stage.centerOnScreen();
        stage.setAlwaysOnTop(true);
        stage.show();
        stage.toFront();
        stage.requestFocus();
        showAll();
    }

    private void viewAccount(String s) {
        try {
            int no = Integer.parseInt(s);
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement p = c.prepareStatement(
                     "SELECT * FROM accounts WHERE account_no=?")) {
                p.setInt(1, no);
                ResultSet r = p.executeQuery();
                if (r.next()) {
                    output.setText("Account No : " + r.getInt("account_no") +
                        "\nName       : " + r.getString("customer_name") +
                        "\nType       : " + r.getString("account_type") +
                        "\nPhone      : " + r.getString("phone") +
                        "\nBalance    : ₹" + r.getDouble("balance"));
                } else output.setText("Account not found.");
            }
        } catch (Exception ex) {
            output.setText("Error: " + ex.getMessage());
        }
    }

    private void transaction(String a, String amt, boolean deposit) {
        try {
            int no = Integer.parseInt(a);
            double amount = Double.parseDouble(amt);
            if (amount <= 0) throw new IllegalArgumentException("Enter a positive amount.");

            try (Connection c = DBConnection.getConnection()) {
                c.setAutoCommit(false);

                // Fetch account to instantiate domain model
                String selectQuery = "SELECT customer_name, account_type, balance FROM accounts WHERE account_no = ?";
                Account account = null;
                try (PreparedStatement sp = c.prepareStatement(selectQuery)) {
                    sp.setInt(1, no);
                    try (ResultSet rs = sp.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Account not found.");
                        String name = rs.getString("customer_name");
                        String type = rs.getString("account_type");
                        double bal = rs.getDouble("balance");

                        // Polymorphism: instantiate appropriate Account subclass
                        if ("Savings".equalsIgnoreCase(type)) {
                            account = new SavingsAccount(no, name, bal);
                        } else {
                            account = new CurrentAccount(no, name, bal);
                        }
                    }
                }

                // Apply OOP domain methods (Abstraction, Polymorphism & Exception handling)
                if (deposit) {
                    account.deposit(amount);
                } else {
                    account.withdraw(amount); // Throws InsufficientBalanceException if rule violated
                }

                // Persist updated balance
                String updateQuery = "UPDATE accounts SET balance = ? WHERE account_no = ?";
                try (PreparedStatement up = c.prepareStatement(updateQuery)) {
                    up.setDouble(1, account.getBalance());
                    up.setInt(2, no);
                    up.executeUpdate();
                }

                try (PreparedStatement p = c.prepareStatement(
                    "INSERT INTO transactions(account_no,transaction_type,amount) VALUES(?,?,?)")) {
                    p.setInt(1, no); p.setString(2, deposit ? "DEPOSIT" : "WITHDRAW");
                    p.setDouble(3, amount); p.executeUpdate();
                }
                c.commit();
                output.setText((deposit ? "Deposit" : "Withdrawal") +
                    " successful for account " + no + " (" + account.getCustomerName() + ")\n" +
                    "Amount: ₹" + amount + "\nNew Balance: ₹" + account.getBalance());
            }
            showAll();
        } catch (InsufficientBalanceException ex) {
            output.setText("Transaction failed (Insufficient Balance): " + ex.getMessage());
        } catch (Exception ex) {
            output.setText("Transaction failed: " + ex.getMessage());
        }
    }

    private void showAll() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-12s %-20s %-12s %-15s %-12s%n",
            "Account No", "Customer", "Type", "Phone", "Balance"));
        sb.append("-".repeat(75)).append("\n");
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT * FROM accounts ORDER BY account_no")) {
            while (r.next()) {
                sb.append(String.format("%-12d %-20s %-12s %-15s ₹%-10.2f%n",
                    r.getInt("account_no"), r.getString("customer_name"),
                    r.getString("account_type"), r.getString("phone"),
                    r.getDouble("balance")));
            }
            output.setText(sb.toString());
        } catch (Exception ex) {
            output.setText("Database connection error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
