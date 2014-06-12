package com.zapp.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/*
*   Async Task to perform forwarding
*   operation of mails using GMailSender.java class
*   TODO: comment code
*/

public class SendMailTask extends AsyncTask {

    private ProgressDialog statusDialog;
    private Activity sendMailActivity;
    boolean password_checked;


    public SendMailTask(Activity activity) {
        this.sendMailActivity = activity;
        password_checked = true;

    }

    protected void onPreExecute() {
        statusDialog = new ProgressDialog(sendMailActivity);
        statusDialog.setMessage("Getting ready...");
        statusDialog.setIndeterminate(false);
        statusDialog.setCancelable(false);
        statusDialog.show();
    }

    /*
    *   Execute task to send email reading from fields
    */
    @Override
    protected Object doInBackground(Object... args) {
        try {
            Log.i("SendMailTask", "About to instantiate GMailSender...");
            publishProgress("Processing input....");


            if (args.length==7){
                GMailSender androidEmail = new GMailSender(args[0].toString(),
                        args[1].toString(), (List) args[2], args[3].toString(),
                        args[4].toString(), (List) args[5], (List) args[6]);
                publishProgress("Preparing mail message....");
                androidEmail.createEmailMessage();
                publishProgress("Sending email....");
                androidEmail.sendEmail();
                password_checked = androidEmail.passwordChecked();
                publishProgress("Email Sent.");
                Log.i("SendMailTask", "Mail Sent.");
            }
            else{
                GMailSender androidEmail = new GMailSender(args[0].toString(),
                        args[1].toString(), (List) args[2], args[3].toString(),
                        args[4].toString(), (List) args[5], (List) args[6], (ArrayList) args[7]);

                publishProgress("Preparing mail message....");
                androidEmail.createEmailMessage();
                publishProgress("Sending email....");
                androidEmail.sendEmail();
                password_checked = androidEmail.passwordChecked();
                publishProgress("Email Sent.");
                Log.i("SendMailTask", "Mail Sent.");
            }

        } catch (Exception e) {
            publishProgress(e.getMessage());
            Log.e("SendMailTask", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void onProgressUpdate(Object... values) {
        statusDialog.setMessage(values[0].toString());

    }

    @Override
    public void onPostExecute(Object result) {
        statusDialog.dismiss();
        if(password_checked != true) {
            if (sendMailActivity instanceof SendMailActivity) ((SendMailActivity)sendMailActivity).passwordDialog();
            else if (sendMailActivity instanceof ReplyActivity) ((ReplyActivity)sendMailActivity).passwordDialog();
            Log.d("Check", "SendMailTask");
        } else sendMailActivity.finish();


    }

}