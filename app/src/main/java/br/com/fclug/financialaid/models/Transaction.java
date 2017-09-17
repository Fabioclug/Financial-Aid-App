package br.com.fclug.financialaid.models;

import java.util.Date;

import br.com.fclug.financialaid.utils.AppUtils;

/**
 * Created by Fabioclug on 2016-06-09.
 */
public class Transaction {
    private long id;
    private boolean credit;
    private String description;
    private double value;
    private Category category;
    private Date date;
    private long accountId;

    public Transaction() {

    }

    public Transaction(long id, boolean credit, String description, double value, Category category, Date date, long accountId) {
        this.id = id;
        this.credit = credit;
        this.description = description;
        this.value = value;
        this.category = category;
        this.date = date;
        this.accountId = accountId;
    }

    public Transaction(boolean credit, String description, double value, Category category, Date date, long accountId) {
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

    public String getFormattedValue() {
        return AppUtils.formatValue(value);
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
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

    public double getSignedValue() {
        if(isCredit()) {
            return value;
        } else {
            return value * -1;
        }
    }
}
