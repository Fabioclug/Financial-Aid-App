package br.com.fclug.financialaid;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import br.com.fclug.financialaid.adapter.MemberListAdapter;
import br.com.fclug.financialaid.database.GroupDao;
import br.com.fclug.financialaid.models.Group;
import br.com.fclug.financialaid.models.User;

/**
 * Created by Fabioclug on 2017-06-25.
 */

public class CreateLocalGroupActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mGroupName;
    private EditText mGroupMember;
    private ListView mGroupMembersList;
    private MemberListAdapter mListAdapter;
    private Button mAddMemberButton;
    private FloatingActionButton mConfirmButton;

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
            setTitle("Create Local Group");
        }


        mGroupName = (EditText) findViewById(R.id.create_group_name);
        mGroupMember = (EditText) findViewById(R.id.create_group_username);
        mGroupMembersList = (ListView) findViewById(R.id.create_group_member_list);
        mAddMemberButton = (Button) findViewById(R.id.create_group_add_member);
        mConfirmButton = (FloatingActionButton) findViewById(R.id.confirm_group_fab);
        mListAdapter = new MemberListAdapter(this);

        mGroupMembersList.setAdapter(mListAdapter);
        mAddMemberButton.setVisibility(View.VISIBLE);

        mAddMemberButton.setOnClickListener(this);
        mConfirmButton.setOnClickListener(this);
    }

    private void addMember() {
        String name = mGroupMember.getText().toString();
        User member = new User(name, name);
        mListAdapter.addMember(member);
        mListAdapter.notifyDataSetChanged();
        mGroupMember.getText().clear();
    }

    private void createGroup() {
        GroupDao groupDao = new GroupDao(this);
        List<User> members = mListAdapter.getMembersList();
        Group group = new Group(mGroupName.getText().toString(), members);
        groupDao.save(group);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.create_group_add_member) {
            addMember();
        } else {
            createGroup();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
