package br.com.fclug.financialaid;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import br.com.fclug.financialaid.dialog.AddAccountDialog;
import br.com.fclug.financialaid.models.Account;


public class AccountsFragment extends Fragment {

    private GridView mAccountsGrid;
    private AccountGridAdapter mGridAdapter;
    private FloatingActionButton mAddAccountFab;

    private OnClickListener mAddAccountFabClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            AddAccountDialog addAccountDialog = new AddAccountDialog(getContext());
            addAccountDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    // refresh list
                    mGridAdapter.updateListItems();
                }
            });
            // show the dialog
            addAccountDialog.show();
        }
    };

    private OnItemClickListener mAccountClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Account clickedAccount = (Account) mGridAdapter.getItem(position);
            Intent intent = new Intent(getContext(), CashFlowControlActivity.class);
            intent.putExtra("account", clickedAccount.getId());
            getContext().startActivity(intent);
        }
    };

    public AccountsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (container == null)
            return null;
        View view = inflater.inflate(R.layout.content_cashflowcontrol, container, false);
        mAccountsGrid = (GridView) view.findViewById(R.id.account_gridview);
        mGridAdapter = new AccountGridAdapter(getContext());
        mGridAdapter.updateListItems();
        mAccountsGrid.setAdapter(mGridAdapter);
        mAccountsGrid.setOnItemClickListener(mAccountClickListener);

        mAddAccountFab = (FloatingActionButton) view.findViewById(R.id.add_control_entry);
        mAddAccountFab.setOnClickListener(mAddAccountFabClickListener);

        return view;
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
