package br.com.fclug.financialaid.models;

/**
 * Created by Fabioclug on 2017-10-21.
 */

public class OnlineUser extends User {

    private String name;

    public OnlineUser(String username, String name) {
        super(username);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getExhibitName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        OnlineUser that = (OnlineUser) o;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
