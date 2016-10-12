package br.com.fclug.financialaid.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.fclug.financialaid.AppUtils;
import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.database.AccountDao;
import br.com.fclug.financialaid.database.TransactionDao;
import br.com.fclug.financialaid.models.Account;
import br.com.fclug.financialaid.models.Transaction;

/**
 * Created by Fabioclug on 2016-08-15.
 */
public class AddTransactionDialog extends Dialog implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private Context mContext;
    private AccountDao mAccountDao;
    private List<Account> mAccounts;
    private Account mCurrentAccount;
    private Account mSelectedAccount;
    private Transaction mUpdateTransaction;
    private int mCurrentIndex;
    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("MM/dd/yy", Locale.US);

    private EditText mTransactionDescription;
    private EditText mTransactionCategory;
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
        mTransactionCategory = (EditText) findViewById(R.id.add_transaction_category);
        mTransactionValue = (EditText) findViewById(R.id.add_transaction_value);
        mTransactionDate = (EditText) findViewById(R.id.add_transaction_date);
        mCreditButton = (ImageButton) findViewById(R.id.add_transaction_credit);
        mDebtButton = (ImageButton) findViewById(R.id.add_transaction_debt);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_transaction_view);

        mAccountDao = new AccountDao(mContext);

        Button addTransactionButton = (Button) findViewById(R.id.add_transaction_button);
        addTransactionButton.setOnClickListener(this);

        final Calendar myCalendar = Calendar.getInstance();
        mTransactionDate.setFocusable(false);

        List<String> accountNames = getAccountList();

        Spinner transactionAccount = (Spinner) findViewById(R.id.add_transaction_account);
        createSpinnerLayout(transactionAccount, accountNames);
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

        // build the calendar to pick a date
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                mTransactionDate.setText(mDateFormatter.format(myCalendar.getTime()));

            }

        };

        // show the calendar when the field is clicked
        mTransactionDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(mContext, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        if (mUpdateTransaction != null) {
            TextView title = (TextView) findViewById(R.id.transaction_dialog_title);
            title.setText(R.string.edit_transaction);
            fillToUpdate();
        }
    }

    @Override
    public void onClick(View v) {
        EditText transactionDescription = (EditText) findViewById(R.id.add_transaction_description);
        EditText transactionCategory = (EditText) findViewById(R.id.add_transaction_category);
        EditText transactionValue = (EditText) findViewById(R.id.add_transaction_value);
        EditText transactionDate = (EditText) findViewById(R.id.add_transaction_date);
        Date date = null;
        try {
            date = mDateFormatter.parse(transactionDate.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long accountId = mSelectedAccount.getId();
        TransactionDao transactionDao = new TransactionDao(mContext);
        double value = Double.parseDouble(transactionValue.getText().toString());
        value = AppUtils.roundValue(value);
        Log.d("dialog", "value: " + value);
        if (mUpdateTransaction == null) {
            Transaction newTransaction = new Transaction(mTransactionCredit, transactionDescription.getText().toString(),
                    value, transactionCategory.getText().toString(), date, accountId);

            // save the new transaction
            transactionDao.save(newTransaction);
            // update account balance
            mAccountDao.updateBalance(mSelectedAccount, newTransaction);
        } else {
            Transaction updatedTransaction = new Transaction();
            updatedTransaction.setId(mUpdateTransaction.getId());
            updatedTransaction.setDescription(transactionDescription.getText().toString());
            updatedTransaction.setValue(value);
            updatedTransaction.setCategory(transactionCategory.getText().toString());
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
        mTransactionCategory.setText(mUpdateTransaction.getCategory());
        mTransactionDate.setText(mDateFormatter.format(mUpdateTransaction.getDate()));
        if(mUpdateTransaction.isCredit()) {
            mCreditButton.callOnClick();
        } else {
            mDebtButton.callOnClick();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mSelectedAccount = mAccounts.get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
