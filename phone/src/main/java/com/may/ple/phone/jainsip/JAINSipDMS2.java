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
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;
import gov.nist.javax.sip.stack.SIPTransaction;

public class JAINSipDMS2 implements SipListener {
	private static final Logger LOGGER = Logger.getLogger(JAINSipDMS2.class.getName());
	private static final String transport = "ws";
	private static final int myPort = 9001;
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

			//-----
			String fromTag = sipRequest.getFromTag();
			String callId = sipRequest.getCallId().getCallId();
			String sdp = new String((byte[])request.getContent());
//			sdp = TestRTPEngineNG.manage("offer", sdp, callId, fromTag, "");

			//-----
			sipRequest.setContent(sdp.getBytes(), headerFactory.createContentTypeHeader("application", "sdp"));

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
			ViaHeader topmostViaHeader = sipRequest.getTopmostViaHeader();
			ServerTransaction serverTransactionId = reqEvent.getServerTransaction();

			SipStackImpl sipStackImpl = (SipStackImpl)sipStack;
			LOGGER.debug("ClientTransactionTableSize " + sipStackImpl.getClientTransactionTableSize());
			LOGGER.debug("ServerTransactionTableSize " + sipStackImpl.getServerTransactionTableSize());

			SIPTransaction trx = sipStackImpl.findTransaction(topmostViaHeader.getBranch().toLowerCase(), true);
			LOGGER.debug("---------: TRX = " + trx);

