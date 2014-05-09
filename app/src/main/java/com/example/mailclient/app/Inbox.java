package com.example.mailclient.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import eu.erikw.PullToRefreshListView;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBarUtils;
import fr.castorflex.android.smoothprogressbar.SmoothProgressDrawable;

import com.example.mailclient.app.DrawerAdapter;


/*
 *  Activity that displays the inbox
 */
public class Inbox extends Activity {

    /*
    *   Set main variables
    */
    static EmailAdapter adapter;
    public static Context baseContext;
    public static SmoothProgressBar mPocketBar;
    public static PullToRefreshListView listView;
    public static Button refresh_button;
    private Animator animator;
    public static ArrayList<Email> inbox_email_list;

    /*
     *  Drawer menu variables
     */
    private String[] mDrawerLinks;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    int list_position, list_visible_position;
    View child_focused;

    public Inbox() {
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_list);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        /*
         *  Implement drawer layout and adapters
         */
        mDrawerLinks = getResources().getStringArray(R.array.sidebar_icons);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new DrawerAdapter(this, R.layout.drawer_list, mDrawerLinks));
        // TODO: complete the drawer listener
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    finish();
                }
                else if (i == 1) {
                    mDrawerLayout.closeDrawers();
                }
                else if (i == 3) {
                    Intent intent;
                    intent = new Intent(Todo.baseContext, TrashBin.class);
                    startActivity(intent);
                    finish();
                }
                else {
                }
            }
        });

        /*
         *  Drawer adapter and list
         */
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, R.drawable.ic_drawer , R.string.drawer_open, R.string.drawer_close){
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }
            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        /*
         *  Istantiate cache / storage classes
         */
        baseContext = getBaseContext();

        /*
         *  Instantiate pullable listView
         */
        listView = (PullToRefreshListView) findViewById(R.id.listView);
        listView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                receiveEmail();
            }
        });

        /*
         *  Instantiate list adapter
         */
        inbox_email_list = new ArrayList<Email>();
        for (int i=0; i<Mailbox.emailList.size(); i++) {
            if (!Mailbox.emailList.get(i).deleted) {
                inbox_email_list.add(Mailbox.emailList.get(i));
            }
        }
        adapter = new EmailAdapter(this, R.id.list_subject, inbox_email_list){
            @Override
            public void processPosition(View view) {
                list_position = listView.getPositionForView(view);
                list_visible_position = list_position - listView.getFirstVisiblePosition();
                Log.i("swipe", "processPosition returned " + list_position);
                child_focused = view;
            }
        };
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.empty_email));

        /*
         *  Istantiate progress bar and hide it
         */
        mPocketBar = (SmoothProgressBar) findViewById(R.id.pocket);
        mPocketBar.setSmoothProgressDrawableBackgroundDrawable(
                SmoothProgressBarUtils.generateDrawableWithColors(
                        getResources().getIntArray(R.array.pocket_background_colors),
                        ((SmoothProgressDrawable) mPocketBar.getIndeterminateDrawable()).getStrokeWidth()));
        mPocketBar.setVisibility(View.GONE);
        mPocketBar.progressiveStop();

        animator = new Animator();

        listView.setOnTouchListener(new SwipeDetector(1) {
            @Override
            public void getResults() {
                if (this.swipeDetected()){

                    if (this.getAction().equals(SwipeDetector.Action.RL_TRIGGER)) {
                        // do the onSwipe action
                        Log.i("swipe", "pinned su el " + list_position);
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(baseContext, "pinned", duration);
                        toast.show();
                        Mailbox.emailList.get(Mailbox.emailList.indexOf(inbox_email_list.get(list_position - 1))).addTodo();
                        animator.resetView(listView.getChildAt(list_visible_position));
                    }
                    else if (this.getAction().equals(SwipeDetector.Action.LR_TRIGGER)) {
                        Log.i("swipe", "delete su el " + list_position);
                        // eliminare
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(baseContext, "deleted", duration);
                        toast.show();
                        Log.i("droidrage", "size di inbox_email_list è " + inbox_email_list.size());
                        Email email = Mailbox.emailList.get(Mailbox.emailList.indexOf(inbox_email_list.get(list_position - 1)));
                        email.removeTodo();
                        animator.swipeDelete(child_focused, list_position - 1);
                        Mailbox.emailList.get(Mailbox.emailList.indexOf(inbox_email_list.get(list_position - 1))).deleted = true;
                        UpdateDeletedMailTask update_deleted_task = new UpdateDeletedMailTask(Inbox.this);
                        update_deleted_task.execute(email.ID);
                    }
                    else if (this.getAction().equals(SwipeDetector.Action.CLICK)) {
                        // open email
                        Log.i("swipe", "open el " + list_position);
                        Intent intent = new Intent(Inbox.this, ReadMail.class);
                        int index = Mailbox.emailList.indexOf(inbox_email_list.get(list_position - 1));
                        intent.putExtra("index", index);
                        startActivity(intent);
                    }
                    else if (this.getAction().equals(SwipeDetector.Action.LR_BACK)) {
                        Log.i("swipe", "back su el " + list_position);
                        animator.resetView(listView.getChildAt(list_visible_position));
                    }
                    else if (this.getAction().equals(SwipeDetector.Action.RL_BACK)) {
                        Log.i("swipe", "back su el " + list_position);
                        //go back
                        animator.resetView(listView.getChildAt(list_visible_position));
                    }
                    else if (this.getAction().equals(SwipeDetector.Action.RESET)){
                        animator.resetView(listView.getChildAt(list_visible_position));
                    }
                }
            }
        });
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    //inflate the menu with custom actions
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("on resume", "resumed");
        adapter.notifyDataSetChanged();
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void onPause() {
        super.onPause();
        Mailbox.save(Mailbox.emailList);
    }

    /*
     *  Triggered from send email button:
     *  call send email activity
     */
    public void sendEmail(MenuItem menu) {
        Intent intent = new Intent(this, SendMailActivity.class);
        startActivity(intent);
    }

    /*
     *  Execute receive mail async task
     *  and triggers progress bar.
     *  The firs is triggered from main activity,
     *  the second is triggered from refresh_button
     */
    public void receiveEmail() {
        mPocketBar.setVisibility(View.VISIBLE);
        mPocketBar.progressiveStart();
        ReceiveMailTask receive_task = new ReceiveMailTask(Inbox.this);
        receive_task.execute(Mailbox.account_email, Mailbox.account_password);
    }

    public void receiveMail(View view) {
        refresh_button = (Button) findViewById(R.id.empty_email);
        refresh_button.setVisibility(View.GONE);
        mPocketBar.setVisibility(View.VISIBLE);
        mPocketBar.progressiveStart();
        ReceiveMailTask receive_task = new ReceiveMailTask(Inbox.this);
        receive_task.execute(Mailbox.account_email, Mailbox.account_password);
    }
}
