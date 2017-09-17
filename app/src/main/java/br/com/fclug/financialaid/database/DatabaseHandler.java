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
    public static final String CATEGORY_TABLE = "category";
    public static final String TRANSACTION_TABLE = "cash_transaction";
    public static final String GROUP_TABLE = "expense_group";
    public static final String GROUP_MEMBER_TABLE = "group_member";
    public static final String GROUP_TRANSACTION_TABLE = "group_transaction";
    public static final String TRANSACTION_SPLIT_TABLE = "transaction_split";

    private static final String CREATE_ACCOUNT = "CREATE TABLE " + ACCOUNT_TABLE + "(id INTEGER PRIMARY KEY," +
            "name TEXT NOT NULL, balance REAL NOT NULL, type TEXT NOT NULL)";

    private static final String CREATE_CATEGORY = "CREATE TABLE " + CATEGORY_TABLE + "(name TEXT PRIMARY KEY," +
            "color INTEGER NOT NULL)";

    private static final String CREATE_TRANSACTION = "CREATE TABLE " + TRANSACTION_TABLE + "(id INTEGER PRIMARY KEY, " +
            "category TEXT NOT NULL, credit INTEGER NOT NULL CHECK (credit IN (0,1)) NOT NULL, account INTEGER, " +
            "description TEXT NOT NULL, register_date INTEGER NOT NULL, value REAL NOT NULL, FOREIGN KEY(account) " +
            "REFERENCES account(id) ON DELETE CASCADE, FOREIGN KEY(category) REFERENCES category(name))";

    private static final String CREATE_GROUP = "CREATE TABLE " + GROUP_TABLE + "(id INTEGER PRIMARY KEY, name TEXT " +
            "NOT NULL)";

    private static final String CREATE_GROUP_MEMBER = "CREATE TABLE " + GROUP_MEMBER_TABLE + "(user TEXT, group_id " +
            "INTEGER, value REAL NOT NULL, PRIMARY KEY(user, group_id), FOREIGN KEY(group_id) REFERENCES expense_group(id))";

    private static final String CREATE_GROUP_TRANSACTION = "CREATE TABLE " + GROUP_TRANSACTION_TABLE + "(id INTEGER " +
            "PRIMARY KEY, group_id INTEGER NOT NULL, name TEXT NOT NULL, value REAL NOT NULL, moment INTEGER NOT " +
            "NULL, creditor TEXT NOT NULL, FOREIGN KEY(group_id) REFERENCES expense_group(id), FOREIGN KEY(creditor) " +
            "REFERENCES group_member(user))";

    private static final String CREATE_TRANSACTION_SPLIT = "CREATE TABLE " + TRANSACTION_SPLIT_TABLE + "(transaction_id " +
            "INTEGER NOT NULL, debtor TEXT NOT NULL, value REAL NOT NULL, FOREIGN KEY(transaction_id) REFERENCES " +
            "group_transaction(id), FOREIGN KEY(debtor) REFERENCES group_member(user))";

    private Context mContext;

    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TRANSACTION);
        db.execSQL(CREATE_CATEGORY);
        db.execSQL(CREATE_ACCOUNT);

        db.execSQL(CREATE_GROUP);
        db.execSQL(CREATE_GROUP_MEMBER);
        db.execSQL(CREATE_GROUP_TRANSACTION);
        db.execSQL(CREATE_TRANSACTION_SPLIT);

        CategoryDao.insertDefaultValues(mContext, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists" + TRANSACTION_TABLE);
        db.execSQL("drop table if exists" + CATEGORY_TABLE);
        db.execSQL("drop table if exists" + ACCOUNT_TABLE);
        onCreate(db);
    }
}
