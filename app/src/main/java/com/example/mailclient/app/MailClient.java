package com.example.mailclient.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBarUtils;
import fr.castorflex.android.smoothprogressbar.SmoothProgressDrawable;


public class MailClient extends Activity {

    /*
    *   Set main variables
    */
    static ArrayList<Email> emailList;
    static EmailAdapter adapter;
    static InternalStorage storer;
    static String KEY = "mailClient";

    public static Context baseContext;
    public static SmoothProgressBar mPocketBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         *  Needed for saving methods
         */
        baseContext = getBaseContext();
        storer = new InternalStorage();
        emailList  = new ArrayList<Email>();

        /*
         *  Instantiate list adapter
         */
        ListView listView = (ListView) findViewById(R.id.listView);
        adapter = new EmailAdapter(this, R.id.list_subject, emailList);
        listView.setAdapter(adapter);

        /*
         *  Load previously cached email objects from internal storage
         */
        try {
            ArrayList<Email> emailList = (ArrayList<Email>) InternalStorage.readObject(this, KEY);
            adapter.addAll(emailList);
            adapter.notifyDataSetChanged();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        /*
         *  Open email when clicking on email in the list
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MailClient.this, ReadMail.class);
                int index = i;
                intent.putExtra("index", index);
                startActivity(intent);
            }
        });


        /*
         *  Istantiate progress bar and hide it
         */
        mPocketBar = (SmoothProgressBar) findViewById(R.id.pocket);
        mPocketBar.setSmoothProgressDrawableBackgroundDrawable(
                SmoothProgressBarUtils.generateDrawableWithColors(
                        getResources().getIntArray(R.array.pocket_background_colors),
                        ((SmoothProgressDrawable) mPocketBar.getIndeterminateDrawable()).getStrokeWidth()));
        mPocketBar.setVisibility(View.INVISIBLE);
        mPocketBar.progressiveStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    public void sendEmail(View view) {
        Intent intent = new Intent(this, SendMailActivity.class);
        startActivity(intent);
    }

    /*
     *  Triggered from receive email button:
     *  execute receive mail async task
     *  and triggers progress bar
     */
    public void receiveEmail(View view) {
        mPocketBar.setVisibility(View.VISIBLE);
        mPocketBar.progressiveStart();
        String account_email = "mailclientandroid@gmail.com";
        String account_password = "android2014";
        ReceiveMailTask receive_task = new ReceiveMailTask(MailClient.this);
        receive_task.execute(account_email, account_password);
    }

    /*
     *  Triggered from ReceiveMailTask onPostExecute method:
     *  store retrieved mail into internal storage
     */
    public static void save(ArrayList<Email> result) {
        try {
            storer.writeObject(baseContext, KEY, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
