package br.com.fclug.financialaid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.fclug.financialaid.database.GroupDao;
import br.com.fclug.financialaid.models.Group;
import br.com.fclug.financialaid.models.User;
import br.com.fclug.financialaid.server.ApiRequest;
import br.com.fclug.financialaid.server.ServerUtils;

/**
 * Created by Fabioclug on 2016-09-19.
 */
public class GroupsListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Group> mOnlineGroups;
    private List<Group> mOfflineGroups;

    GroupsListAdapter(Context context) {
        mContext = context;
        mOnlineGroups = new ArrayList<>();
        mOfflineGroups = new ArrayList<>();
    }

    private static class ViewHolderGroupItem {
        TextView groupName;
        TextView groupMembers;
    }

    public void updateListItems() {

        // get offline groups
        GroupDao mGroupsDao = new GroupDao(mContext);
        mOfflineGroups = new ArrayList<>();
        List<Group> offlineGroups = mGroupsDao.findAll();
        for (Group offlineGroup : offlineGroups) {
            mOfflineGroups.add(offlineGroup);
        }
        notifyDataSetChanged();

        // get online groups
        SessionManager s = new SessionManager(mContext);
        HashMap<String, String> user = s.getUserDetails();
        mOnlineGroups = new ArrayList<>();
        ApiRequest.RequestCallback callback = new ApiRequest.RequestCallback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                JSONArray groups = response.getJSONArray("result");
                for (int i = 0; i < groups.length(); i++) {
                    JSONObject group = groups.getJSONObject(i);

                    JSONArray members = group.getJSONArray("members");
                    List<User> memberList = new ArrayList<>();
                    for(int j = 0; j < members.length(); j++) {
                        JSONObject member = members.getJSONObject(j);
                        memberList.add(new User(member.getString("username"), member.getString("name")));
                    }
                    mOnlineGroups.add(new Group(group.getLong("group_id"), group.getString("name"), memberList, true));
                }
                notifyDataSetChanged();
            }

            @Override
            public void onFailure(int code) {

            }
        };
        JSONObject args = new JSONObject();
        try {
            args.put("username", user.get(SessionManager.KEY_NAME));
            args.put("token", user.get(SessionManager.KEY_TOKEN));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_GET_GROUPS, args, callback).execute();
    }

    @Override
    public int getCount() {
        return mOnlineGroups.size() + mOfflineGroups.size();
    }

    @Override
    public Group getItem(int position) {
        int countOnline = mOnlineGroups.size();
        if (position < countOnline) {
            return mOnlineGroups.get(position);
        } else {
            return mOfflineGroups.get(position - countOnline);
        }
    }

    @Override
    public long getItemId(int position) {
        int countOnline = mOnlineGroups.size();
        if (position < countOnline) {
            return mOnlineGroups.get(position).getId();
        } else {
            return mOfflineGroups.get(position - countOnline).getId();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderGroupItem viewHolderGroupItem;
        Group groupItem;

        int countOnline = mOnlineGroups.size();
        if (position < countOnline) {
            groupItem = mOnlineGroups.get(position);
        } else {
            groupItem = mOfflineGroups.get(position - countOnline);
        }

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_account_list_item, parent, false);
            viewHolderGroupItem = new ViewHolderGroupItem();
            viewHolderGroupItem.groupName = (TextView) convertView.findViewById(R.id.group_name);
            viewHolderGroupItem.groupMembers = (TextView) convertView.findViewById(R.id.group_members);

            convertView.setTag(viewHolderGroupItem);
        } else {
            viewHolderGroupItem = (ViewHolderGroupItem) convertView.getTag();
        }

        viewHolderGroupItem.groupName.setText(groupItem.getName());
        viewHolderGroupItem.groupMembers.setText(String.valueOf(groupItem.getMembersNumber()));

        return convertView;
    }
}
