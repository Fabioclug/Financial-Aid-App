package br.com.fclug.financialaid.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.SessionManager;
import br.com.fclug.financialaid.database.GroupDao;
import br.com.fclug.financialaid.models.Group;
import br.com.fclug.financialaid.models.OnlineUser;
import br.com.fclug.financialaid.models.TransactionSplit;
import br.com.fclug.financialaid.models.User;
import br.com.fclug.financialaid.server.ApiRequest;
import br.com.fclug.financialaid.server.ServerUtils;
import br.com.fclug.financialaid.utils.AppUtils;

/**
 * Created by Fabioclug on 2017-10-31.
 */

public class GroupTempListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<Group> mOnlineGroups;
    private List<Group> mOfflineGroups;

    private final int MAXIMUM_MEMBERS_SHOWED = 3;

    public GroupTempListAdapter(Context context) {
        mContext = context;
        mOnlineGroups = new ArrayList<>();
        mOfflineGroups = new ArrayList<>();
    }

    public void updateListItems() {

        mOnlineGroups = new ArrayList<>();
        mOfflineGroups = new ArrayList<>();

        // get offline groups
        GroupDao mGroupsDao = new GroupDao(mContext);
        List<Group> offlineGroups = mGroupsDao.findAll();
        for (Group offlineGroup : offlineGroups) {
            mOfflineGroups.add(offlineGroup);
        }
        notifyDataSetChanged();

        // get online groups
        HashMap<String, String> user = SessionManager.getUserDetails(mContext);
        ApiRequest.RequestCallback callback = new ApiRequest.RequestCallback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                JSONArray groups = response.getJSONArray("result");
                for (int i = 0; i < groups.length(); i++) {
                    JSONObject group = groups.getJSONObject(i);

                    JSONArray members = group.getJSONArray("members");
                    List<User> memberList = new ArrayList<>();
                    List<TransactionSplit> memberCredits = new ArrayList<>();
                    for(int j = 0; j < members.length(); j++) {
                        JSONObject member = members.getJSONObject(j);
                        OnlineUser user = new OnlineUser(member.getString("username"), member.getString("name"));
                        memberList.add(user);
                        memberCredits.add(new TransactionSplit(user, member.getDouble("value")));
                    }
                    mOnlineGroups.add(new Group(group.getLong("group_id"), group.getString("name"), memberList,
                            memberCredits, true));
                }
                notifyDataSetChanged();
            }

            @Override
            public void onFailure(int code) {

            }
        };
        JSONObject args = new JSONObject();
        try {
            args.put("username", user.get(SessionManager.KEY_USERNAME));
            args.put("token", user.get(SessionManager.KEY_TOKEN));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_GET_GROUPS, args, callback).execute();
    }

    private static class ViewHolderHeaderItem {
        TextView title;
    }

    private static class ViewHolderGroupItem {
        TextView groupName;
        TextView groupMembers;
        TextView groupBalance;
    }

    @Override
    public int getGroupCount() {
        return 2;
    }

    @Override
    public int getChildrenCount(int i) {
        switch (i) {
            case 0:
                return mOfflineGroups.size();
            case 1:
                return mOnlineGroups.size();
            default:
                return 0;
        }
    }

    @Override
    public Object getGroup(int i) {
        switch (i) {
            case 0:
                return R.string.groups_header_offline;
            case 1:
                return R.string.groups_header_online;
            default:
                return 0;
        }
    }

    @Override
    public Group getChild(int i, int i1) {
        switch (i) {
            case 0:
                return mOfflineGroups.get(i1);
            case 1:
                return mOnlineGroups.get(i1);
            default:
                return null;
        }
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public long getChildId(int i, int i1) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        ViewHolderHeaderItem viewHolderHeaderItem;

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.group_header_list_item, parent, false);
            viewHolderHeaderItem = new ViewHolderHeaderItem();
            viewHolderHeaderItem.title = (TextView) convertView.findViewById(R.id.group_header_title);
            convertView.setTag(viewHolderHeaderItem);
        } else {
            viewHolderHeaderItem = (ViewHolderHeaderItem) convertView.getTag();
        }

        if (groupPosition == 0) {
            viewHolderHeaderItem.title.setText(R.string.groups_header_offline);
        } else {
            viewHolderHeaderItem.title.setText(R.string.groups_header_online);
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolderGroupItem viewHolderGroupItem;
        Group groupItem = null;

        if (groupPosition == 0) {
            groupItem = mOfflineGroups.get(childPosition);
        } else if (groupPosition == 1) {
            groupItem = mOnlineGroups.get(childPosition);
        }

        if (groupItem != null) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.group_account_list_item, parent, false);
                viewHolderGroupItem = new ViewHolderGroupItem();
                viewHolderGroupItem.groupName = (TextView) convertView.findViewById(R.id.group_name);
                viewHolderGroupItem.groupMembers = (TextView) convertView.findViewById(R.id.group_members);
                viewHolderGroupItem.groupBalance = (TextView) convertView.findViewById(R.id.group_balance);

                convertView.setTag(viewHolderGroupItem);
            } else {
                viewHolderGroupItem = (ViewHolderGroupItem) convertView.getTag();
            }

            StringBuilder members = new StringBuilder();
            int i, membersNumber = groupItem.getMembersNumber(),
                    maximum = (membersNumber > MAXIMUM_MEMBERS_SHOWED ? MAXIMUM_MEMBERS_SHOWED : membersNumber);

            for (i = 0; i < maximum; i++) {
                members.append(groupItem.getMembers().get(i).getExhibitName()).append("\n");
            }

            if (membersNumber > MAXIMUM_MEMBERS_SHOWED) {
                int remaining = membersNumber - MAXIMUM_MEMBERS_SHOWED;
                members.append("and ").append(remaining).append(" more");
            }

            viewHolderGroupItem.groupName.setText(groupItem.getName());
            viewHolderGroupItem.groupMembers.setText(members.toString());

            double balance = getBalance(groupItem);
            viewHolderGroupItem.groupBalance.setText(AppUtils.formatCurrencyValue(balance));

            if (balance == 0) {
                viewHolderGroupItem.groupBalance.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            } else if (balance < 0) {
                viewHolderGroupItem.groupBalance.setTextColor(ContextCompat.getColor(mContext, R.color.transaction_type_debt));
            } else if (balance > 0) {
                viewHolderGroupItem.groupBalance.setTextColor(ContextCompat.getColor(mContext, R.color.transaction_type_credit));
            }
        }

        return convertView;
    }

    private double getBalance(Group group) {
        double value = 0;
        if (SessionManager.isLoggedIn(mContext) && group.isOnline()) {
            HashMap<String, String> userDetails = SessionManager.getUserDetails(mContext);
            User current = new User(userDetails.get(SessionManager.KEY_USERNAME));
            List<TransactionSplit> credits = group.getGroupCredits();
            for (TransactionSplit credit : credits) {
                if (current.equals(credit.getDebtor())) {
                    value = credit.getValue();
                }
            }
        } //else {

        //}
        return value;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
