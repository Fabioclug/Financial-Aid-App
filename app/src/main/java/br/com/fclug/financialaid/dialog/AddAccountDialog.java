package br.com.fclug.financialaid.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import br.com.fclug.financialaid.interfaces.OnObjectOperationListener;
import br.com.fclug.financialaid.models.Account;
import br.com.fclug.financialaid.utils.AppUtils;
import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.database.AccountDao;
import br.com.fclug.financialaid.database.CategoryDao;
import br.com.fclug.financialaid.database.TransactionDao;
import br.com.fclug.financialaid.models.Category;
import br.com.fclug.financialaid.models.Transaction;

/**
 * Created by Fabioclug on 2016-08-14.
 */
public class AddAccountDialog extends Dialog implements View.OnClickListener {

    private Account mUpdateAccount;
    private EditText mAccountName;
    private EditText mAccountBalance;
    private Spinner mAccountType;

    private OnObjectOperationListener mOperationListener;

    public AddAccountDialog(Context context) {
        super(context);
    }

    public AddAccountDialog(Context context, Account account) {
        super(context);
        mUpdateAccount = account;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_account_layout);

        mAccountName = findViewById(R.id.add_account_name);
        mAccountBalance = findViewById(R.id.add_account_balance);
        mAccountType = findViewById(R.id.add_account_category);
        mAccountBalance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().trim();
                if (!text.isEmpty()) {
                    AppUtils.handleValueInput(mAccountBalance);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if (mUpdateAccount != null) {
            TextView title = findViewById(R.id.account_dialog_title);
            title.setText(R.string.edit_account);
            mAccountName.setText(mUpdateAccount.getName());
            TextView balanceLabel = findViewById(R.id.add_account_balance_label);
            balanceLabel.setVisibility(View.GONE);
            mAccountBalance.setVisibility(View.GONE);
            mAccountBalance.setText(String.valueOf(mUpdateAccount.getBalance()));
            String[] accountTypes = getContext().getResources().getStringArray(R.array.account_types);
            mAccountType.setSelection(Arrays.asList(accountTypes).indexOf(mUpdateAccount.getType()));
        }

        Button addAccountButton = findViewById(R.id.add_account_button);
        addAccountButton.setOnClickListener(this);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onClick(View v) {
        AccountDao accountDao = new AccountDao(getContext());

        String accountName = mAccountName.getText().toString();
        String accountType = String.valueOf(mAccountType.getSelectedItem());

        if (mUpdateAccount == null) {
            long accountBalance = AppUtils.extractCurrencyValue(mAccountBalance.getText().toString());
            Account newAccount = new Account(accountName, accountBalance, accountType);

            // save the new transaction
            accountDao.save(newAccount);

            long balance = newAccount.getBalance();

            List<Category> categories = new CategoryDao(getContext()).findIncoming();

            if (balance != 0) {
                Transaction initialTransaction = new Transaction("Initial Balance", balance, categories.get(0),
                        new Date(), newAccount.getId());
                new TransactionDao(getContext()).save(initialTransaction);
            }
            mOperationListener.onAdd(newAccount);
        } else {

            mUpdateAccount.setName(accountName);
            //mUpdateAccount.setBalance(accountBalance);
            mUpdateAccount.setType(accountType);
            accountDao.update(mUpdateAccount);
            mOperationListener.onUpdate(mUpdateAccount);
        }

        // close dialog
        dismiss();
    }

    public void setOnTransactionOperationListener(OnObjectOperationListener listener) {
        mOperationListener = listener;
    }
}
