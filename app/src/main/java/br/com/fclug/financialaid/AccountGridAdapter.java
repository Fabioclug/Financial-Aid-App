package br.com.fclug.financialaid;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import br.com.fclug.financialaid.database.AccountDao;
import br.com.fclug.financialaid.models.Account;

/**
 * Created by Fabioclug on 2016-07-15.
 */
public class AccountGridAdapter extends BaseAdapter {

    private List<Account> mAccounts;
    private Context mContext;
    private Account mAccountItem;
    private int[] mAccountColors;
    private TypedArray mAccountIcons;

    static class ViewHolderAccountItem {
        ImageView accountImage;
        TextView accountName;
        TextView accountBalance;
    }

    AccountGridAdapter(Context context) {
        mContext = context;
        Resources res = mContext.getResources();
        mAccountColors = res.getIntArray(R.array.account_colors);
        mAccountIcons = res.obtainTypedArray(R.array.account_icons);
    }

    public void updateListItems() {
        AccountDao transactionDao = new AccountDao(mContext);
        mAccounts = transactionDao.findAll();
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mAccounts.size();
    }

    @Override
    public Object getItem(int position) {
        return mAccounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mAccounts.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderAccountItem viewHolderAccountItem;
        mAccountItem = mAccounts.get(position);
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.account_grid_item, parent, false);

            viewHolderAccountItem = new ViewHolderAccountItem();
            viewHolderAccountItem.accountImage = (ImageView) convertView.findViewById(R.id.account_grid_item_image);
            viewHolderAccountItem.accountName = (TextView) convertView.findViewById(R.id.account_grid_item_name);
            viewHolderAccountItem.accountBalance = (TextView) convertView.findViewById(R.id.account_grid_item_balance);

            convertView.setTag(viewHolderAccountItem);

        } else {
            viewHolderAccountItem = (ViewHolderAccountItem) convertView.getTag();
        }

        @DrawableRes int accountIcon = R.drawable.ic_cash;
        GradientDrawable gd = (GradientDrawable) viewHolderAccountItem.accountImage.getBackground();
        gd.setColor(ContextCompat.getColor(mContext, R.color.account_image_bg));
        int typeIndex = AppUtils.getAccountTypeIndex(mContext, mAccountItem.getType());
        if (typeIndex != -1) {
            gd.setColor(mAccountColors[typeIndex]);
            accountIcon = mAccountIcons.getResourceId(typeIndex, R.drawable.ic_cash);
        }
        viewHolderAccountItem.accountImage.setImageResource(accountIcon);

        if(mAccountItem != null) {
            viewHolderAccountItem.accountName.setText(mAccountItem.getName());
            viewHolderAccountItem.accountBalance.setText(String.valueOf(mAccountItem.getBalance()));

            if(mAccountItem.getBalance() >= 0) {
                viewHolderAccountItem.accountBalance.setTextColor(ContextCompat.getColor(mContext, R.color.transaction_type_credit));
            } else {
                viewHolderAccountItem.accountBalance.setTextColor(ContextCompat.getColor(mContext, R.color.transaction_type_debt));
            }
            viewHolderAccountItem.accountBalance.setText(String.valueOf(mAccountItem.getBalance()));
        }

        return convertView;
    }


}
