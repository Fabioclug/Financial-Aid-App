package br.com.fclug.financialaid;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.fclug.financialaid.adapter.GroupPaymentsListAdapter;
import br.com.fclug.financialaid.models.Group;
import br.com.fclug.financialaid.models.GroupDebt;
import br.com.fclug.financialaid.models.TransactionSplit;
import br.com.fclug.financialaid.models.User;
import br.com.fclug.financialaid.server.ApiRequest;
import br.com.fclug.financialaid.server.ServerUtils;

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

        mGroupMembers = new HashMap<String, User>();
        for (User u : mGroup.getMembers()) {
            mGroupMembers.put(u.getUsername(), u);
        }

        mGroupOverview = (TextView) findViewById(R.id.group_summary_overview);
        //String overviewText = String.format(getResources().getString(R.string.group_summary_debt), "Fulano", 30.0, "Ciclano");
        //mGroupOverview.setText(overviewText);
        getGroupDebts();

        final ExpandableListView groupTransactionsList = (ExpandableListView) findViewById(R.id.group_transactions_list);
        GroupPaymentsListAdapter adapter = new GroupPaymentsListAdapter(this, mGroup);
        if (groupTransactionsList != null) {
            groupTransactionsList.setAdapter(adapter);
            adapter.updateListItems(mGroupMembers);

            groupTransactionsList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                @Override
                public void onGroupExpand(int groupPosition) {

                }
            });
            groupTransactionsList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
                @Override
                public void onGroupCollapse(int groupPosition) {

                }
            });
            groupTransactionsList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    return false;
                }
            });
        }

        FloatingActionButton addGroupTransactionButton = (FloatingActionButton) findViewById(R.id.add_group_transaction);
        addGroupTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupSummaryActivity.this, CreateGroupPaymentActivity.class);
                intent.putExtra("group", mGroup);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private TransactionSplit closest(double value, List<TransactionSplit> debits) {
        double min = Integer.MAX_VALUE;
        TransactionSplit closest = null;

        for (TransactionSplit debit : debits) {
            double debitValue = debit.getValue();
            final double diff = Math.abs(debitValue - value);

            if (diff < min) {
                min = diff;
                closest = debit;
            }
        }

        return closest;
    }

    private void calculateGroupDebts() {
        if (mGroupDebts == null) {
            // create debts between everyone in the group
            mGroupDebts = new GroupDebtHashMap<>();
            List<User> members = mGroup.getMembers();
            for (int i = 0; i < members.size() - 1; i++) {
                for (int j = i+1; j < members.size(); j++) {
                    mGroupDebts.put(new GroupDebt(members.get(i), members.get(j), 0));
                }
            }
        }

        for (TransactionSplit credit : mGroupCredits) {
            while (credit.getValue() >= 0.01) {
                Log.d("value", ": " + credit.getValue());
                double creditValue = credit.getValue();
                // get the debt from the list that has the closest value to what we need
                TransactionSplit debit = closest(creditValue, mGroupDebits);
                double debitValue = debit.getValue();

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

    }

    private void updateGroupOverview() {
        String overview = "";

        for (Object obj : mGroupDebts.values()) {
            GroupDebt debt = (GroupDebt) obj;
            if (debt.getValue() != 0) {
                overview += String.format(getResources().getString(R.string.group_summary_debt),
                        debt.getDebtor().getName(), debt.getValue(), debt.getCreditor().getName()) + "\n";
            }
        }

        if (overview.isEmpty()) {
            overview = getResources().getString(R.string.group_summary_no_debts);
        }

        mGroupOverview.setText(overview);
    }

    private void getGroupDebts() {
        SessionManager session = new SessionManager(this);
        JSONObject args = new JSONObject();
        try {
            args.put("token", session.getToken());
            args.put("group_id", mGroup.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.RequestCallback callback = new ApiRequest.RequestCallback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                mGroupCredits = new ArrayList<>();
                mGroupDebits = new ArrayList<>();
                JSONArray result = response.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject creditJson = result.getJSONObject(i);
                    String member = creditJson.getString("user_id");
                    double value = creditJson.getDouble("value");
                    TransactionSplit entry = new TransactionSplit(mGroupMembers.get(member), value);
                    if (value < 0) {
                        entry.setValue(entry.getValue() * -1);
                        mGroupDebits.add(entry);
                    }
                    else {
                        mGroupCredits.add(entry);
                    }
                }
                calculateGroupDebts();
                updateGroupOverview();
            }

            @Override
            public void onFailure(int code) {

            }
        };

        new ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_GET_CREDITS, args, callback).execute();
    }
}
