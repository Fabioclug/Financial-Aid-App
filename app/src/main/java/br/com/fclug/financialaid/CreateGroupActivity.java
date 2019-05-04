package br.com.fclug.financialaid;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.fclug.financialaid.adapter.MemberListAdapter;
import br.com.fclug.financialaid.models.Group;
import br.com.fclug.financialaid.models.OnlineUser;
import br.com.fclug.financialaid.models.User;
import br.com.fclug.financialaid.server.ApiRequest;
import br.com.fclug.financialaid.server.ServerUtils;

public class CreateGroupActivity extends AppCompatActivity {

    private Group mGroup;

    private class AutoCompleteAdapter extends MemberListAdapter implements Filterable {


        public AutoCompleteAdapter(Context context) {
            super(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            mDeleteButton.setVisibility(View.GONE);
            return view;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if (constraint != null) {
                        try {
                            mUsers = new ArrayList<User>();
                            JSONObject args = new JSONObject();
                            args.put("pattern", constraint.toString());
                            args.put("token", mUserData.get(SessionManager.KEY_TOKEN));
                            JSONObject response = ServerUtils.doPostRequest(ServerUtils.ROUTE_GET_SIMILAR_USERS, args);
                            JSONArray users = response.getJSONArray("result");
                            for (int i = 0; i < users.length(); i++) {
                                JSONObject user = users.getJSONObject(i);
                                String username = user.getString("username");
                                if(!username.equals(mUserData.get(SessionManager.KEY_USERNAME))) {
                                    User suggestion = new OnlineUser(username, user.getString("name"));
                                    if (!mListAdapter.getMembersList().contains(suggestion)) {
                                        mUsers.add(suggestion);
                                    }
                                }
                            }
                            results.values = mUsers;
                            results.count = mUsers.size();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if(results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

    private ApiRequest.RequestCallback createGroupCallback = new ApiRequest.RequestCallback() {
        @Override
        public void onSuccess(JSONObject response) {
            Toast.makeText(CreateGroupActivity.this, "Group created successfully!", Toast.LENGTH_LONG)
                    .show();
            finish();
        }

        @Override
        public void onFailure(int code) {

        }
    };

    private HashMap<String, String> mUserData;
    private EditText mGroupName;
    private AutoCompleteTextView member;
    private MemberListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            setTitle("Create Group");
        }

        mUserData = SessionManager.getUserDetails(this);

        mGroupName = (EditText) findViewById(R.id.create_group_name);
        member = (AutoCompleteTextView) findViewById(R.id.create_group_username);
        ListView membersList = (ListView) findViewById(R.id.create_group_member_list);
        FloatingActionButton confirmButton = (FloatingActionButton) findViewById(R.id.confirm_group_fab);
        mListAdapter = new MemberListAdapter(this);
        if (membersList != null) {
            membersList.setAdapter(mListAdapter);
        }

        final AutoCompleteAdapter adapter = new AutoCompleteAdapter(this);
        member.setAdapter(adapter);
        member.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                member.getText().clear();
                User selectedUser = adapter.getItem(position);
                mListAdapter.addMember(selectedUser);
                mListAdapter.notifyDataSetChanged();
            }
        });

        if (confirmButton != null) {
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String groupName = mGroupName.getText().toString();
                    List<User> members = mListAdapter.getMembersList();
                    mGroup = new Group(groupName, members);

                    JSONObject args = new JSONObject();
                    JSONArray memberList = new JSONArray();
                    try {
                        for(User member : members) {
                            JSONObject name = new JSONObject();
                            name.put("username", member.getUsername());
                            memberList.put(name);
                        }
//                        JSONObject creator = new JSONObject();
//                        creator.put("username", mUserData.get(SessionManager.KEY_USERNAME));
//                        memberList.put(creator);
                        args.put("token", mUserData.get(SessionManager.KEY_TOKEN));
                        args.put("name", mGroupName.getText().toString());
                        args.put("members", memberList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    new ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_CREATE_GROUP, args, createGroupCallback)
                            .execute();
                    finish();
                }
            });
        }
        addCurrentUser();
    }

    private void addCurrentUser() {
        HashMap<String, String> currentUser = SessionManager.getUserDetails(this);
        OnlineUser user = new OnlineUser(currentUser.get(SessionManager.KEY_USERNAME),
                currentUser.get(SessionManager.KEY_NAME));
        mListAdapter.addMember(user);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
