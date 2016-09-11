package br.com.fclug.financialaid.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Date;

import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.database.AccountDao;
import br.com.fclug.financialaid.database.TransactionDao;
import br.com.fclug.financialaid.models.Account;
import br.com.fclug.financialaid.models.Transaction;

/**
 * Created by Fabioclug on 2016-08-14.
 */
public class AddAccountDialog extends Dialog implements View.OnClickListener {

    public AddAccountDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_account_layout);

        Button addAccountButton = (Button) findViewById(R.id.add_account_button);
        addAccountButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        EditText accountName = (EditText) findViewById(R.id.add_account_name);
        EditText accountBalance = (EditText) findViewById(R.id.add_account_balance);
        Spinner accountType = (Spinner) findViewById(R.id.add_account_category);

        Account newAccount = new Account(accountName.getText().toString(),
                Double.parseDouble(accountBalance.getText().toString()), String.valueOf(accountType.getSelectedItem()));
        AccountDao accountDao = new AccountDao(getContext());

        // save the new transaction
        accountDao.save(newAccount);

        double balance = newAccount.getBalance();
        boolean credit = balance >= 0;
        if(balance != 0) {
            Transaction initialTransaction = new Transaction(credit, "Initial Balance", balance, "", new Date(),
                    newAccount.getId());
            new TransactionDao(getContext()).save(initialTransaction);
        }

        // close dialog
        dismiss();
    }
}
