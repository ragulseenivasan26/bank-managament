package com.smartbank;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class WebServer {
    private static final int PORT = 8080;
    private static final String WEB_DIR = "src/main/resources/web";

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // Static Web Page
            server.createContext("/", new StaticFileHandler());

            // API Endpoints
            server.createContext("/api/accounts", new AccountsHandler());
            server.createContext("/api/transactions", new TransactionsHandler());
            server.createContext("/api/deposit", new DepositHandler());
            server.createContext("/api/withdraw", new WithdrawHandler());
            server.createContext("/api/stats", new StatsHandler());

            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();

            System.out.println("=================================================");
            System.out.println("  SmartBank Localhost Server is RUNNING!");
            System.out.println("  URL: http://localhost:" + PORT);
            System.out.println("=================================================");

            // Open user's default browser automatically
            openBrowser("http://localhost:" + PORT);

        } catch (Exception e) {
            System.err.println("Failed to start WebServer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void openBrowser(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new URI(url));
            } else {
                new ProcessBuilder("cmd.exe", "/c", "start", url).start();
            }
        } catch (Exception ignored) {
            try {
                new ProcessBuilder("cmd.exe", "/c", "start", url).start();
            } catch (Exception e) {
                System.out.println("Please open your browser and navigate to: " + url);
            }
        }
    }

    // Static HTML Handler
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            Path filePath = Paths.get(WEB_DIR + path);
            if (!Files.exists(filePath)) filePath = Paths.get("web" + path);
            if (!Files.exists(filePath)) filePath = Paths.get("Bank_Management_System_Sample/web" + path);
            if (!Files.exists(filePath)) filePath = Paths.get("Bank_Management_System_Sample/src/main/resources/web" + path);
            if (!Files.exists(filePath)) filePath = Paths.get("." + path);

            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                byte[] bytes = Files.readAllBytes(filePath);
                String contentType = getContentType(path);
                exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String notFound = "404 Not Found: " + path;
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes());
                }
            }
        }

        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".svg")) return "image/svg+xml";
            return "text/plain";
        }
    }

    // Get all accounts / Create Account
    static class AccountsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod().toUpperCase();

            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equals(method)) {
                try (Connection c = DBConnection.getConnection();
                     Statement s = c.createStatement();
                     ResultSet r = s.executeQuery("SELECT * FROM accounts ORDER BY account_no")) {

                    StringBuilder json = new StringBuilder("[");
                    boolean first = true;
                    while (r.next()) {
                        if (!first) json.append(",");
                        json.append(String.format(Locale.US,
                            "{\"accountNo\":%d,\"customerName\":\"%s\",\"accountType\":\"%s\",\"phone\":\"%s\",\"balance\":%.2f}",
                            r.getInt("account_no"),
                            escapeJson(r.getString("customer_name")),
                            escapeJson(r.getString("account_type")),
                            escapeJson(r.getString("phone")),
                            r.getDouble("balance")));
                        first = false;
                    }
                    json.append("]");
                    sendJsonResponse(exchange, 200, json.toString());
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            } else if ("POST".equals(method)) {
                // Create new account
                try {
                    String body = readBody(exchange);
                    Map<String, String> data = parseSimpleJson(body);

                    int accountNo = Integer.parseInt(data.get("accountNo"));
                    String name = data.get("customerName");
                    String type = data.get("accountType");
                    String phone = data.get("phone");
                    double initialBalance = Double.parseDouble(data.get("balance"));

                    try (Connection c = DBConnection.getConnection()) {
                        c.setAutoCommit(false);
                        String insert = "INSERT INTO accounts(account_no, customer_name, account_type, phone, balance) VALUES(?,?,?,?,?)";
                        try (PreparedStatement p = c.prepareStatement(insert)) {
                            p.setInt(1, accountNo);
                            p.setString(2, name);
                            p.setString(3, type);
                            p.setString(4, phone);
                            p.setDouble(5, initialBalance);
                            p.executeUpdate();
                        }
                        if (initialBalance > 0) {
                            String tx = "INSERT INTO transactions(account_no, transaction_type, amount) VALUES(?, 'DEPOSIT', ?)";
                            try (PreparedStatement tp = c.prepareStatement(tx)) {
                                tp.setInt(1, accountNo);
                                tp.setDouble(2, initialBalance);
                                tp.executeUpdate();
                            }
                        }
                        c.commit();
                    }
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Account opened successfully!\"}");
                } catch (Exception e) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                }
            }
        }
    }

    // Get transactions
    static class TransactionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try (Connection c = DBConnection.getConnection();
                 Statement s = c.createStatement();
                 ResultSet r = s.executeQuery(
                     "SELECT t.id, t.account_no, a.customer_name, t.transaction_type, t.amount, t.transaction_time " +
                     "FROM transactions t LEFT JOIN accounts a ON t.account_no = a.account_no " +
                     "ORDER BY t.transaction_time DESC, t.id DESC LIMIT 40")) {

                StringBuilder json = new StringBuilder("[");
                boolean first = true;
                while (r.next()) {
                    if (!first) json.append(",");
                    json.append(String.format(Locale.US,
                        "{\"id\":%d,\"accountNo\":%d,\"customerName\":\"%s\",\"type\":\"%s\",\"amount\":%.2f,\"time\":\"%s\"}",
                        r.getInt("id"),
                        r.getInt("account_no"),
                        escapeJson(r.getString("customer_name") != null ? r.getString("customer_name") : "N/A"),
                        r.getString("transaction_type"),
                        r.getDouble("amount"),
                        r.getTimestamp("transaction_time")));
                    first = false;
                }
                json.append("]");
                sendJsonResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // Deposit handler with OOP
    static class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            try {
                String body = readBody(exchange);
                Map<String, String> data = parseSimpleJson(body);
                int accNo = Integer.parseInt(data.get("accountNo"));
                double amount = Double.parseDouble(data.get("amount"));

                if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");

                Account account = null;
                try (Connection c = DBConnection.getConnection()) {
                    c.setAutoCommit(false);

                    String sel = "SELECT customer_name, account_type, balance FROM accounts WHERE account_no = ?";
                    try (PreparedStatement p = c.prepareStatement(sel)) {
                        p.setInt(1, accNo);
                        try (ResultSet rs = p.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Account not found: " + accNo);
                            String name = rs.getString("customer_name");
                            String type = rs.getString("account_type");
                            double bal = rs.getDouble("balance");

                            if ("Savings".equalsIgnoreCase(type)) {
                                account = new SavingsAccount(accNo, name, bal);
                            } else {
                                account = new CurrentAccount(accNo, name, bal);
                            }
                        }
                    }

                    // OOP Polymorphic deposit
                    account.deposit(amount);

                    // Update balance
                    try (PreparedStatement up = c.prepareStatement("UPDATE accounts SET balance=? WHERE account_no=?")) {
                        up.setDouble(1, account.getBalance());
                        up.setInt(2, accNo);
                        up.executeUpdate();
                    }

                    // Log transaction
                    try (PreparedStatement tp = c.prepareStatement(
                        "INSERT INTO transactions(account_no, transaction_type, amount) VALUES(?, 'DEPOSIT', ?)")) {
                        tp.setInt(1, accNo);
                        tp.setDouble(2, amount);
                        tp.executeUpdate();
                    }

                    c.commit();
                }

                String resp = String.format(Locale.US,
                    "{\"success\":true,\"message\":\"Successfully deposited ₹%.2f\",\"newBalance\":%.2f,\"accountNo\":%d,\"customerName\":\"%s\"}",
                    amount, account.getBalance(), accNo, escapeJson(account.getCustomerName()));
                sendJsonResponse(exchange, 200, resp);
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // Withdraw handler with OOP Exception Handling
    static class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            try {
                String body = readBody(exchange);
                Map<String, String> data = parseSimpleJson(body);
                int accNo = Integer.parseInt(data.get("accountNo"));
                double amount = Double.parseDouble(data.get("amount"));

                if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");

                Account account = null;
                try (Connection c = DBConnection.getConnection()) {
                    c.setAutoCommit(false);

                    String sel = "SELECT customer_name, account_type, balance FROM accounts WHERE account_no = ?";
                    try (PreparedStatement p = c.prepareStatement(sel)) {
                        p.setInt(1, accNo);
                        try (ResultSet rs = p.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Account not found: " + accNo);
                            String name = rs.getString("customer_name");
                            String type = rs.getString("account_type");
                            double bal = rs.getDouble("balance");

                            if ("Savings".equalsIgnoreCase(type)) {
                                account = new SavingsAccount(accNo, name, bal);
                            } else {
                                account = new CurrentAccount(accNo, name, bal);
                            }
                        }
                    }

                    // OOP Polymorphic withdrawal - throws InsufficientBalanceException if rule violated!
                    account.withdraw(amount);

                    // Update balance
                    try (PreparedStatement up = c.prepareStatement("UPDATE accounts SET balance=? WHERE account_no=?")) {
                        up.setDouble(1, account.getBalance());
                        up.setInt(2, accNo);
                        up.executeUpdate();
                    }

                    // Log transaction
                    try (PreparedStatement tp = c.prepareStatement(
                        "INSERT INTO transactions(account_no, transaction_type, amount) VALUES(?, 'WITHDRAW', ?)")) {
                        tp.setInt(1, accNo);
                        tp.setDouble(2, amount);
                        tp.executeUpdate();
                    }

                    c.commit();
                }

                String resp = String.format(Locale.US,
                    "{\"success\":true,\"message\":\"Successfully withdrawn ₹%.2f\",\"newBalance\":%.2f,\"accountNo\":%d,\"customerName\":\"%s\"}",
                    amount, account.getBalance(), accNo, escapeJson(account.getCustomerName()));
                sendJsonResponse(exchange, 200, resp);
            } catch (InsufficientBalanceException ibe) {
                // Catch custom OOP Exception
                sendJsonResponse(exchange, 400,
                    "{\"success\":false,\"isInsufficientBalance\":true,\"error\":\"InsufficientBalanceException: " +
                    escapeJson(ibe.getMessage()) + "\"}");
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // Stats handler
    static class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try (Connection c = DBConnection.getConnection();
                 Statement s = c.createStatement()) {

                double totalBal = 0;
                int totalAccs = 0;
                int savingsCount = 0;
                int currentCount = 0;

                try (ResultSet r = s.executeQuery("SELECT account_type, balance FROM accounts")) {
                    while (r.next()) {
                        totalAccs++;
                        double b = r.getDouble("balance");
                        totalBal += b;
                        if ("Savings".equalsIgnoreCase(r.getString("account_type"))) savingsCount++;
                        else currentCount++;
                    }
                }

                int totalTx = 0;
                try (ResultSet r = s.executeQuery("SELECT count(*) FROM transactions")) {
                    if (r.next()) totalTx = r.getInt(1);
                }

                String resp = String.format(Locale.US,
                    "{\"totalBalance\":%.2f,\"totalAccounts\":%d,\"savingsAccounts\":%d,\"currentAccounts\":%d,\"totalTransactions\":%d,\"dbStatus\":\"Connected (MySQL 8.0)\"}",
                    totalBal, totalAccs, savingsCount, currentCount, totalTx);
                sendJsonResponse(exchange, 200, resp);
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    // Helper functions
    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);

        String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String val = kv[1].trim().replace("\"", "");
                map.put(key, val);
            }
        }
        return map;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}
