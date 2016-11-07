package br.com.fclug.financialaid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.borax12.materialdaterangepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import br.com.fclug.financialaid.database.AccountDao;
import br.com.fclug.financialaid.database.TransactionDao;
import br.com.fclug.financialaid.dialog.AddTransactionDialog;
import br.com.fclug.financialaid.dialog.OptionsMenuDialog;
import br.com.fclug.financialaid.models.Account;
import br.com.fclug.financialaid.models.Transaction;

public class CashFlowControlActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private AccountDao mAccountDao;
    private TextView mPeriodTitle;
    private SwipeMenuListView mTransactionList;
    private TransactionDao mTransactionDao;
    private TransactionListAdapter mListAdapter;
    private AddTransactionDialog mAddTransactionView;
    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private Account mAccount;

    private ImageButton mPreviousButton;
    private ImageButton mNextButton;

    private SwipeMenuCreator mSwipeMenuCreator = new SwipeMenuCreator() {

        @Override
        public void create(SwipeMenu menu) {
            // create "edit" item
            SwipeMenuItem editItem = new SwipeMenuItem(mContext);
            editItem.setBackground(new ColorDrawable(ContextCompat.getColor(mContext, R.color.colorPrimary)));
            editItem.setWidth(AppUtils.dpToPixels(mContext, 90));
            editItem.setIcon(R.drawable.ic_edit);

            // create "delete" item
            SwipeMenuItem deleteItem = new SwipeMenuItem(mContext);
            deleteItem.setBackground(new ColorDrawable(ContextCompat.getColor(mContext, R.color.transaction_type_debt)));
            deleteItem.setWidth(AppUtils.dpToPixels(mContext, 90));
            deleteItem.setIcon(R.drawable.ic_delete);

            // add items to menu
            menu.addMenuItem(editItem);
            menu.addMenuItem(deleteItem);
        }
    };

    private DialogInterface.OnDismissListener mDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            mAccount = mAccountDao.findById(mAccount.getId());
            updateBalance();
            // refresh list
            mListAdapter.updateListItems();
        }
    };

    private AdapterView.OnItemLongClickListener mTransactionLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            Transaction clickedTransaction = (Transaction) mListAdapter.getItem(position - mTransactionList.getHeaderViewsCount());
            OptionsMenuDialog dialog = new OptionsMenuDialog(mContext, mAccount, clickedTransaction);
            dialog.setOnDismissListener(mDismissListener);
            dialog.show();
            return true;
        }
    };

    private DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
            Calendar startPeriod = Calendar.getInstance();
            startPeriod.set(year, monthOfYear, dayOfMonth);
            Calendar endPeriod = Calendar.getInstance();
            endPeriod.set(yearEnd, monthOfYearEnd, dayOfMonthEnd);
            AppUtils.setTime(startPeriod, 0, 0, 0, 0);
            AppUtils.setTime(endPeriod, 23, 59, 59, 59);
            Date start = startPeriod.getTime();
            Date end = endPeriod.getTime();
            changePeriodLabel(start, end);
            changePeriodFilter(start, end);
            mPreviousButton.setVisibility(View.GONE);
            mNextButton.setVisibility(View.GONE);
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
            actionBar.setDisplayShowTitleEnabled(false);
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
        registerForContextMenu(mTransactionList);

        LayoutInflater inflater = getLayoutInflater();

        // set the account overview and the period header as headers for the list
        ViewGroup accountOverview = (ViewGroup) inflater.inflate(R.layout.account_overview, mTransactionList, false);
        mTransactionList.addHeaderView(accountOverview, null, false);
        ViewGroup periodHeader = (ViewGroup) inflater.inflate(R.layout.period_header, mTransactionList, false);
        mTransactionList.addHeaderView(periodHeader, null, false);

        mPreviousButton = (ImageButton) periodHeader.findViewById(R.id.transactions_previous);
        mNextButton = (ImageButton) periodHeader.findViewById(R.id.transactions_next);

        // replace the overview with the account info
        TextView accountName = (TextView) accountOverview.findViewById(R.id.account_overview_name);
        TextView accountType = (TextView) accountOverview.findViewById(R.id.account_overview_type);
        ImageView accountImage = (ImageView) accountOverview.findViewById(R.id.account_overview_image);

        accountName.setText(mAccount.getName());
        accountType.setText(mAccount.getType() + " account");

        int[] accountColors = getResources().getIntArray(R.array.account_colors);
        TypedArray accountIcons = getResources().obtainTypedArray(R.array.account_icons);

        @DrawableRes int accountIcon = R.drawable.ic_cash;
        GradientDrawable gd = (GradientDrawable) accountImage.getBackground();
        gd.setColor(ContextCompat.getColor(mContext, R.color.account_image_bg));
        int typeIndex = AppUtils.getAccountTypeIndex(mContext, mAccount.getType());
        if (typeIndex != -1) {
            gd.setColor(accountColors[typeIndex]);
            accountIcon = accountIcons.getResourceId(typeIndex, R.drawable.ic_cash);
        }
        accountImage.setImageResource(accountIcon);
        accountIcons.recycle();

        // set period title (like "October 2016")
        mPeriodTitle = (TextView) periodHeader.findViewById(R.id.transactions_period);
        Calendar startPeriod = Calendar.getInstance();
        String period = startPeriod.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + startPeriod.get(Calendar.YEAR);
        mPeriodTitle.setText(period);

        startPeriod.set(Calendar.DAY_OF_MONTH, 1);
        Calendar endPeriod = Calendar.getInstance();
        endPeriod.set(Calendar.DAY_OF_MONTH, endPeriod.getActualMaximum(Calendar.DAY_OF_MONTH));
        AppUtils.setTime(startPeriod, 0, 0, 0, 0);
        AppUtils.setTime(endPeriod, 23, 59, 59, 59);
        changePeriodFilter(startPeriod.getTime(), endPeriod.getTime());

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
                        mAddTransactionView.setOnDismissListener(mDismissListener);
                        mAddTransactionView.show();
                        break;
                    case 1:
                        mTransactionDao.delete(item);
                        item.setCredit(!item.isCredit());
                        mAccountDao.updateBalance(mAccount, item);
                        updateBalance();
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
                        AppUtils.getViewByPosition(position, mTransactionList, 2).startAnimation(animation);
                        break;
                }
                // close the menu
                return false;
            }
        });
        mTransactionList.setOnItemLongClickListener(mTransactionLongClickListener);

        FloatingActionButton mAddTransactionFab = (FloatingActionButton) findViewById(R.id.add_control_entry);
        if (mAddTransactionFab != null) {
            mAddTransactionFab.setOnClickListener(this);
        }

    }

    @Override
    public void onClick(View v) {
        mAddTransactionView = new AddTransactionDialog(mContext, mAccount);
        mAddTransactionView.setOnDismissListener(mDismissListener);
        // show the dialog
        mAddTransactionView.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_change_period:
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        mDateListener,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.setOnDateSetListener(mDateListener);
                dpd.show(getFragmentManager(), "DatePickerDialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the app bar with items from menu.
        getMenuInflater().inflate(R.menu.transactions_menu, menu);
        return true;
    }

    private void updateBalance() {
        TextView accountBalance = (TextView) findViewById(R.id.balance_value);
        String balance = mAccount.getBalance() + "$";
        if (accountBalance != null) {
            accountBalance.setText(balance);
        }
    }

    private void changePeriodLabel(Date startPeriod, Date endPeriod) {
        String periodTitle = mDateFormatter.format(startPeriod) + " - " + mDateFormatter.format(endPeriod);
        mPeriodTitle.setText(periodTitle);
    }

    private void changePeriodFilter(Date startPeriod, Date endPeriod) {
        mListAdapter.setPeriodStart(startPeriod);
        mListAdapter.setPeriodEnd(endPeriod);
        mListAdapter.updateListItems();
    }
}
