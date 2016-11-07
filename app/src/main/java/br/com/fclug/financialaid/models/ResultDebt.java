package br.com.fclug.financialaid.models;

/**
 * Created by Fabioclug on 2016-10-17.
 */

public class ResultDebt {

    private User creditor;
    private User debtor;
    private double value;

    public ResultDebt(User creditor, User debtor, double value) {
        this.creditor = creditor;
        this.debtor = debtor;
        this.value = value;
    }

    public User getCreditor() {
        return creditor;
    }

    public User getDebtor() {
        return debtor;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
