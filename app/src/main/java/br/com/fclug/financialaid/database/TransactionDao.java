package br.com.fclug.financialaid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.fclug.financialaid.models.Transaction;

/**
 * Created by Fabioclug on 2016-06-09.
 */
public class TransactionDao {

    private DatabaseHandler mDbHandler;
    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("dd/MM/yyyy");

    public TransactionDao(Context context) {
        mDbHandler = new DatabaseHandler(context);
        Transaction t1 = new Transaction(false, "Aniversário e um monte de palavra pra ficar um nome bem longo", 674.90, "debt", Calendar.getInstance().getTime());
        Transaction t2 = new Transaction(true, "Salário", 1776.94, "credit", new Date(1134839678));
        save(t1);
        save(t2);
    }

    public boolean save(Transaction transaction) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("credit", transaction.isCredit()? 1:0);
        contentValues.put("category", transaction.getCategory());
        contentValues.put("value", transaction.getValue());
        contentValues.put("description", transaction.getDescription());
        contentValues.put("register_date", mDateFormatter.format(transaction.getDate()));
        long result = mDbHandler.getWritableDatabase().insert("cash_transaction", null, contentValues);
        transaction.setId(result);
        return result > 0;
    }

    private Transaction build(Cursor cursor) {
        int tId = cursor.getInt(cursor.getColumnIndex("id"));
        boolean tDebt = cursor.getInt(cursor.getColumnIndex("credit")) == 1;
        String tCategory = cursor.getString(cursor.getColumnIndex("category"));
        float tValue = cursor.getFloat(cursor.getColumnIndex("value"));
        String tDescription = cursor.getString(cursor.getColumnIndex("description"));
        Date tDate = null;
        try {
            tDate = mDateFormatter.parse(cursor.getString(cursor.getColumnIndex("register_date")));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new Transaction(tId, tDebt, tDescription, tValue, tCategory, tDate);
    }

    public List<Transaction> findAll() {
        List<Transaction> transactions = new ArrayList<>();
        Cursor cursor = mDbHandler.getReadableDatabase().rawQuery("SELECT * FROM cash_transaction", null);
        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                Transaction transaction = build(cursor);
                transactions.add(transaction);
                cursor.moveToNext();
            }
        }
        return transactions;
    }

}
