package br.com.fclug.financialaid.models;

import java.util.List;

/**
 * Created by Fabioclug on 2016-09-23.
 */
public class Group {
    private long id;
    private String name;
    private int membersNumber;
    private List<User> members;

    public Group(long id, String name, int membersNumber) {
        this.id = id;
        this.name = name;
        this.membersNumber = membersNumber;
    }

    public Group(long id, String name, int membersNumber, List<User> members) {
        this.id = id;
        this.name = name;
        this.membersNumber = membersNumber;
        this.members = members;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMembersNumber() {
        return membersNumber;
    }

    public void setMembersNumber(int membersNumber) {
        this.membersNumber = membersNumber;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }
}
