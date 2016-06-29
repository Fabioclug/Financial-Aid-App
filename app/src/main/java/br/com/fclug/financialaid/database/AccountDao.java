package br.com.fclug.financialaid.database;

import android.content.Context;
import android.database.Cursor;

import java.util.List;

import br.com.fclug.financialaid.models.Account;

/**
 * Created by Fabioclug on 2016-06-26.
 */
public class AccountDao {

    private DatabaseHandler mDbHandler;

    public AccountDao(Context context) {
        mDbHandler = new DatabaseHandler(context);
    }

    public boolean save(Account account) {
        return true;
    }

    private Account build(Cursor cursor) {
        return null;
    }

    public List<Account> findAll() {
        return null;
    }
}
