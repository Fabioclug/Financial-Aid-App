package br.com.fclug.financialaid.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Fabioclug on 2016-09-23.
 */
public class Group implements Parcelable {

    private long id;
    private String name;
    private User creator;
    private List<User> members;
    private List<TransactionSplit> groupBalances;
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

    public Group(String name, List<User> members, User creator) {
        this.name = name;
        this.members = members;
        this.creator = creator;
    }

    public Group(long id, String name, List<User> members, List<TransactionSplit> groupBalances, boolean online) {
        this.id = id;
        this.name = name;
        this.members = members;
        this.groupBalances = groupBalances;
        this.online = online;
    }

    public Group(JSONObject groupJsonData) throws JSONException {
        this(groupJsonData.getLong("group_id"), groupJsonData.getString("name"), true);
        String creator = groupJsonData.getString("creator");
        setGroupBalances(groupJsonData.getJSONArray("members"), creator, true);
    }

    private void setGroupBalances(JSONArray membersObject, String creator, boolean setMembers) throws JSONException {
        List<User> memberList = new ArrayList<>();
        List<TransactionSplit> memberCredits = new ArrayList<>();
        for (int j = 0; j < membersObject.length(); j++) {
            JSONObject member = membersObject.getJSONObject(j);
            OnlineUser user = new OnlineUser(member.getString("username"), member.getString("name"));
            if(user.getUsername().equals(creator)) {
                this.creator = user;
            }
            memberList.add(user);
            memberCredits.add(new TransactionSplit(user, member.getLong("value")));
        }
        if(setMembers) {
            this.members = memberList;
        }
        this.groupBalances = memberCredits;
    }

    protected Group(Parcel in) {
        id = in.readLong();
        name = in.readString();
        creator = in.readParcelable(User.class.getClassLoader());
        online = in.readByte() != 0;
        if (in.readByte() == 0x01) {
            members = new ArrayList<>();
            in.readList(members, User.class.getClassLoader());
        } else {
            members = null;
        }

        if (in.readByte() == 0x01) {
            groupBalances = new ArrayList<>();
            in.readList(groupBalances, TransactionSplit.class.getClassLoader());
        } else {
            groupBalances = null;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeParcelable(creator, flags);
        dest.writeByte((byte) (online ? 1 : 0));
        if (members == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(members);
        }

        if (groupBalances == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(groupBalances);
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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
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

    public List<TransactionSplit> getGroupBalances() {
        return groupBalances;
    }

    public void setGroupBalances(List<TransactionSplit> groupBalances) {
        this.groupBalances = groupBalances;
    }

    public HashMap<String, User> getMembersDictionary() {
        HashMap<String, User> groupMembers = new HashMap<>();
        for (User u : members) {
            groupMembers.put(u.getUsername(), u);
        }
        return groupMembers;
    }
}
