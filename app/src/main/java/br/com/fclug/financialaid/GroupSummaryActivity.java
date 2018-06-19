package br.com.fclug.financialaid;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import br.com.fclug.financialaid.adapter.GroupTransactionRecyclerViewListAdapter;
import br.com.fclug.financialaid.database.GroupDao;
import br.com.fclug.financialaid.models.Group;
import br.com.fclug.financialaid.models.GroupDebt;
import br.com.fclug.financialaid.models.GroupTransaction;
import br.com.fclug.financialaid.models.TransactionSplit;
import br.com.fclug.financialaid.models.User;
import br.com.fclug.financialaid.server.ApiRequest;
import br.com.fclug.financialaid.server.ServerUtils;
import br.com.fclug.financialaid.utils.AppConstants;
import br.com.fclug.financialaid.utils.AppUtils;
import br.com.fclug.financialaid.utils.SwipeUtil;

public class GroupSummaryActivity extends AppCompatActivity {

    private class GroupDebtHashMap<K, V> extends HashMap {

        public String generateKey(String user1, String user2) {
            String key1 = user1.toLowerCase();
            String key2 = user2.toLowerCase();
            String key;
            if (key1.compareTo(key2) < 0) {
                key = key1 + " " + key2;
            } else {
                key = key2 + " " + key1;
            }
            return key;
        }

        public Object put(GroupDebt debt) {
            String user1 = debt.getCreditor().getUsername();
            String user2 = debt.getDebtor().getUsername();
            return super.put(generateKey(user1, user2), debt);
        }

        public GroupDebt get(String user1, String user2) {
            return (GroupDebt) super.get(generateKey(user1, user2));
        }
    }

    private Group mGroup;
    private List<TransactionSplit> mGroupCredits;
    private List<TransactionSplit> mGroupDebits;
    private GroupDebtHashMap<String, GroupDebt> mGroupDebts;
    private HashMap<String, User> mGroupMembers;
    private TextView mGroupOverview;
    private RecyclerView mGroupTransactionsRecyclerView;
    private GroupTransactionRecyclerViewListAdapter mListAdapter;
    private boolean mGroupUpdated = false;

