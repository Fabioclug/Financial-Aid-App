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

    private Group(GroupBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.creator = builder.creator;
        this.members = builder.members;
        this.groupBalances = builder.groupBalances;
        this.online = builder.online;
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

    public static class GroupBuilder {
        private long id;
        private String name;
        private User creator;
        private List<User> members;
        private List<TransactionSplit> groupBalances;
        private boolean online;

        public GroupBuilder() {
            this.groupBalances = new ArrayList<>();
        }

        public GroupBuilder setId(long id) {
            this.id = id;
            return this;
        }

        public GroupBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public GroupBuilder setCreator(User creator) {
            this.creator = creator;
            return this;
        }

        public GroupBuilder setMembers(List<User> members) {
            this.members = members;
            return this;
        }

        public GroupBuilder setGroupBalances(List<TransactionSplit> groupBalances) {
            this.groupBalances = groupBalances;
            return this;
        }

        public GroupBuilder setOnline(boolean online) {
            this.online = online;
            return this;
        }

        public Group build() {
            return new Group(this);
        }

        public Group buildFromJson(JSONObject groupJsonData) throws JSONException {
            this.id = groupJsonData.getLong("group_id");
            this.name = groupJsonData.getString("name");
            this.online = true;
            String creator = groupJsonData.getString("creator");
            setGroupBalances(groupJsonData.getJSONArray("members"), creator, true);
            return new Group(this);
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
    }
}
