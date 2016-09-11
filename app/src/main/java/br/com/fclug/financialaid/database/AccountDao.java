package br.com.fclug.financialaid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import br.com.fclug.financialaid.models.Account;
import br.com.fclug.financialaid.models.Transaction;

/**
 * Created by Fabioclug on 2016-06-26.
 */
public class AccountDao {

    private DatabaseHandler mDbHandler;

    public AccountDao(Context context) {
        mDbHandler = new DatabaseHandler(context);
        //Account a1 = new Account("Checking", 200, "Bank");
        //Account a2 = new Account("Credit Card", 200, "Card");
        //save(a1);
        //save(a2);
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

    private Account build(Cursor cursor) {
        long aId = cursor.getLong(cursor.getColumnIndex("id"));
        String aName = cursor.getString(cursor.getColumnIndex("name"));
        double aBalance = cursor.getDouble(cursor.getColumnIndex("balance"));
        String aType = cursor.getString(cursor.getColumnIndex("type"));
        return new Account(aId, aName, aBalance, aType);
    }

    public List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();
        Cursor cursor = mDbHandler.getReadableDatabase().rawQuery("SELECT * FROM account", null);
        if(cursor.moveToFirst()) {
            int i = 0;
            while(!cursor.isAfterLast()) {
                Account account = build(cursor);
                accounts.add(account);
                cursor.moveToNext();
                i++;
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
        boolean credit = transaction.isCredit();
        double value = transaction.getValue();
        account.setBalance(account.getBalance() + (credit? value : (value * -1)));
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
