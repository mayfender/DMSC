package com.may.ple.phone.sip;

import webphone.webphone;

public class Sip1 {
	private SIPNotifications notify;
	private webphone wobj;
	
	public void init() {
		System.out.println("Init");
		
		try {
			wobj = new webphone();
			
			// Set to false if you don’t need the popup for the incoming calls.
			wobj.API_SetParameter("hasincomingcallpopup", "false");
			wobj.API_SetParameter("loglevel", "1");
			wobj.API_SetParameter("logtoconsole", "true");
			wobj.API_SetParameter("polling", "3");
			wobj.API_SetParameter("startsipstack", "1");
			wobj.API_SetParameter("serveraddress", "192.168.2.253");
			wobj.API_SetParameter("username", "100");
			wobj.API_SetParameter("password", "abc123");
			
            
			notify = new SIPNotifications(wobj);
			notify.start();
			Thread.sleep(100);
			
			wobj.API_Start();
			System.out.println("Start sip finished.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		try {
			wobj.API_Stop();
			notify.Stop();
			System.exit(0);
			System.out.println("stop");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
