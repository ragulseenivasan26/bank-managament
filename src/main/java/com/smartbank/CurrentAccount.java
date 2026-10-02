package com.smartbank;

public class CurrentAccount extends Account {
    private static final double OVERDRAFT_LIMIT = 10000.0;

    public CurrentAccount(int no, String name, double balance) {
        super(no, name, balance);
    }

    @Override
    public void withdraw(double amount) throws InsufficientBalanceException {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (amount > getBalance() + OVERDRAFT_LIMIT)
            throw new InsufficientBalanceException("Overdraft limit exceeded.");
        setBalance(getBalance() - amount);
    }
}
