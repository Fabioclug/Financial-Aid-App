package br.com.fclug.financialaid.models;

import java.io.Serializable;

/**
 * Created by Fabioclug on 2016-09-06.
 */
public class User implements Serializable {

    private String username;
    private String name;

    public User(String username, String name) {
        this.username = username;
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return username.equals(user.username) && name.equals(user.name);

    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
