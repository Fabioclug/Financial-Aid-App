package br.com.fclug.financialaid;

import android.animation.Animator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import br.com.fclug.financialaid.models.Group;

public class GroupsFragment extends Fragment implements View.OnClickListener {

    private ListView mGroupsListView;
    private GroupsListAdapter mListAdapter;
    private boolean isFabMenuOpen = false;

    private LinearLayout createOnlineGroupLayout;
    private LinearLayout createOfflineGroupLayout;
    private FloatingActionButton createGroupButton;
    private RelativeLayout obstructor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (container == null)
            return null;
        View view = inflater.inflate(R.layout.group_payments_fragment, container, false);
        mGroupsListView = (ListView) view.findViewById(R.id.groups_list);
        mListAdapter = new GroupsListAdapter(getContext());
        mGroupsListView.setAdapter(mListAdapter);

        mGroupsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Group group = mListAdapter.getItem(position);
                Intent intent = new Intent(getContext(), GroupSummaryActivity.class);
                intent.putExtra("group", group);
                startActivity(intent);
            }
        });

        createGroupButton = (FloatingActionButton) view.findViewById(R.id.create_group_fab);

        createOnlineGroupLayout = (LinearLayout) view.findViewById(R.id.create_online_group_layout);
        createOfflineGroupLayout = (LinearLayout) view.findViewById(R.id.create_offline_group_layout);

        FloatingActionButton createOnlineGroup = (FloatingActionButton) view.findViewById(R.id.create_online_group_fab);
        FloatingActionButton createOfflineGroup = (FloatingActionButton) view.findViewById(R.id.create_offline_group_fab);

        TextView createOnlineGroupLabel = (TextView) view.findViewById(R.id.create_online_group_label);
        TextView createOfflineGroupLabel = (TextView) view.findViewById(R.id.create_offline_group_label);

        obstructor = (RelativeLayout) view.findViewById(R.id.obstructor);

        createGroupButton.setOnClickListener(this);
        createOnlineGroup.setOnClickListener(this);
        createOfflineGroup.setOnClickListener(this);
        obstructor.setOnClickListener(this);
        createOnlineGroupLabel.setOnClickListener(this);
        createOfflineGroupLabel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mListAdapter.updateListItems();
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
                startActivity(intent);
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
        createOnlineGroupLayout.setVisibility(View.VISIBLE);
        createOfflineGroupLayout.setVisibility(View.VISIBLE);
        createGroupButton.animate().rotationBy(180);
        createOnlineGroupLayout.animate().translationY(-getResources().getDimension(R.dimen.fab_first_button_anim));
        createOfflineGroupLayout.animate().translationY(-getResources().getDimension(R.dimen.fab_second_button_anim));
    }

    private void closeFabMenu() {
        isFabMenuOpen = false;
        obstructor.setVisibility(View.GONE);
        createGroupButton.animate().rotationBy(-180);
        createOnlineGroupLayout.animate().translationY(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isFabMenuOpen) {
                    createOnlineGroupLayout.setVisibility(View.GONE);
                    createOfflineGroupLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        createOfflineGroupLayout.animate().translationY(0);
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
