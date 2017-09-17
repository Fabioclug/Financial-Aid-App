package br.com.fclug.financialaid.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.fclug.financialaid.interfaces.OnListClickListener;
import br.com.fclug.financialaid.utils.AppUtils;
import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.database.AccountDao;
import br.com.fclug.financialaid.models.Account;

/**
 * Created by Fabioclug on 2016-07-15.
 */
public class AccountListAdapter extends RecyclerView.Adapter<AccountListAdapter.ViewHolderAccountItem> {

    private List<Account> mAccounts;
    private List<Account> mPendingRemovalAccounts;
    private SparseArray<Runnable> pendingRunnables = new SparseArray<>();

    private Context mContext;
    private int[] mAccountColors;
    private TypedArray mAccountIcons;

    private OnListClickListener mListItemClickListener;
    private Handler handler = new Handler();

    public static final int PENDING_REMOVAL_TIMEOUT = 3000;

    public class ViewHolderAccountItem extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        RelativeLayout regularLayout;
        LinearLayout swipeLayout;
        ImageView accountImage;
        TextView accountName;
        TextView accountType;
        TextView accountBalance;
        LinearLayout undo;

        public ViewHolderAccountItem(View view) {
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
            Account account = mAccounts.get(getAdapterPosition());
            mListItemClickListener.onItemClick(account);
        }

        @Override
        public boolean onLongClick(View v) {
            Account account = mAccounts.get(getAdapterPosition());
            mListItemClickListener.onItemLongClick(v, account);
            return true;
        }
    }

    public AccountListAdapter(Context context) {
        mContext = context;
        Resources res = mContext.getResources();
        mAccountColors = res.getIntArray(R.array.account_colors);
        mAccountIcons = res.obtainTypedArray(R.array.account_icons);
        mPendingRemovalAccounts = new ArrayList<>();

        // force the RecyclerView to rebind rows when another row is deleted
        setHasStableIds(true);
    }

    @Override
    public ViewHolderAccountItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_list_item, parent, false);
        return new ViewHolderAccountItem(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolderAccountItem holder, int position) {
        final Account accountItem = mAccounts.get(position);

        if (mPendingRemovalAccounts.contains(accountItem)) {
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

    public void setPendingRemoval(final int position, boolean showSwipeLayout) {
        final Account pendingRemoval = mAccounts.get(position);
        if (!mPendingRemovalAccounts.contains(pendingRemoval)) {
            mPendingRemovalAccounts.add(pendingRemoval);
            if (!showSwipeLayout) {
                mAccounts.remove(position);
                notifyItemRemoved(position);
            } else {
                notifyItemChanged(position);
            }
            // crete a runnable to delete the account after the timeout
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    remove(position);
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.append((int) pendingRemoval.getId(), pendingRemovalRunnable);
        }
    }

    public int setPendingRemoval(Account account) {
        int position = mAccounts.indexOf(account);
        setPendingRemoval(position, false);
        notifyItemRemoved(position);
        return position;
    }

    private void remove(int position) {
        Account pendingRemoval = mAccounts.get(position);
        if (mPendingRemovalAccounts.contains(pendingRemoval)) {
            mPendingRemovalAccounts.remove(pendingRemoval);
        }
        if (mAccounts.contains(pendingRemoval)) {
            AccountDao accountDao = new AccountDao(mContext);
            accountDao.delete(pendingRemoval);
            mAccounts.remove(position);
            notifyItemRemoved(position);
        }
    }

    public boolean isPendingRemoval(int position) {
        Account account = mAccounts.get(position);
        return mPendingRemovalAccounts.contains(account);
    }

    public void undoRemoval(Account account, int... position) {
        int hashIndex = (int) account.getId();
        Runnable pendingRemovalRunnable = pendingRunnables.get(hashIndex);
        pendingRunnables.remove(hashIndex);
        if (pendingRemovalRunnable != null)
            handler.removeCallbacks(pendingRemovalRunnable);
        mPendingRemovalAccounts.remove(account);
        // this will rebind the row in "normal" state
        if (mAccounts.contains(account)) {
            notifyItemChanged(mAccounts.indexOf(account));
        } else {
            mAccounts.add(position[0], account);
            notifyItemInserted(mAccounts.indexOf(account));
        }
    }

    public void setListItems() {
        AccountDao transactionDao = new AccountDao(mContext);
        mAccounts = transactionDao.findAll();
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mAccounts.size();
    }

    @Override
    public long getItemId(int position) {
        return mAccounts.get(position).getId();
    }

    public void setListItemClickListener(OnListClickListener listItemClickListener) {
        mListItemClickListener = listItemClickListener;
    }

    public void updateItemView(Account account) {
        int position = mAccounts.indexOf(account);
        notifyItemChanged(position);
    }

    public Account getItem(int position) {
        return mAccounts.get(position);
    }
}
