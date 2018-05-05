package br.com.fclug.financialaid.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
 * Created by Fabioclug on 2018-01-17.
 */

public class GroupTransactionRecyclerViewListAdapter extends RecyclerViewListAdapter<GroupTransaction, GroupTransactionRecyclerViewListAdapter.GroupTransactionViewHolder> {

    private Context mContext;
    private Group mGroup;
    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("MM/dd", Locale.US);
    public static SimpleDateFormat buildDateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private SparseArray<Runnable> mPendingCallbacks = new SparseArray<>();

    public static class GroupTransactionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LinearLayout regularLayout;
        LinearLayout swipeLayout;
        TextView transactionName;
        TextView transactionDate;
        TextView transactionValue;
        TextView transactionPayer;
        ImageView transactionExpand;
        LinearLayout transactionSplits;
        LinearLayout undo;
        boolean isExpanded;
        boolean splitsBound;

        public GroupTransactionViewHolder(View view) {
            super(view);
            regularLayout = (LinearLayout) view.findViewById(R.id.regular_layout);
            swipeLayout = (LinearLayout) view.findViewById(R.id.swipe_layout);
            transactionName = (TextView) view.findViewById(R.id.group_transaction_name);
            transactionDate = (TextView) view.findViewById(R.id.group_transaction_date);
            transactionValue = (TextView) view.findViewById(R.id.group_transaction_value);
            transactionPayer = (TextView) view.findViewById(R.id.group_transaction_payer);
            transactionExpand = (ImageView) view.findViewById(R.id.group_transaction_expand);
            transactionSplits = (LinearLayout) view.findViewById(R.id.transaction_splits);
            undo = (LinearLayout) view.findViewById(R.id.undo);
            isExpanded = false;
            splitsBound = false;
            regularLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            expandOrCollapse();
        }

        public void expandOrCollapse() {
            isExpanded = !isExpanded;
            transactionSplits.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            transactionExpand.setImageResource(isExpanded ? R.drawable.ic_collapse : R.drawable.ic_expand);
        }
    }

    public GroupTransactionRecyclerViewListAdapter(Context context, Group group) {
        mContext = context;
        mGroup = group;
        mItems = new ArrayList<>();
    }

    @Override
    public GroupTransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_transaction_list_item, parent, false);
        return new GroupTransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GroupTransactionViewHolder holder, int position) {
        final GroupTransaction transactionItem = mItems.get(position);

        if (mPendingRemovalItems.contains(transactionItem)) {
            holder.regularLayout.setVisibility(View.INVISIBLE);
            holder.swipeLayout.setVisibility(View.VISIBLE);
            holder.undo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    undoRemoval(transactionItem);
                }
            });
        } else {
            holder.regularLayout.setVisibility(View.VISIBLE);
            holder.swipeLayout.setVisibility(View.GONE);
            holder.transactionName.setText(transactionItem.getDescription());
            holder.transactionDate.setText(mDateFormatter.format(transactionItem.getDate()));
            holder.transactionValue.setText(AppUtils.formatCurrencyValue(transactionItem.getValue()));
            holder.transactionPayer.setText(transactionItem.getPayer().getExhibitName());

            holder.transactionSplits.setVisibility(holder.isExpanded ? View.VISIBLE : View.GONE);
            holder.transactionExpand.setImageResource(R.drawable.ic_expand);

            if (!holder.splitsBound) {
                for (TransactionSplit split : transactionItem.getSplits()) {
                    bindChildView(holder, split);
                }
                holder.splitsBound = true;
            }
        }
    }

    private void bindChildView(GroupTransactionViewHolder holder, TransactionSplit split) {
        View splitView = LayoutInflater.from(mContext).inflate(R.layout.transaction_split_list_row, null);
        TextView transactionSplitDebtor = (TextView) splitView.findViewById(R.id.transaction_split_debtor);
        TextView transactionSplitValue = (TextView) splitView.findViewById(R.id.transaction_split_value);
        transactionSplitDebtor.setText(split.getDebtor().getExhibitName());
        transactionSplitValue.setText(AppUtils.formatCurrencyValue(split.getValue()));
        holder.transactionSplits.addView(splitView);
    }

    @Override
    public void removeFromDatabase(GroupTransaction item) {
        if(mGroup.isOnline()) {
            //TODO: implementation on the server
        } else {
            new GroupDao(mContext).delete(item);
        }
    }

    @Override
    public void setListItems() {
        HashMap<String, User> members = mGroup.getMembersDictionary();
        if (mGroup.isOnline()) {
            getOnlineTransactions(members);
        } else {
            getOfflineTransactions(members);
        }
    }

    public void addTransaction(GroupTransaction transaction) {
        mItems.add(transaction);
        notifyItemInserted(mItems.size() - 1);
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
                mItems.clear();
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
                        mItems.add(transaction);
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
        mItems = dao.findGroupTransactions(mGroup, members);
    }

    public void collapseView(RecyclerView recyclerView, int position) {
        GroupTransactionViewHolder viewHolder= (GroupTransactionViewHolder)
                recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder.isExpanded) {
            viewHolder.expandOrCollapse();
        }
    }

    public GroupTransaction setPendingRemoval(int position, boolean showSwipeLayout, Runnable callback) {
        mPendingCallbacks.append((int) mItems.get(position).getId(), callback);
        return super.setPendingRemoval(position, showSwipeLayout);
    }

    @Override
    protected void remove(GroupTransaction pendingRemoval) {
        super.remove(pendingRemoval);
        int hashIndex = (int) pendingRemoval.getId();
        Runnable pendingCallback = mPendingCallbacks.get(hashIndex);
        pendingCallback.run();
        mPendingCallbacks.remove(hashIndex);
    }
}
