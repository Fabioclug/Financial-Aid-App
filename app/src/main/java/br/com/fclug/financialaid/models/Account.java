package br.com.fclug.financialaid.models;

import android.support.annotation.Nullable;

import br.com.fclug.financialaid.utils.AppUtils;

/**
 * Created by Fabioclug on 2016-06-25.
 */
public class Account {
    private long id;
    private String name;
    private double balance;
    private String type;

    private Account(AccountBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.balance = builder.balance;
        this.type = builder.type;
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

    public String getFormattedBalance() {
        return AppUtils.formatValue(balance);
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


    public static class AccountBuilder {
        private long id;
        private String name;
        private double balance;
        private String type;

        public AccountBuilder setId(long id) {
            this.id = id;
            return this;
        }

        public AccountBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public AccountBuilder setBalance(double balance) {
            this.balance = balance;
            return this;
        }

        public AccountBuilder setType(String type) {
            this.type = type;
            return this;
        }

        public Account build() {
            return new Account(this);
        }
    }
}
