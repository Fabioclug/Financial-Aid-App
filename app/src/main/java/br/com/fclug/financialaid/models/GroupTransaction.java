package br.com.fclug.financialaid.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fabioclug on 2016-10-17.
 */

public class GroupTransaction {

    private long id;
    private String description;
    private User payer;
    private double value;
    private List<TransactionSplit> splits;

    public GroupTransaction(long id, String description, User payer, double value) {
        this.id = id;
        this.description = description;
        this.payer = payer;
        this.value = value;
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
}
