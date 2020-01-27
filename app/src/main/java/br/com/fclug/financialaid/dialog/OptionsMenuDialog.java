package br.com.fclug.financialaid.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.database.AccountDao;
import br.com.fclug.financialaid.database.TransactionDao;
import br.com.fclug.financialaid.interfaces.OnObjectOperationListener;
import br.com.fclug.financialaid.models.Account;
import br.com.fclug.financialaid.models.Transaction;

/**
 * Created by Fabioclug on 2016-09-12.
 */
public class OptionsMenuDialog extends Dialog implements View.OnClickListener {

    private Context mContext;
    private Account mAccount;
    private Transaction mTransaction;
    private OnDismissListener mDismissListener;
    private OnObjectOperationListener mOperationListener;

    public OptionsMenuDialog(Context context, Account account) {
        super(context);
        mContext = context;
        mAccount = account;
    }

    public OptionsMenuDialog(Context context, Account account, Transaction transaction) {
        super(context);
        mContext = context;
        mAccount = account;
        mTransaction = transaction;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.options_menu_layout);
        LinearLayout edit = findViewById(R.id.option_edit);
        LinearLayout delete = findViewById(R.id.option_delete);
        edit.setOnClickListener(this);
        delete.setOnClickListener(this);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onClick(View v) {
        AccountDao accountDao = new AccountDao(mContext);
        if (mTransaction == null) {
            if (v.getId() == R.id.option_edit) {
                AddAccountDialog dialog = new AddAccountDialog(mContext, mAccount);
                dialog.setOnDismissListener(mDismissListener);
                dialog.setOnTransactionOperationListener(mOperationListener);
                dialog.show();
            } else {
                super.setOnDismissListener(mDismissListener);
                mOperationListener.onDelete(mAccount);
            }
        } else {
            TransactionDao transactionDao = new TransactionDao(mContext);
            if (v.getId() == R.id.option_edit) {
                AddTransactionDialog dialog = new AddTransactionDialog(mContext, mAccount, mTransaction);
                dialog.setOnDismissListener(mDismissListener);
                dialog.setOnTransactionOperationListener(mOperationListener);
                dialog.show();
            } else {
                super.setOnDismissListener(mDismissListener);
                transactionDao.delete(mTransaction);
                accountDao.updateBalance(mAccount, mTransaction.getValue() * -1);
                mOperationListener.onDelete(mTransaction);
            }
        }
        dismiss();
    }

    @Override
    public void setOnDismissListener(OnDismissListener listener) {
        mDismissListener = listener;
    }

    public void setOnObjectOperationListener(OnObjectOperationListener listener) {
        mOperationListener = listener;
    }
}
