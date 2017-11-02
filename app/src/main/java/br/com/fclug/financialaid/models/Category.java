package br.com.fclug.financialaid.models;

/**
 * Created by Fabioclug on 2017-01-08.
 */

public class Category {

    private String name;
    private int color;
    private boolean incoming;

    public Category(String name, int color, boolean incoming) {
        this.name = name;
        this.color = color;
        this.incoming = incoming;
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

    public boolean isIncoming() {
        return incoming;
    }

    public void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }

    @Override
    public String toString() {
        return name;
    }
}
