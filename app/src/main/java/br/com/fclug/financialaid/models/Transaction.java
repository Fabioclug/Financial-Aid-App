package br.com.fclug.financialaid.models;

import java.util.Date;

import br.com.fclug.financialaid.utils.AppUtils;

/**
 * Created by Fabioclug on 2016-06-09.
 */
public class Transaction implements UniqueObject {
    private long id;
    private String description;
    private long value;
    private Category category;
    private Date date;
    private long accountId;

    public Transaction() {

    }

    public Transaction(long id, String description, long value, Category category, Date date, long accountId) {
        this.id = id;
        this.description = description;
        this.value = value;
        this.category = category;
        this.date = date;
        this.accountId = accountId;
    }

    public Transaction(String description, long value, Category category, Date date, long accountId) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getValue() {
        return value;
    }

    public String getFormattedValue() {
        return AppUtils.formatCurrencyValue(value);
    }

    public void setValue(long value) {
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

}
