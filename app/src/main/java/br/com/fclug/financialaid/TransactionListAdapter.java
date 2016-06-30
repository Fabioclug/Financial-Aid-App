package br.com.fclug.financialaid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import br.com.fclug.financialaid.database.TransactionDao;
import br.com.fclug.financialaid.models.Transaction;

/**
 * Created by Fabioclug on 2016-06-11.
 */
public class TransactionListAdapter extends BaseAdapter {

    private List<Transaction> mTransactions;
    private Context mContext;
    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("MM/dd", Locale.US);

    static class ViewHolderTransactionItem {
        TextView transactionDate;
        TextView transactionDescription;
        TextView transactionValue;
    }

    TransactionListAdapter(Context context) {
        mContext = context;
    }

    public void updateListItems() {
        TransactionDao transactionDao = new TransactionDao(mContext);
        mTransactions = transactionDao.findAll();
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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderTransactionItem viewHolderTransactionItem;
        Transaction transactionItem = mTransactions.get(position);

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.transaction_list_item, null);
            //itemView.setClickable(false);
            //itemView.setFocusable(false);

            viewHolderTransactionItem = new ViewHolderTransactionItem();
            viewHolderTransactionItem.transactionDate = (TextView) convertView.findViewById(R.id.transaction_item_date);
            viewHolderTransactionItem.transactionDescription = (TextView) convertView.findViewById(R.id.transaction_item_description);
            viewHolderTransactionItem.transactionValue = (TextView) convertView.findViewById(R.id.transaction_item_value);

            convertView.setTag(viewHolderTransactionItem);
        } else {
            viewHolderTransactionItem = (ViewHolderTransactionItem) convertView.getTag();
        }

        if(transactionItem != null) {
            viewHolderTransactionItem.transactionDate.setText(mDateFormatter.format(transactionItem.getDate()));
            viewHolderTransactionItem.transactionDescription.setText(transactionItem.getDescription());
            viewHolderTransactionItem.transactionDescription.setSelected(true);
            viewHolderTransactionItem.transactionValue.setText(String.format("%.2f", transactionItem.getValue()));
        }

        return convertView;
    }
}
