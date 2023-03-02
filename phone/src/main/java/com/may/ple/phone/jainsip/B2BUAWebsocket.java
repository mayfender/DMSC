package com.may.ple.phone.jainsip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
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
import javax.sip.Transaction;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;

/**
 * This class is a B2BUA using Websocket transport. You can use any two Websocket SIP phones
 * to register into the server and call each other by the username the advertised in the REGISTER
 * request. The registrar is just storing the contacts of the users in a HashMap locally.
 *
 * Requiring registration is the only significant difference from the usual JAIN-SIP call flow. The
 * registrations are required because a phone must be able to receive calls on the websocket while
 * being idle otherwise.
 *
 * @author Vladimir Ralev
 */
public class B2BUAWebsocket implements SipListener {

	private static AddressFactory addressFactory;

	private static MessageFactory messageFactory;

	private static HeaderFactory headerFactory;

	private static SipStack sipStack;

	private static final String myAddress = "127.0.0.1";

	private static final int myPort = 9000;

	private AtomicLong counter = new AtomicLong();

	private ListeningPoint listeningPoint;
	private ListeningPoint listeningPointProxy;

	private SipProvider sipProvider;

	private String transport = "ws";

	private HashMap<String, SipURI> registrar = new HashMap<>();
	private HashMap<String, Request> websocketRequest = new HashMap<>();
//	private HashMap<String, ServerTransaction> websocketTxn = new HashMap<>();
	private HashMap<String, ViaHeader> websocketViaHeader = new HashMap<>();

	private long cSeqNo = 1;

	private Dialog dialog;

	private ClientTransaction inviteTid;

