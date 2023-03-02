package com.may.ple.phone.jainsip;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.RouteList;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;

public class JAINSipDMS implements SipListener {
	private static final String transport = "ws";
	private static final int myPort = 9000;
	private static final String myAddress = "127.0.0.1";
	private static final String gatewayAddr = "192.168.43.238";
	private static final int gatewayPort = 8000;
	private static final String gatewayTransport = "udp";

	private static AddressFactory addressFactory;
	private static MessageFactory messageFactory;
	private static HeaderFactory headerFactory;
	private static SipStack sipStack;

	private ListeningPoint listeningPoint;
	private ListeningPoint listeningPointProxy;
	private SipProvider sipProvider;

	private Map<String, Map<String, Object>> registrar = new HashMap<>();
	private AtomicLong counter = new AtomicLong();

	//
	private void processRegister(RequestEvent requestEvent, ServerTransaction serverTrxId) {
		Request request = requestEvent.getRequest();
		try {
            SIPRequest sipRequest = (SIPRequest)request;
            ViaHeader topmostViaHeader = sipRequest.getTopmostViaHeader();
            ToHeader to = (ToHeader) request.getHeader(ToHeader.NAME);
            SipURI toUri = (SipURI) to.getAddress().getURI();
            Map<String, Object> clientData = new HashMap<>();
            clientData.put("host", topmostViaHeader.getHost());
            clientData.put("port", topmostViaHeader.getPort());
            clientData.put("Contact", request.getHeader("Contact"));
            registrar.put(toUri.getUser(), clientData);

            RouteList routeHeaders = sipRequest.getRouteHeaders();
            Address address = routeHeaders.get(0).getAddress();
            address.setURI(addressFactory.createURI("sip:192.168.43.238:5066"));

            topmostViaHeader.setTransport(gatewayTransport);
            topmostViaHeader.setHost(gatewayAddr);
            topmostViaHeader.setPort(gatewayPort);

            sipProvider.sendRequest(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processInviteCall(RequestEvent requestEvent) {
		Request request = requestEvent.getRequest();

		try {
			System.out.println("b2bua: got an Invite sending Trying");

			SIPRequest sipRequest = (SIPRequest)request;
			ViaHeader topmostViaHeader = sipRequest.getTopmostViaHeader();
			topmostViaHeader.setTransport(gatewayTransport);
			topmostViaHeader.setHost(gatewayAddr);
			topmostViaHeader.setPort(gatewayPort);

			RouteList routeHeaders = sipRequest.getRouteHeaders();
            Address address = routeHeaders.get(0).getAddress();
            address.setURI(addressFactory.createURI("sip:192.168.43.238:5066"));


            String sdp =  new String((byte[])sipRequest.getContent());
            int oIndex = sdp.indexOf("o=");
    		int loIndex = sdp.indexOf("\n", oIndex);
    		int cIndex = sdp.indexOf("c=");
    		int lcIndex = sdp.indexOf("\n", cIndex);
    		int mIndex = sdp.indexOf("m=");
    		int lmIndex = sdp.indexOf("\n", mIndex);
    		String oParam = sdp.substring(oIndex, loIndex);
    		String cParam = sdp.substring(cIndex, lcIndex);
    		String mParam = sdp.substring(mIndex, lmIndex);

    		int faudioIndex = mParam.indexOf("audio")+"audio".length();
    		int laudioIndex = mParam.indexOf(" ", faudioIndex + 1);
    		String mAudioPort = mParam.substring(faudioIndex, laudioIndex).trim();

    		int candidateIndex = sdp.indexOf("a=candidate");
    		int lcandidateIndex = sdp.indexOf("\n", candidateIndex);
    		String candidateParam = sdp.substring(candidateIndex, lcandidateIndex);
    		int local = candidateParam.indexOf(".local") + 6;
    		int local2 = candidateParam.indexOf(" ", local + 1);
    		int port = Integer.valueOf(candidateParam.substring(local, local2).trim());

            String sdpData =  "v=0 \r\n"
//							+ "o=100 0 0 IN IP4 192.168.43.238"
            				+ oParam
							+ "s=- \r\n"
							+ "c=IN IP4 " + gatewayAddr + " \r\n"
//							+ "c=IN IP4 127.0.0.1 \r\n"
//							+ cParam
							+ "t=0 0 \r\n"
//							+ "m=audio " + port + " RTP/AVPF 0 8 \r\n"
							+ "m=audio " + port + " RTP/AVP 0 8 \r\n"
//							+ "m=audio " + mAudioPort + " RTP/AVP 0 8 \r\n"
							+ "a=rtpmap:0 PCMU/8000 \r\n"
							+ "a=rtpmap:8 PCMA/8000 \r\n";
			byte[] contents = sdpData.getBytes();
            sipRequest.setContent(contents, headerFactory.createContentTypeHeader("application", "sdp"));


			sipProvider.sendRequest(request);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	//----

	@Override
	public void processRequest(RequestEvent reqEvent) {
		try {
			System.out.println("# Start processRequest");
			Request request = reqEvent.getRequest();
			SIPRequest sipRequest = (SIPRequest)request;
			ServerTransaction serverTransactionId = reqEvent.getServerTransaction();

			if (request.getMethod().equals(Request.REGISTER)) {
				if(sipRequest.getAuthorization() == null) {
					processRegister(reqEvent, serverTransactionId);
				} else {
					System.out.println("Send Authorization");

					RouteList routeHeaders = sipRequest.getRouteHeaders();
		            Address address = routeHeaders.get(0).getAddress();
		            address.setURI(addressFactory.createURI("sip:192.168.43.238:5066"));

		            ViaHeader topmostViaHeader = sipRequest.getTopmostViaHeader();
		            topmostViaHeader.setTransport(gatewayTransport);
		            topmostViaHeader.setHost(gatewayAddr);
		            topmostViaHeader.setPort(gatewayPort);


		            SipURI contactURI = addressFactory.createSipURI("100", "192.168.43.238");
					contactURI.setPort(8000);
					Address contactAddress = addressFactory.createAddress(contactURI);
					ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
					request.setHeader(contactHeader);


		            sipProvider.sendRequest(request);
				}
			} else if (request.getMethod().equals(Request.OPTIONS)) {
				//---: Request to Web-Client

				/*sipRequest.removeHeader("Via", true);*/

				Map<String, Object> clientData = registrar.get("100");

        		ViaHeader topmostViaHeader = sipRequest.getTopmostViaHeader();
				topmostViaHeader.setTransport("ws");
        		topmostViaHeader.setHost("127.0.0.1");
        		topmostViaHeader.setPort((int)clientData.get("port"));
        		topmostViaHeader.setRPort();
//        		topmostViaHeader.setReceived("127.0.0.1");

        		SipUri requestURI = (SipUri)request.getRequestURI();
        		requestURI.setHost(clientData.get("host").toString());
        		requestURI.setPort((int)clientData.get("port"));

				sipProvider.sendRequest(request);
			} else if (request.getMethod().equals(Request.INVITE)) {
				SipURI sipURI = (SipURI)sipRequest.getRequestURI();
				if(sipURI.getPort() == gatewayPort) {
					//---: Invite to WebRTC
					Map<String, Object> clientData = registrar.get("100");

					sipURI.setHost("127.0.0.1");
					sipURI.setPort((int)clientData.get("port"));

					ViaHeader topmostViaHeader = sipRequest.getTopmostViaHeader();
					topmostViaHeader.setTransport("ws");
	        		topmostViaHeader.setHost("127.0.0.1");
	        		topmostViaHeader.setPort((int)clientData.get("port"));
	        		topmostViaHeader.setRPort();

	        		sipProvider.sendRequest(request);
				} else {
					processInviteCall(reqEvent);
				}
			} else if (request.getMethod().equals(Request.ACK)) {
				System.out.println("ACK");

				ViaHeader topmostViaHeader = sipRequest.getTopmostViaHeader();
	            topmostViaHeader.setTransport(gatewayTransport);
	            topmostViaHeader.setHost(gatewayAddr);
	            topmostViaHeader.setPort(gatewayPort);

	            RouteList routeHeaders = sipRequest.getRouteHeaders();
	            Address address = routeHeaders.get(0).getAddress();
	            address.setURI(addressFactory.createURI("sip:192.168.43.238:5066"));

	            sipProvider.sendRequest(request);
			} else {
				System.out.println("Unknow processRequest ************ " + request.getMethod());
			}
			System.out.println("# End processRequest");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processResponse(ResponseEvent respEvent) {
		Response response = (Response) respEvent.getResponse();
		System.out.println("# Start processResponse");

        try {
        	if (response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED || response.getStatusCode() == Response.UNAUTHORIZED) {
            	System.out.println("1. ############# UNAUTHORIZED and resend to WEB.");
            	CSeqHeader cseqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

            	if (cseqHeader.getMethod().equals(Request.INVITE)) {
            		System.out.println("INVITE UNAUTHORIZED");

            		FromHeader from = (FromHeader) response.getHeader(FromHeader.NAME);
            		SipURI fromUri = (SipURI) from.getAddress().getURI();
            		Map<String, Object> clientData = registrar.get(fromUri.getUser());

            		SIPResponse sipResponse = (SIPResponse)response;
            		ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();
            		topmostViaHeader.setTransport("ws");
            		topmostViaHeader.setHost(clientData.get("host").toString());
            		topmostViaHeader.setPort((int)clientData.get("port"));
//            		topmostViaHeader.setReceived(clientData.get("host").toString());
            		topmostViaHeader.setRPort();

            		sipProvider.sendResponse(response);
            	} else {
            		ToHeader to = (ToHeader) response.getHeader(ToHeader.NAME);
            		SipURI toUri = (SipURI) to.getAddress().getURI();
            		Map<String, Object> clientData = registrar.get(toUri.getUser());

            		SIPResponse sipResponse = (SIPResponse)response;
            		ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();

            		topmostViaHeader.setTransport("ws");
            		topmostViaHeader.setHost(clientData.get("host").toString());
            		topmostViaHeader.setPort((int)clientData.get("port"));
            		topmostViaHeader.setRPort();
//            		topmostViaHeader.setReceived(clientData.get("host").toString());

            		sipProvider.sendResponse(response);
            	}
            } else if (response.getStatusCode() == Response.OK) {
            	CSeqHeader cseqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

            	System.out.println("Method " + cseqHeader.getMethod());

		        if (cseqHeader.getMethod().equals(Request.REGISTER)) {
		        	ToHeader to = (ToHeader) response.getHeader(ToHeader.NAME);
	    			SipURI toUri = (SipURI) to.getAddress().getURI();
	    			Map<String, Object> clientData = registrar.get(toUri.getUser());

	    			response.setHeader((Header)clientData.get("Contact"));

	            	SIPResponse sipResponse = (SIPResponse)response;
	            	ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();
	    			/*topmostViaHeader.setTransport("ws");
	    			topmostViaHeader.setHost(clientData.get("host").toString());
	    			topmostViaHeader.setPort((int)clientData.get("port"));
	    			topmostViaHeader.setReceived(clientData.get("host").toString());*/

					topmostViaHeader.setTransport("ws");
	        		topmostViaHeader.setHost("127.0.0.1");
	        		topmostViaHeader.setPort((int)clientData.get("port"));
	        		topmostViaHeader.setRPort();

	            	sipProvider.sendResponse(response);
		        } else if (cseqHeader.getMethod().equals(Request.OPTIONS)) {

		        	//---: Response to SIP Server
		        	SIPResponse sipResponse = (SIPResponse)response;
		        	ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();
		            topmostViaHeader.setTransport("udp");
		            topmostViaHeader.setHost("192.168.43.238");
		            topmostViaHeader.setPort(5066);
		            topmostViaHeader.setRPort();
//		            topmostViaHeader.removeParameter("received");

		           /* RouteList routeHeaders = sipResponse.getRouteHeaders();
		            if(routeHeaders == null) {
		            	sipResponse.addHeader(headerFactory.createRouteHeader(addressFactory.createAddress("sip:192.168.43.238:5066")));
		            } else {
		            	Address address = routeHeaders.get(0).getAddress();
		            	address.setURI(addressFactory.createURI("sip:192.168.43.238:5066"));
		            }*/

					sipProvider.sendResponse(response);
		        } else if (cseqHeader.getMethod().equals(Request.INVITE)) {
		        	System.out.println("Callee answered call");

	            	SIPResponse sipResponse = (SIPResponse)response;
	            	ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();

	            	if(topmostViaHeader.getTransport().equals("WS") || topmostViaHeader.getTransport().equals("WSS")) {
	            		System.out.println("From Browser.");

	    	            topmostViaHeader.setTransport(gatewayTransport);
	    	            topmostViaHeader.setHost(gatewayAddr);
	    	            topmostViaHeader.setPort(gatewayPort);
	            	} else {
	            		System.out.println("From SIP Server.");

	            		FromHeader from = (FromHeader) response.getHeader(FromHeader.NAME);
		    			SipURI toUri = (SipURI) from.getAddress().getURI();
		    			Map<String, Object> clientData = registrar.get(toUri.getUser());

		    			topmostViaHeader.setTransport("ws");
		    			topmostViaHeader.setHost(clientData.get("host").toString());
		    			topmostViaHeader.setPort((int)clientData.get("port"));
		    			topmostViaHeader.setReceived(clientData.get("host").toString());
		    			topmostViaHeader.setRPort();

		    			//---Test
		    			String sdp =  new String((byte[])sipResponse.getContent());

		    			int mIndex = sdp.indexOf("m=");
		        		int lmIndex = sdp.indexOf("\n", mIndex);
		        		String mParam = sdp.substring(mIndex, lmIndex);
		        		int faudioIndex = mParam.indexOf("audio")+"audio".length();
		        		int laudioIndex = mParam.indexOf(" ", faudioIndex + 1);
		        		String mAudioPort = mParam.substring(faudioIndex, laudioIndex).trim();

		    			sdp += "a=ice-ufrag:iyTe\r\n";
		    			sdp += "a=ice-pwd:01JGYTQ0FjvOKpwSPzKFKb5+\r\n";
//		    			sdp += "a=candidate:Hc0a82b64 1 UDP 2130706431 127.0.0.1 " + mAudioPort + " typ host\r\n";
		    			sdp += "a=connection:new\r\n";
		    			sdp += "a=setup:active\r\n";
		    			sdp += "a=fingerprint:SHA-256 06:5E:84:54:C7:E1:DB:51:4F:92:89:F4:B6:50:37:F3:68:C4:03:ED:0A:D7:F4:36:81:A5:9E:D5:B3:9C:3E:6A\r\n";
		    			sdp += "a=sendrecv\r\n";

		    			byte[] contents = sdp.getBytes();
		    			sipResponse.setContent(contents, headerFactory.createContentTypeHeader("application", "sdp"));
		    			//---Test
	            	}

	            	sipProvider.sendResponse(response);
		        }
            } else if (response.getStatusCode() == Response.TRYING) {
            	System.out.println("TRYING");

            	SIPResponse sipResponse = (SIPResponse)response;
            	ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();
            	if(topmostViaHeader.getTransport().equals("WS") || topmostViaHeader.getTransport().equals("WSS")) {
            		System.out.println("From Browser.");

//            		topmostViaHeader.removeParameter("received");
//		            topmostViaHeader.setTransport("udp");
//		            topmostViaHeader.setHost("192.168.43.238");
//		            topmostViaHeader.setPort(5066);
//		            topmostViaHeader.setRPort();
            	} else {
            		System.out.println("From SIP Server.");

            		FromHeader from = (FromHeader) response.getHeader(FromHeader.NAME);
        			SipURI toUri = (SipURI) from.getAddress().getURI();
        			Map<String, Object> clientData = registrar.get(toUri.getUser());

            		topmostViaHeader.setTransport("ws");
            		topmostViaHeader.setHost(clientData.get("host").toString());
            		topmostViaHeader.setPort((int)clientData.get("port"));
            		topmostViaHeader.setReceived(clientData.get("host").toString());
            		topmostViaHeader.setRPort();
            	}

            	sipProvider.sendResponse(response);
            } else if (response.getStatusCode() == Response.RINGING) {
            	System.out.println("RINGING");

            	FromHeader from = (FromHeader) response.getHeader(FromHeader.NAME);
    			SipURI toUri = (SipURI) from.getAddress().getURI();
    			Map<String, Object> clientData = registrar.get(toUri.getUser());

            	SIPResponse sipResponse = (SIPResponse)response;
            	ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();

            	if(topmostViaHeader.getTransport().equals("WS") || topmostViaHeader.getTransport().equals("WSS")) {
            		System.out.println("From Browser.");

            		topmostViaHeader.setTransport("udp");
		            topmostViaHeader.setHost("192.168.43.238");
		            topmostViaHeader.setPort(5066);
		            topmostViaHeader.setRPort();
            	} else {
            		topmostViaHeader.setTransport("ws");
            		topmostViaHeader.setHost(clientData.get("host").toString());
            		topmostViaHeader.setPort((int)clientData.get("port"));
            		topmostViaHeader.setReceived(clientData.get("host").toString());
            		topmostViaHeader.setRPort();
            	}

            	sipProvider.sendResponse(response);
            } else if (response.getStatusCode() == Response.REQUEST_TIMEOUT) {
            	SIPResponse sipResponse = (SIPResponse)response;
            	ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();

            	if(topmostViaHeader.getTransport().equals("WS") || topmostViaHeader.getTransport().equals("WSS")) {
            		System.out.println("From Browser.");

            		topmostViaHeader.setTransport("udp");
		            topmostViaHeader.setHost("192.168.43.238");
		            topmostViaHeader.setPort(5066);
		            topmostViaHeader.setRPort();
            	} else {
            		System.out.println("From SIP Server.");
            	}
            	sipProvider.sendResponse(response);
            } else {
            	System.out.println("Unknow processResponse ************ " + response.getStatusCode());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
		System.out.println("# End processResponse");
	}

	@Override
	public void processDialogTerminated(DialogTerminatedEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processIOException(IOExceptionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processTimeout(TimeoutEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void init() {
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "DMS_WEBRTC");
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");
//		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "shootmedebug.txt");
//		properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "shootmelog.txt");
		properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());

		try {
			// Create SipStack object
			sipStack = sipFactory.createSipStack(properties);
			System.out.println("sipStack = " + sipStack);
		} catch (PeerUnavailableException e) {
			// could not find
			// gov.nist.jain.protocol.ip.sip.SipStackImpl
			// in the classpath
			e.printStackTrace();
			System.err.println(e.getMessage());
			if (e.getCause() != null)
				e.getCause().printStackTrace();
		}

		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			this.listeningPoint = sipStack.createListeningPoint(myAddress, myPort, transport);
			this.listeningPointProxy = sipStack.createListeningPoint(gatewayAddr, gatewayPort, gatewayTransport);

			sipProvider = sipStack.createSipProvider(listeningPoint);
			sipProvider.addListeningPoint(listeningPointProxy);

			System.out.println("ws provider " + sipProvider);
			sipProvider.addSipListener(this);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}

	public static void main(String args[]) {
		new JAINSipDMS().init();
	}

}
