package com.smartbank;

public interface Transactable {
    void deposit(double amount);
    void withdraw(double amount) throws InsufficientBalanceException;
}