	@Override
	public void processRequest(RequestEvent requestEvent) {
		Request request = requestEvent.getRequest();
		ServerTransaction serverTransactionId = requestEvent.getServerTransaction();

		try {
            SIPRequest sipRequest = (SIPRequest)request;
            ViaHeader topmostViaHeader = sipRequest.getTopmostViaHeader();
            System.out.println("### FROM : " + topmostViaHeader.getTransport() + " " + topmostViaHeader.getHost());

			System.out.println("\n\nRequest " + request.getMethod()
					+ " received at " + sipStack.getStackName()
					+ " with server transaction id " + serverTransactionId);

			if (request.getMethod().equals(Request.INVITE)) {
				processInvite(requestEvent, serverTransactionId);
			} else if (request.getMethod().equals(Request.ACK)) {
				processAck(requestEvent, serverTransactionId);
			} else if (request.getMethod().equals(Request.CANCEL)) {
				processCancel(requestEvent, serverTransactionId);
			} else if (request.getMethod().equals(Request.REGISTER)) {
				if(sipRequest.getAuthorization() == null) {
					processRegister(requestEvent, serverTransactionId);
				} else {
					System.out.println("Send Authorization");

		            topmostViaHeader.setTransport("UDP");
		            topmostViaHeader.setHost("192.168.43.58");
		            topmostViaHeader.setPort(8888);

					sipProvider.sendRequest(request);
				}
			} else if (request.getMethod().equals(Request.OPTIONS)) {
				System.out.println("############# OPTIONS Query information.");
				if(serverTransactionId == null) {
					/*SipProvider sipProvider = (SipProvider) requestEvent.getSource();
					serverTransactionId = sipProvider.getNewServerTransaction(request);
					Response response = messageFactory.createResponse(Response.OK, request);
					serverTransactionId.sendResponse(response);*/


//					Request requestSipServ = (Request)request.clone();
//					SIPRequest sipRequest = (SIPRequest)requestSipServ;

					/*Response responesSipClient = (Response)response.clone();
					SIPResponse sipResponse = (SIPResponse)responesSipClient;
					ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();

					SipURI toUri = (SipURI) sipResponse.getTo().getAddress().getURI();

		            topmostViaHeader.setTransport(toUri.getTransportParam());
		            topmostViaHeader.setHost(toUri.getHost());
		            topmostViaHeader.setPort(toUri.getPort());

					serverTransactionId.sendResponse(responesSipClient);*/



					//---------------------------------------------------------------
//					Request requestSipServ = (Request)request.clone();
//			        SIPRequest sipRequest = (SIPRequest)requestSipServ;

//			        ViaHeader topmostViaHeader = sipRequest.getTopmostViaHeader();
//			        topmostViaHeader.setTransport("UDP");
//			        topmostViaHeader.setHost("192.168.43.58");
//			        topmostViaHeader.setPort(8888);

//					ClientTransaction newClientTransaction = sipProvider.getNewClientTransaction(request);
//					newClientTransaction.sendRequest();
					sipProvider.sendRequest(request);
					//---------------------------------------------------------------

					System.out.println("############# Pass OPTIONS to WEB.");
				}
			} else {
				processInDialogRequest(requestEvent, serverTransactionId);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
            junit.framework.TestCase.fail("Exit JVM");
        }
	}

	@Override
	public void processResponse(ResponseEvent responseEvent) {
		System.out.println("Got a response");
        Response response = (Response) responseEvent.getResponse();
        ClientTransaction tid = responseEvent.getClientTransaction();
        CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

        SIPResponse sipResponse = (SIPResponse)response;
        ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();
        System.out.println("### FROM : " + topmostViaHeader.getTransport() + " " + topmostViaHeader.getHost());

        System.out.println("Response received : Status Code = "
                + response.getStatusCode() + " " + cseq);
        if (tid == null) {
            System.out.println("Stray response -- dropping ");
//            return;
        }
//        System.out.println("transaction state is " + tid.getState());
//        System.out.println("Dialog = " + tid.getDialog());
//        System.out.println("Dialog State is " + tid.getDialog().getState());

        try {
            if (response.getStatusCode() == Response.OK) {
                if (cseq.getMethod().equals(Request.INVITE)) {
                    Dialog dialog = inviteTid.getDialog();
                    Request ackRequest = dialog.createAck( cseq.getSeqNumber() );
                    System.out.println("Sending ACK");
                    dialog.sendAck(ackRequest);
                } else if (cseq.getMethod().equals(Request.CANCEL)) {
                    if (dialog.getState() == DialogState.CONFIRMED) {
                        // oops cancel went in too late. Need to hang up the
                        // dialog.
                        System.out
                                .println("Sending BYE -- cancel went in too late !!");
                        Request byeRequest = dialog.createRequest(Request.BYE);
                        ClientTransaction ct = sipProvider
                                .getNewClientTransaction(byeRequest);
                        dialog.sendRequest(ct);
                    }
                } else if (cseq.getMethod().equals(Request.OPTIONS)) {

                	topmostViaHeader.setTransport("UDP");
		            topmostViaHeader.setHost("192.168.43.58");
		            topmostViaHeader.setPort(8888);

					sipProvider.sendResponse(response);
					System.out.println("Passed OPTIONS to SIP SERVER.");
                } else if (cseq.getMethod().equals(Request.REGISTER)) {
                	System.out.println("2. ############# Register Success");
                	/*FromHeader from = (FromHeader) response.getHeader(FromHeader.NAME);
        			SipURI toUri = (SipURI) from.getAddress().getURI();

                	response = messageFactory.createResponse(Response.OK, registrarBranch.get(toUri.getUser()));
                	SIPResponse sIPResponse = (SIPResponse)response;
                	ContactHeader contactHeader = headerFactory.createContactHeader(addressFactory.createAddress("101", registrar.get(toUri.getUser())));
                	sIPResponse.addHeader(contactHeader);

        			ServerTransaction serverTransaction = sipProvider.getNewServerTransaction(registrarBranch.get(toUri.getUser()));
        			serverTransaction.sendResponse(response);*/
                }
            } else if (response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED || response.getStatusCode() == Response.UNAUTHORIZED) {
            	System.out.println("1. ############# UNAUTHORIZED and resend to WEB.");

            	FromHeader from = (FromHeader) response.getHeader(FromHeader.NAME);
    			SipURI toUri = (SipURI) from.getAddress().getURI();

    			ViaHeader viaHeader = websocketViaHeader.get(toUri.getUser());
    			topmostViaHeader.setTransport(viaHeader.getTransport());
    			topmostViaHeader.setHost(viaHeader.getHost());
    			topmostViaHeader.setPort(viaHeader.getPort());

            	ServerTransaction serverTransaction = sipProvider.getNewServerTransaction(websocketRequest.get(toUri.getUser()));
            	serverTransaction.sendResponse(response);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            junit.framework.TestCase.fail("Exit JVM");
        }
	}

	/**
	 * Process the ACK request, forward it to the other leg.
	 */
	public void processAck(RequestEvent requestEvent,
			ServerTransaction serverTransaction) {
		try {
			Dialog dialog = serverTransaction.getDialog();
			System.out.println("b2bua: got an ACK! ");
			System.out.println("Dialog State = " + dialog.getState());
			Dialog otherDialog = (Dialog) dialog.getApplicationData();
			Request request = otherDialog.createAck(otherDialog.getLocalSeqNumber());
			otherDialog.sendAck(request);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Process the invite request.
	 */
	public void processInvite(RequestEvent requestEvent,
			ServerTransaction serverTransaction) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		try {
			System.out.println("b2bua: got an Invite sending Trying");
			ServerTransaction st = requestEvent.getServerTransaction();
			if(st == null) {
				st = sipProvider.getNewServerTransaction(request);
			}
			Dialog dialog = st.getDialog();

			ToHeader to = (ToHeader) request.getHeader(ToHeader.NAME);
			SipURI toUri = (SipURI) to.getAddress().getURI();

			SipURI target = registrar.get(toUri.getUser());

			if(target == null) {
				System.out.println("User " + toUri + " is not registered.");
				throw new RuntimeException("User not registered " + toUri);
			} else {
				ClientTransaction otherLeg = call(target);
				otherLeg.setApplicationData(st);
				st.setApplicationData(otherLeg);
				dialog.setApplicationData(otherLeg.getDialog());
				otherLeg.getDialog().setApplicationData(dialog);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
//			junit.framework.TestCase.fail("Exit JVM");
		}
	}

	/**
	 * Process the any in dialog request - MESSAGE, BYE, INFO, UPDATE.
	 */
	public void processInDialogRequest(RequestEvent requestEvent,
			ServerTransaction serverTransactionId) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		Dialog dialog = requestEvent.getDialog();
		System.out.println("local party = " + dialog.getLocalParty());
		try {
			System.out.println("b2bua:  got a bye sending OK.");
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);
			System.out.println("Dialog State is "
					+ serverTransactionId.getDialog().getState());

			Dialog otherLeg = (Dialog) dialog.getApplicationData();
			Request otherBye = otherLeg.createRequest(request.getMethod());
			ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(otherBye);
			clientTransaction.setApplicationData(serverTransactionId);
			serverTransactionId.setApplicationData(clientTransaction);
			otherLeg.sendRequest(clientTransaction);

		} catch (Exception ex) {
			ex.printStackTrace();
//			junit.framework.TestCase.fail("Exit JVM");

		}
	}
	public void processRegister(RequestEvent requestEvent, ServerTransaction serverTransactionId) {
		Request request = requestEvent.getRequest();
		ContactHeader contact = (ContactHeader) request.getHeader(ContactHeader.NAME);
		SipURI contactUri = (SipURI) contact.getAddress().getURI();
		FromHeader from = (FromHeader) request.getHeader(FromHeader.NAME);
		SipURI fromUri = (SipURI) from.getAddress().getURI();

		registrar.put(fromUri.getUser(), contactUri);

		try {
			websocketRequest.put(fromUri.getUser(), request);
			websocketViaHeader.put(fromUri.getUser(), ((SIPRequest)request).getTopmostViaHeader());


            Request requestSipServ = (Request)request.clone();
            SIPRequest sipRequest = (SIPRequest)requestSipServ;
            cSeqNo = sipRequest.getCSeq().getSeqNumber();

            ViaHeader topmostViaHeader = sipRequest.getTopmostViaHeader();
            topmostViaHeader.setTransport("UDP");
            topmostViaHeader.setHost("192.168.43.58");
            topmostViaHeader.setPort(8888);

            ClientTransaction transaction = sipProvider.getNewClientTransaction(requestSipServ);
            transaction.sendRequest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processCancel(RequestEvent requestEvent,
			ServerTransaction serverTransactionId) {}

	@Override
	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
		Transaction transaction;
		if (timeoutEvent.isServerTransaction()) {
			transaction = timeoutEvent.getServerTransaction();
		} else {
			transaction = timeoutEvent.getClientTransaction();
		}
		System.out.println("state = " + transaction.getState());
		System.out.println("dialog = " + transaction.getDialog());
		System.out.println("dialogState = "
				+ transaction.getDialog().getState());
		System.out.println("Transaction Time out");
	}

	public void init() {

		/*ConsoleAppender console = new ConsoleAppender(); //create appender
		//configure the appender
		String PATTERN = "%d [%p|%c|%C{1}] %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.DEBUG);
		console.activateOptions();
		//add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(console);*/


		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "shootme");
//		properties.setProperty("javax.sip.OUTBOUND_PROXY", "192.168.43.100:5060");
		// You need 16 for logging traces. 32 for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
//		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");
//		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "LOG4J");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "shootmedebug.txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "shootmelog.txt");
//		properties.setProperty("gov.nist.javax.sip.USE_TLS_GATEWAY", "true");
		properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
//		properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", OIOMessageProcessorFactory.class.getName());

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
//			junit.framework.TestCase.fail("Exit JVM");
		}

		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			this.listeningPoint = sipStack.createListeningPoint("127.0.0.1", myPort, transport);
//			this.listeningPoint = sipStack.createListeningPoint("192.168.43.58", myPort, transport);
			this.listeningPointProxy = sipStack.createListeningPoint("192.168.43.238", 8888, "udp");

			B2BUAWebsocket listener = this;

			sipProvider = sipStack.createSipProvider(listeningPoint);
			sipProvider.addListeningPoint(listeningPointProxy);

			System.out.println("ws provider " + sipProvider);
			sipProvider.addSipListener(listener);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}

	public static void main(String args[]) {
		new B2BUAWebsocket().init();
	}

	@Override
	public void processIOException(IOExceptionEvent exceptionEvent) {
		System.out.println("IOException");

	}

	@Override
	public void processTransactionTerminated(
			TransactionTerminatedEvent transactionTerminatedEvent) {
		if (transactionTerminatedEvent.isServerTransaction())
			System.out.println("Transaction terminated event recieved"
					+ transactionTerminatedEvent.getServerTransaction());
		else
			System.out.println("Transaction terminated "
					+ transactionTerminatedEvent.getClientTransaction());

	}

	@Override
	public void processDialogTerminated(
			DialogTerminatedEvent dialogTerminatedEvent) {
		System.out.println("Dialog terminated event recieved");
		Dialog d = dialogTerminatedEvent.getDialog();
		System.out.println("Local Party = " + d.getLocalParty());

	}

	public ClientTransaction call(SipURI destination) {
		try {

			String fromName = "B2BUA";
			String fromSipAddress = "here.com";
			String fromDisplayName = "B2BUA";

			String toSipAddress = "there.com";
			String toUser = "Target";
			String toDisplayName = "Target";

			// create >From Header
			SipURI fromAddress = addressFactory.createSipURI(fromName,
					fromSipAddress);

			Address fromNameAddress = addressFactory.createAddress(fromAddress);
			fromNameAddress.setDisplayName(fromDisplayName);
			FromHeader fromHeader = headerFactory.createFromHeader(
					fromNameAddress, new Long(counter.getAndIncrement()).toString());

			// create To Header
			SipURI toAddress = addressFactory
					.createSipURI(toUser, toSipAddress);
			Address toNameAddress = addressFactory.createAddress(toAddress);
			toNameAddress.setDisplayName(toDisplayName);
			ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
					null);

			// create Request URI
			SipURI requestURI = destination;

			// Create ViaHeaders

			ArrayList viaHeaders = new ArrayList();
			String ipAddress = listeningPoint.getIPAddress();
			ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress,
					sipProvider.getListeningPoint(transport).getPort(),
					transport, null);

			// add via headers
			viaHeaders.add(viaHeader);

			// Create ContentTypeHeader
			ContentTypeHeader contentTypeHeader = headerFactory
					.createContentTypeHeader("application", "sdp");

			// Create a new CallId header
			CallIdHeader callIdHeader = sipProvider.getNewCallId();

			// Create a new Cseq header
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
					Request.INVITE);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = headerFactory
					.createMaxForwardsHeader(70);

