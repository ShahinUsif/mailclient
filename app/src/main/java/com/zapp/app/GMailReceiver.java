package com.zapp.app;

import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

/*
*  GMailSender Java class to receive emails using imap
*  protocol over gmail servers
*/

public class GMailReceiver extends javax.mail.Authenticator {
    private static final String TAG = "GMailReceiver";

    private String mailhost = "imap.gmail.com";
    private Session session;
    private Store store;
    private boolean isPasswordCorrect = true;

    public GMailReceiver(String user, String password) {

        Properties props = System.getProperties();
        if (props == null){
        }else{
            props.setProperty("mail.store.protocol", "imaps");
//            Log.d(TAG, "Transport: "+props.getProperty("mail.transport.protocol"));
//            Log.d(TAG, "Store: "+props.getProperty("mail.store.protocol"));
//            Log.d(TAG, "Host: "+props.getProperty("mail.imap.host"));
//            Log.d(TAG, "Authentication: "+props.getProperty("mail.imap.auth"));
//            Log.d(TAG, "Port: "+props.getProperty("mail.imap.port"));
        }
        try {
            session = Session.getDefaultInstance(props, null);
            store = session.getStore("imaps");
            store.connect(mailhost, user, password);
            /*
             * Call this method to list all the available folders:
             *
             *   Folder[] folderList = store.getFolder("[Gmail]").list();
             *   for (int i = 0; i < folderList.length; i++) {
             *      Log.i("gmsend", folderList[i].getFullName());
             *   }
             */
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (AuthenticationFailedException e) {
            //Here it is the exception for password wrong
            isPasswordCorrect = false;
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public boolean passwordChecked() {
        return isPasswordCorrect;
    }


    /*
     *  Reads only unread mails
     *  and marks them as read
     *  NOT USED ANYMORE
     */
    public synchronized Message[] readNewMail() throws Exception {
        try {
            Folder folder = store.getFolder("Inbox");
            folder.open(Folder.READ_WRITE);
            Message[] new_msgs = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            folder.setFlags(new_msgs, new Flags(Flags.Flag.SEEN), true);
            return new_msgs;
        } catch (Exception e) {
            return null;
        }
    }

    /*
     *  Reads mails from the inbox that are not cached yet
     *  Marks every downloaded email as read in the IMAP folder
     *  It retrieves a maximum of 30 mails.
     */
    public synchronized Message[] readLastMails() throws Exception {
        try {
            Folder folder = store.getFolder("Inbox");
            folder.open(Folder.READ_WRITE);
            int already_count = 1;
            int mess_count = folder.getMessageCount();
            if ( Mailbox.emailList.size() > 0 ) {
                for (int i = 0; i < mess_count; i++) {
                    String cur_ID = folder.getMessage(mess_count - i).getHeader("Message-Id")[0];
                    if ( cur_ID.equals(Mailbox.emailList.get(0).ID) ) {
                        already_count = i;
                        i = mess_count + 1;
                    } else {
                        // set a number of max receivable mails
                        if (mess_count > 30) {
                            already_count = 30;
                        } else {
                            already_count = mess_count;
                        }
                    }
                }
            }
            else {
                if (mess_count > 30) {
                    already_count = 30;
                } else {
                    already_count = mess_count;
                }
            }
            if ( (mess_count - already_count) != mess_count ) {
                return folder.getMessages( (mess_count - already_count) + 1, mess_count);
            }
            else {
                return new Message[0];
            }
        } catch (Exception e) {
            return null;
        }
    }

    /*
     * Method that reads sent messages from [Gmail]/Posta Inviata
     * IMAP folder. Does not download already stored emails
     */
    public synchronized Message[] readSentMails() throws Exception {
        try {
            Folder folder = store.getFolder("[Gmail]/Posta inviata");
            folder.open(Folder.READ_WRITE);
            int already_count = 1;
            int mess_count = folder.getMessageCount();
            if ( Mailbox.sentList.size() > 0 ) {
                for (int i = 0; i < mess_count; i++) {
                    String cur_ID = folder.getMessage(mess_count - i).getHeader("Message-Id")[0];
                    if ( cur_ID.equals(Mailbox.sentList.get(0).ID) ) {
                        already_count = i;
                        i = mess_count + 1;
                    } else {
                        // set a number of max receivable mails
                        if (mess_count > 30) {
                            already_count = 30;
                        } else {
                            already_count = mess_count;
                        }
                    }
                }
            }
            else {
                if (mess_count > 30) {
                    already_count = 30;
                } else {
                    already_count = mess_count;
                }
            }
            if ( (mess_count - already_count) != mess_count ) {
                return folder.getMessages( (mess_count - already_count) + 1, mess_count);
            }
            else {
                return new Message[0];
            }

        } catch (Exception e) {
            return null;
        }
    }
}