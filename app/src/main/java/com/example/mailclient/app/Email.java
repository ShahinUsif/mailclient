package com.example.mailclient.app;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;

/**
 * Created by Leo on 30/03/14.
 */
/*
 *  Java Class that implements an email object to store email contents
 */
public class Email implements Serializable {

    boolean seen;
    String subject;
    Date date;
    ArrayList<String> body;
    Address[] from,to;
    String excerpt;
    String ID;
    String attachmentPath;

    public Email() {
        subject = new String();
        date = new Date();
        body = new ArrayList<String> ();
        excerpt = "";
        seen = false;
        attachmentPath= "";
    }

    /*
     *  Fill up Email object
     */

    public void setSeen() {
        seen = true;
    }

    public void setUnSeen() {
        seen = false;
    }

    public void setSubject(String sub) {
        subject = sub;
    }

    public void setDate(Date dat) {
        date = dat;
    }

    public void setFrom(Address[] addr) {
        from = new Address[addr.length];
        for (int i = 0; i < addr.length; i++) {
            from[i] = addr[i];
        }
    }

    public void setTo(Address[] addr) {
        to = new Address[addr.length];
        for (int i = 0; i < addr.length; i++) {
            to[i] = addr[i];
        }
    }

    /*
     *  Store body content if it is a Multipart Message
     */
    public void setContent(Multipart multipart) throws MessagingException, IOException {
        try {
            for (int x = 0; x < multipart.getCount(); x++) {
                MimeBodyPart bodyPart = (MimeBodyPart) multipart.getBodyPart(x);

                String disposition = bodyPart.getDisposition();


                // TODO: se la parte di Multipart è attachment fare qualcosa
                if (disposition != null && (disposition.equalsIgnoreCase(BodyPart.ATTACHMENT))) {
                    Log.i("multipart" + x, disposition);
                    Log.i("multipart" + x,"Mail have some attachment : ");

                    String fileName=bodyPart.getFileName(); //Now it takes the attachment's name

                    Log.i("Attach name", fileName);
                    attachmentPath = "media/" + fileName;

                    bodyPart.saveFile(attachmentPath);


                } else {
                    Log.i("multipart" + x, "mail is a string");
                    Log.i("multipart" + x, "string is:" + bodyPart.getContent().toString());
                    body.add(bodyPart.getContent().toString());
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setExcerpt();
//      Log.i("Check","tipo= "+multipart.getContentType());
//
//        try {
//            for (int x = 0; x < multipart.getCount(); x++) {
//                BodyPart bodyPart = multipart.getBodyPart(x);
//                    body.addBodyPart(bodyPart);
//            }
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
    }

    /*
     *  Store body content if it is a String
     */
    public void setContentToString(String string) throws MessagingException, IOException {
        body.add(string);
        setExcerpt();
    }

    /*
     *  Parse body content and extract the excerpt of body.
     *  - saves only first line without multiple spaces
     */
    public void setExcerpt() throws MessagingException, IOException {

        //BISOGNA CONTROLLARE SE IL BODY È DI TIPO MIME O SOLO TESTO

        MimeBodyPart messageBodyPart;
        String body_content = "";

        for (int i=0; i<body.size(); i++) {

            String disposition = body.get(i);

            //attachment here!
            // disposition == null
            if (disposition != null && (disposition.equalsIgnoreCase("ATTACHMENT"))) {
                Log.i("excerpt" + i,"Mail have some attachment");
                //buttiamo fuori il multipart!
            }
            else {
                //è testo, quindi lo concateno!
                body_content = body_content.concat(disposition);  // the changed code
                Log.i("excerpt" + i, "body content: " + body_content);

            }
        }

        //butto il body_content in html
        Spanned spanned = Html.fromHtml(body_content);
        if (spanned.toString().replaceAll("  ","").split("\\r?\\n")[0].length() > 39) {
            excerpt += spanned.toString().replaceAll("  ", "").split("\\r?\\n")[0].substring(0, 39);
        }
        else {
            excerpt += spanned.toString().replaceAll("  ", "").split("\\r?\\n")[0];
        }
        excerpt += "...";
    }

    public void setID(String s) {
        ID = s;
    }

}
