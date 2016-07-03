package br.com.fclug.financialaid;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.view.View.OnClickListener;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import br.com.fclug.financialaid.database.TransactionDao;
import br.com.fclug.financialaid.models.Transaction;

public class CashFlowControl extends Fragment {

    private SwipeMenuListView mTransactionList;
    private TransactionListAdapter mListAdapter;
    private FloatingActionButton mAddTransactionFab;
    private Dialog mAddTransactionView;
    SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy", Locale.US);

    private OnClickListener mAddTransactionFabClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            mAddTransactionView = new Dialog(getContext());
            mAddTransactionView.setContentView(R.layout.add_transaction_view);

            Button addTransactionButton = (Button) mAddTransactionView.findViewById(R.id.add_transaction_button);
            addTransactionButton.setOnClickListener(mConfirmTransactionButtonClickListener);

            final Calendar myCalendar = Calendar.getInstance();
            final EditText transactionDate = (EditText) mAddTransactionView.findViewById(R.id.add_transaction_date);
            transactionDate.setFocusable(false);

            // build the calendar to pick a date
            final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    transactionDate.setText(dateFormatter.format(myCalendar.getTime()));

                }

            };

            // show the calendar when the field is clicked
            transactionDate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DatePickerDialog(getContext(), date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });

            // show the dialog
            mAddTransactionView.show();
        }
    };

    private OnClickListener mConfirmTransactionButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText transactionDescription = (EditText) mAddTransactionView.findViewById(R.id.add_transaction_description);
            EditText transactionCategory = (EditText) mAddTransactionView.findViewById(R.id.add_transaction_category);
            EditText transactionValue = (EditText) mAddTransactionView.findViewById(R.id.add_transaction_value);
            EditText transactionDate = (EditText) mAddTransactionView.findViewById(R.id.add_transaction_date);
            Date date = null;
            try {
                date = dateFormatter.parse(transactionDate.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Transaction newTransaction = new Transaction(true, transactionDescription.getText().toString(),
                    Double.parseDouble(transactionValue.getText().toString()),
                    transactionCategory.getText().toString(), date);
            TransactionDao tdao= new TransactionDao(getContext());

            // save the new transaction
            tdao.save(newTransaction);
            // close dialog
            mAddTransactionView.dismiss();
            // refresh list
            mListAdapter.updateListItems();
        }
    };

    private SwipeMenuCreator creator = new SwipeMenuCreator() {

        @Override
        public void create(SwipeMenu menu) {
            // create "edit" item
            SwipeMenuItem editItem = new SwipeMenuItem(getContext());
            editItem.setBackground(new ColorDrawable(Color.rgb(0x19, 0x76, 0xD2)));
            editItem.setWidth(dp2px(90));
            editItem.setIcon(R.drawable.ic_edit);

            // create "delete" item
            SwipeMenuItem deleteItem = new SwipeMenuItem(getContext());
            deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
            deleteItem.setWidth(dp2px(90));
            deleteItem.setIcon(R.drawable.ic_delete_black_24dp);

            // add items to menu
            menu.addMenuItem(editItem);
            menu.addMenuItem(deleteItem);
        }
    };

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    public CashFlowControl() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (container == null)
            return null;
        View view = inflater.inflate(R.layout.content_cashflowcontrol, container, false);
        mTransactionList = (SwipeMenuListView) view.findViewById(R.id.control_entries_list);
        mListAdapter = new TransactionListAdapter(getContext());
        mListAdapter.updateListItems();
        mTransactionList.setAdapter(mListAdapter);
        mTransactionList.setMenuCreator(creator);
        mTransactionList.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        // take care of swipe menu clicks
        mTransactionList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                Transaction item = (Transaction) mListAdapter.getItem(position);
                switch (index) {
                    case 0:
                        // edit
                        break;
                    case 1:
                        new TransactionDao(getContext()).delete(item);
                        mListAdapter.updateListItems();
                        break;
                }
                // close the menu
                return false;
            }
        });

        mAddTransactionFab = (FloatingActionButton) view.findViewById(R.id.add_control_entry);
        mAddTransactionFab.setOnClickListener(mAddTransactionFabClickListener);

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
