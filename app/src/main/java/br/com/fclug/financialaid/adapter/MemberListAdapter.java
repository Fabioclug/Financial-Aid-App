package br.com.fclug.financialaid.adapter;

import android.content.Context;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.models.OnlineUser;
import br.com.fclug.financialaid.models.User;

/**
 * Created by Fabioclug on 2016-09-28.
 */
public class MemberListAdapter extends BaseAdapter {

    protected Context mContext;
    protected List<User> mUsers;
    protected FloatingActionButton mDeleteButton;

    public MemberListAdapter(Context context) {
        mContext = context;
        mUsers = new ArrayList<>();
    }

    private void insertCurrentUser() {
        User currentUser = new User("You");
        mUsers.add(currentUser);
    }

    public void addMember(User user) {
        mUsers.add(user);
    };

    public List<User> getMembersList() {
        return mUsers;
    }

    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public User getItem(int position) {
        return mUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        User user = getItem(position);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.member_list_item, parent, false);
        TextView username = (TextView) convertView.findViewById(R.id.member_username);
        TextView name = (TextView) convertView.findViewById(R.id.member_name);
        if (user instanceof OnlineUser) {
            username.setText("(" + user.getUsername() + ")");
        } else {
            username.setVisibility(View.GONE);
        }
        name.setText(user.getExhibitName());

        mDeleteButton = (FloatingActionButton) convertView.findViewById(R.id.member_delete);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsers.remove(position);
                notifyDataSetChanged();
            }
        });

        if (position == 0) {
            convertView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.divider_color));
            mDeleteButton.setVisibility(View.GONE);
        }
        return convertView;
    }
}
