package com.example.mailclient.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;


public class MainActivity extends Activity {

    /*
    *   Set main variables
    */
    public static Mailbox mailbox;
    public static Context baseContext;
    public static int current_fragment;
    public static String TAG;

    /*
     *  Drawer menu variables
     */
    String[] mDrawerLinks;
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_list);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        baseContext = getBaseContext();
        mailbox = new Mailbox(baseContext);

        TAG = "TODO";

        /*
         *  Implement drawer layout and adapters
         */
        mDrawerLinks = getResources().getStringArray(R.array.sidebar_icons);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new DrawerAdapter(this, R.layout.drawer_list, mDrawerLinks));
        // TODO: complete the drawer listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

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
         *  Istantiate fragment
         */
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        TodoFragment fragment = new TodoFragment();
        fragmentTransaction.add(R.id.main_content, fragment, TAG);
        fragmentTransaction.commit();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
     *  The first is triggered from main activity,
     *  the second is triggered from refresh_button
     */
    public void receiveMail(SmoothProgressBar mPocketBar) {
        mPocketBar.setVisibility(View.VISIBLE);
        mPocketBar.progressiveStart();
        ReceiveSentMailTask receive_sent_task = new ReceiveSentMailTask(MainActivity.this);
        ReceiveInboxTask receive_task = new ReceiveInboxTask(MainActivity.this);
        receive_sent_task.execute(Mailbox.account_email, Mailbox.account_password);
        receive_task.execute(Mailbox.account_email, Mailbox.account_password);
    }

    public void receiveMail(View view) {
        SmoothProgressBar mPocketBar = (SmoothProgressBar) getFragmentManager().findFragmentById(R.id.main_content).getView().findViewById(R.id.pocket);
        mPocketBar.setVisibility(View.VISIBLE);
        mPocketBar.progressiveStart();
        ReceiveSentMailTask receive_sent_task = new ReceiveSentMailTask(MainActivity.this);
        ReceiveInboxTask receive_task = new ReceiveInboxTask(MainActivity.this);
        receive_sent_task.execute(Mailbox.account_email, Mailbox.account_password);
        receive_task.execute(Mailbox.account_email, Mailbox.account_password);
    }

    /*
     * Drawer listener for click events
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /*
     * This method is called when the user clicks
     * on an item of the drawer pane
     */
    private void selectItem(int position) {
        if (position == current_fragment) {
            mDrawerLayout.closeDrawers();
        }
        else {
            Fragment fragment = null;
            FragmentManager fragmentManager = getFragmentManager();
            switch (position) {
                case 0:
                    fragment = new TodoFragment();
                    TAG = "TODO";
                    break;
                case 1:
                    fragment = new InboxFragment();
                    TAG = "INBOX";
                    break;
                case 2:
                    fragment = new SentFragment();
                    TAG = "SENT";
                    break;
                case 3:
                    fragment = new TrashBinFragment();
                    TAG = "TRASH";
                    break;
            }
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
            ft.replace(R.id.main_content, fragment, TAG);
            ft.commit();

        }
    }

    /*
     * Call this method from fragment to set
     * panel title
     */
    public void setActionBarTitle(String title){
        setTitle(title);
    }
}