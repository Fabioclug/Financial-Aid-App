package br.com.fclug.financialaid.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fabioclug on 2016-09-23.
 */
public class Group implements Parcelable {
    private long id;
    private String name;
    private List<User> members;

    public Group(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Group(long id, String name, List<User> members) {
        this.id = id;
        this.name = name;
        this.members = members;
    }

    protected Group(Parcel in) {
        id = in.readLong();
        name = in.readString();
        if (in.readByte() == 0x01) {
            members = new ArrayList<>();
            in.readList(members, User.class.getClassLoader());
        } else {
            members = null;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        if (members == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(members);
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
}
