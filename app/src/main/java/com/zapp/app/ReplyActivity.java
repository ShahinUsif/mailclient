package com.zapp.app;


/**
 * Created by teo on 01/04/14.
 */

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mailclient.app.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/*
*   Send and receive emails activity.
*   Async tasks are used because Android doesn't
*   allow Network operation in the main thread.
*/

public class ReplyActivity extends Activity {

    AuthPreferences authPreferences;
    private static final int OLDERVERSION = 0;
    private static final int NEWVERSION = 1;
    ArrayList<String> selectedImagePath,attachmentList;
    EditText toEmailText, ccEmailText, bccEmailText, subjectEmailText, bodyEmailText;
    TextView attachmentView;
    LinearLayout.LayoutParams params;
    TableLayout lm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sendmailactivity);


        toEmailText = (EditText) this.findViewById(R.id.send_to_edit);
        ccEmailText = (EditText) this.findViewById(R.id.send_cc_edit);
        bccEmailText = (EditText) this.findViewById(R.id.send_bcc_edit);

        subjectEmailText = (EditText) this.findViewById(R.id.send_subject_edit);
        bodyEmailText = (EditText) this.findViewById(R.id.send_body);
        selectedImagePath = new ArrayList<String>();

        final Intent intent = getIntent();
        subjectEmailText.setText(intent.getStringExtra("subject"));
        toEmailText.setText(intent.getStringExtra("to"));
        ccEmailText.setText(intent.getStringExtra("cc"));
        bccEmailText.setText(intent.getStringExtra("bcc"));

        this.setTitle(intent.getStringExtra("replyType"));



        /*
         *  Set quoted original message
         */
        String body_cont = intent.getStringExtra("body");
        /*
         *  Insert message if voice-called
         */
        String speech_content = intent.getStringExtra("content");
        if (speech_content != null) {
            bodyEmailText.setText(speech_content+"\nOriginal message: \n\n" + body_cont);
        }
        else {
            bodyEmailText.setText("\nOriginal message: \n\n" + body_cont);
        }
        bodyEmailText.requestFocus();
        bodyEmailText.setSelection(0);


        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        attachmentView = (TextView) this.findViewById(R.id.attachment);
        attachmentList = new ArrayList<String> ();

        lm = (TableLayout) findViewById(R.id.array_button);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            selectedImagePath.add(getPath(selectedImageUri));

            //Show toast with "name of file" added
            File file = new File(getPath(selectedImageUri));
            String fileName=file.getName();
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, fileName + " attached!", duration);
            toast.show();

            //concat filename to attachment's textview
            attachmentList.add(fileName);
            lm.removeAllViews();

            for (int i = 0; i < attachmentList.size(); i++) {

                // Create LinearLayout
                final LinearLayout ll = new LinearLayout(this);
                ll.setOrientation(LinearLayout.VERTICAL);

                final Button btn = new Button(this);
                btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.view_attachment,0,0,0);
                btn.setText(new File(attachmentList.get(i)).getName());
                btn.setLayoutParams(params);
                btn.setId(i);
                final int indice = i;
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReplyActivity.this);
                        // Add the buttons
                        alertDialog.setPositiveButton(R.string.view, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User views the attachment
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(new File(selectedImagePath.get(indice))), "image/*");
                                startActivity(intent);
                            }
                        });
                        alertDialog.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User removes the attachment
                                selectedImagePath.remove(indice);
                                attachmentList.remove(indice);
                                ll.removeView(btn);
                            }
                        });
                        alertDialog.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User cancels the dialog
                            }
                        });
                        alertDialog.show();
                    }
                });

                //Add button to LinearLayout
                ll.addView(btn);
                //Add button to LinearLayout defined in XML
                lm.addView(ll);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_mail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                //Check before exit

                new AlertDialog.Builder(this)
                        .setTitle("You are closing the email sender ")
                        .setMessage("Do you want to close your email?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Close the activity
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        } else
            return uri.getPath();               //
    }

    public void sendEmail(MenuItem menu) {

        toEmailText = (EditText) this.findViewById(R.id.send_to_edit);
        ccEmailText = (EditText) this.findViewById(R.id.send_cc_edit);
        bccEmailText = (EditText) this.findViewById(R.id.send_bcc_edit);

        subjectEmailText = (EditText) this.findViewById(R.id.send_subject_edit);
        bodyEmailText = (EditText) this.findViewById(R.id.send_body);

        String fromEmail = Mailbox.account_email;
        String fromPassword = Mailbox.account_password;
        String toEmails = toEmailText.getText().toString();
        String ccEmails = ccEmailText.getText().toString();
        String bccEmails = bccEmailText.getText().toString();
        List<String> toEmailList = Arrays.asList(toEmails
                .split("\\s*,\\s*"));
        List<String> ccEmailList = Arrays.asList(ccEmails
                .split("\\s*,\\s*"));
        List<String> bccEmailList = Arrays.asList(bccEmails
                .split("\\s*,\\s*"));
        String emailSubject = subjectEmailText.getText().toString();
        String emailBody = bodyEmailText.getText().toString();

        if (selectedImagePath.size() == 0) {
            new SendMailTask(ReplyActivity.this).execute(fromEmail, fromPassword, toEmailList, emailSubject, emailBody, ccEmailList, bccEmailList);
        } else {
            new SendMailTask(ReplyActivity.this).execute(fromEmail, fromPassword, toEmailList, emailSubject, emailBody, ccEmailList, bccEmailList, selectedImagePath);
        }
    }

    public void addAttachment(MenuItem menu) {
        if (Build.VERSION.SDK_INT < 19) {
            Intent intent = new Intent();
            intent.setType("image/jpeg");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, OLDERVERSION);
        } else {
            // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
            // browser.
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

            // Filter to only show results that can be "opened", such as a
            // file (as opposed to a list of contacts or timezones)
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            // Filter to show only images, using the image MIME data type.
            // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
            // To search for all documents available via installed storage providers,
            // it would be "*/*".
            intent.setType("image/*");
        }
    }
    public void passwordDialog(){

        AlertDialog.Builder alert = new AlertDialog.Builder(ReplyActivity.this);

        alert.setTitle("You credentials are wrong");
        alert.setMessage("Insert the correct password for the account " + Mailbox.account_email);

        // Set an EditText view to get user input
        final EditText input = new EditText(ReplyActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        alert.setView(input);

        authPreferences = new AuthPreferences(ReplyActivity.this);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                authPreferences.setPassword(input.getText().toString());
                Mailbox.account_password = input.getText().toString();
                //ReplyActivity.this.finish();
                // Do something with value!
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //ReplyActivity.this.finish();
                // Canceled.
            }
        });

        alert.show();
    }
}
