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
//    private static final String CREATE_USER = "CREATE TABLE usuario (id integer not null primary key, " +
//            "PrimNome text not null, SobreNome text not null,cidade text not null," +
//            "email text unique not null, senha text not null, latitude real, longitude real)";

    public static final String ACCOUNT_TABLE = "account";
    public static final String TRANSACTION_TABLE = "cash_transaction";

    private static final String CREATE_ACCOUNT = "create table " + ACCOUNT_TABLE + "(id integer primary key," +
            "name text not null, balance real not null, type text not null)";

    private static final String CREATE_TRANSACTION = "create table " + TRANSACTION_TABLE + "(id integer not null primary key, " +
            "category text not null, credit integer not null check (credit in (0,1)) not null, account integer, " +
            "description text not null, register_date date not null, value real not null, foreign key(account) references account(id))";


    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TRANSACTION);
        db.execSQL(CREATE_ACCOUNT);
        //db.execSQL(CREATE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists cash_transaction");
        db.execSQL("drop table if exists account");
        onCreate(db);
    }
}
