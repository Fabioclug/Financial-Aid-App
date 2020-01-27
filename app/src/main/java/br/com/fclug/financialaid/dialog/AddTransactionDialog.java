package br.com.fclug.financialaid.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.fclug.financialaid.interfaces.OnObjectOperationListener;
import br.com.fclug.financialaid.utils.AppUtils;
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
public class AddTransactionDialog extends Dialog implements AdapterView.OnItemSelectedListener, View.OnClickListener, DialogInterface.OnDismissListener{

    private Context mContext;
    private AccountDao mAccountDao;
    private CategoryDao mCategoryDao;
    private List<Account> mAccounts;
    private List<Category> mIncomingCategories;
    private List<Category> mOutgoingCategories;
    private Account mCurrentAccount;
    private Account mSelectedAccount;
    private Category mSelectedCategory;
    private Transaction mUpdateTransaction;
    private int mCurrentIndex;
    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("MM/dd/yy", Locale.US);

    private EditText mTransactionDescription;
    private Spinner mTransactionCategory;
    private EditText mTransactionValue;
    private EditText mTransactionDate;
    private ImageButton mCreditButton;
    private ImageButton mDebtButton;

    private CategorySpinnerAdapter mCategoriesAdapter;
    private OnObjectOperationListener mOperationListener;

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

        mCategoryDao = new CategoryDao(mContext);
        mIncomingCategories = mCategoryDao.findIncoming();
        mOutgoingCategories = mCategoryDao.findOutgoing();
        mCategoriesAdapter = new CategorySpinnerAdapter(mContext, mIncomingCategories);
        mTransactionCategory.setAdapter(mCategoriesAdapter);
        mTransactionCategory.setOnItemSelectedListener(this);

        mCreditButton.setColorFilter(ContextCompat.getColor(mContext, R.color.transaction_type_credit));
        mTransactionCredit = true;

        mCreditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreditButton.setColorFilter(ContextCompat.getColor(mContext, R.color.transaction_type_credit));
                mDebtButton.setColorFilter(ContextCompat.getColor(mContext, R.color.color_black));
                mCategoriesAdapter.setCategories(mIncomingCategories);
                mTransactionCredit = true;
            }
        });

        mDebtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreditButton.setColorFilter(ContextCompat.getColor(mContext, R.color.color_black));
                mDebtButton.setColorFilter(ContextCompat.getColor(mContext, R.color.transaction_type_debt));
                mCategoriesAdapter.setCategories(mOutgoingCategories);
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
        long value = AppUtils.extractCurrencyValue(mTransactionValue.getText().toString());
        if (!mTransactionCredit) {
            value *= -1;
        }
        if (mUpdateTransaction == null) {
            Transaction newTransaction = new Transaction(mTransactionDescription.getText().toString(), value,
                    mSelectedCategory, date, accountId);

            // save the new transaction
            transactionDao.save(newTransaction);
            // update account balance
            mAccountDao.updateBalance(mSelectedAccount, newTransaction);
            mOperationListener.onAdd(newTransaction);
        } else {
            Transaction updatedTransaction = new Transaction(mUpdateTransaction.getId(),
                    mTransactionDescription.getText().toString(), value, mSelectedCategory, date,
                    accountId);
            transactionDao.update(updatedTransaction);
            long valueDifference = updatedTransaction.getValue() - mUpdateTransaction.getValue();
            mAccountDao.updateBalance(mSelectedAccount, valueDifference);
            mOperationListener.onUpdate(updatedTransaction);
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
        mTransactionValue.setText(mUpdateTransaction.getFormattedValue());
        // place cursor at end of value
        mTransactionValue.setSelection(mTransactionValue.getText().length());
        //mTransactionCategory.setSelection(????);
        mTransactionDate.setText(mDateFormatter.format(mUpdateTransaction.getDate()));
        if(mUpdateTransaction.getValue() >= 0) {
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
                List<Category> categories = mTransactionCredit? mIncomingCategories : mOutgoingCategories;
                if (position < categories.size()) {
                    mSelectedCategory = categories.get(position);
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
        AddCategoryDialog dialog = new AddCategoryDialog(mContext);
        dialog.setOnDismissListener(this);
        dialog.show();
    }

    public void setOnTransactionOperationListener(OnObjectOperationListener listener) {
        mOperationListener = listener;
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        mIncomingCategories = mCategoryDao.findIncoming();
        mOutgoingCategories = mCategoryDao.findOutgoing();
        if (mTransactionCredit) {
            mCategoriesAdapter.setCategories(mIncomingCategories);
        } else {
            mCategoriesAdapter.setCategories(mOutgoingCategories);
        }
    }
}
