package br.com.fclug.financialaid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.models.GroupTransaction;
import br.com.fclug.financialaid.models.TransactionSplit;
import br.com.fclug.financialaid.models.User;

/**
 * Created by Fabioclug on 2016-10-30.
 */

public class GroupPaymentsListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<GroupTransaction> mGroupTransactions;

    public GroupPaymentsListAdapter(Context context) {
        this.mContext = context;
        mGroupTransactions = new ArrayList<>();
        User fabio = new User("fclug", "Fabio Clug");
        User audrey = new User("adey", "Audrey Clug");
        GroupTransaction tr1 = new GroupTransaction(1, "Compra na padaria", fabio, 50);
        GroupTransaction tr2 = new GroupTransaction(1, "Mercado", audrey, 80);
        TransactionSplit sp1 = new TransactionSplit(fabio, 25);
        TransactionSplit sp2 = new TransactionSplit(audrey, 25);
        tr1.addSplit(sp1);
        tr1.addSplit(sp2);
        tr2.addSplit(sp2);
        mGroupTransactions.add(tr1);
        mGroupTransactions.add(tr2);
    }

    @Override
    public int getGroupCount() {
        return mGroupTransactions.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mGroupTransactions.get(groupPosition).getSplits().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroupTransactions.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroupTransactions.get(groupPosition).getSplits().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return mGroupTransactions.get(groupPosition).getId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupTransaction item = (GroupTransaction) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.group_payment_list_row, null);
        }
        TextView transactionName = (TextView) convertView.findViewById(R.id.group_transaction_name);
        TextView transactionValue = (TextView) convertView.findViewById(R.id.group_transaction_value);
        TextView transactionPayer = (TextView) convertView.findViewById(R.id.group_transaction_payer);
        ImageView transactionExpand = (ImageView) convertView.findViewById(R.id.group_transaction_expand);

        transactionName.setText(item.getDescription());
        transactionValue.setText(String.valueOf(item.getValue()));
        transactionPayer.setText(item.getPayer().getName());

        int transactionExpandedImage = isExpanded ? R.drawable.ic_collapse : R.drawable.ic_expand;
        transactionExpand.setImageResource(transactionExpandedImage);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TransactionSplit item = (TransactionSplit) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.transaction_split_list_row, null);
        }
        TextView transactionSplitDebtor = (TextView) convertView.findViewById(R.id.transaction_split_debtor);
        TextView transactionSplitValue = (TextView) convertView.findViewById(R.id.transaction_split_value);
        transactionSplitDebtor.setText(item.getDebtor().getName());
        transactionSplitValue.setText(String.valueOf(item.getValue()));

        View divider = convertView.findViewById(R.id.transaction_split_divider);
        if(isLastChild) {
            divider.setVisibility(View.VISIBLE);
        } else {
            divider.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
