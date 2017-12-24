package br.com.fclug.financialaid;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import br.com.fclug.financialaid.adapter.AccountRecyclerViewListAdapter;
import br.com.fclug.financialaid.adapter.RecyclerViewListAdapter;
import br.com.fclug.financialaid.dialog.AddAccountDialog;
import br.com.fclug.financialaid.dialog.OptionsMenuDialog;
import br.com.fclug.financialaid.interfaces.OnListClickListener;
import br.com.fclug.financialaid.interfaces.OnObjectOperationListener;
import br.com.fclug.financialaid.models.Account;
import br.com.fclug.financialaid.utils.SwipeUtil;

public class AccountsFragment extends Fragment implements OnClickListener {

    private RecyclerView mAccountsRecyclerView;
    private AccountRecyclerViewListAdapter mListAdapter;
    private FloatingActionButton mAddAccountFab;

    private DialogInterface.OnDismissListener mDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            // refresh list
            mListAdapter.setListItems();
        }
    };

    private OnListClickListener mListClickListener = new OnListClickListener() {
        @Override
        public void onItemClick(Object account) {
            Account clickedAccount = (Account) account;
            Intent intent = new Intent(getContext(), TransactionsActivity.class);
            intent.putExtra("account", clickedAccount.getId());
            getContext().startActivity(intent);
        }

        @Override
        public void onItemLongClick(View view, Object account) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            Account clickedAccount = (Account) account;
            OptionsMenuDialog dialog = new OptionsMenuDialog(getContext(), clickedAccount);
            dialog.setOnObjectOperationListener(mAccountOperationListener);
            dialog.show();
        }
    };

    private OnObjectOperationListener mAccountOperationListener = new OnObjectOperationListener() {
        @Override
        public void onAdd(Object account) {
            Snackbar.make(mAddAccountFab, R.string.account_created, Snackbar.LENGTH_LONG).show();
        }

        @Override
        public void onUpdate(Object account) {
            Account updatedAccount = (Account) account;
            mListAdapter.updateItemView(updatedAccount);
            Snackbar.make(mAddAccountFab, R.string.account_updated, Snackbar.LENGTH_LONG).show();
        }

        @Override
        public void onDelete(Object account) {
            final Account pendingRemovalAccount = (Account) account;
            final int position = mListAdapter.setPendingRemoval(pendingRemovalAccount);
            Snackbar.make(mAddAccountFab, R.string.account_removed, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.undo, new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mListAdapter.undoRemoval(pendingRemovalAccount, position);
                        }
                    })
                    .setDuration(RecyclerViewListAdapter.PENDING_REMOVAL_TIMEOUT)
                    .setActionTextColor(ContextCompat.getColor(getContext(), R.color.swipe_background))
                    .show();
        }
    };

    public AccountsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.accounts_fragment, container, false);
        mAccountsRecyclerView = (RecyclerView) view.findViewById(R.id.account_recyclerview);

        mAddAccountFab = (FloatingActionButton) view.findViewById(R.id.add_control_entry);
        mAddAccountFab.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mAccountsRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration itemDecorator = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.account_list_divider));
        mAccountsRecyclerView.addItemDecoration(itemDecorator);

        mListAdapter = new AccountRecyclerViewListAdapter(getContext());
        mAccountsRecyclerView.setAdapter(mListAdapter);
        mListAdapter.setListItemClickListener(mListClickListener);
        //mAccountsRecyclerView.getItemAnimator().setRemoveDuration(0);
//        mAccountsRecyclerView.setItemAnimator(new DefaultItemAnimator() {
//            @Override
//            public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder,
//                                         int fromX, int fromY, int toX, int toY) {
//                if (oldHolder == newHolder) {
//                    newHolder.itemView.animate()
//                            .alpha(1f)
//                            .setDuration(500)
//                            .setListener(null);
//                }
//                return true;
//            }
//        });
        setSwipeForRecyclerView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mListAdapter.setListItems();
    }

    private void setSwipeForRecyclerView() {

        final SwipeUtil swipeHelper = new SwipeUtil(getContext()) {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    int swipedPosition = viewHolder.getAdapterPosition();
                    mListAdapter.setPendingRemoval(swipedPosition, true);
                } else {
                    int position = viewHolder.getAdapterPosition();
                    Account account = mListAdapter.getItem(position);
                    AddAccountDialog dialog = new AddAccountDialog(getActivity(), account);
                    dialog.setOnDismissListener(mDismissListener);
                    dialog.setOnTransactionOperationListener(mAccountOperationListener);
                    mListAdapter.notifyItemChanged(position);
                    dialog.show();
                }
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                if (mListAdapter.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onLeftClick(int position) {
                Account account = mListAdapter.getItem(position);
                AddAccountDialog dialog = new AddAccountDialog(getActivity(), account);
                dialog.setOnDismissListener(mDismissListener);
                dialog.setOnTransactionOperationListener(mAccountOperationListener);
                dialog.show();
            }
        };

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(swipeHelper);
        mItemTouchHelper.attachToRecyclerView(mAccountsRecyclerView);

        //set swipe label
        swipeHelper.setLeftSwipeLabel("Delete");
        //set swipe background-Color
        swipeHelper.setLeftSwipeColor(ContextCompat.getColor(getActivity(), R.color.swipe_background));
        swipeHelper.setRightColorCode(ContextCompat.getColor(getActivity(), R.color.electronics_category));

    }

    @Override
    public void onClick(View view) {
        AddAccountDialog addAccountDialog = new AddAccountDialog(getContext());
        addAccountDialog.setOnDismissListener(mDismissListener);
        addAccountDialog.setOnTransactionOperationListener(mAccountOperationListener);
        // show the dialog
        addAccountDialog.show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
