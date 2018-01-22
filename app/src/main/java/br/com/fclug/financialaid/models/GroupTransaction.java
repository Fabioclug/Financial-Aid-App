package br.com.fclug.financialaid.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Fabioclug on 2016-10-17.
 */

public class GroupTransaction implements UniqueObject {

    private long id;
    private String description;
    private User payer;
    private double value;
    private Date date;
    private List<TransactionSplit> splits;

    private GroupTransaction(GroupTransactionBuilder builder) {
        this.id = builder.id;
        this.description = builder.description;
        this.payer = builder.payer;
        this.value = builder.value;
        this.date = builder.date;
        this.splits = builder.splits;
    }

    public GroupTransaction(String description, User payer, double value, Date date, List<TransactionSplit> splits) {
        this.description = description;
        this.payer = payer;
        this.value = value;
        this.date = date;
        this.splits = splits;
    }

    public GroupTransaction(long id, String description, User payer, double value, Date date) {
        this.id = id;
        this.description = description;
        this.payer = payer;
        this.value = value;
        this.date = date;
        splits = new ArrayList<>();
    }

    public GroupTransaction(long id, String description, User payer, double value, List<TransactionSplit> splits) {
        this.id = id;
        this.description = description;
        this.payer = payer;
        this.value = value;
        this.splits = splits;
    }

    public List<TransactionSplit> getSplits() {
        return splits;
    }

    public void setSplits(List<TransactionSplit> splits) {
        this.splits = splits;
    }

    public void addSplit(TransactionSplit split) {
        splits.add(split);
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public User getPayer() {
        return payer;
    }

    public double getValue() {
        return value;
    }

    public Date getDate() {
        return date;
    }

    public static class GroupTransactionBuilder {
        private String description;
        private User payer;
        private double value;
        private Date date;
        private List<TransactionSplit> splits;
        private long id;

        public GroupTransactionBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public GroupTransactionBuilder setPayer(User payer) {
            this.payer = payer;
            return this;
        }

        public GroupTransactionBuilder setValue(double value) {
            this.value = value;
            return this;
        }

        public GroupTransactionBuilder setDate(Date date) {
            this.date = date;
            return this;
        }

        public GroupTransactionBuilder setSplits(List<TransactionSplit> splits) {
            this.splits = splits;
            return this;
        }

        public GroupTransactionBuilder setId(long id) {
            this.id = id;
            return this;
        }

        public GroupTransaction build() {
            return new GroupTransaction(this);
        }
    }
}
