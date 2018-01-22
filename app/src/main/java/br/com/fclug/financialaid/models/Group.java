package br.com.fclug.financialaid.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Fabioclug on 2016-09-23.
 */
public class Group implements Parcelable {

    private long id;
    private String name;
    private List<User> members;
    private List<TransactionSplit> groupCredits;
    private boolean online;

    public Group(long id, String name, boolean online) {
        this.id = id;
        this.name = name;
        this.online = online;
    }

    public Group(String name, List<User> members) {
        this.name = name;
        this.members = members;
    }

    public Group(long id, String name, List<User> members, List<TransactionSplit> groupCredits, boolean online) {
        this.id = id;
        this.name = name;
        this.members = members;
        this.groupCredits = groupCredits;
        this.online = online;
    }

    protected Group(Parcel in) {
        id = in.readLong();
        name = in.readString();
        online = in.readByte() != 0;
        if (in.readByte() == 0x01) {
            members = new ArrayList<>();
            in.readList(members, User.class.getClassLoader());
        } else {
            members = null;
        }

        if (in.readByte() == 0x01) {
            groupCredits = new ArrayList<>();
            in.readList(groupCredits, TransactionSplit.class.getClassLoader());
        } else {
            groupCredits = null;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeByte((byte) (online ? 1 : 0));
        if (members == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(members);
        }

        if (groupCredits == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(groupCredits);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

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

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getMembersNumber() {
        if (members == null) return 0;
        return members.size();
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public List<TransactionSplit> getGroupCredits() {
        return groupCredits;
    }

    public void setGroupCredits(List<TransactionSplit> groupCredits) {
        this.groupCredits = groupCredits;
    }

    public HashMap<String, User> getMembersDictionary() {
        HashMap<String, User> groupMembers = new HashMap<>();
        for (User u : members) {
            groupMembers.put(u.getUsername(), u);
        }
        return groupMembers;
    }
}
