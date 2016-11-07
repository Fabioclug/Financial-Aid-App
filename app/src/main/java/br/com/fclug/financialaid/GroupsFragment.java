package br.com.fclug.financialaid;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import br.com.fclug.financialaid.models.Group;

public class GroupsFragment extends Fragment {

    private ListView mGroupsListView;
    private GroupsListAdapter mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (container == null)
            return null;
        View view = inflater.inflate(R.layout.fragment_group_payments, container, false);
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

        FloatingActionButton createGroupButton = (FloatingActionButton) view.findViewById(R.id.create_group_fab);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateGroupActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mListAdapter.updateListItems();
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
