package br.com.fclug.financialaid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.fclug.financialaid.models.Account;
import br.com.fclug.financialaid.models.Category;
import br.com.fclug.financialaid.models.Transaction;
import br.com.fclug.financialaid.database.FinancialAppContract.TransactionTable;
import br.com.fclug.financialaid.database.FinancialAppContract.AccountTable;

/**
 * Created by Fabioclug on 2016-06-09.
 */
public class TransactionDao {

    private Context mContext;
    private DatabaseHandler mDbHandler;

    public TransactionDao(Context context) {
        mContext = context;
        mDbHandler = new DatabaseHandler(context);
    }

    public long writeOnDb(Transaction transaction, boolean update) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TransactionTable.COLUMN_CATEGORY, transaction.getCategory().getName());
        contentValues.put(TransactionTable.COLUMN_VALUE, transaction.getValue());
        contentValues.put(TransactionTable.COLUMN_DESCRIPTION, transaction.getDescription());
        contentValues.put(TransactionTable.COLUMN_DATE, transaction.getDate().getTime());
        contentValues.put(TransactionTable.COLUMN_ACCOUNT, transaction.getAccountId());
        long result;
        if(update) {
            String whereClause = TransactionTable._ID + " = ?";
            String[] whereArgs = new String[] {String.valueOf(transaction.getId())};
            result = mDbHandler.getWritableDatabase()
                               .update(TransactionTable.TABLE_NAME, contentValues, whereClause, whereArgs);
        } else {
            result = mDbHandler.getWritableDatabase().insert(TransactionTable.TABLE_NAME, null, contentValues);
        }
        return result;
    }

    public boolean save(Transaction transaction) {
        long result = writeOnDb(transaction, false);
        if (result > 0) {
            transaction.setId(result);
        }
        return result > 0;
    }

    public boolean update(Transaction transaction) {
        long result = writeOnDb(transaction, true);
        return result == 1;
    }

    private Transaction build(Cursor cursor) {
        long tId = cursor.getInt(cursor.getColumnIndex(TransactionTable._ID));
        String categoryName = cursor.getString(cursor.getColumnIndex(TransactionTable.COLUMN_CATEGORY));
        long tValue = cursor.getLong(cursor.getColumnIndex(TransactionTable.COLUMN_VALUE));
        String tDescription = cursor.getString(cursor.getColumnIndex(TransactionTable.COLUMN_DESCRIPTION));
        long aId = cursor.getInt(cursor.getColumnIndex(TransactionTable.COLUMN_ACCOUNT));
        Date tDate = new Date(cursor.getLong(cursor.getColumnIndex(TransactionTable.COLUMN_DATE)));

        CategoryDao categoryDao = new CategoryDao(mContext);
        Category tCategory = categoryDao.findByName(categoryName);

        return new Transaction(tId, tDescription, tValue, tCategory, tDate, aId);
    }

    private List<Transaction> queryList(String whereClause, String[] whereArgs) {
        List<Transaction> transactions = new ArrayList<>();
        try (Cursor cursor = mDbHandler.getReadableDatabase()
                                       .query(TransactionTable.TABLE_NAME, null, whereClause, whereArgs,
                                               null, null, null, null)) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    Transaction transaction = build(cursor);
                    transactions.add(transaction);
                    cursor.moveToNext();
                }
            }
        }
        return transactions;
    }

    public List<Transaction> findAll() {
        return queryList(null, null);
    }

    public List<Transaction> findByAccount(Account account, Date from, Date to) {
        String whereClause;
        String[] whereArgs;
        if (from != null && to != null) {
            whereClause = TransactionTable.COLUMN_ACCOUNT + " = ? AND " + TransactionTable.COLUMN_DATE + " >= ? AND " +
                    TransactionTable.COLUMN_DATE + " <= ?";
            whereArgs = new String[] {String.valueOf(account.getId()), String.valueOf(from.getTime()),
                    String.valueOf(to.getTime())};
        } else {
            whereClause = TransactionTable.COLUMN_ACCOUNT + " = ?";
            whereArgs = new String[] {String.valueOf(account.getId())};
        }

        return queryList(whereClause, whereArgs);
    }

    public List<Map.Entry<String, Float>> findSumOnLastSevenDays() {

        Map<String, Float> totalPerDay = new HashMap<>();
        String query = "SELECT strftime('%Y-%m-%d', " + TransactionTable.COLUMN_DATE +"/1000, 'unixepoch') AS " +
                "entry_day, SUM(" + TransactionTable.COLUMN_VALUE + ") AS total FROM " +
                TransactionTable.TABLE_NAME + " WHERE entry_day >= date('now', '-7 day') " +
                "GROUP BY entry_day ORDER BY entry_day";
        try (Cursor cursor = mDbHandler.getReadableDatabase().rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    String entry_day = cursor.getString(cursor.getColumnIndex("entry_day"));
                    float sum = cursor.getFloat(cursor.getColumnIndex("total"));
                    totalPerDay.put(entry_day, sum);
                    cursor.moveToNext();
                }
            }
        }

        // Build list with 7 last days and put the values for days with expenses
        List<Map.Entry<String, Float>> entries = new ArrayList<>();
        SimpleDateFormat dateComparisonFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat finalDateFormat = new SimpleDateFormat("MM/dd");
        Calendar cal = Calendar.getInstance();
        // get starting date
        cal.add(Calendar.DAY_OF_YEAR, -7);
        // loop adding one day in each iteration
        for(int i = 0; i< 7; i++){
            cal.add(Calendar.DAY_OF_YEAR, 1);
            String date = dateComparisonFormat.format(cal.getTime());
            if (totalPerDay.keySet().contains(date)) {
                entries.add(new AbstractMap.SimpleEntry<>(finalDateFormat.format(cal.getTime()), totalPerDay.get(date)));
            } else {
                entries.add(new AbstractMap.SimpleEntry<>(finalDateFormat.format(cal.getTime()), 0f));
            }
        }

        return entries;
    }

    public HashMap<String, Float> findTransactionsPerAccount() {
        HashMap<String, Float> transactions = new HashMap<>();
        String query = "SELECT SUM(value) AS total, name FROM " + TransactionTable.TABLE_NAME + " JOIN " +
                AccountTable.TABLE_NAME + " ON " + TransactionTable.TABLE_NAME + ".account = " +
                AccountTable.TABLE_NAME + "." + AccountTable._ID + " GROUP BY name";
        System.out.println(query);
        try(Cursor cursor = mDbHandler.getReadableDatabase().rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    transactions.put(cursor.getString(cursor.getColumnIndex("name")),
                            cursor.getFloat(cursor.getColumnIndex("total")));
                }
            }
        }
        return transactions;
    }

    public boolean delete(Transaction transaction) {
        long id = transaction.getId();
        String whereClause = TransactionTable._ID + " = ?";
        String[] whereArgs = new String[] { String.valueOf(id) };
        int result = mDbHandler.getWritableDatabase().delete(TransactionTable.TABLE_NAME, whereClause,
                whereArgs);
        return result > 0;
    }

}
