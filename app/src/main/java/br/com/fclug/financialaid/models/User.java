package br.com.fclug.financialaid.models;

import java.io.Serializable;

/**
 * Created by Fabioclug on 2016-09-06.
 */
public class User implements Serializable {

    private String username;

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getExhibitName() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof User)) return false;

        User user = (User) o;

        return username != null ? username.equals(user.username) : user.username == null;

    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}
