package br.com.fclug.financialaid.adapter;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import br.com.fclug.financialaid.models.UniqueObject;

/**
 * Created by Fabioclug on 2017-10-08.
 */

public abstract class RecyclerViewListAdapter<T extends UniqueObject, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    protected List<T> mItems;
    protected List<T> mPendingRemovalItems;
    private SparseArray<Runnable> mPendingRunnables = new SparseArray<>();

    private Handler handler = new Handler();

    public static final int PENDING_REMOVAL_TIMEOUT = 3000;

    public RecyclerViewListAdapter() {
        // force the RecyclerView to rebind rows when another row is deleted
        setHasStableIds(true);
        mPendingRemovalItems = new ArrayList<>();
    }

    public void setPendingRemoval(final int position, boolean showSwipeLayout) {
        final T pendingRemoval = mItems.get(position);
        if (!mPendingRemovalItems.contains(pendingRemoval)) {
            mPendingRemovalItems.add(pendingRemoval);
            if (!showSwipeLayout) {
                mItems.remove(position);
                notifyItemRemoved(position);
            } else {
                notifyItemChanged(position);
            }
            // create a runnable to delete the item after the timeout
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    remove(position);
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            mPendingRunnables.append((int) pendingRemoval.getId(), pendingRemovalRunnable);
        }
    }

    public int setPendingRemoval(T item) {
        int position = mItems.indexOf(item);
        setPendingRemoval(position, false);
        notifyItemRemoved(position);
        return position;
    }

    private void remove(int position) {
        T pendingRemoval = mItems.get(position);
        if (mPendingRemovalItems.contains(pendingRemoval)) {
            mPendingRemovalItems.remove(pendingRemoval);
        }
        if (mItems.contains(pendingRemoval)) {
            removeFromDatabase(pendingRemoval);
            mItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public abstract void removeFromDatabase(T item);

    public boolean isPendingRemoval(int position) {
        T item = mItems.get(position);
        return mPendingRemovalItems.contains(item);
    }

    public void undoRemoval(T item, int... position) {
        int hashIndex = (int) item.getId();
        Runnable pendingRemovalRunnable = mPendingRunnables.get(hashIndex);
        mPendingRunnables.remove(hashIndex);
        if (pendingRemovalRunnable != null)
            handler.removeCallbacks(pendingRemovalRunnable);
        mPendingRemovalItems.remove(item);
        // this will rebind the row in "normal" state
        if (mItems.contains(item)) {
            notifyItemChanged(mItems.indexOf(item));
        } else {
            mItems.add(position[0], item);
            notifyItemInserted(mItems.indexOf(item));
        }
    }

    public abstract void setListItems();

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).getId();
    }

    public void updateItemView(T item) {
        int position = mItems.indexOf(item);
        notifyItemChanged(position);
    }

    public T getItem(int position) {
        return mItems.get(position);
    }
}
