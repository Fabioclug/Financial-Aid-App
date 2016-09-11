package br.com.fclug.financialaid;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import br.com.fclug.financialaid.database.TransactionDao;
import br.com.fclug.financialaid.models.Account;
import br.com.fclug.financialaid.models.Transaction;

/**
 * Created by Fabioclug on 2016-06-11.
 */
public class TransactionListAdapter extends BaseAdapter {

    private List<Transaction> mTransactions;
    private Context mContext;
    private Account mAccount;
    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("MM/dd", Locale.US);
    private final TransactionDao mTransactionDao;

    private long pendingDeletedItem;
    private boolean hasPendingDeletedItem = false;

    static class ViewHolderTransactionItem {
        TextView transactionDate;
        TextView transactionDescription;
        TextView transactionValue;
        TextView transactionType;
    }

    TransactionListAdapter(Context context, Account account) {
        mContext = context;
        mAccount = account;
        mTransactionDao = new TransactionDao(mContext);
    }

    public void updateListItems() {
        mTransactions = mTransactionDao.findByAccount(mAccount);
        this.notifyDataSetChanged();
    }

    public void notifyDeletedItem(long id) {
        hasPendingDeletedItem = true;
        pendingDeletedItem = id;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mTransactions.size();
    }

    @Override
    public Object getItem(int position) {
        return mTransactions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mTransactions.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderTransactionItem viewHolderTransactionItem;
        Transaction transactionItem = mTransactions.get(position);

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.transaction_list_item, parent, false);

            viewHolderTransactionItem = new ViewHolderTransactionItem();
            viewHolderTransactionItem.transactionDate = (TextView) convertView.findViewById(R.id.transaction_item_date);
            viewHolderTransactionItem.transactionDescription = (TextView) convertView.findViewById(R.id.transaction_item_description);
            viewHolderTransactionItem.transactionValue = (TextView) convertView.findViewById(R.id.transaction_item_value);
            viewHolderTransactionItem.transactionType = (TextView) convertView.findViewById(R.id.transaction_item_type);

            convertView.setTag(viewHolderTransactionItem);
        } else {
            viewHolderTransactionItem = (ViewHolderTransactionItem) convertView.getTag();
        }

        if(transactionItem != null) {
            viewHolderTransactionItem.transactionDate.setText(mDateFormatter.format(transactionItem.getDate()));
            viewHolderTransactionItem.transactionDescription.setText(transactionItem.getDescription());
            viewHolderTransactionItem.transactionDescription.setSelected(true);
            viewHolderTransactionItem.transactionValue.setText(String.format(Locale.getDefault(), "%.2f", transactionItem.getValue()));
            if(transactionItem.isCredit()) {
                viewHolderTransactionItem.transactionType.setText("");
                viewHolderTransactionItem.transactionValue.setTextColor(ContextCompat.getColor(mContext, R.color.transaction_type_credit));
            } else {
                viewHolderTransactionItem.transactionType.setText("-");
                viewHolderTransactionItem.transactionType.setTextColor(ContextCompat.getColor(mContext, R.color.transaction_type_debt));
                viewHolderTransactionItem.transactionValue.setTextColor(ContextCompat.getColor(mContext, R.color.transaction_type_debt));
            }
        }

//        if (hasPendingDeletedItem && pendingDeletedItem == transactionItem.getId()) {
//            final Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_out);
//            animation.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    updateListItems();
//                }
//            });
//            animation.setRepeatCount(1);
//
//            convertView.startAnimation(animation);
//        }

        return convertView;
    }
}
