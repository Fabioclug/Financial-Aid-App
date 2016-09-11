package br.com.fclug.financialaid.models;

import android.support.annotation.Nullable;

/**
 * Created by Fabioclug on 2016-06-25.
 */
public class Account {
    private long id;
    private String name;
    private double balance;
    private String type;

    public Account(long id, String name, double balance, String type) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.type = type;
    }

    public Account(String name, double balance, String type) {
        this.name = name;
        this.balance = balance;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        return id == account.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
