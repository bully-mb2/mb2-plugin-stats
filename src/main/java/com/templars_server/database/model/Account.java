package com.templars_server.database.model;

public class Account {

    private final String id;
    private int balance;
    private boolean save;

    public Account(String id, int balance) {
        this.id = id;
        this.balance = balance;
        this.save = false;
    }

    public String getId() {
        return id;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.save = true;
        this.balance = balance;
    }

    public void addBalance(int balance) {
        this.save = true;
        this.balance += balance;
    }

    public boolean shouldSave() {
        return save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", balance=" + balance +
                '}';
    }

}
