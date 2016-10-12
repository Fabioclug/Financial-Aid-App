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

    public static final String ACCOUNT_TABLE = "account";
    public static final String TRANSACTION_TABLE = "cash_transaction";

    private static final String CREATE_ACCOUNT = "CREATE TABLE " + ACCOUNT_TABLE + "(id INTEGER PRIMARY KEY," +
            "name TEXT NOT NULL, balance REAL NOT NULL, type TEXT NOT NULL)";

    private static final String CREATE_TRANSACTION = "CREATE TABLE " + TRANSACTION_TABLE + "(id INTEGER NOT NULL PRIMARY KEY, " +
            "category TEXT NOT NULL, credit INTEGER NOT NULL CHECK (credit IN (0,1)) NOT NULL, account INTEGER, " +
            "description TEXT NOT NULL, register_date INTEGER NOT NULL, value REAL NOT NULL, FOREIGN KEY(account) REFERENCES account(id))";


    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TRANSACTION);
        db.execSQL(CREATE_ACCOUNT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists cash_transaction");
        db.execSQL("drop table if exists account");
        onCreate(db);
    }
}
