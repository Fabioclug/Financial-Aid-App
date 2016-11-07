package br.com.fclug.financialaid;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import br.com.fclug.financialaid.adapter.GroupPaymentsListAdapter;
import br.com.fclug.financialaid.models.Group;

public class GroupSummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_summary_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Group group = getIntent().getParcelableExtra("group");

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(group.getName());
        }

        final ExpandableListView groupTransactionsList = (ExpandableListView) findViewById(R.id.group_transactions_list);
        GroupPaymentsListAdapter adapter = new GroupPaymentsListAdapter(this);
        if (groupTransactionsList != null) {
            groupTransactionsList.setAdapter(adapter);

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

        FloatingActionButton addGroupTransaction = (FloatingActionButton) findViewById(R.id.add_group_transaction);
        addGroupTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupSummaryActivity.this, CreateGroupPaymentActivity.class);
                intent.putExtra("group", group);
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
}
