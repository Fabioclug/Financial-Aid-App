package br.com.fclug.financialaid.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.fclug.financialaid.AppUtils;
import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.adapter.CategorySpinnerAdapter;
import br.com.fclug.financialaid.database.AccountDao;
import br.com.fclug.financialaid.database.CategoryDao;
import br.com.fclug.financialaid.database.TransactionDao;
import br.com.fclug.financialaid.models.Account;
import br.com.fclug.financialaid.models.Category;
import br.com.fclug.financialaid.models.Transaction;

/**
 * Created by Fabioclug on 2016-08-15.
 */
public class AddTransactionDialog extends Dialog implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private Context mContext;
    private AccountDao mAccountDao;
    private List<Account> mAccounts;
    private List<Category> mCategories;
    private Account mCurrentAccount;
    private Account mSelectedAccount;
    private  Category mSelectedCategory;
    private Transaction mUpdateTransaction;
    private int mCurrentIndex;
    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("MM/dd/yy", Locale.US);

    private EditText mTransactionDescription;
    private Spinner mTransactionCategory;
    private EditText mTransactionValue;
    private EditText mTransactionDate;
    private ImageButton mCreditButton;
    private ImageButton mDebtButton;

    private boolean mTransactionCredit;

    public AddTransactionDialog(Context context, Account currentAccount) {
        super(context);
        mContext = context;
        mCurrentAccount = currentAccount;
    }

    public AddTransactionDialog(Context context, Account currentAccount, Transaction updateTransaction) {
        super(context);
        mContext = context;
        mCurrentAccount = currentAccount;
        mUpdateTransaction = updateTransaction;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mTransactionDescription = (EditText) findViewById(R.id.add_transaction_description);
        mTransactionCategory = (Spinner) findViewById(R.id.add_transaction_category);
        mTransactionValue = (EditText) findViewById(R.id.add_transaction_value);
        mTransactionDate = (EditText) findViewById(R.id.add_transaction_date);
        mCreditButton = (ImageButton) findViewById(R.id.add_transaction_credit);
        mDebtButton = (ImageButton) findViewById(R.id.add_transaction_debt);
        mTransactionValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().trim();
                if (!text.isEmpty()) {
                    AppUtils.handleValueInput(mTransactionValue);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_transaction_view);

        mAccountDao = new AccountDao(mContext);

        Button addTransactionButton = (Button) findViewById(R.id.add_transaction_button);
        addTransactionButton.setOnClickListener(this);

        List<String> accountNames = getAccountList();

        Spinner transactionAccount = (Spinner) findViewById(R.id.add_transaction_account);
        createSpinnerLayout(transactionAccount, accountNames);

        CategoryDao categoryDao = new CategoryDao(mContext);
        mCategories = categoryDao.findAll();
        mTransactionCategory.setAdapter(new CategorySpinnerAdapter(mContext, mCategories));
        mTransactionCategory.setOnItemSelectedListener(this);

        mCreditButton.setColorFilter(ContextCompat.getColor(mContext, R.color.transaction_type_credit));
        mTransactionCredit = true;

        mCreditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreditButton.setColorFilter(ContextCompat.getColor(mContext, R.color.transaction_type_credit));
                mDebtButton.setColorFilter(ContextCompat.getColor(mContext, R.color.color_black));
                mTransactionCredit = true;
            }
        });

        mDebtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreditButton.setColorFilter(ContextCompat.getColor(mContext, R.color.color_black));
                mDebtButton.setColorFilter(ContextCompat.getColor(mContext, R.color.transaction_type_debt));
                mTransactionCredit = false;
            }
        });

        AppUtils.attachCalendarToEditText(mContext, mTransactionDate, mDateFormatter);

        if (mUpdateTransaction != null) {
            TextView title = (TextView) findViewById(R.id.transaction_dialog_title);
            title.setText(R.string.edit_transaction);
            fillToUpdate();
        }
    }

    @Override
    public void onClick(View v) {
        Date date = null;
        try {
            date = mDateFormatter.parse(mTransactionDate.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long accountId = mSelectedAccount.getId();
        TransactionDao transactionDao = new TransactionDao(mContext);
        double value = Double.parseDouble(mTransactionValue.getText().toString());
        value = AppUtils.roundValue(value);
        Log.d("dialog", "value: " + value);
        if (mUpdateTransaction == null) {
            Transaction newTransaction = new Transaction(mTransactionCredit, mTransactionDescription.getText().toString(),
                    value, mSelectedCategory, date, accountId);

            // save the new transaction
            transactionDao.save(newTransaction);
            // update account balance
            mAccountDao.updateBalance(mSelectedAccount, newTransaction);
        } else {
            Transaction updatedTransaction = new Transaction();
            updatedTransaction.setId(mUpdateTransaction.getId());
            updatedTransaction.setDescription(mTransactionDescription.getText().toString());
            updatedTransaction.setValue(value);
            updatedTransaction.setCategory(mSelectedCategory);
            updatedTransaction.setDate(date);
            updatedTransaction.setAccountId(accountId);
            updatedTransaction.setCredit(mTransactionCredit);
            transactionDao.update(updatedTransaction);
            double valueDifference = updatedTransaction.getSignedValue() - mUpdateTransaction.getSignedValue();
            mAccountDao.updateBalance(mSelectedAccount, valueDifference);
        }

        // close dialog
        dismiss();
    }

    private List<String> getAccountList() {
        mAccounts = mAccountDao.findAll();
        List<String> accountNames = new ArrayList<>();
        mCurrentIndex = mAccounts.indexOf(mCurrentAccount);
        for(Account a : mAccounts) {
            accountNames.add(a.getName());
        }
        return accountNames;
    }

    private void createSpinnerLayout(Spinner spinner, List<String> list) {
        ArrayAdapter<String> accountsAdapter = new ArrayAdapter<String>(mContext,
                R.layout.support_simple_spinner_dropdown_item, list);
        spinner.setAdapter(accountsAdapter);
        spinner.setOnItemSelectedListener(this);
        if(mCurrentIndex != -1) {
            spinner.setSelection(mCurrentIndex);
        }
    }

    private void fillToUpdate() {
        mTransactionDescription.setText(mUpdateTransaction.getDescription());
        mTransactionValue.setText(String.valueOf(mUpdateTransaction.getValue()));
        //mTransactionCategory.setSelection(????);
        mTransactionDate.setText(mDateFormatter.format(mUpdateTransaction.getDate()));
        if(mUpdateTransaction.isCredit()) {
            mCreditButton.callOnClick();
        } else {
            mDebtButton.callOnClick();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.add_transaction_account:
                mSelectedAccount = mAccounts.get(position);
                break;
            case R.id.add_transaction_category:
                if (position < mCategories.size()) {
                    mSelectedCategory = mCategories.get(position);
                } else {
                    addCategory();
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void addCategory() {
        //Toast.makeText(mContext, "clicked", Toast.LENGTH_SHORT).show();
        AddCategoryDialog dialog = new AddCategoryDialog(mContext);
        dialog.show();
    }

}