			// Create the request.
			Request request = messageFactory.createRequest(requestURI,
					Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
					toHeader, viaHeaders, maxForwards);
			// Create contact headers
			String host = "127.0.0.1";

			SipURI contactUrl = addressFactory.createSipURI(fromName, host);
			contactUrl.setPort(listeningPoint.getPort());
			contactUrl.setLrParam();

			// Create the contact name address.
			SipURI contactURI = addressFactory.createSipURI(fromName, host);
			contactURI.setPort(sipProvider.getListeningPoint(transport)
					.getPort());

			Address contactAddress = addressFactory.createAddress(contactURI);

			// Add the contact address.
			contactAddress.setDisplayName(fromName);

			ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
			request.addHeader(contactHeader);

			// You can add extension headers of your own making
			// to the outgoing SIP request.
			// Add the extension header.
			Header extensionHeader = headerFactory.createHeader("My-Header",
					"my header value");
			request.addHeader(extensionHeader);

			String sdpData = "v=0\r\n"
					+ "o=4855 13760799956958020 13760799956958020"
					+ " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
					+ "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
					+ "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
					+ "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
					+ "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
			byte[] contents = sdpData.getBytes();

			request.setContent(contents, contentTypeHeader);
			// You can add as many extension headers as you
			// want.

			extensionHeader = headerFactory.createHeader("My-Other-Header",
					"my new header value ");
			request.addHeader(extensionHeader);

