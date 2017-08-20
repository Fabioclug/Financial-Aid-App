package br.com.fclug.financialaid.models;

/**
 * Created by Fabioclug on 2017-01-08.
 */

public class Category {

    private String name;
    private int color;

    public Category(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return name;
    }
}
