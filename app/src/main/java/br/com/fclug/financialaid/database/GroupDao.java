package br.com.fclug.financialaid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import br.com.fclug.financialaid.CreateGroupPaymentActivity;
import br.com.fclug.financialaid.adapter.GroupTransactionRecyclerViewListAdapter;
import br.com.fclug.financialaid.models.Group;
import br.com.fclug.financialaid.models.GroupTransaction;
import br.com.fclug.financialaid.models.GroupTransaction.GroupTransactionBuilder;
import br.com.fclug.financialaid.models.TransactionSplit;
import br.com.fclug.financialaid.models.User;
import br.com.fclug.financialaid.database.FinancialAppContract.GroupTable;
import br.com.fclug.financialaid.database.FinancialAppContract.GroupMemberTable;
import br.com.fclug.financialaid.database.FinancialAppContract.GroupTransactionTable;
import br.com.fclug.financialaid.database.FinancialAppContract.TransactionSplitTable;

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

    public boolean saveTransaction(Group group, GroupTransaction transaction) {
        SQLiteDatabase database = mDbHandler.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GroupTransactionTable.COLUMN_DESCRIPTION, transaction.getDescription());
        contentValues.put(GroupTransactionTable.COLUMN_CREDITOR, transaction.getPayer().getUsername());
        contentValues.put(GroupTransactionTable.COLUMN_VALUE, transaction.getValue());
        contentValues.put(GroupTransactionTable.COLUMN_DATE, CreateGroupPaymentActivity.dateSaveFormatter.format(
                transaction.getDate()));
        contentValues.put(GroupTransactionTable.COLUMN_GROUP, group.getId());
        long transactionId = database.insert(GroupTransactionTable.TABLE_NAME, null, contentValues);

        boolean result = (transactionId > 0);
        if (result) {
            transaction.setId(transactionId);
        }

        for (TransactionSplit split : transaction.getSplits()) {
            contentValues = new ContentValues();
            contentValues.put(TransactionSplitTable.COLUMN_TRANSACTION, transactionId);
            contentValues.put(TransactionSplitTable.COLUMN_DEBTOR, split.getDebtor().getUsername());
            contentValues.put(TransactionSplitTable.COLUMN_VALUE, split.getValue());
            result &= (database.insert(TransactionSplitTable.TABLE_NAME, null, contentValues) > 0);
        }

        return result;
    }

    public List<GroupTransaction> findGroupTransactions(Group group, HashMap<String, User> groupMembers) {
        List<GroupTransaction> transactions = new ArrayList<>();
        String whereClause = GroupTransactionTable.COLUMN_GROUP + " = ?";
        String[] whereArgs = new String[] {String.valueOf(group.getId())};
        SQLiteDatabase db = mDbHandler.getReadableDatabase();
        try (Cursor cursor = db.query(GroupTransactionTable.TABLE_NAME, null, whereClause, whereArgs,
                null, null, null)) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(GroupTransactionTable._ID));
                String description = cursor.getString(cursor.getColumnIndex(GroupTransactionTable.COLUMN_DESCRIPTION));
                User payer = groupMembers.get(cursor.getString(cursor.getColumnIndex(
                        GroupTransactionTable.COLUMN_CREDITOR)));
                double value = cursor.getDouble(cursor.getColumnIndex(GroupTransactionTable.COLUMN_VALUE));
                String date = cursor.getString(cursor.getColumnIndex(GroupTransactionTable.COLUMN_DATE));
                Date formattedDate = null;
                try {
                    formattedDate = GroupTransactionRecyclerViewListAdapter.buildDateFormatter.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                GroupTransactionBuilder transactionBuilder = new GroupTransactionBuilder()
                        .setId(id)
                        .setDescription(description)
                        .setPayer(payer)
                        .setValue(value)
                        .setDate(formattedDate);

                whereClause = TransactionSplitTable.COLUMN_TRANSACTION + " = ?";
                whereArgs = new String[] {String.valueOf(id)};
                List<TransactionSplit> splits = new ArrayList<>();

                try (Cursor splitCursor = db.query(TransactionSplitTable.TABLE_NAME, null, whereClause, whereArgs, null,
                        null, null)) {
                    while (splitCursor.moveToNext()) {
                        User debtor = groupMembers.get(splitCursor.getString(splitCursor.getColumnIndex(
                                TransactionSplitTable.COLUMN_DEBTOR)));
                        double split = splitCursor.getDouble(splitCursor.getColumnIndex(
                                TransactionSplitTable.COLUMN_VALUE));
                        splits.add(new TransactionSplit(debtor, split));

                    }
                }
                transactionBuilder.setSplits(splits);
                transactions.add(transactionBuilder.build());
            }
        }
        return transactions;
    }
}
