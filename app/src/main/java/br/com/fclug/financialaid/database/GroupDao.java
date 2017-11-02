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
import br.com.fclug.financialaid.database.FinancialAppContract.GroupTable;
import br.com.fclug.financialaid.database.FinancialAppContract.GroupMemberTable;

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
        contentValues.put(GroupTable.COLUMN_NAME, group.getName());
        long groupId = database.insert(GroupTable.TABLE_NAME, null, contentValues);
        group.setId(groupId);
        boolean result = (groupId > 0);
        long memberId;
        for(User user : group.getMembers()) {
            contentValues = new ContentValues();
            contentValues.put(GroupMemberTable.COLUMN_USER, user.getUsername());
            contentValues.put(GroupMemberTable.COLUMN_GROUP, groupId);
            contentValues.put(GroupMemberTable.COLUMN_VALUE, 0);
            memberId = database.insert(GroupMemberTable.TABLE_NAME, null, contentValues);
            result &= (memberId > 0);
        }
        return result;
    }

    private Group buildGroup(Cursor cursor) {
        long gId = cursor.getLong(cursor.getColumnIndex(GroupTable._ID));
        String gName = cursor.getString(cursor.getColumnIndex(GroupTable.COLUMN_NAME));
        return new Group(gId, gName, false);
    }

    private User buildMember(Cursor cursor) {
        String mName = cursor.getString(cursor.getColumnIndex(GroupMemberTable.COLUMN_USER));
        return new User(mName);
    }

    public List<Group> findAll() {
        List<Group> groups = new ArrayList<>();
        SQLiteDatabase db = mDbHandler.getReadableDatabase();
        String[] columns = new String[] {GroupMemberTable.COLUMN_USER, GroupMemberTable.COLUMN_VALUE};
        String whereClause = GroupMemberTable.COLUMN_GROUP + " = ?";
        try (Cursor cursor = db.query(GroupTable.TABLE_NAME, null, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                Group group = buildGroup(cursor);
                groups.add(group);

                List<User> members = new ArrayList<>();
                List<TransactionSplit> credits = new ArrayList<>();
                String[] whereArgs = new String[] {String.valueOf(group.getId())};
                try (Cursor memberCursor = db.query(GroupMemberTable.TABLE_NAME, columns,
                        whereClause, whereArgs, null, null, null)) {
                    while (memberCursor.moveToNext()) {
                        User member = buildMember(memberCursor);
                        members.add(member);
                        credits.add(new TransactionSplit(member,
                                memberCursor.getDouble(memberCursor.getColumnIndex(GroupMemberTable.COLUMN_VALUE))));
                    }
                }
                group.setMembers(members);
                group.setGroupCredits(credits);
            }
        }
        return groups;
    }

    public List<TransactionSplit> getGroupCredits(Group group, HashMap<String, User> groupMembers) {
        List<TransactionSplit> credits = new ArrayList<>();
        String whereClause = GroupMemberTable.COLUMN_GROUP + " = ?";
        String[] whereArgs = new String[] {String.valueOf(group.getId())};
        SQLiteDatabase db = mDbHandler.getReadableDatabase();
        try (Cursor memberCursor = db.query(GroupMemberTable.TABLE_NAME, null, whereClause, whereArgs,
                null, null, null)) {
            while (memberCursor.moveToNext()) {
                User user = groupMembers.get(memberCursor.getString(memberCursor
                        .getColumnIndex(GroupMemberTable.COLUMN_USER)));
                double value = memberCursor.getDouble(memberCursor.getColumnIndex(GroupMemberTable.COLUMN_VALUE));
                credits.add(new TransactionSplit(user, value));
            }
        }
        return credits;
    }
}
