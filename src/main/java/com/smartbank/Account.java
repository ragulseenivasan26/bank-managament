package com.smartbank;

public abstract class Account implements Transactable {
    private final int accountNo;
    private final String customerName;
    private double balance;

    public Account(int accountNo, String customerName, double balance) {
        this.accountNo = accountNo;
        this.customerName = customerName;
        this.balance = balance;
    }

    public int getAccountNo() { return accountNo; }
    public String getCustomerName() { return customerName; }
    public double getBalance() { return balance; }

    protected void setBalance(double balance) { this.balance = balance; }

    @Override
    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        balance += amount;
    }

    // Method overloading (Polymorphism)
    public void deposit(double amount, String note) {
        deposit(amount);
    }

    public abstract void withdraw(double amount) throws InsufficientBalanceException;
}
