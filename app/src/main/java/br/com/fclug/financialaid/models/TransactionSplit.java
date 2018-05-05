package br.com.fclug.financialaid.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Fabioclug on 2016-10-17.
 */

public class TransactionSplit implements Parcelable {

    private User debtor;
    private double value;

    public TransactionSplit(User debtor, double value) {
        this.debtor = debtor;
        this.value = value;
    }

    protected TransactionSplit(Parcel in) {
        debtor = in.readParcelable(User.class.getClassLoader());
        value = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(debtor, flags);
        dest.writeDouble(value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TransactionSplit> CREATOR = new Creator<TransactionSplit>() {
        @Override
        public TransactionSplit createFromParcel(Parcel in) {
            return new TransactionSplit(in);
        }

        @Override
        public TransactionSplit[] newArray(int size) {
            return new TransactionSplit[size];
        }
    };

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
