package br.com.fclug.financialaid.models;

/**
 * Created by Fabioclug on 2016-11-19.
 */

public class MemberDebt {

    private String member;
    private double value;

    public MemberDebt(String member) {
        this.member = member;
    }

    public MemberDebt(String member, double value) {
        this.member = member;
        this.value = value;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
