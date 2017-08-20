package br.com.fclug.financialaid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.text.DateFormat;
import java.text.ParseException;
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
        contentValues.put("credit", transaction.isCredit()? 1:0);
        contentValues.put("category", transaction.getCategory().getName());
        contentValues.put("value", transaction.getValue());
        contentValues.put("description", transaction.getDescription());
        contentValues.put("register_date", transaction.getDate().getTime());
        contentValues.put("account", transaction.getAccountId());
        long result;
        if(update) {
            String whereClause = "id = ?";
            String[] whereArgs = new String[] {String.valueOf(transaction.getId())};
            result = mDbHandler.getWritableDatabase().update(DatabaseHandler.TRANSACTION_TABLE, contentValues, whereClause, whereArgs);
        } else {
            result = mDbHandler.getWritableDatabase().insert(DatabaseHandler.TRANSACTION_TABLE, null, contentValues);
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
        long tId = cursor.getInt(cursor.getColumnIndex("id"));
        boolean tDebt = cursor.getInt(cursor.getColumnIndex("credit")) == 1;
        String categoryName = cursor.getString(cursor.getColumnIndex("category"));
        float tValue = cursor.getFloat(cursor.getColumnIndex("value"));
        String tDescription = cursor.getString(cursor.getColumnIndex("description"));
        long aId = cursor.getInt(cursor.getColumnIndex("account"));
        Date tDate = new Date(cursor.getLong(cursor.getColumnIndex("register_date")));

        CategoryDao categoryDao = new CategoryDao(mContext);
        Category tCategory = categoryDao.findByName(categoryName);

        return new Transaction(tId, tDebt, tDescription, tValue, tCategory, tDate, aId);
    }

    private List<Transaction> queryList(String whereClause, String[] whereArgs) {
        List<Transaction> transactions = new ArrayList<>();
        try (Cursor cursor = mDbHandler.getReadableDatabase().query(DatabaseHandler.TRANSACTION_TABLE, null, whereClause, whereArgs, null, null, null, null)) {
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
            whereClause = "account = ? AND register_date >= ? AND register_date <= ?";
            whereArgs = new String[] {String.valueOf(account.getId()), String.valueOf(from.getTime()), String.valueOf(to.getTime())};
        } else {
            whereClause = "account = ?";
            whereArgs = new String[] {String.valueOf(account.getId())};
        }

        return queryList(whereClause, whereArgs);
    }

    public List<Map.Entry<String, Float>> findSumOnLastSevenDays() {

        Map<String, Float> totalPerDay = new HashMap<>();
        String query = "SELECT strftime('%Y-%m-%d', register_date/1000, 'unixepoch') AS entry_day, SUM(value) AS total " +
                "FROM cash_transaction WHERE entry_day >= date('now', '-6 day') GROUP BY entry_day ORDER BY entry_day";
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        // get starting date
        cal.add(Calendar.DAY_OF_YEAR, -7);
        // loop adding one day in each iteration
        for(int i = 0; i< 7; i++){
            cal.add(Calendar.DAY_OF_YEAR, 1);
            String date = dateFormat.format(cal.getTime());
            if (totalPerDay.keySet().contains(date)) {
                entries.add(new AbstractMap.SimpleEntry<>(dateFormat.format(cal.getTime()), totalPerDay.get(date)));
            } else {
                entries.add(new AbstractMap.SimpleEntry<>(dateFormat.format(cal.getTime()), 0f));
            }
        }

        return entries;
    }

    public boolean delete(Transaction transaction) {
        long id = transaction.getId();
        String whereClause = "id = ?";
        String[] whereArgs = new String[] { String.valueOf(id) };
        int result = mDbHandler.getWritableDatabase().delete(DatabaseHandler.TRANSACTION_TABLE, whereClause, whereArgs);
        return result > 0;
    }

}
