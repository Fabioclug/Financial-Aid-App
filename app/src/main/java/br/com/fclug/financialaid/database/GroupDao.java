package br.com.fclug.financialaid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.fclug.financialaid.models.Group;
import br.com.fclug.financialaid.models.TransactionSplit;
import br.com.fclug.financialaid.models.User;

/**
 * Created by Fabioclug on 2017-06-26.
 */

public class GroupDao {

    private DatabaseHandler mDbHandler;

    public GroupDao(Context context) {
        mDbHandler = new DatabaseHandler(context);
    }

    public boolean save(Group group) {
        SQLiteDatabase database = mDbHandler.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", group.getName());
        long groupId = database.insert(DatabaseHandler.GROUP_TABLE, null, contentValues);
        group.setId(groupId);
        boolean result = (groupId > 0);
        long memberId;
        for(User user : group.getMembers()) {
            contentValues = new ContentValues();
            contentValues.put("user", user.getName());
            contentValues.put("group_id", groupId);
            contentValues.put("value", 0);
            memberId = database.insert(DatabaseHandler.GROUP_MEMBER_TABLE, null, contentValues);
            result &= (memberId > 0);
        }
        return result;
    }

    private Group buildGroup(Cursor cursor) {
        long gId = cursor.getLong(cursor.getColumnIndex("id"));
        String gName = cursor.getString(cursor.getColumnIndex("name"));
        return new Group(gId, gName, false);
    }

    private User buildMember(Cursor cursor) {
        String mName = cursor.getString(cursor.getColumnIndex("user"));
        return new User(mName, mName);
    }

    public List<Group> findAll() {
        List<Group> groups = new ArrayList<>();
        SQLiteDatabase db = mDbHandler.getReadableDatabase();
        String[] columns = new String[] {"user"};
        String whereClause = "group_id = ?";
        try (Cursor cursor = db.query(DatabaseHandler.GROUP_TABLE, null, null, null, null, null, null)) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    Group group = buildGroup(cursor);
                    groups.add(group);

                    List<User> members = new ArrayList<>();
                    String[] whereArgs = new String[] {String.valueOf(group.getId())};
                    try (Cursor memberCursor = db.query(DatabaseHandler.GROUP_MEMBER_TABLE, columns, whereClause, whereArgs, null, null, null)) {
                        if (memberCursor.moveToFirst()) {
                            while (!memberCursor.isAfterLast()) {
                                User member = buildMember(memberCursor);
                                members.add(member);
                                memberCursor.moveToNext();
                            }
                        }
                    }
                    group.setMembers(members);
                    cursor.moveToNext();
                }
            }
        }
        return groups;
    }

    public List<TransactionSplit> getGroupDebts(Group group, HashMap<String, User> groupMembers) {
        List<TransactionSplit> debts = new ArrayList<>();
        String whereClause = "group_id = ?";
        String[] whereArgs = new String[] {String.valueOf(group.getId())};
        SQLiteDatabase db = mDbHandler.getReadableDatabase();
        try (Cursor memberCursor = db.query(DatabaseHandler.GROUP_MEMBER_TABLE, null, whereClause, whereArgs, null, null, null)) {
            
        }
        return null;
    }
}
