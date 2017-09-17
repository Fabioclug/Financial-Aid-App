package br.com.fclug.financialaid;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.fclug.financialaid.models.Group;
import br.com.fclug.financialaid.models.User;
import br.com.fclug.financialaid.server.ApiRequest;
import br.com.fclug.financialaid.server.ServerUtils;
import br.com.fclug.financialaid.utils.AppUtils;

public class CreateGroupPaymentActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener{

    private Group mGroup;
    private List<User> mMembers;
    private User mPayer;

    private Context mContext;
    private EditText mDescription;
    private EditText mValue;
    private EditText mDate;
    private EditText[] mSplits;
    private Spinner mPayerSpinner;
    private boolean mChangeValues = true;

    private SimpleDateFormat mFormatter = new SimpleDateFormat("MM/dd/yy", Locale.US);

    private TextWatcher mValueWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(mChangeValues) {
                mChangeValues = false;
                String text = s.toString().trim();
                if (!text.isEmpty()) {
                    text = AppUtils.handleValueInput(mValue);
                    mValue.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                    if(!TextUtils.isEmpty(text)) {
                        double totalValue = Double.parseDouble(text);
                        double split = AppUtils.roundValue(totalValue / mSplits.length);
                        for (EditText splitEditText : mSplits) {
                            splitEditText.setText(String.valueOf(split));
                        }
                    }
                } else {
                    for (EditText splitEditText : mSplits) {
                        splitEditText.getText().clear();
                    }
                }
                mChangeValues = true;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher mSplitsWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mChangeValues) {
                mChangeValues = false;
                String text;
                double totalValue = 0;
                for (EditText splitEditText : mSplits) {
                    text = splitEditText.getText().toString();
                    if (!text.isEmpty()) {
                        totalValue += Double.parseDouble(text);
                    }
                }
                totalValue = AppUtils.roundValue(totalValue);
                mValue.setText(String.valueOf(totalValue));
                mChangeValues = true;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group_payment_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle("Create group payment");
        }

        mContext = this;
        mDescription = (EditText) findViewById(R.id.create_payment_description);
        mValue = (EditText) findViewById(R.id.create_payment_value);
        mDate = (EditText) findViewById(R.id.create_payment_date);
        mPayerSpinner = (Spinner) findViewById(R.id.create_payment_payer);
        LinearLayout membersLayout = (LinearLayout) findViewById(R.id.create_payment_members);

        AppUtils.attachCalendarToEditText(mContext, mDate, mFormatter);

        mGroup = getIntent().getParcelableExtra("group");
        mMembers = mGroup.getMembers();
        mSplits = new EditText[mMembers.size()];

        mValue.addTextChangedListener(mValueWatcher);

        LayoutInflater inflater = getLayoutInflater();

        List<String> memberNames = new ArrayList<>();

        for(int i = 0; i < mMembers.size(); i++) {

            View memberItem = inflater.inflate(R.layout.create_payment_member_row, null, false);

            TextView memberName = (TextView) memberItem.findViewById(R.id.create_payment_member_name);
            mSplits[i] = (EditText) memberItem.findViewById(R.id.create_payment_member_split);
            mSplits[i].addTextChangedListener(mSplitsWatcher);

            User member = mMembers.get(i);
            memberName.setText(member.getName());
            memberItem.setId(i);
            if (membersLayout != null) {
                membersLayout.addView(memberItem);
            }
            memberNames.add(member.getName());
        }
        ArrayAdapter<String> accountsAdapter = new ArrayAdapter<String>(mContext,
                R.layout.support_simple_spinner_dropdown_item, memberNames);
        mPayerSpinner.setAdapter(accountsAdapter);
        mPayerSpinner.setOnItemSelectedListener(this);

        FloatingActionButton confirmTransaction = (FloatingActionButton) findViewById(R.id.confirm_group_transaction_fab);
        confirmTransaction.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd", Locale.US);
        JSONObject args = new JSONObject();
        try {
            SessionManager session = new SessionManager(mContext);
            args.put("token", session.getToken());
            args.put("group", mGroup.getId());
            args.put("description", mDescription.getText());
            args.put("value", mValue.getText());
            args.put("payer", mPayer.getUsername());
            try {
                Date date = mFormatter.parse(mDate.getText().toString());
                args.put("date", formatter.format(date));
                Log.d("CreateGroupPayment", formatter.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            JSONArray transactionSplits = new JSONArray();
            List<User> groupMembers = mGroup.getMembers();
            for (int i = 0; i < groupMembers.size(); i++) {
                JSONObject split = new JSONObject();
                split.put("username", groupMembers.get(i).getUsername());
                split.put("value", mSplits[i].getText());
                transactionSplits.put(split);
            }
            args.put("splits", transactionSplits);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("CreateGroupPayment", args.toString());

        new ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_CREATE_GROUP_TRANSACTION, args,
                new ApiRequest.RequestCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        finish();
                    }

                    @Override
                    public void onFailure(int code) {
                        // perhaps a dialog?
                    }
                }).execute();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mPayer = mMembers.get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
