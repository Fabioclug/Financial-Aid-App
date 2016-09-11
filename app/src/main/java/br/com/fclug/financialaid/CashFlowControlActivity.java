package br.com.fclug.financialaid;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import br.com.fclug.financialaid.database.AccountDao;
import br.com.fclug.financialaid.database.TransactionDao;
import br.com.fclug.financialaid.dialog.AddTransactionDialog;
import br.com.fclug.financialaid.models.Account;
import br.com.fclug.financialaid.models.Transaction;

public class CashFlowControlActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private AccountDao mAccountDao;
    private SwipeMenuListView mTransactionList;
    private TransactionDao mTransactionDao;
    private TransactionListAdapter mListAdapter;
    private AddTransactionDialog mAddTransactionView;
    private Account mAccount;

    private SwipeMenuCreator mSwipeMenuCreator = new SwipeMenuCreator() {

        @Override
        public void create(SwipeMenu menu) {
            // create "edit" item
            SwipeMenuItem editItem = new SwipeMenuItem(mContext);
            editItem.setBackground(new ColorDrawable(Color.rgb(0x19, 0x76, 0xD2)));
            editItem.setWidth(AppUtils.dpToPixels(mContext, 90));
            editItem.setIcon(R.drawable.ic_edit);

            // create "delete" item
            SwipeMenuItem deleteItem = new SwipeMenuItem(mContext);
            deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
            deleteItem.setWidth(AppUtils.dpToPixels(mContext, 90));
            deleteItem.setIcon(R.drawable.ic_delete);

            // add items to menu
            menu.addMenuItem(editItem);
            menu.addMenuItem(deleteItem);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_flow_control);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            setTitle(getTitle());
        }

        mContext = this;

        mAccountDao = new AccountDao(mContext);
        mTransactionDao = new TransactionDao(mContext);

        // get the selected account via intent
        long accountId = getIntent().getLongExtra("account", -1);
        mAccount = mAccountDao.findById(accountId);

        //set the bottom view balance
        updateBalance();

        mTransactionList = (SwipeMenuListView) findViewById(R.id.control_entries_list);
        mListAdapter = new TransactionListAdapter(mContext, mAccount);
        mListAdapter.updateListItems();
        mTransactionList.setAdapter(mListAdapter);
        mTransactionList.setMenuCreator(mSwipeMenuCreator);
        mTransactionList.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        // take care of swipe menu clicks
        mTransactionList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                Transaction item = (Transaction) mListAdapter.getItem(position);
                switch (index) {
                    case 0:
                        // edit
                        mAddTransactionView = new AddTransactionDialog(mContext, mAccount, item);
                        mAddTransactionView.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                mAccount = mAccountDao.findById(mAccount.getId());
                                updateBalance();
                                // refresh list
                                mListAdapter.updateListItems();
                            }
                        });

                        // show the dialog
                        mAddTransactionView.show();
                        break;
                    case 1:
                        mTransactionDao.delete(item);
                        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_out);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                mListAdapter.updateListItems();
                            }
                        });
                        //animation.setRepeatCount(1);
                        AppUtils.getViewByPosition(position, mTransactionList).startAnimation(animation);
                        break;
                }
                // close the menu
                return false;
            }
        });

        FloatingActionButton mAddTransactionFab = (FloatingActionButton) findViewById(R.id.add_control_entry);
        if (mAddTransactionFab != null) {
            mAddTransactionFab.setOnClickListener(this);
        }

    }

    @Override
    public void onClick(View v) {
        mAddTransactionView = new AddTransactionDialog(mContext, mAccount);
        mAddTransactionView.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mAccount = mAccountDao.findById(mAccount.getId());
                updateBalance();
                // refresh list
                mListAdapter.updateListItems();
            }
        });

        // show the dialog
        mAddTransactionView.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateBalance() {
        TextView accountBalance = (TextView) findViewById(R.id.balance_value);
        String balance = "$" + mAccount.getBalance();
        if (accountBalance != null) {
            accountBalance.setText(balance);
        }
    }
}
