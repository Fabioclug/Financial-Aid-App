package br.com.fclug.financialaid.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.database.AccountDao;
import br.com.fclug.financialaid.interfaces.OnListClickListener;
import br.com.fclug.financialaid.models.Account;
import br.com.fclug.financialaid.utils.AppUtils;

/**
 * Created by Fabioclug on 2017-10-10.
 */

public class AccountRecyclerViewListAdapter extends RecyclerViewListAdapter<Account, AccountRecyclerViewListAdapter.AccountViewHolder> {

    private Context mContext;
    private int[] mAccountColors;
    private TypedArray mAccountIcons;
    private int lastPosition = -1;

    private OnListClickListener mListItemClickListener;

    public static class AccountViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        Account account;
        RelativeLayout regularLayout;
        LinearLayout swipeLayout;
        ImageView accountImage;
        TextView accountName;
        TextView accountType;
        TextView accountBalance;
        LinearLayout undo;
        OnListClickListener clickListener;

        public AccountViewHolder(View view) {
            super(view);
            regularLayout = (RelativeLayout) view.findViewById(R.id.regular_layout);
            accountImage = (ImageView) view.findViewById(R.id.account_overview_image);
            accountName = (TextView) view.findViewById(R.id.account_overview_name);
            accountType = (TextView) view.findViewById(R.id.account_overview_type);
            accountBalance = (TextView) view.findViewById(R.id.account_overview_balance);
            swipeLayout = (LinearLayout) view.findViewById(R.id.swipe_layout);
            undo = (LinearLayout) view.findViewById(R.id.undo);
            regularLayout.setOnClickListener(this);
            regularLayout.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(account, this.getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            clickListener.onItemLongClick(v, account);
            return true;
        }
    }

    public AccountRecyclerViewListAdapter(Context context) {
        super();
        mContext = context;
        Resources res = mContext.getResources();
        mAccountColors = res.getIntArray(R.array.account_colors);
        mAccountIcons = res.obtainTypedArray(R.array.account_icons);
    }

    @Override
    public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_list_item, parent, false);
        return new AccountViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AccountViewHolder holder, int position) {
        final Account accountItem = mItems.get(position);
        holder.account = accountItem;
        holder.clickListener = mListItemClickListener;

        if (mPendingRemovalItems.contains(accountItem)) {
            // show the swiped view layout
            holder.regularLayout.setVisibility(View.GONE);
            holder.swipeLayout.setVisibility(View.VISIBLE);
            holder.undo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    undoRemoval(accountItem);
                }
            });
        } else {
            holder.regularLayout.setVisibility(View.VISIBLE);
            holder.swipeLayout.setVisibility(View.GONE);

            @DrawableRes int accountIcon = R.drawable.ic_cash;
            GradientDrawable gd = (GradientDrawable) holder.accountImage.getBackground();
            gd.setColor(ContextCompat.getColor(mContext, R.color.account_image_bg));
            int typeIndex = AppUtils.getAccountTypeIndex(mContext, accountItem.getType());
            if (typeIndex != -1) {
                gd.setColor(mAccountColors[typeIndex]);
                accountIcon = mAccountIcons.getResourceId(typeIndex, R.drawable.ic_cash);
            }
            holder.accountImage.setImageResource(accountIcon);
            holder.accountName.setText(accountItem.getName());
            String accountType = accountItem.getType() + " account";
            holder.accountType.setText(accountType);

            if (accountItem.getBalance() >= 0) {
                holder.accountBalance.setTextColor(ContextCompat.getColor(mContext, R.color.transaction_type_credit));
            } else {
                holder.accountBalance.setTextColor(ContextCompat.getColor(mContext, R.color.transaction_type_debt));
            }
            holder.accountBalance.setText(accountItem.getFormattedBalance());
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        // animate if the position wasn't loaded before
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void removeFromDatabase(Account pendingRemoval) {
        AccountDao accountDao = new AccountDao(mContext);
        accountDao.delete(pendingRemoval);
    }

    @Override
    public void setListItems() {
        AccountDao transactionDao = new AccountDao(mContext);
        mItems = transactionDao.findAll();
        notifyDataSetChanged();
    }

    public void setListItemClickListener(OnListClickListener listItemClickListener) {
        mListItemClickListener = listItemClickListener;
    }
}
