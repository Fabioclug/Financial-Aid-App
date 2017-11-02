package br.com.fclug.financialaid.models;

import java.io.Serializable;

/**
 * Created by Fabioclug on 2016-10-17.
 */

public class TransactionSplit implements Serializable {

    private User debtor;
    private double value;

    public TransactionSplit(User debtor, double value) {
        this.debtor = debtor;
        this.value = value;
    }

    public User getDebtor() {
        return debtor;
    }

    public void setDebtor(User debtor) {
        this.debtor = debtor;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
