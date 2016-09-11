package br.com.fclug.financialaid.models;

import java.util.Date;

/**
 * Created by Fabioclug on 2016-06-09.
 */
public class Transaction {
    private long id;
    private boolean credit;
    private String description;
    private double value;
    private String category;
    private Date date;
    private long accountId;

    public Transaction(long id, boolean credit, String description, double value, String category, Date date, long accountId) {
        this.id = id;
        this.credit = credit;
        this.description = description;
        this.value = value;
        this.category = category;
        this.date = date;
        this.accountId = accountId;
    }

    public Transaction(boolean credit, String description, double value, String category, Date date, long accountId) {
        this.credit = credit;
        this.description = description;
        this.value = value;
        this.category = category;
        this.date = date;
        this.accountId = accountId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isCredit() {
        return credit;
    }

    public void setCredit(boolean credit) {
        this.credit = credit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }
}