			if (request.getMethod().equals(Request.REGISTER)) {
				if(sipRequest.getAuthorization() == null) {
					processRegister(reqEvent, serverTransactionId);
				} else {
					System.out.println("Send Authorization");

					/*RouteList routeHeaders = sipRequest.getRouteHeaders();
		            Address address = routeHeaders.get(0).getAddress();
		            address.setURI(addressFactory.createURI("sip:192.168.43.238:5066"));*/


		            topmostViaHeader.setTransport(gatewayTransport);
		            topmostViaHeader.setHost(gatewayAddr);
		            topmostViaHeader.setPort(gatewayPort);


		            /*SipURI contactURI = addressFactory.createSipURI("104", "192.168.43.238");
					contactURI.setPort(8000);
					Address contactAddress = addressFactory.createAddress(contactURI);
					ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
					request.setHeader(contactHeader);*/


		            sipProvider.sendRequest(request);
				}
			} else if (request.getMethod().equals(Request.OPTIONS)) {
				//---: Request to Web-Client

				/*sipRequest.removeHeader("Via", true);*/

				/*Map<String, Object> clientData = registrar.get("104");

        		ViaHeader topmostViaHeader = sipRequest.getTopmostViaHeader();
				topmostViaHeader.setTransport("ws");
        		topmostViaHeader.setHost("127.0.0.1");
        		topmostViaHeader.setPort((int)clientData.get("port"));
        		topmostViaHeader.setRPort();
//        		topmostViaHeader.setReceived("127.0.0.1");

        		SipUri requestURI = (SipUri)request.getRequestURI();
        		requestURI.setHost(clientData.get("host").toString());
        		requestURI.setPort((int)clientData.get("port"));*/

				sipProvider.sendRequest(request);
			} else if (request.getMethod().equals(Request.INVITE)) {
				processInviteCall(reqEvent);
			} else if (request.getMethod().equals(Request.ACK)) {
				System.out.println("ACK");

	            topmostViaHeader.setTransport(gatewayTransport);
	            topmostViaHeader.setHost(gatewayAddr);
	            topmostViaHeader.setPort(gatewayPort);

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

		SipStackImpl sipStackImpl = (SipStackImpl)sipStack;
		LOGGER.debug("ClientTransactionTableSize " + sipStackImpl.getClientTransactionTableSize());
		LOGGER.debug("ServerTransactionTableSize " + sipStackImpl.getServerTransactionTableSize());

		SIPResponse sipResponse = (SIPResponse)response;
		ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();

		SIPTransaction trx = sipStackImpl.findTransaction(topmostViaHeader.getBranch().toLowerCase(), true);
		LOGGER.debug("---------: TRX = " + trx);

        try {
        	if (response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED || response.getStatusCode() == Response.UNAUTHORIZED) {
            	System.out.println("1. ############# UNAUTHORIZED and resend to WEB.");
            	CSeqHeader cseqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

            	if (cseqHeader.getMethod().equals(Request.INVITE)) {
            		System.out.println("INVITE UNAUTHORIZED");

            		FromHeader from = (FromHeader) response.getHeader(FromHeader.NAME);
            		SipURI fromUri = (SipURI) from.getAddress().getURI();
            		Map<String, Object> clientData = registrar.get(fromUri.getUser());

            		topmostViaHeader.setTransport("ws");
            		topmostViaHeader.setHost(clientData.get("host").toString());
            		topmostViaHeader.setPort((int)clientData.get("port"));
            		topmostViaHeader.setReceived(clientData.get("host").toString());
//            		topmostViaHeader.setRPort();

            		sipProvider.sendResponse(response);
            	} else {
            		ToHeader to = (ToHeader) response.getHeader(ToHeader.NAME);
            		SipURI toUri = (SipURI) to.getAddress().getURI();
            		Map<String, Object> clientData = registrar.get(toUri.getUser());

            		topmostViaHeader.setTransport("ws");
            		topmostViaHeader.setHost(clientData.get("host").toString());
            		topmostViaHeader.setPort((int)clientData.get("port"));
//            		topmostViaHeader.setRPort();
            		topmostViaHeader.removeParameter("received");

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

	    			/*topmostViaHeader.setTransport("ws");
	    			topmostViaHeader.setHost(clientData.get("host").toString());
	    			topmostViaHeader.setPort((int)clientData.get("port"));
	    			topmostViaHeader.setReceived(clientData.get("host").toString());*/

					topmostViaHeader.setTransport("ws");
	        		topmostViaHeader.setHost("127.0.0.1");
	        		topmostViaHeader.setPort((int)clientData.get("port"));
//	        		topmostViaHeader.setRPort();

	            	sipProvider.sendResponse(response);
		        } else if (cseqHeader.getMethod().equals(Request.OPTIONS)) {

		        	//---: Response to SIP Server
		            topmostViaHeader.setTransport(gatewayTransport);
//		            topmostViaHeader.setHost(gatewayAddr);
//		            topmostViaHeader.setPort(gatewayPort);

//		            topmostViaHeader.setRPort();
//		            topmostViaHeader.removeParameter("received");

					sipProvider.sendResponse(response);
		        } else if (cseqHeader.getMethod().equals(Request.INVITE)) {
		        	System.out.println("Callee answered call");

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
//		    			topmostViaHeader.setRPort();


		    			//-----
		    			String fromTag = sipResponse.getFromTag();
		    			String toTag = sipResponse.getToTag();
		    			String callId = sipResponse.getCallId().getCallId();
		    			String sdp = new String((byte[])sipResponse.getContent());
//		    			sdp = TestRTPEngineNG.manage("answer", sdp, callId, fromTag, toTag);

		    			//-----
		    			sipResponse.setContent(sdp.getBytes(), headerFactory.createContentTypeHeader("application", "sdp"));
	            	}

	            	sipProvider.sendResponse(response);
		        }
            } else if (response.getStatusCode() == Response.TRYING) {
            	System.out.println("TRYING");

            	FromHeader from = (FromHeader) response.getHeader(FromHeader.NAME);
    			SipURI toUri = (SipURI) from.getAddress().getURI();
    			Map<String, Object> clientData = registrar.get(toUri.getUser());

            	if(topmostViaHeader.getTransport().equals("WS") || topmostViaHeader.getTransport().equals("WSS")) {
            		System.out.println("From Browser.");
            	} else {
            		System.out.println("From SIP Server.");
            		topmostViaHeader.setTransport("ws");
            		topmostViaHeader.setHost(clientData.get("host").toString());
            		topmostViaHeader.setPort((int)clientData.get("port"));
            		topmostViaHeader.setReceived(clientData.get("host").toString());
//            		topmostViaHeader.setRPort();
            	}

            	sipProvider.sendResponse(response);
            } else if (response.getStatusCode() == Response.RINGING) {
            	System.out.println("RINGING");

            	FromHeader from = (FromHeader) response.getHeader(FromHeader.NAME);
    			SipURI toUri = (SipURI) from.getAddress().getURI();
    			Map<String, Object> clientData = registrar.get(toUri.getUser());

    			topmostViaHeader.setTransport("ws");
    			topmostViaHeader.setHost(clientData.get("host").toString());
    			topmostViaHeader.setPort((int)clientData.get("port"));
    			topmostViaHeader.setReceived(clientData.get("host").toString());
//    			topmostViaHeader.setRPort();

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
//		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");
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
		new JAINSipDMS2().init();
	}

}
