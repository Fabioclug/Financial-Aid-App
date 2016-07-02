package br.com.fclug.financialaid;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import br.com.fclug.financialaid.database.TransactionDao;
import br.com.fclug.financialaid.models.Transaction;

public class CashFlowControl extends Fragment {

    private ListView mTransactionList;
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
            //addTransactionView.setCancelable(false);
            Button addTransactionButton = (Button) mAddTransactionView.findViewById(R.id.add_transaction_button);
            addTransactionButton.setOnClickListener(mConfirmTransactionButtonClickListener);


            final Calendar myCalendar = Calendar.getInstance();

            final EditText transactionDate = (EditText) mAddTransactionView.findViewById(R.id.add_transaction_date);
            transactionDate.setFocusable(false);

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

            transactionDate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DatePickerDialog(getContext(), date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
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
            tdao.save(newTransaction);
            mAddTransactionView.dismiss();
            mListAdapter.updateListItems();
            mListAdapter.notifyDataSetChanged();
        }
    };

    public CashFlowControl() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (container == null)
            return null;
        View view = inflater.inflate(R.layout.content_cashflowcontrol, container, false);
        mTransactionList = (ListView) view.findViewById(R.id.control_entries_list);
        mListAdapter = new TransactionListAdapter(getContext());
        mListAdapter.updateListItems();
        mTransactionList.setAdapter(mListAdapter);

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
