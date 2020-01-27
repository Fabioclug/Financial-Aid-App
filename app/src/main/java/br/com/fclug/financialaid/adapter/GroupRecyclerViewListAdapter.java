package br.com.fclug.financialaid.adapter;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
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
import br.com.fclug.financialaid.interfaces.OnListClickListener;
import br.com.fclug.financialaid.models.Group;
import br.com.fclug.financialaid.models.TransactionSplit;
import br.com.fclug.financialaid.models.User;
import br.com.fclug.financialaid.server.ApiRequest;
import br.com.fclug.financialaid.server.ServerUtils;
import br.com.fclug.financialaid.utils.AppUtils;

/**
 * Created by Fabioclug on 2017-11-03.
 *
 * Credits: https://github.com/anandbose/ExpandableListViewDemo
 */

public class GroupRecyclerViewListAdapter extends RecyclerViewListAdapter<GroupListItem,
        RecyclerView.ViewHolder> {

    private Context mContext;
    private OnListClickListener mListItemClickListener;
    private RecyclerView mRecyclerView;

    // used for rendering animations
    private int mLastPositionLoaded = -1;
    private final int MAXIMUM_MEMBERS_SHOWED = 3;

    private GroupListItem mOfflineHeader;
    private GroupListItem mOnlineHeader;

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitle;
        ImageView expandButton;
        View headerView;

        public HeaderViewHolder(View view) {
            super(view);
            headerTitle = view.findViewById(R.id.group_header_title);
            expandButton = view.findViewById(R.id.group_list_expand);
            headerView = view;
        }
    }

    private static class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Group group;
        TextView groupName;
        TextView groupMembers;
        TextView groupBalance;
        OnListClickListener clickListener;

        public GroupViewHolder(View view) {
            super(view);
            groupName = view.findViewById(R.id.group_name);
            groupMembers = view.findViewById(R.id.group_members);
            groupBalance = view.findViewById(R.id.group_balance);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(group, this.getAdapterPosition());
        }
    }

    public GroupRecyclerViewListAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<>();
        mOfflineHeader = new GroupListItem(mContext.getResources().getString(R.string.groups_header_offline));
        mOnlineHeader = new GroupListItem(mContext.getResources().getString(R.string.groups_header_online));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case GroupListItem.HEADER:
                itemView = inflater.inflate(R.layout.group_header_list_item, parent, false);
                return new HeaderViewHolder(itemView);
            case GroupListItem.CHILD:
                itemView = inflater.inflate(R.layout.group_account_list_item, parent, false);
                return new GroupViewHolder(itemView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final GroupListItem item = mItems.get(position);
        switch (item.type) {
            case GroupListItem.HEADER:
                final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.headerTitle.setText(item.title);

                if (item.hiddenChildren == null) {
                    headerViewHolder.expandButton.setImageResource(R.drawable.ic_collapse);
                } else {
                    headerViewHolder.expandButton.setImageResource(R.drawable.ic_expand);
                }
                headerViewHolder.headerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (item.hiddenChildren == null) {
                            item.hiddenChildren = new ArrayList<GroupListItem>();
                            int count = 0;
                            int pos = headerViewHolder.getAdapterPosition();
                            while (mItems.size() > pos + 1 && mItems.get(pos + 1).type == GroupListItem.CHILD) {
                                item.hiddenChildren.add(mItems.remove(pos + 1));
                                count++;
                            }
                            notifyItemRangeRemoved(pos + 1, count);
                            headerViewHolder.expandButton.setImageResource(R.drawable.ic_expand);
                        } else {
                            int pos = headerViewHolder.getAdapterPosition();
                            int index = pos + 1;
                            for (GroupListItem i : item.hiddenChildren) {
                                mItems.add(index, i);
                                index++;
                            }
                            notifyItemRangeInserted(pos + 1, index - pos - 1);
                            headerViewHolder.expandButton.setImageResource(R.drawable.ic_collapse);
                            item.hiddenChildren = null;
                        }
                    }
                });
                break;
            case GroupListItem.CHILD:
                final Group groupItem = item.group;
                GroupViewHolder groupViewHolder = (GroupViewHolder) holder;
                groupViewHolder.group = groupItem;
                groupViewHolder.clickListener = mListItemClickListener;
                StringBuilder members = new StringBuilder();

                // display only the maximum number of members, if there are more, show how many
                int i, membersNumber = groupItem.getMembersNumber(),
                        maximum = (membersNumber > MAXIMUM_MEMBERS_SHOWED ? MAXIMUM_MEMBERS_SHOWED : membersNumber);

                for (i = 0; i < maximum; i++) {
                    members.append(groupItem.getMembers().get(i).getExhibitName()).append("\n");
                }

                if (membersNumber > MAXIMUM_MEMBERS_SHOWED) {
                    int remaining = membersNumber - MAXIMUM_MEMBERS_SHOWED;
                    members.append("and ").append(remaining).append(" more");
                }

                groupViewHolder.groupName.setText(groupItem.getName());
                groupViewHolder.groupMembers.setText(members.toString());

                long balance = getBalance(groupItem);
                groupViewHolder.groupBalance.setText(AppUtils.formatCurrencyValue(balance));

                if (balance == 0) {
                    groupViewHolder.groupBalance.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                } else if (balance < 0) {
                    groupViewHolder.groupBalance.setTextColor(ContextCompat.getColor(mContext,
                            R.color.transaction_type_debt));
                } else {
                    groupViewHolder.groupBalance.setTextColor(ContextCompat.getColor(mContext,
                            R.color.transaction_type_credit));
                }
                break;
        }
    }

    @Override
    public void removeFromDatabase(GroupListItem item) {
        Group groupItem = item.group;
        if (groupItem != null) {
            if (groupItem.isOnline()) {
                ApiRequest.RequestCallback callback = new ApiRequest.RequestCallback() {
                    @Override
                    public void onSuccess(JSONObject response) throws JSONException {
                        Log.d("GroupListAdapter", "Group successfully removed");
                    }

                    @Override
                    public void onFailure(int code) {
                        Log.e("GroupListAdapter", "Unable to remove group");
                    }
                };
                JSONObject args = new JSONObject();
                try {
                    args.put("token", SessionManager.getUserDetails(mContext).get(SessionManager.KEY_TOKEN));
                    args.put("group_id", groupItem.getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new ApiRequest(ServerUtils.METHOD_POST, ServerUtils.ROUTE_REMOVE_GROUP, args, callback).execute();
            } else {
                GroupDao dao = new GroupDao(mContext);
                dao.delete(groupItem);
            }
        }
    }

    /** Important: Never call notifyDataSetChanged, because it ruins the elevation on the views **/
    @Override
    public void setListItems() {

        // get offline groups
        GroupDao mGroupsDao = new GroupDao(mContext);
        List<Group> offlineGroups = mGroupsDao.findAll();
        if (offlineGroups.size() > 0) {
            mItems.add(mOfflineHeader);
            for (Group offlineGroup : offlineGroups) {
                GroupListItem offlineGroupItem = new GroupListItem(offlineGroup);
                mItems.add(offlineGroupItem);
            }
        }

        final int loadedItems = mItems.size();
        if (loadedItems > 0) {
            notifyItemRangeInserted(0, loadedItems);
        }

        // get online groups
        HashMap<String, String> user = SessionManager.getUserDetails(mContext);
        ApiRequest.RequestCallback callback = new ApiRequest.RequestCallback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                JSONArray groups = response.getJSONArray("result");
                if (groups.length() > 0) {
                    mItems.add(mOnlineHeader);
                    for (int i = 0; i < groups.length(); i++) {
                        JSONObject groupJsonData = groups.getJSONObject(i);
                        GroupListItem onlineGroupItem = new GroupListItem(new Group(groupJsonData));
                        mItems.add(onlineGroupItem);
                    }

                    // this is needed to load the new results in the UI thread, the data is not supposed to be loaded on
                    // a background thread
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyItemRangeInserted(loadedItems, mItems.size() - loadedItems);
                        }
                    });
                }
            }

            @Override
            public void onFailure(int code) {
                Log.d("GroupListAdapter", code + "couldn't get online groups");
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

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).type;
    }

    private long getBalance(Group group) {
        long value = 0;
        List<TransactionSplit> credits = group.getGroupBalances();
        User current;
        if (SessionManager.isLoggedIn(mContext) && group.isOnline()) {
            HashMap<String, String> userDetails = SessionManager.getUserDetails(mContext);
            current = new User(userDetails.get(SessionManager.KEY_USERNAME));
        } else {
            current = new User(mContext.getResources().getString(R.string.offline_default_member));
        }
        for (TransactionSplit credit : credits) {
            if (current.equals(credit.getDebtor())) {
                value = credit.getValue();
            }
        }
        
        return value;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.mRecyclerView = recyclerView;
    }

    public void setListItemClickListener(OnListClickListener listItemClickListener) {
        mListItemClickListener = listItemClickListener;
    }

    public void addGroup(Group group) {
        GroupListItem newItem = new GroupListItem(group);
        if (group.isOnline()) {
            int rangeStart = mItems.size();
            if (!mItems.contains(mOnlineHeader)) {
                mItems.add(mOnlineHeader);
            }
            mItems.add(newItem);
            notifyItemRangeInserted(rangeStart, mItems.size() - rangeStart);
        } else {
            int offlineIndex = 0;
            int addedCount = 1;
            if (!mItems.contains(mOfflineHeader)) {
                mItems.add(offlineIndex, mOfflineHeader);
                mItems.add(offlineIndex + 1, newItem);
                addedCount = 2;
            } else {
                if (mItems.contains(mOnlineHeader)) {
                    offlineIndex = mItems.indexOf(mOnlineHeader);
                } else {
                    offlineIndex = mItems.size();
                }
                mItems.add(offlineIndex, newItem);
            }
            notifyItemRangeInserted(offlineIndex, addedCount);
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        // animate when item appears on screen
        super.onViewAttachedToWindow(holder);
        int position = holder.getAdapterPosition();
        if (position > mLastPositionLoaded) {
            mLastPositionLoaded = position;
            holder.itemView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_from_bottom));
        } else {
            holder.itemView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fall_down_fade_in));
        }
    }

    @Override
    public GroupListItem setPendingRemoval(int position, boolean showSwipeLayout) {
        GroupListItem result = super.setPendingRemoval(position, showSwipeLayout);
        int headerPosition = position - 1;
        if (result.group.isOnline()) {
            if (mItems.indexOf(mOnlineHeader) == (mItems.size() - 1)) {
                mItems.remove(mOnlineHeader);
                notifyItemRemoved(headerPosition);
            }
        } else {
            if (mItems.size() == 1 ||
                    (headerPosition == mItems.indexOf(mOfflineHeader) && position == mItems.indexOf(mOnlineHeader))) {
                mItems.remove(mOfflineHeader);
                notifyItemRemoved(headerPosition);
            }
        }
        return result;
    }

    @Override
    public void undoRemoval(GroupListItem item, int... position) {
        GroupListItem header = item.group.isOnline() ? mOnlineHeader : mOfflineHeader;
        int headerPosition = position[0] - 1;
        if (!mItems.contains(header)) {
            mItems.add(headerPosition, header);
            notifyItemInserted(headerPosition);
        }

        super.undoRemoval(item, position);
    }

    public void updateGroupItem(Group updatedGroup, int position) {
        GroupListItem groupItem = mItems.get(position);
        groupItem.group = updatedGroup;
        updateItemView(groupItem);
    }
}