			Header callInfoHeader = headerFactory.createHeader("Call-Info",
					"<http://www.antd.nist.gov>");
			request.addHeader(callInfoHeader);

			// Create the client transaction.
			ClientTransaction inviteTid = sipProvider.getNewClientTransaction(request);

			System.out.println("inviteTid = " + inviteTid);

			// send the request out.

			inviteTid.sendRequest();

			return inviteTid;

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
		return null;
	}

}





/*ServerTransaction st = null;
if (requestEvent.getServerTransaction() == null) {
    st = sipProvider.getNewServerTransaction(request);
}*/

//----------------------------------------------------------------------------------------------
/*SipProvider txProvider = (SipProvider) requestEvent.getSource();
ListeningPoint lp = txProvider.getListeningPoint(transport);
String host = lp.getIPAddress();
int port = lp.getPort();
System.out.println(host + " " + port);

Request newRequest = (Request) request.clone();
SipURI sipUri = addressFactory.createSipURI("UAC", "192.168.43.200");
sipUri.setPort(5060);
sipUri.setLrParam();

Address address = addressFactory.createAddress("client", sipUri);
RouteHeader rheader = headerFactory.createRouteHeader(address);
newRequest.addFirst(rheader);
ViaHeader viaHeader = headerFactory.createViaHeader(host, port, transport, null);
newRequest.addFirst(viaHeader);
ClientTransaction ct1 = txProvider.getNewClientTransaction(newRequest);

sipUri = addressFactory.createSipURI("proxy", "127.0.0.1");
address = addressFactory.createAddress("proxy",sipUri);
sipUri.setPort(5082);
sipUri.setLrParam();
RecordRouteHeader recordRoute = headerFactory.createRecordRouteHeader(address);
newRequest.addHeader(recordRoute);
ct1.setApplicationData(st);
ct1.sendRequest();*/


