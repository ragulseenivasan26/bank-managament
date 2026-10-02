package com.smartbank;

public class SavingsAccount extends Account {
    public SavingsAccount(int no, String name, double balance) {
        super(no, name, balance);
    }

    @Override
    public void withdraw(double amount) throws InsufficientBalanceException {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (amount > getBalance())
            throw new InsufficientBalanceException("Insufficient balance.");
        setBalance(getBalance() - amount);
    }
}
