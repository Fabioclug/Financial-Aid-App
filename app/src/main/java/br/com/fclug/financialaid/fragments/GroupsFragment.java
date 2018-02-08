package br.com.fclug.financialaid.fragments;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import br.com.fclug.financialaid.CreateGroupActivity;
import br.com.fclug.financialaid.CreateLocalGroupActivity;
import br.com.fclug.financialaid.GroupSummaryActivity;
import br.com.fclug.financialaid.R;
import br.com.fclug.financialaid.adapter.GroupRecyclerViewListAdapter;
import br.com.fclug.financialaid.adapter.RecyclerViewListAdapter;
import br.com.fclug.financialaid.interfaces.OnListClickListener;
import br.com.fclug.financialaid.models.Group;
import br.com.fclug.financialaid.utils.AppConstants;

public class GroupsFragment extends Fragment implements View.OnClickListener {

    private RecyclerView mGroupsListView;
    private GroupRecyclerViewListAdapter mListAdapter;
    private boolean isFabMenuOpen = false;

    private LinearLayout mCreateOnlineGroupLayout;
    private LinearLayout mCreateOfflineGroupLayout;
    private FloatingActionButton mCreateGroupButton;
    private RelativeLayout obstructor;

    private int mLastClickedPosition;

    public static int REQUEST_ADD_GROUP = 1;
    public static int REQUEST_UPDATE_GROUPS = 2;

    private OnListClickListener mListClickListener = new OnListClickListener() {
        @Override
        public void onItemClick(Object groupObject, int position) {
            Group group = (Group) groupObject;
            mLastClickedPosition = position;
            Intent intent = new Intent(getContext(), GroupSummaryActivity.class);
            intent.putExtra("group", group);
            startActivityForResult(intent, REQUEST_UPDATE_GROUPS);
        }

        @Override
        public void onItemLongClick(View v, Object o) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (container == null)
            return null;
        View view = inflater.inflate(R.layout.groups_fragment, container, false);
        mGroupsListView = (RecyclerView) view.findViewById(R.id.groups_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mGroupsListView.setLayoutManager(layoutManager);

        mListAdapter = new GroupRecyclerViewListAdapter(getContext());
        mGroupsListView.setAdapter(mListAdapter);
        mListAdapter.setListItemClickListener(mListClickListener);
        mListAdapter.setListItems();

        mCreateGroupButton = (FloatingActionButton) view.findViewById(R.id.create_group_fab);

        mCreateOnlineGroupLayout = (LinearLayout) view.findViewById(R.id.create_online_group_layout);
        mCreateOfflineGroupLayout = (LinearLayout) view.findViewById(R.id.create_offline_group_layout);

        FloatingActionButton createOnlineGroup = (FloatingActionButton) view.findViewById(R.id.create_online_group_fab);
        FloatingActionButton createOfflineGroup =
                (FloatingActionButton) view.findViewById(R.id.create_offline_group_fab);

        TextView createOnlineGroupLabel = (TextView) view.findViewById(R.id.create_online_group_label);
        TextView createOfflineGroupLabel = (TextView) view.findViewById(R.id.create_offline_group_label);

        obstructor = (RelativeLayout) view.findViewById(R.id.obstructor);

        mCreateGroupButton.setOnClickListener(this);
        createOnlineGroup.setOnClickListener(this);
        createOfflineGroup.setOnClickListener(this);
        obstructor.setOnClickListener(this);
        createOnlineGroupLabel.setOnClickListener(this);
        createOfflineGroupLabel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_group_fab:
                if(!isFabMenuOpen) {
                    openFabMenu();
                } else {
                    closeFabMenu();
                }
                break;
            case R.id.create_online_group_fab:
            case R.id.create_online_group_label:
                Intent intent = new Intent(getContext(), CreateGroupActivity.class);
                startActivity(intent);
                closeFabMenu();
                break;
            case R.id.create_offline_group_fab:
            case R.id.create_offline_group_label:
                intent = new Intent(getContext(), CreateLocalGroupActivity.class);
                startActivityForResult(intent, REQUEST_ADD_GROUP);
                closeFabMenu();
                break;
            case R.id.obstructor:
                closeFabMenu();
                break;
        }
    }

    private void openFabMenu() {
        isFabMenuOpen = true;
        obstructor.setVisibility(View.VISIBLE);
        mCreateOnlineGroupLayout.setVisibility(View.VISIBLE);
        mCreateOfflineGroupLayout.setVisibility(View.VISIBLE);
        mCreateGroupButton.animate().rotationBy(360);
        mCreateOnlineGroupLayout.animate().translationY(-getResources().getDimension(R.dimen.fab_first_button_anim));
        mCreateOfflineGroupLayout.animate().translationY(-getResources().getDimension(R.dimen.fab_second_button_anim));
    }

    private void closeFabMenu() {
        isFabMenuOpen = false;
        obstructor.setVisibility(View.GONE);
        mCreateGroupButton.animate().rotationBy(-360);
        mCreateOnlineGroupLayout.animate().translationY(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isFabMenuOpen) {
                    mCreateOnlineGroupLayout.setVisibility(View.GONE);
                    mCreateOfflineGroupLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mCreateOfflineGroupLayout.animate().translationY(0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_GROUP) {
            if (resultCode == Activity.RESULT_OK) {
                Group createdGroup = data.getParcelableExtra("group");
                mListAdapter.addGroup(createdGroup);
                Snackbar.make(mCreateGroupButton, R.string.group_created, Snackbar.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_UPDATE_GROUPS) {
            if (resultCode == Activity.RESULT_OK && data.hasExtra("operation")) {
                int operation = data.getIntExtra("operation", 0);
                if (operation == AppConstants.GROUP_OPERATION_UPDATE) {
//                    mListAdapter.updateItemView(new GroupRecyclerViewListAdapter.Item(mLastClickedGroup));
//                    Snackbar.make(mCreateGroupButton, "Group updated", Snackbar.LENGTH_LONG).show();
                } else if (operation == AppConstants.GROUP_OPERATION_DELETE) {
                    final GroupRecyclerViewListAdapter.Item pendingRemoval =
                            mListAdapter.setPendingRemoval(mLastClickedPosition, false);
                    Snackbar.make(mCreateGroupButton, R.string.group_removed, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.undo, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    mListAdapter.undoRemoval(pendingRemoval, mLastClickedPosition);
                                }
                            })
                            .setDuration(RecyclerViewListAdapter.PENDING_REMOVAL_TIMEOUT)
                            .setActionTextColor(ContextCompat.getColor(getContext(), R.color.swipe_background))
                            .show();
                }
            }
        }
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