    public static int REQUEST_ADD_TRANSACTION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_summary_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mGroup = getIntent().getParcelableExtra("group");

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(mGroup.getName());
        }

        mGroupMembers = mGroup.getMembersDictionary();

        mGroupTransactionsRecyclerView = (RecyclerView) findViewById(R.id.group_transactions_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mGroupTransactionsRecyclerView.setLayoutManager(layoutManager);
        mGroupTransactionsRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        mListAdapter = new GroupTransactionRecyclerViewListAdapter(this, mGroup);
        mGroupTransactionsRecyclerView.setAdapter(mListAdapter);
        mListAdapter.setListItems();
        mGroupTransactionsRecyclerView.setNestedScrollingEnabled(false);
        setSwipeForRecyclerView();

        mGroupOverview = (TextView) findViewById(R.id.group_summary_overview);

        updateGroupDebts(false);

        FloatingActionButton addGroupTransactionButton =
                (FloatingActionButton) findViewById(R.id.add_group_transaction);
        addGroupTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupSummaryActivity.this, CreateGroupPaymentActivity.class);
                intent.putExtra("group", mGroup);
                startActivityForResult(intent, REQUEST_ADD_TRANSACTION);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_remove_group:
                final Intent returnIntent = new Intent();
                new AlertDialog.Builder(GroupSummaryActivity.this).setTitle(R.string.remove_group_title)
                        .setMessage(getString(R.string.remove_group_confirm))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int id) {
                                returnIntent.putExtra("operation", AppConstants.GROUP_OPERATION_DELETE);
                                setResult(RESULT_OK, returnIntent);
                                finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu, menu);
        if (mGroup.isOnline()) {
            menu.findItem(R.id.action_remove_group).setIcon(R.drawable.ic_exit);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            GroupTransaction transaction = data.getParcelableExtra("transaction");
            mListAdapter.addTransaction(transaction);
            updateGroupDebts(true);
            mGroupUpdated = true;
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("operation", AppConstants.GROUP_OPERATION_UPDATE);
        returnIntent.putExtra("updated", mGroupUpdated);
        returnIntent.putExtra("group", mGroup);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private TransactionSplit closest(double value, List<TransactionSplit> debits) {
        double min = Integer.MAX_VALUE;
        TransactionSplit closest = null;

        for (TransactionSplit debit : debits) {
            double debitValue = debit.getValue();
            double diff = Math.abs(debitValue - value);

            if (diff < min) {
                min = diff;
                closest = debit;
            }
        }

        return closest;
    }

    private void updateGroupDebts(boolean reloadBalances) {
        if (reloadBalances) {
            if (mGroup.isOnline()) {
                getOnlineGroupBalances();
            } else {
                GroupDao dao = new GroupDao(this);
                mGroup.setGroupBalances(dao.getGroupCredits(mGroup, mGroupMembers));
            }
        }
        if (!reloadBalances || !mGroup.isOnline()) {
            classifyBalances();
            calculateGroupDebts();
        }
    }

    private void calculateGroupDebts() {
        // create debts between everyone in the group
        mGroupDebts = new GroupDebtHashMap<>();
        List<User> members = mGroup.getMembers();
        for (int i = 0; i < members.size() - 1; i++) {
            for (int j = i+1; j < members.size(); j++) {
                mGroupDebts.put(new GroupDebt(members.get(i), members.get(j), 0));
            }
        }

        for (TransactionSplit credit : mGroupCredits) {
            while (AppUtils.roundValue(credit.getValue()) > 0.03) {
                double creditValue = AppUtils.roundValue(credit.getValue());
                Log.d("GroupSummaryActivity", "credit value: " + creditValue);
                // get the debt from the list that has the closest value to what we need
                TransactionSplit debit = closest(creditValue, mGroupDebits);
                double debitValue = AppUtils.roundValue(debit.getValue());
                Log.d("GroupSummaryActivity", "debit value: " + debitValue);

                String creditor = credit.getDebtor().getUsername();
                String debtor = debit.getDebtor().getUsername();

                // get the debt that involves both members
                GroupDebt debt = mGroupDebts.get(creditor, debtor);

                // if the credit is smaller, this credit will be cleared
                if (creditValue < debitValue) {
                    credit.setValue(0);
                    debit.setValue(debitValue - creditValue);
                    if (creditor.equals(debt.getCreditor().getUsername())) {
                        debt.setValue(debt.getValue() + creditValue);
                    } else {
                        debt.setValue(debt.getValue() - creditValue);
                    }
                }
                // if the debit is smaller, then is the debit that will be cleared
                else {
                    debit.setValue(0);
                    credit.setValue(creditValue - debitValue);
                    if (creditor.equals(debt.getCreditor().getUsername())) {
                        debt.setValue(debt.getValue() + debitValue);
                    } else {
                        debt.setValue(debt.getValue() - debitValue);
                    }
                }

                // the value of the debt will be kept positive
                if (debt.getValue() < 0) {
                    debt.swapMembers();
                }
            }
        }

        updateGroupOverview();
    }

    private void updateGroupOverview() {
        StringBuilder overview = new StringBuilder();

        for (Object obj : mGroupDebts.values()) {
            GroupDebt debt = (GroupDebt) obj;
            if (debt.getValue() != 0) {
                overview.append(String.format(getResources().getString(R.string.group_summary_debt),
                        debt.getDebtor().getExhibitName(), debt.getCreditor().getExhibitName(),
                        AppUtils.formatCurrencyValue(debt.getValue())));
            }
        }

        if (overview.toString().isEmpty()) {
            overview.append(getResources().getString(R.string.group_summary_no_debts));
        }

        mGroupOverview.setText(overview.toString().trim());
    }

    private void getOnlineGroupBalances() {
        JSONObject args = new JSONObject();
        try {
            args.put("token", SessionManager.getToken(this));
            args.put("group_id", mGroup.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.RequestCallback callback = new ApiRequest.RequestCallback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                mGroupCredits = new ArrayList<>();
                mGroupDebits = new ArrayList<>();
                List<TransactionSplit> memberBalances = new ArrayList<>();
                JSONArray result = response.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject creditJson = result.getJSONObject(i);
                    String member = creditJson.getString("user_id");
                    double value = creditJson.getDouble("value");
                    memberBalances.add(new TransactionSplit(mGroupMembers.get(member), value));
                }
                mGroup.setGroupBalances(memberBalances);
                classifyBalances();
                calculateGroupDebts();
            }

            @Override
            public void onFailure(int code) {

            }
        };

        new ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_GET_CREDITS, args, callback).execute();
    }

    private void classifyBalances() {
        mGroupCredits = new ArrayList<>();
        mGroupDebits = new ArrayList<>();
        List<TransactionSplit> memberBalances = mGroup.getGroupBalances();
        for (TransactionSplit balance : memberBalances) {
            TransactionSplit newBalance = new TransactionSplit(balance);
            if (balance.getValue() < 0) {
                newBalance.setValue(newBalance.getValue() * -1);
                mGroupDebits.add(newBalance);
            }
            else {
                mGroupCredits.add(newBalance);
            }
        }
    }

    private void setSwipeForRecyclerView() {

        final SwipeUtil swipeHelper = new SwipeUtil(this) {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    int index = viewHolder.getAdapterPosition();
                    //final GroupTransaction transaction = mListAdapter.getItem(index);
                    mListAdapter.setPendingRemoval(index, true, new Runnable() {
                        @Override
                        public void run() {
                            updateGroupDebts(true);
                            mGroupUpdated = true;
                        }
                    });
                }
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                if (mListAdapter.isPendingRemoval(position)) {
                    return 0;
                }
                mListAdapter.collapseView(mGroupTransactionsRecyclerView, position);
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

        };

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(swipeHelper);
        mItemTouchHelper.attachToRecyclerView(mGroupTransactionsRecyclerView);

        //set swipe label
        swipeHelper.setLeftSwipeLabel("Delete");
        //set swipe background-Color
        swipeHelper.setLeftSwipeColor(ContextCompat.getColor(this, R.color.swipe_background));
        swipeHelper.setRightSwipeColor(ContextCompat.getColor(this, R.color.electronics_category));

    }
}
