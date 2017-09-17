package br.com.fclug.financialaid.database;

import android.provider.BaseColumns;

/**
 * Created by Fabioclug on 2017-09-16.
 */

public final class FinancialAppContract {

    public static final String PRIMARY_KEY = "PRIMARY KEY";
    public static final String FOREIGN_KEY = "FOREIGN KEY";
    public static final String NOT_NULL = "NOT NULL";

    private FinancialAppContract() {}

    public static class Account implements BaseColumns {
        public static final String TABLE_NAME = "account";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_BALANCE = "balance";
        public static final String COLUMN_TYPE = "type";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER " + PRIMARY_KEY + ", " +
                COLUMN_NAME + " TEXT " + NOT_NULL + ", " +
                COLUMN_BALANCE + " REAL " + NOT_NULL + ", " +
                COLUMN_TYPE + " TEXT " + NOT_NULL + ")";

        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static class Category {
        public static final String TABLE_NAME = "category";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_COLOR = "color";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                COLUMN_NAME + " TEXT PRIMARY KEY, " +
                COLUMN_COLOR + " INTEGER " + NOT_NULL + ")";

        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static class Transaction implements BaseColumns {
        public static final String TABLE_NAME = "app_transaction";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_CREDIT = "credit";
        public static final String COLUMN_ACCOUNT = "account";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DATE = "register_date";
        public static final String COLUMN_VALUE = "value";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + "INTEGER " + PRIMARY_KEY + ", " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_CREDIT + " INTEGER " + NOT_NULL + " CHECK (" + COLUMN_CREDIT + " IN (0,1)), " +
                COLUMN_ACCOUNT + " INTEGER, " +
                COLUMN_DESCRIPTION + " TEXT " + NOT_NULL + ", " +
                COLUMN_DATE + " TEXT " + NOT_NULL + ", " +
                COLUMN_VALUE + " REAL " + NOT_NULL + ", " +
                FOREIGN_KEY + "(" + COLUMN_ACCOUNT + ") REFERENCES " + Account.TABLE_NAME + "(" + Account._ID + ") " +
                    "ON DELETE CASCADE, " +
                FOREIGN_KEY + "(" + COLUMN_CATEGORY + ") REFERENCES " +
                    Category.TABLE_NAME + "(" + Category.COLUMN_NAME + "))";

        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static class Group implements BaseColumns {
        public static final String TABLE_NAME = "expense_group";
        public static final String COLUMN_NAME = "name";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + "INTEGER " + PRIMARY_KEY + ", " +
                COLUMN_NAME + "TEXT " + NOT_NULL + ")";

        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static class GroupMember {
        public static final String TABLE_NAME = "group_member";
        public static final String COLUMN_USER = "user";
        public static final String COLUMN_GROUP = "group_id";
        public static final String COLUMN_VALUE = "value";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                COLUMN_USER + " TEXT " + NOT_NULL + ", " +
                COLUMN_GROUP + " INTEGER, " +
                COLUMN_VALUE + "REAL " + NOT_NULL + ", " +
                PRIMARY_KEY + "(" + COLUMN_USER + ", " + COLUMN_GROUP + ", " +
                FOREIGN_KEY + "(" + COLUMN_GROUP + ") REFERENCES " + Group.TABLE_NAME + "(" + Group._ID + "))";
    }

    public static class GroupTransaction implements BaseColumns{
        public static final String TABLE_NAME = "group_transaction";
        public static final String COLUMN_GROUP = "group_id";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_VALUE = "value";
        public static final String COLUMN_DATE = "register_date";
        public static final String COLUMN_CREDITOR = "creditor";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                _ID + " INTEGER " + PRIMARY_KEY + ", " +
                COLUMN_GROUP + " INTEGER, " +
                COLUMN_DESCRIPTION + " TEXT " + NOT_NULL + ", " +
                COLUMN_VALUE + " REAL " + NOT_NULL + ", " +
                COLUMN_DATE + " TEXT " + NOT_NULL + ", " +
                COLUMN_CREDITOR + " TEXT " + NOT_NULL + ", " +
                FOREIGN_KEY + "(" + COLUMN_GROUP + ") REFERENCES " + Group.TABLE_NAME + "(" + Group._ID + "), " +
                FOREIGN_KEY + "(" + COLUMN_CREDITOR + ") REFERENCES " +
                    GroupMember.TABLE_NAME + "(" + GroupMember.COLUMN_USER + "))";
    }

    public static class TransactionSplit {
        public static final String TABLE_NAME = "transaction_split";
        public static final String COLUMN_TRANSACTION = "transaction_id";
        public static final String COLUMN_DEBTOR = "debtor";
        public static final String COLUMN_VALUE = "value";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                COLUMN_TRANSACTION + " INTEGER, " +
                COLUMN_DEBTOR + " TEXT, " +
                COLUMN_VALUE + " REAL " + NOT_NULL + ", " +
                FOREIGN_KEY + "(" + COLUMN_TRANSACTION + ") REFERENCES " +
                    GroupTransaction.TABLE_NAME + "(" + GroupTransaction._ID + "), " +
                FOREIGN_KEY + "(" + COLUMN_DEBTOR + ") REFERENCES " +
                    GroupMember.TABLE_NAME + "(" + GroupMember.COLUMN_USER + "))";
    }
}
