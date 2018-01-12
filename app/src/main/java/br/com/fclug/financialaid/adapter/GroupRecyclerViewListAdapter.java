package br.com.fclug.financialaid.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
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
import br.com.fclug.financialaid.models.OnlineUser;
import br.com.fclug.financialaid.models.TransactionSplit;
import br.com.fclug.financialaid.models.UniqueObject;
import br.com.fclug.financialaid.models.User;
import br.com.fclug.financialaid.server.ApiRequest;
import br.com.fclug.financialaid.server.ServerUtils;
import br.com.fclug.financialaid.utils.AppUtils;

/**
 * Created by Fabioclug on 2017-11-03.
 *
 * Credits: https://github.com/anandbose/ExpandableListViewDemo
 */

public class GroupRecyclerViewListAdapter extends RecyclerViewListAdapter<GroupRecyclerViewListAdapter.Item,
        RecyclerView.ViewHolder> {

    private Context mContext;
    private OnListClickListener mListItemClickListener;
    private RecyclerView recyclerView;
    private int mLastPositionLoaded = -1;
    private int mLastOfflineGroupIndex = -1;

    public static final int HEADER = 0;
    public static final int CHILD = 1;
    private final int MAXIMUM_MEMBERS_SHOWED = 3;


    public static class Item implements UniqueObject {
        public static long incrementalId = 1;
        public long id;
        public int type;
        public String title;
        public Group group;
        public List<Item> hiddenChildren;

        public Item(String title) {
            type = HEADER;
            this.title = title;
            setId(incrementalId);
        }

        public Item(Group group) {
            type = CHILD;
            this.group = group;
            setId(incrementalId);
        }

        @Override
        public long getId() {
            return id;
        }

        @Override
        public void setId(long id) {
            this.id = id;
            incrementalId++;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item item = (Item) o;

            if (type != item.type) return false;
            if (title != null ? !title.equals(item.title) : item.title != null) return false;
            return group != null ? group.equals(item.group) : item.group == null;
        }

        @Override
        public int hashCode() {
            int result = type;
            result = 31 * result + (title != null ? title.hashCode() : 0);
            result = 31 * result + (group != null ? group.hashCode() : 0);
            return result;
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerTitle;
        ImageView expandButton;
        View headerView;

        public HeaderViewHolder(View view) {
            super(view);
            headerTitle = (TextView) view.findViewById(R.id.group_header_title);
            expandButton = (ImageView) view.findViewById(R.id.group_list_expand);
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
            groupName = (TextView) view.findViewById(R.id.group_name);
            groupMembers = (TextView) view.findViewById(R.id.group_members);
            groupBalance = (TextView) view.findViewById(R.id.group_balance);
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
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case HEADER:
                itemView = inflater.inflate(R.layout.group_header_list_item, parent, false);
                return new HeaderViewHolder(itemView);
            case CHILD:
                itemView = inflater.inflate(R.layout.group_account_list_item, parent, false);
                return new GroupViewHolder(itemView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Item item = mItems.get(position);
        switch (item.type) {
            case HEADER:
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
                            item.hiddenChildren = new ArrayList<Item>();
                            int count = 0;
                            int pos = headerViewHolder.getAdapterPosition();
                            while (mItems.size() > pos + 1 && mItems.get(pos + 1).type == CHILD) {
                                item.hiddenChildren.add(mItems.remove(pos + 1));
                                count++;
                            }
                            notifyItemRangeRemoved(pos + 1, count);
                            headerViewHolder.expandButton.setImageResource(R.drawable.ic_expand);
                        } else {
                            int pos = headerViewHolder.getAdapterPosition();
                            int index = pos + 1;
                            for (Item i : item.hiddenChildren) {
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
            case CHILD:
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

                double balance = getBalance(groupItem);
                groupViewHolder.groupBalance.setText(AppUtils.formatCurrencyValue(balance));

                if (balance == 0) {
                    groupViewHolder.groupBalance.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                } else if (balance < 0) {
                    groupViewHolder.groupBalance.setTextColor(ContextCompat.getColor(mContext,
                            R.color.transaction_type_debt));
                } else if (balance > 0) {
                    groupViewHolder.groupBalance.setTextColor(ContextCompat.getColor(mContext,
                            R.color.transaction_type_credit));
                }
                break;
        }
    }

    @Override
    public void removeFromDatabase(Item item) {
        //TODO: implement removal from database
    }

    /** Important: Never call notifyDataSetChanged, because it ruins the elevation on the views **/
    @Override
    public void setListItems() {

        Item offlineHeader = new Item(mContext.getResources().getString(R.string.groups_header_offline));
        mItems.add(offlineHeader);

        // get offline groups
        GroupDao mGroupsDao = new GroupDao(mContext);
        List<Group> offlineGroups = mGroupsDao.findAll();
        for (Group offlineGroup : offlineGroups) {
            Item offlineGroupItem = new Item(offlineGroup);
            mItems.add(offlineGroupItem);
        }

        mLastOfflineGroupIndex = mItems.size() - 1;

        Item onlineHeader = new Item(mContext.getResources().getString(R.string.groups_header_online));
        mItems.add(onlineHeader);
        final int loadedItems = mItems.size();
        notifyItemRangeInserted(0, loadedItems);


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
                        OnlineUser user = new OnlineUser(member.getString("username"),
                                member.getString("name"));
                        memberList.add(user);
                        memberCredits.add(new TransactionSplit(user, member.getDouble("value")));
                    }
                    Item onlineGroupItem = new Item(new Group(group.getLong("group_id"),
                            group.getString("name"), memberList, memberCredits, true));
                    mItems.add(onlineGroupItem);
                }

                // this is needed to load the new results in the UI thread, the data is not supposed to be loaded on
                // a background thread
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemRangeInserted(loadedItems, mItems.size() - loadedItems);
                    }
                });
            }

            @Override
            public void onFailure(int code) {
                Log.d("RC", code + "asasas");
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

    private double getBalance(Group group) {
        double value = 0;
        List<TransactionSplit> credits = group.getGroupCredits();
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
        this.recyclerView = recyclerView;

    }

    public void setListItemClickListener(OnListClickListener listItemClickListener) {
        mListItemClickListener = listItemClickListener;
    }

    public void addGroup(Group group) {

        Item newItem = new Item(group);
        if (group.isOnline()) {
            mItems.add(newItem);
            notifyItemInserted(mItems.size() - 1);
        } else {
            mLastOfflineGroupIndex++;
            mItems.add(mLastOfflineGroupIndex, newItem);
            notifyItemInserted(mLastOfflineGroupIndex);
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
            holder.itemView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_from_bottom));
            mLastPositionLoaded = position;
        } else {
            holder.itemView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fall_down_fade_in));
        }
    }

    @Override
    protected void remove(Item pendingRemoval) {
        super.remove(pendingRemoval);
        if (!pendingRemoval.group.isOnline()) {
            mLastOfflineGroupIndex--;
        }
    }
}