/*  Address address = addressFactory.createAddress("sip101@192.198.43.100:5060");
URI requestURI = address.getURI();
System.out.println(requestURI);*/

//String uriphy = listeningPoint.getIPAddress() + ":" + listeningPoint.getPort();
//String uriphy = "192.198.43.100:5060";

//creation de SIPuri
/*SipURI sipuri = addressFactory.createSipURI("101", uriphy);
System.out.println(sipuri);

Address adrphysique = addressFactory.createAddress("101", sipuri);
System.out.println(adrphysique);

String nomdomaine = "192.198.43.100:5060";
SipURI urilogique = addressFactory.createSipURI("101", nomdomaine);
Address adrlogique = addressFactory.createAddress("TestTahar2", urilogique);

CallIdHeader callidheader = sipProvider.getNewCallId();
CSeqHeader cseqHeader = headerFactory.createCSeqHeader(cSeq++, Request.REGISTER);

MaxForwardsHeader maxforward = headerFactory.createMaxForwardsHeader(70);

FromHeader fromHeader = headerFactory.createFromHeader(adrphysique, ""+new Random().nextInt());

ContactHeader contactheader = headerFactory.createContactHeader(adrphysique);
contactheader.setExpires(6000);

ToHeader toheader = headerFactory.createToHeader(adrlogique, null);
System.out.println(toheader);

ArrayList<ViaHeader> via = new ArrayList<ViaHeader>();
ViaHeader viaheader= headerFactory.createViaHeader(listeningPoint.getIPAddress(), listeningPoint.getPort(), listeningPoint.getTransport(), null);
via.add(viaheader);

ContentTypeHeader contenttypeheader = headerFactory.createContentTypeHeader("text", "txt");
System.out.println(contactheader.getName());

Request register = messageFactory.createRequest(
		adrlogique.getURI(), Request.REGISTER, callidheader,
		cseqHeader, fromHeader, toheader, via, maxforward,
		contenttypeheader, contactheader
		);
System.out.println(adrlogique.getURI());

//sipProvider.addListeningPoint(listeningPointProxy);
ClientTransaction clienttransaction = sipProvider.getNewClientTransaction(register);
clienttransaction.sendRequest();*/







