package br.com.fclug.financialaid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.SessionManager;
import br.com.fclug.financialaid.database.GroupDao;
import br.com.fclug.financialaid.models.Group;
import br.com.fclug.financialaid.models.GroupTransaction;
import br.com.fclug.financialaid.models.TransactionSplit;
import br.com.fclug.financialaid.models.User;
import br.com.fclug.financialaid.server.ApiRequest;
import br.com.fclug.financialaid.server.ServerUtils;
import br.com.fclug.financialaid.utils.AppUtils;

/**
 * Created by Fabioclug on 2016-10-30.
 */

public class GroupPaymentsListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<GroupTransaction> mGroupTransactions;
    private Group mGroup;
    public static SimpleDateFormat buildDateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("MM/dd", Locale.US);

    public GroupPaymentsListAdapter(Context context, Group group) {
        this.mContext = context;
        mGroupTransactions = new ArrayList<>();
        mGroup = group;
    }

    public void updateListItems(HashMap<String, User> members) {
        if (mGroup.isOnline()) {
            getOnlineTransactions(members);
        } else {
            getOfflineTransactions(members);
        }
    }

    private void getOnlineTransactions(final HashMap<String, User> members) {
        JSONObject args = new JSONObject();
        try {
            args.put("token", SessionManager.getToken(mContext));
            args.put("group_id", mGroup.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.RequestCallback callback =  new ApiRequest.RequestCallback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                mGroupTransactions.clear();
                try {
                    JSONArray result = response.getJSONArray("result");
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject transactionJson = result.getJSONObject(i);
                        User payer = members.get(transactionJson.getString("creditor"));
                        GroupTransaction transaction = new GroupTransaction(transactionJson.getLong("id"),
                                transactionJson.getString("name"), payer, transactionJson.getDouble("value"),
                                buildDateFormatter.parse(transactionJson.getString("moment")));
                        JSONArray transactionSplits = transactionJson.getJSONArray("splits");
                        for (int j = 0; j < transactionSplits.length(); j++) {
                            JSONObject splitJson = transactionSplits.getJSONObject(j);
                            User debtor = members.get(splitJson.getString("debtor_id"));
                            TransactionSplit split = new TransactionSplit(debtor, splitJson.getDouble("value"));
                            transaction.addSplit(split);
                        }
                        mGroupTransactions.add(transaction);
                    }

                    notifyDataSetChanged();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int code) {

            }
        };

        new ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_GET_TRANSACTIONS, args, callback).execute();
    }

    private void getOfflineTransactions(HashMap<String, User> members) {
        GroupDao dao = new GroupDao(mContext);
        mGroupTransactions = dao.findGroupTransactions(mGroup, members);
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
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupTransaction item = (GroupTransaction) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.group_payment_list_row, null);
        }
        TextView transactionName = (TextView) convertView.findViewById(R.id.group_transaction_name);
        TextView transactionDate = (TextView) convertView.findViewById(R.id.group_transaction_date);
        TextView transactionValue = (TextView) convertView.findViewById(R.id.group_transaction_value);
        TextView transactionPayer = (TextView) convertView.findViewById(R.id.group_transaction_payer);
        ImageView transactionExpand = (ImageView) convertView.findViewById(R.id.group_transaction_expand);
        TextView transactionSplitsHeader = (TextView) convertView.findViewById(R.id.splits_header);

        transactionName.setText(item.getDescription());
        transactionDate.setText(mDateFormatter.format(item.getDate()));
        transactionValue.setText(AppUtils.formatCurrencyValue(item.getValue()));
        transactionPayer.setText(item.getPayer().getExhibitName());

        transactionSplitsHeader.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
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
        transactionSplitDebtor.setText(item.getDebtor().getExhibitName());
        transactionSplitValue.setText(AppUtils.formatCurrencyValue(item.getValue()));

        View divider = convertView.findViewById(R.id.transaction_split_divider);
        divider.setVisibility(isLastChild ? View.VISIBLE : View.GONE);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
