package br.com.fclug.financialaid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.com.fclug.financialaid.models.Account.AccountBuilder;
import br.com.fclug.financialaid.utils.AppUtils;
import br.com.fclug.financialaid.models.Account;
import br.com.fclug.financialaid.models.Transaction;

/**
 * Created by Fabioclug on 2016-06-26.
 */
public class AccountDao {

    private DatabaseHandler mDbHandler;

    public AccountDao(Context context) {
        mDbHandler = new DatabaseHandler(context);
    }

    public boolean save(Account account) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", account.getName());
        contentValues.put("balance", account.getBalance());
        contentValues.put("type", account.getType());
        long result = mDbHandler.getWritableDatabase().insert("account", null, contentValues);
        account.setId(result);
        return result > 0;
    }

    public boolean update(Account account) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", account.getName());
        contentValues.put("balance", account.getBalance());
        contentValues.put("type", account.getType());
        String whereClause = "id = ?";
        String[] whereArgs = new String[] {String.valueOf(account.getId())};

        long result = mDbHandler.getWritableDatabase().update(DatabaseHandler.ACCOUNT_TABLE, contentValues, whereClause, whereArgs);
        return result > 0;
    }

    private Account build(Cursor cursor) {
        long aId = cursor.getLong(cursor.getColumnIndex("id"));
        String aName = cursor.getString(cursor.getColumnIndex("name"));
        double aBalance = cursor.getDouble(cursor.getColumnIndex("balance"));
        String aType = cursor.getString(cursor.getColumnIndex("type"));
        return new AccountBuilder().setId(aId)
                                   .setName(aName)
                                   .setBalance(aBalance)
                                   .setType(aType)
                                   .build();
    }

    public List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();
        Cursor cursor = mDbHandler.getReadableDatabase().rawQuery("SELECT * FROM account", null);
        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                Account account = build(cursor);
                accounts.add(account);
                cursor.moveToNext();
            }
        }
        return accounts;
    }

    public Account findById(long id) {
        String whereClause = "id = ?";
        String[] whereArgs = new String[] {String.valueOf(id)};
        Cursor cursor = mDbHandler.getReadableDatabase().query("account", null, whereClause, whereArgs, null, null, null);
        Account result = null;
        if(cursor.moveToFirst()) {
            result = build(cursor);
        }
        return result;
    }

    public boolean updateBalance(Account account, Transaction transaction) {
        double balance = AppUtils.roundValue(account.getBalance() + transaction.getSignedValue());
        Log.d("dao", "balance: " + balance);
        account.setBalance(balance);
        ContentValues values = new ContentValues();
        values.put("balance", account.getBalance());
        String whereClause = "id = ?";
        String[] whereArgs = new String[] {String.valueOf(account.getId())};
        int result = mDbHandler.getWritableDatabase().update("account", values, whereClause, whereArgs);
        return result > 0;
    }

    public boolean updateBalance(Account account, double difference) {
        double balance = AppUtils.roundValue(account.getBalance() + difference);
        Log.d("dao", "balance: " + balance);
        account.setBalance(balance);
        ContentValues values = new ContentValues();
        values.put("balance", account.getBalance());
        String whereClause = "id = ?";
        String[] whereArgs = new String[] {String.valueOf(account.getId())};
        int result = mDbHandler.getWritableDatabase().update("account", values, whereClause, whereArgs);
        return result > 0;
    }

    public boolean delete(Account account) {
        long id = account.getId();
        String whereClause = "id = ?";
        String[] whereArgs = new String[] { String.valueOf(id) };
        int result = mDbHandler.getWritableDatabase().delete("account", whereClause, whereArgs);
        return result > 0;
    }
}
