package com.example.mailclient.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import eu.erikw.PullToRefreshListView;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBarUtils;
import fr.castorflex.android.smoothprogressbar.SmoothProgressDrawable;

/*
 *  Activity that displays the to do panel
 */
public class TodoFragment extends Fragment {

    public static ArrayList<Email> todo_list;
    static SmoothProgressBar mPocketBar;
    static PullToRefreshListView listView;
    static EmailAdapter adapter;
    Animator animator;
    int list_position, list_visible_position;
    View child_focused;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*
         *  Instantiate to do array list and its adapter
         */
        todo_list = new ArrayList<Email>();
        for (int i=0; i<Mailbox.emailList.size(); i++) {
            if ((Mailbox.emailList.get(i).todo || !Mailbox.emailList.get(i).seen) && (!Mailbox.emailList.get(i).deleted)) {
                todo_list.add(Mailbox.emailList.get(i));
            }
            else {}
        }

        View view = inflater.inflate(R.layout.fragment_layout, container, false);

        /*
         *  Instantiate pullable listView
         */
        listView = (PullToRefreshListView) view.findViewById(R.id.listView);
        listView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).receiveMail(mPocketBar);
                }
            }
        });

        adapter = new EmailAdapter(this.getActivity(), R.id.list_subject, todo_list){
            /*
             * Used to get visible position of the list item in the adapter
             * (different from actual position in the list) !
             */
            @Override
            public void processPosition(View view) {
                list_position = listView.getPositionForView(view);
                list_visible_position = list_position - listView.getFirstVisiblePosition();
                child_focused = view;
            }
        };
        listView.setAdapter(adapter);
        listView.setEmptyView(view.findViewById(R.id.empty_todo));

        /*
         *  Istantiate progress bar and hide it
         */
        mPocketBar = (SmoothProgressBar) view.findViewById(R.id.pocket);
        mPocketBar.setVisibility(View.GONE);
        mPocketBar.setSmoothProgressDrawableBackgroundDrawable(
                SmoothProgressBarUtils.generateDrawableWithColors(
                        getResources().getIntArray(R.array.pocket_background_colors),
                        ((SmoothProgressDrawable) mPocketBar.getIndeterminateDrawable()).getStrokeWidth())
        );
        mPocketBar.progressiveStop();

        /*
         *  Notify main activity of fragment
         */
        MainActivity.current_fragment = 0;

        animator = new Animator();

        /*
         * Add touch listener to swipe and unpin
         * email from the listview
         */
        listView.setOnTouchListener(new SwipeDetector(0) {
            @Override
            public void getResults() {
                if (this.swipeDetected()){
                    if (this.getAction().equals(Action.LR_TRIGGER)) {
                        // do the onSwipe action
                        animator.swipeTodo(child_focused, list_position - 1);
                        Email email = Mailbox.emailList.get(Mailbox.emailList.indexOf(todo_list.get(list_position - 1)));
                        email.removeTodo();
                        if (!email.seen) {
                            UpdateSeenMailTask update_task = new UpdateSeenMailTask(getActivity());
                            update_task.execute(email.ID);
                            email.seen = true;
                        }
                    }
                    else if (this.getAction().equals(Action.CLICK)) {
                        // open email
                        Intent intent = new Intent(getActivity(), ReadMail.class);
                        int index = Mailbox.emailList.indexOf(todo_list.get(list_position - 1));
                        intent.putExtra("index", index);
                        intent.putExtra("todo", true);
                        startActivity(intent);
                    }
                    else if (this.getAction().equals(Action.LR_BACK)) {
                        // reset view
                        animator.resetView(listView.getChildAt(list_visible_position));
                    }
                    else {

                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setActionBarTitle("to do");
            ((MainActivity) getActivity()).mDrawerLayout.closeDrawers();
        }
    }



//
//        mDrawerLayout.closeDrawers();
//    }
}