package br.com.fclug.financialaid;

import android.content.Context;
import android.content.Intent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
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
            mDeleteButton.hide();
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
                            args.put("token", mSessionManager.getToken());
                            JSONObject response = ServerUtils.doPostRequest(ServerUtils.ROUTE_GET_SIMILAR_USERS, args);
                            JSONArray users = response.getJSONArray("result");
                            for (int i = 0; i < users.length(); i++) {
                                JSONObject user = users.getJSONObject(i);
                                String username = user.getString("username");
                                if(!username.equals(mSessionManager.getUsername())) {
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
            Intent returnIntent = new Intent();
            try {
                long groupId = response.getLong("group_id");
                mGroup.setId(groupId);
                returnIntent.putExtra("group", mGroup);
            } catch (JSONException e) {
                Log.d("CreateGroupActivity", "Unable to get group id from response");
                e.printStackTrace();
            }
            setResult(RESULT_OK, returnIntent);
            finish();
        }

        @Override
        public void onFailure(int code) {
            Toast.makeText(CreateGroupActivity.this, "Unable to create group", Toast.LENGTH_SHORT).show();
        }
    };

    private EditText mGroupName;
    private AutoCompleteTextView member;
    private MemberListAdapter mListAdapter;
    private SessionManager mSessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            setTitle("Create Group");
        }

        mSessionManager = new SessionManager(this);

        mGroupName = findViewById(R.id.create_group_name);
        member = findViewById(R.id.create_group_username);
        ListView membersList = findViewById(R.id.create_group_member_list);
        FloatingActionButton confirmButton = findViewById(R.id.confirm_group_fab);
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

        final User creator = addCurrentUser();

        if (confirmButton != null) {
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String groupName = mGroupName.getText().toString();
                    List<User> members = mListAdapter.getMembersList();
                    mGroup = new Group(groupName, creator, members, true);

                    JSONObject args = new JSONObject();
                    JSONArray memberList = new JSONArray();
                    try {
                        for(User member : members) {
                            JSONObject name = new JSONObject();
                            name.put("username", member.getUsername());
                            memberList.put(name);
                        }
                        args.put("token", mSessionManager.getToken());
                        args.put("name", mGroupName.getText().toString());
                        args.put("members", memberList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    new ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_CREATE_GROUP, args, createGroupCallback)
                            .execute();
                }
            });
        }
    }

    private User addCurrentUser() {
        OnlineUser user = new OnlineUser(mSessionManager.getUsername(), mSessionManager.getName());
        mListAdapter.addMember(user);
        return user;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