/*
ViaHeader topmostViaHeader = sipRequest.getTopmostViaHeader();
topmostViaHeader.setTransport("UDP");
topmostViaHeader.setHost("192.168.43.58");
topmostViaHeader.setPort(8888);

Address fromAddress = addressFactory.createAddress("sip:192.168.43.58:8888");
sipRequest.getFrom().setAddress(fromAddress);
sipRequest.getTo().setAddress(addressFactory.createAddress("sip:192.168.43.100:5060"));
requestSipServ.setRequestURI(addressFactory.createAddress("sip:192.168.43.100:5060").getURI());
requestSipServ.setHeader(headerFactory.createContactHeader(fromAddress));
sipRequest.setCallId(sipProvider.getNewCallId());
sipRequest.getRouteHeaders().clear();  //---: Remove Route
*/

/*String msg = "REGISTER sip:asterisk SIP/2.0\r\n" +
		"Via: SIP/2.0/UDP 192.168.43.58:8888;branch=z9hG4bKLP6ePe2TTGnTyMhuX6eXyxlXz4foOHx7;rport\r\n" +
		"From: \"May\"<sip:101@192.16.43.100>;tag=kIhlNatMzWgbuQACpS8C\r\n" +
		"To: \"May\"<sip:101@192.16.43.100>\r\n" +
		"Contact: \"May\"<sip:101@df7jal23ls0d.invalid;rtcweb-breaker=no;transport=ws>;expires=200;click2call=no;+g.oma.sip-im;+audio;language=\"en,fr\"\r\n" +
		"Call-ID: c82b01ed-aa42-4261-02c2-7ddfb4e13e4e\r\n" +
		"CSeq: 1 REGISTER\r\n" +
		"Expires: 3600\r\n" +
		"Content-Length: 0\r\n" +
		"Route: <sip:101@192.168.43.100:5060;lr;sipml5-outbound;transport=udp>\r\n" +
		"Max-Forwards: 70\r\n" +
		"User-Agent: IM-client/OMA1.0 sipML5-v1.2016.03.04\r\n" +
		"Organization: Doubango Telecom\r\n" +
		"Supported: path" +
		"\r\n\r\n";

requestSipServ = messageFactory.createRequest(msg);*/

