package br.com.fclug.financialaid.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Fabioclug on 2016-06-09.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "financial-app";
    private static final int DB_VERSION = 1;

    private Context mContext;

    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FinancialAppContract.TransactionTable.CREATE_TABLE);
        db.execSQL(FinancialAppContract.CategoryTable.CREATE_TABLE);
        db.execSQL(FinancialAppContract.AccountTable.CREATE_TABLE);

        db.execSQL(FinancialAppContract.GroupTable.CREATE_TABLE);
        db.execSQL(FinancialAppContract.GroupMemberTable.CREATE_TABLE);
        db.execSQL(FinancialAppContract.GroupTransactionTable.CREATE_TABLE);
        db.execSQL(FinancialAppContract.TransactionSplitTable.CREATE_TABLE);
        db.execSQL(FinancialAppContract.GroupTransactionTable.CREATE_VALUE_TRIGGER);
        db.execSQL(FinancialAppContract.TransactionSplitTable.CREATE_VALUE_TRIGGER);

        CategoryDao.insertDefaultValues(mContext, db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL(String.format("PRAGMA foreign_keys = %s;", "ON"));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(FinancialAppContract.TransactionTable.DROP_TABLE);
        db.execSQL(FinancialAppContract.CategoryTable.DROP_TABLE);
        db.execSQL(FinancialAppContract.AccountTable.DROP_TABLE);
        onCreate(db);
    }
}
