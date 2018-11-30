/**
* Notification receiver thread
*/

package com.may.ple.phone.sip; //you might change this after your package name

import webphone.webphone;


public class SIPNotifications extends Thread
{
   boolean terminated = false;
   webphone webphoneobj = null;

   /**
   * ctor
   */
   public SIPNotifications(webphone webphoneobj_in)
   {
       webphoneobj = webphoneobj_in;
   }

   /**
   *Start listening on UDP 19421
   */
   public boolean Start()
   {

       try{
           this.start();
           System.out.println("sip notifications started");
           return true;
       }catch(Exception e) {System.out.println("Exception at SIPNotifications Start: "+e.getMessage()+"\r\n"+e.getStackTrace()); }
       return false;
   }


   /**
   * signal terminate and close the socket
   */
   public void Stop()
   {
       try{
           terminated = true;
           /*
           if (socket != null)
               socket.close();
           */
           //a socket close exception might be raised here which is safe to catch and hide
       }catch(Exception e) { }
   }

   //

   /**
   * blocking read in this thread
   */
   public void run()
   {
        try{
           String sipnotifications = "";
           String[] notarray = null;

           while (!terminated)
           {
                  //get notifications from the SIP stack
                  sipnotifications = webphoneobj.API_GetNotificationsSync();

                  if (sipnotifications != null && sipnotifications.length() > 0)
                  {
                      //split by line
                      System.out.println("\tREC FROM JVOIP: " + sipnotifications);
                      notarray = sipnotifications.split("\r\n");

                      if (notarray == null || notarray.length < 1)
                      {
                         if(!terminated) Thread.sleep(1); //some error occured. sleep a bit just to be sure to avoid busy loop
                      }
                      else
                      {
                        for (int i = 0; i < notarray.length; i++)
                        {
                            if (notarray[i] != null && notarray[i].length() > 0)
                            {
                                ProcessNotifications(notarray[i]);
                            }
                        }
                      }
                  }
                  else
                  {
                      if(!terminated) Thread.sleep(1);  //some error occured. sleep a bit just to be sure to avoid busy loop
                  }
           }

        }catch(Exception e)
        {
           if(!terminated) System.out.println("Exception at SIPNotifications run: "+e.getMessage()+"\r\n"+e.getStackTrace());
        }

    }


    /**
    * all messages from JVoIP will be routed to this function.
    * parse and process them after your needs regarding to your application logic
    */

    public void ProcessNotifications(String msg)
    {
        try{
        	String[] msgArr = msg.split(",");
        	String status = msgArr[0].trim();
        	String line = msgArr[1].trim();
        	
        	if(!status.equals("STATUS") || !line.equals("-1")) return;
        	String statustext = msgArr[2].trim();
        		
        	switch(statustext) {
        	case "Incoming...": System.out.println("### Incoming..."); break;
        	case "Calling...": System.out.println("### Calling..."); break;
        	case "Ringing...": System.out.println("### Ringing..."); break;
        	case "Accept": System.out.println("### Accept"); break;
        	case "Starting Call": System.out.println("### Starting Call"); break;
        	case "Hangup": System.out.println("### Hangup"); break;
        	case "Call Finished": System.out.println("### Call Finished"); break;
        	}
        	
            //frame.jTextArea1.append(msg);

            //TODO: process notifications here (change your user interface or business logic depending on the sipstack state / call state by parsing the strings receiver here).
            //See the "Notifications" chapter in the documentation for the expecteddetails.
        }catch(Exception e) { 
        	System.out.println("Exception at SIPNotifications ProcessNotifications: "+e.getMessage()+"\r\n"+e.getStackTrace()); 
        }
    }
}