/*String msg = "REGISTER sip:192.168.43.100:5060 SIP/2.0\r\n" +
		"Via: SIP/2.0/UDP 192.168.43.58:8888;branch=z9hG4bKc4dfxzUMys7RDQTVC9GgbMfh1lVWSune;rport=51343;received=127.0.0.1\r\n" +
		"From: <sip:192.168.43.58:8888>;tag=JzlMupWH1L4LpAaTmUUG\r\n" +
		"To: <sip:192.168.43.100:5060>\r\n" +
		"CSeq: 30129 REGISTER\r\n" +
		"Route:\r\n" +
		"Max-Forwards: 70\r\n" +
		"User-Agent: IM-client/OMA1.0 sipML5-v1.2016.03.04\r\n" +
		"Organization: Doubango Telecom\r\n" +
		"Supported: path\r\n" +
		"Contact: <sip:192.168.43.58:8888>\r\n" +
		"Call-ID: 757535a94b0b294f230094e8e4bee723@192.168.43.58\r\n" +
		"Content-Length: 0" +
		"\r\n\r\n";*/


//---:

/* Address toAddress = addressFactory.createAddress("sip:192.168.43.100:5060");
URI requestURI = toAddress.getURI();
ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

List<ViaHeader> viaHeaders = new ArrayList<>();
ViaHeader viaHeader = headerFactory.createViaHeader("192.168.43.58", 8888, "udp", null);
viaHeaders.add(viaHeader);

MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
CallIdHeader callIdHeader = sipProvider.getNewCallId();
CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(cSeq++,"REGISTER");

Address fromAddress = addressFactory.createAddress("sip:192.168.43.58:8888");
FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, String.valueOf((new Random()).nextInt()));


request = messageFactory.createRequest(
    requestURI,
    "REGISTER",
    callIdHeader,
    cSeqHeader,
    fromHeader,
    toHeader,
    viaHeaders,
    maxForwardsHeader
);

ContactHeader contactHeader = headerFactory.createContactHeader(fromAddress);
request.addHeader(contactHeader);*/

//---: Set Password
/*AuthorizationHeader authHeader = Utils.makeAuthHeader(headerFactory, response,
		  request, "101", "abc123");
		  request.addHeader(authHeader);*/



//sipProvider.sendRequest(requestSipServ);
//Response response = messageFactory.createResponse(200, request);
//ServerTransaction serverTransaction = sipProvider.getNewServerTransaction(request);
//st.sendResponse(response);
//websocketTxn.put(fromUri.getUser(), sipProvider.getNewServerTransaction(request));



/*AuthenticationHelper authenticationHelper = ((SipStackExt) sipStack).getAuthenticationHelper(new AccountManagerImpl(), headerFactory);
inviteTid = authenticationHelper.handleChallenge(response, tid, sipProvider, 5);
inviteTid.sendRequest();
cSeqNo++;*/