package br.com.fclug.financialaid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.com.fclug.financialaid.models.Account.AccountBuilder;
import br.com.fclug.financialaid.utils.AppUtils;
import br.com.fclug.financialaid.models.Account;
import br.com.fclug.financialaid.models.Transaction;
import br.com.fclug.financialaid.database.FinancialAppContract.AccountTable;


/**
 * Created by Fabioclug on 2016-06-26.
 */
public class AccountDao {

    private DatabaseHandler mDbHandler;
    private SQLiteDatabase mDbReader;

    public AccountDao(Context context) {
        mDbHandler = new DatabaseHandler(context);
        mDbReader = mDbHandler.getReadableDatabase();
    }

    public boolean save(Account account) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AccountTable.COLUMN_NAME, account.getName());
        contentValues.put(AccountTable.COLUMN_BALANCE, account.getBalance());
        contentValues.put(AccountTable.COLUMN_TYPE, account.getType());
        long result = mDbHandler.getWritableDatabase().insert(AccountTable.TABLE_NAME, null, contentValues);
        account.setId(result);
        return result > 0;
    }

    public boolean update(Account account) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AccountTable.COLUMN_NAME, account.getName());
        contentValues.put(AccountTable.COLUMN_TYPE, account.getType());
        String whereClause = AccountTable._ID + " = ?";
        String[] whereArgs = new String[] {String.valueOf(account.getId())};

        long result = mDbHandler.getWritableDatabase()
                                .update(AccountTable.TABLE_NAME, contentValues, whereClause, whereArgs);
        return result > 0;
    }

    private Account build(Cursor cursor) {
        long aId = cursor.getLong(cursor.getColumnIndex(AccountTable._ID));
        String aName = cursor.getString(cursor.getColumnIndex(AccountTable.COLUMN_NAME));
        double aBalance = cursor.getDouble(cursor.getColumnIndex(AccountTable.COLUMN_BALANCE));
        String aType = cursor.getString(cursor.getColumnIndex(AccountTable.COLUMN_TYPE));
        return new AccountBuilder().setId(aId)
                                   .setName(aName)
                                   .setBalance(aBalance)
                                   .setType(aType)
                                   .build();
    }

    public List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();
        try (Cursor cursor = mDbReader.query(AccountTable.TABLE_NAME, null, null, null, null, null, null)) {
            while(cursor.moveToNext()) {
                Account account = build(cursor);
                accounts.add(account);
            }
        }
        return accounts;
    }

    public Account findById(long id) {
        String whereClause = FinancialAppContract.AccountTable._ID + " = ?";
        String[] whereArgs = new String[] {String.valueOf(id)};
        Account result = null;
        try (Cursor cursor = mDbReader.query(AccountTable.TABLE_NAME, null, whereClause, whereArgs, null, null, null)) {
            if (cursor.moveToFirst()) {
                result = build(cursor);
            }
        }
        return result;
    }

    public boolean updateBalance(Account account, Transaction transaction) {
        double balance = AppUtils.roundValue(account.getBalance() + transaction.getValue());
        Log.d("dao", "balance: " + balance);
        account.setBalance(balance);
        ContentValues values = new ContentValues();
        values.put(AccountTable.COLUMN_BALANCE, account.getBalance());
        String whereClause = AccountTable._ID + " = ?";
        String[] whereArgs = new String[] {String.valueOf(account.getId())};
        int result = mDbHandler.getWritableDatabase()
                               .update(AccountTable.TABLE_NAME, values, whereClause, whereArgs);
        return result > 0;
    }

    public boolean updateBalance(Account account, double difference) {
        double balance = AppUtils.roundValue(account.getBalance() + difference);
        Log.d("dao", "balance: " + balance);
        account.setBalance(balance);
        ContentValues values = new ContentValues();
        values.put(AccountTable.COLUMN_BALANCE, account.getBalance());
        String whereClause = AccountTable._ID + " = ?";
        String[] whereArgs = new String[] {String.valueOf(account.getId())};
        int result = mDbHandler.getWritableDatabase()
                               .update(AccountTable.TABLE_NAME, values, whereClause, whereArgs);
        return result > 0;
    }

    public boolean delete(Account account) {
        long id = account.getId();
        String whereClause = AccountTable._ID + " = ?";
        String[] whereArgs = new String[] { String.valueOf(id) };
        int result = mDbHandler.getWritableDatabase()
                               .delete(AccountTable.TABLE_NAME, whereClause, whereArgs);
        return result > 0;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mDbReader.close();
    }
}
