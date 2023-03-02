package com.may.ple.phone.jainsip;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
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
import javax.sip.header.ContactHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.may.ple.phone.TestRTPEngineNG;

import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.ResponseEventExt;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.header.extensions.SessionExpires;
import gov.nist.javax.sip.header.extensions.SessionExpiresHeader;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;
import gov.nist.javax.sip.stack.SIPTransaction;

public class WebRTCGateway implements SipListener {
	private static final Logger LOGGER = Logger.getLogger(WebRTCGateway.class.getName());

	private static final String wsTransport = "ws";
	private static final String wsHost = "127.0.0.1";
	private static final int wsPort = 9001;
	private static final String gatewayHost = "192.168.43.238";
	private static final int gatewayPort = 8000;
	private static final String gatewayTransport = "udp";
	private static final String sipHost = "192.168.43.100";

	private static AddressFactory addressFactory;
	private static MessageFactory messageFactory;
	private static HeaderFactory headerFactory;
	private static SipStack sipStack;

	private ListeningPoint listeningPoint;
	private ListeningPoint listeningPointProxy;
	private SipProvider sipProvider;

	private AtomicLong counter = new AtomicLong();
	private Map<String, Map<String, Object>> registrar = new HashMap<>();

	private Map<String, Map<String, Object>> webrtc_users = new HashMap<>();
//	private Map<String, Map<String, Object>> sip_users = new HashMap<>();
	private Map<String, Map<String, String>> sdps = new HashMap<>();

	@Override
	public void processRequest(RequestEvent requestEvent) {
		Request request = requestEvent.getRequest();
		try {
			SIPRequest sipRequest = (SIPRequest)request;
			RequestEventExt ext = (RequestEventExt)requestEvent;
			CSeqHeader cseqHeader = (CSeqHeader) sipRequest.getHeader(CSeqHeader.NAME);
			LOGGER.debug("#####----: Start processRequest# From : " + ext.getRemoteIpAddress() + ":" + ext.getRemotePort() + " Method : " + cseqHeader.getMethod());
			boolean isFromBack = isReqFromBack(ext);
			LOGGER.debug("From Backend(SIP Server) " + isFromBack);
			LOGGER.debug("---------: TRX = " + requestEvent.getServerTransaction());
			ViaHeader viaHeader = (ViaHeader)request.getHeader(ViaHeader.NAME);
			LOGGER.debug("---------: BRANCH = " + viaHeader.getBranch());

			SipStackImpl sipStackImpl = (SipStackImpl)sipStack;
			LOGGER.debug("ClientTransactionTableSize " + sipStackImpl.getClientTransactionTableSize());
			LOGGER.debug("ServerTransactionTableSize " + sipStackImpl.getServerTransactionTableSize());

			if (request.getMethod().equals(Request.REGISTER)) {
				if(sipRequest.getAuthorization() == null) {
					proxyReqManage(requestEvent);
				} else {
					LOGGER.debug("Send Authorization");
					proxyReqManage(requestEvent);

					ContactHeader contactHeader = (ContactHeader) request.getHeader(ContactHeader.NAME);
					SipURI contactUri = (SipURI)contactHeader.getAddress().getURI();
					if(!webrtc_users.containsKey(contactUri.getUser())) {
						LOGGER.debug("add new user:  " + contactUri + ", ext: " + sipRequest.getAuthorization().getUsername());

						Map<String, Object> userMap = new HashMap<>();
						userMap.put("uri", contactUri);
						userMap.put("ext", sipRequest.getAuthorization().getUsername());
						webrtc_users.put(contactUri.getUser(), userMap);
					}
					LOGGER.debug("## Users size " + webrtc_users.size());
				}
			} else if (request.getMethod().equals(Request.OPTIONS)) {
				//---: Request to Web-Client

				SipURI sipURI = ((SipURI)sipRequest.getRequestURI());
				LOGGER.debug(sipURI);
				String user = sipURI.getUser();
				Map<String, Object> userMap = webrtc_users.get(user);
	            sipURI = (SipURI)userMap.get("uri");
	            request.setRequestURI(sipURI);

				sipProvider.sendRequest(request);
			} else if (request.getMethod().equals(Request.INVITE)) {

				SessionExpiresHeader seh = (SessionExpiresHeader)sipRequest.getHeader(SessionExpires.NAME);
				if(seh != null && StringUtils.isNotBlank(seh.getRefresher())) {

					LOGGER.debug(sdps);
					LOGGER.debug(sipRequest.getCallId().getCallId());

					Map<String, String> sdpMap = sdps.get(sipRequest.getCallId().getCallId());
//            		sipRequest.setContent(sdpTmp_1.getBytes(), headerFactory.createContentTypeHeader("application", "sdp"));
            		sipRequest.setContent(sdpMap.get("answer").getBytes(), headerFactory.createContentTypeHeader("application", "sdp"));
            		String user = ((SipURI)sipRequest.getRequestURI()).getUser();
//    	            SipURI sipURI = users.get(user);
//    	            request.setRequestURI(sipURI);
//					sipProvider.sendRequest(sipRequest);

					//----:
					ServerTransaction st = requestEvent.getServerTransaction();
		            ClientTransaction ct1 = null;
		            if (st == null) {
		                st = sipProvider.getNewServerTransaction(request);
		                LOGGER.debug("Create Server Transaction " + st);
		            }

		            Map<String, Object> userMap = webrtc_users.get(user);
		            SipURI sipURI = (SipURI)userMap.get("uri");
    	            Request newRequest = (Request) request.clone();
    	            newRequest.setRequestURI(sipURI);

    	            viaHeader = (ViaHeader)request.getHeader(ViaHeader.NAME);
    	            viaHeader = headerFactory.createViaHeader(sipURI.getHost(), sipURI.getPort(), wsTransport, viaHeader.getBranch());
    	            newRequest.addFirst(viaHeader);

		            if(ct1 == null) {
		            	ct1 = sipProvider.getNewClientTransaction(newRequest);
		            	LOGGER.debug("Create Client Transaction " + ct1);
		            }

		            ct1.setApplicationData(st);
		            ct1.sendRequest();
				} else {
					if(isFromBack) {
						LOGGER.debug("Send Invite to WebRTC.");
						String user = ((SipURI)sipRequest.getRequestURI()).getUser();
						Map<String, Object> userMap = webrtc_users.get(user);
			            SipURI sipURI = (SipURI)userMap.get("uri");

			            Request newRequest = (Request) request.clone();
	    	            newRequest.setRequestURI(sipURI);

	    	            viaHeader = (ViaHeader)request.getHeader(ViaHeader.NAME);
	    	            viaHeader = headerFactory.createViaHeader(sipURI.getHost(), sipURI.getPort(), wsTransport, viaHeader.getBranch());
	    	            newRequest.addFirst(viaHeader);

	    	            //---
	    	            /*for (Map.Entry<String, Map<String, String>> entry : sdps.entrySet()) {
	    	            	newRequest.setContent(sdps.get(entry.getKey()).get("offer").getBytes(), headerFactory.createContentTypeHeader("application", "sdp"));
	    	            }*/
	    	            //---

	    	            //---:
	    	            String fromTag = sipRequest.getFromTag();
						String callId = sipRequest.getCallId().getCallId();
						String sdp = new String((byte[])request.getContent());

	    	            Map<String, String> params = new HashMap<>();
						params.put("command", "offer");
//						params.put("transportPro", "RTP/SAVPF");
						params.put("transportPro", "UDP/TLS/RTP/SAVPF");
						params.put("sdp", sdp);
						params.put("callId", callId);
						params.put("fromTag", fromTag);
						LOGGER.debug("---------: Offer requeset = " + params);
						Map<String, Object> result = TestRTPEngineNG.manage(params, null);
						sdp = result.get("sdp").toString();
						newRequest.setContent(sdp.getBytes(), headerFactory.createContentTypeHeader("application", "sdp"));
						//---:

			            ServerTransaction st = requestEvent.getServerTransaction();
			            if (st == null) {
			                st = sipProvider.getNewServerTransaction(request);
			                LOGGER.debug("Create Server Transaction " + st);
			            }

		            	ClientTransaction ct1 = sipProvider.getNewClientTransaction(newRequest);
		            	LOGGER.debug("Create Client Transaction " + ct1);

			            ct1.setApplicationData(st);
			            ct1.sendRequest();
					} else {
						String fromTag = sipRequest.getFromTag();
						String callId = sipRequest.getCallId().getCallId();
						String sdp = new String((byte[])request.getContent());

						if(sipRequest.getAuthorization() != null) {
							Map<String, String> params = new HashMap<>();
							params.put("command", "offer");
							params.put("transportPro", "RTP/AVP");
							params.put("sdp", sdp);
							params.put("callId", callId);
							params.put("fromTag", fromTag);
//							params.put("receivedFrom", ext.getRemoteIpAddress());
							LOGGER.debug("---------: Offer requeset = " + params);

							///---: Checking to know that it is the WebRTC users.
							SipURI toUri = (SipURI)sipRequest.getTo().getAddress().getURI();
							LOGGER.debug("show ext: " + toUri.getUser());

							boolean isWebrtc = false;
							Map<String, Object> userVal;
							for (Map.Entry<String, Map<String, Object>> userMap : webrtc_users.entrySet()) {
								userVal = userMap.getValue();
								if(toUri.getUser().equals(userVal.get("ext").toString())) {
									isWebrtc = true;
									break;
								}
							}
							LOGGER.debug("isWebrtc : " + isWebrtc);
							//----:

							Map<String, Object> result = TestRTPEngineNG.manage(params, null);
							sdp = result.get("sdp").toString();

							/*SipURI a = (SipURI)sipRequest.getContactHeader().getAddress();
	            		a.getUser();*/

							Map<String, String> sdpMap = new HashMap<>();
							sdpMap.put("offer", sdp);
							sdpMap.put("fromTag", fromTag);
							sdps.put(callId, sdpMap);

							sipRequest.setContent(sdp.getBytes(), headerFactory.createContentTypeHeader("application", "sdp"));
						}

						proxyReqManage(requestEvent);
					}
				}
			} else if (request.getMethod().equals(Request.ACK)) {
				LOGGER.debug("ACK");

				if(isFromBack) {
					String user = ((SipURI)sipRequest.getRequestURI()).getUser();
					Map<String, Object> userMap = webrtc_users.get(user);
		            SipURI sipURI = (SipURI)userMap.get("uri");
    	            request.setRequestURI(sipURI);
    	            sipProvider.sendRequest(sipRequest);
				} else {
					ViaHeader topmostViaHeader = sipRequest.getTopmostViaHeader();
					topmostViaHeader.setTransport(gatewayTransport);
					topmostViaHeader.setHost(gatewayHost);
					topmostViaHeader.setPort(gatewayPort);

					sipProvider.sendRequest(request);
				}
			} else if (request.getMethod().equals(Request.BYE)) {
				if(isFromBack) {
					String user = ((SipURI)sipRequest.getRequestURI()).getUser();

					Map<String, Object> userMap = webrtc_users.get(user);
		            SipURI sipURI = (SipURI)userMap.get("uri");
    	            request.setRequestURI(sipURI);
    	            sipProvider.sendRequest(sipRequest);

    	            //---: Delete media 1st round.
    	            Map<String, String> params = new HashMap<>();
        			params.put("command", "delete");
            		params.put("callId", sipRequest.getCallId().getCallId());
            		params.put("fromTag", sipRequest.getFromTag());
            		LOGGER.debug("---------: Delete requeset = " + params);
            		Map<String, Object> result = TestRTPEngineNG.manage(params, null);
            		LOGGER.debug("---------: Delete result = " + result);

            		//---: Delete media 2nd round.
            		params = new HashMap<>();
         			params.put("command", "delete");
             		params.put("callId", sipRequest.getCallId().getCallId());
             		params.put("fromTag", sipRequest.getFromTag());
             		params.put("toTag", sipRequest.getToTag());
             		LOGGER.debug("---------: Delete requeset = " + params);
             		result = TestRTPEngineNG.manage(params, null);
             		LOGGER.debug("---------: Delete result = " + result);

             		LOGGER.debug("Remove sdps");
             		sdps.remove(sipRequest.getCallId().getCallId());
             		LOGGER.debug("sdps : " + sdps.size());
				} else {
					ViaHeader topmostViaHeader = sipRequest.getTopmostViaHeader();
					topmostViaHeader.setTransport(gatewayTransport);
					topmostViaHeader.setHost(gatewayHost);
					topmostViaHeader.setPort(gatewayPort);

					sipProvider.sendRequest(request);
				}
			} else {
				LOGGER.error("1 Unknow processRequest ************ " + request.getMethod());
			}
			LOGGER.debug("#####----: End processRequest \n");
		} catch (Exception e) {
			LOGGER.error(e.toString(), e);
			try {
				LOGGER.debug("#### Remove user.");
				String user = ((SipURI)request.getRequestURI()).getUser();
				if(!webrtc_users.containsKey(user)) {
					LOGGER.warn("User have already removed.");
					return;
				}
				webrtc_users.remove(user);
				LOGGER.debug("Users size " + webrtc_users.size());

				LOGGER.debug("#### return NOT FOUND.");
				Response response = messageFactory.createResponse(Response.NOT_FOUND, request);
				sipProvider.sendResponse(response);
			} catch (Exception ex) {
				LOGGER.error(ex.toString(), ex);
			}
		}
	}

	@Override
	public void processResponse(ResponseEvent responseEvent) {
		Response response = (Response) responseEvent.getResponse();
		SIPResponse sipResponse = (SIPResponse)response;
		ResponseEventExt ext = (ResponseEventExt)responseEvent;
		CSeqHeader cseqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

		LOGGER.debug("#####----: Start processResponse# From : " + ext.getRemoteIpAddress()+ ":" + ext.getRemotePort() +
				     " Method : " + cseqHeader.getMethod() +
				     " StatusCode : " + response.getStatusCode());

    	boolean isFromBack = isRespFromBack(ext);
    	LOGGER.debug("From Backend(SIP Server) " + isFromBack);
    	LOGGER.debug("---------: TRX = " + responseEvent.getClientTransaction());
    	ViaHeader viaHeader = (ViaHeader)response.getHeader(ViaHeader.NAME);
		LOGGER.debug("---------: BRANCH = " + viaHeader.getBranch());

		SipStackImpl sipStackImpl = (SipStackImpl)sipStack;
		LOGGER.debug("ClientTransactionTableSize " + sipStackImpl.getClientTransactionTableSize());
		LOGGER.debug("ServerTransactionTableSize " + sipStackImpl.getServerTransactionTableSize());

        try {
        	if (response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED || response.getStatusCode() == Response.UNAUTHORIZED) {
        		LOGGER.debug("1. ############# UNAUTHORIZED and resend to WEB.");

            	if (cseqHeader.getMethod().equals(Request.INVITE)) {
            		LOGGER.debug("INVITE UNAUTHORIZED");
            		proxyRespManage(isFromBack, responseEvent);
            	} else {
            		proxyRespManage(isFromBack, responseEvent);
            	}
            } else if (response.getStatusCode() == Response.OK) {
		        if (cseqHeader.getMethod().equals(Request.REGISTER)) {
	            	proxyRespManage(isFromBack, responseEvent);
		        } else if (cseqHeader.getMethod().equals(Request.OPTIONS)) {
		        	//---: Response to SIP Server

		        	ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();
		            topmostViaHeader.setTransport(gatewayTransport);

					sipProvider.sendResponse(response);
		        } else if (cseqHeader.getMethod().equals(Request.INVITE)) {
		        	LOGGER.debug("Callee answered call");

		        	if(isFromBack) {
		        		LOGGER.debug("From SIP Server");
		        		String fromTag = sipResponse.getFromTag();
		        		String toTag = sipResponse.getToTag();
		        		String callId = sipResponse.getCallId().getCallId();
		        		String sdp = new String((byte[])sipResponse.getContent());

	            		Map<String, String> params = new HashMap<>();
	        			params.put("command", "answer");
//	        			params.put("transportPro", "RTP/SAVPF");
	        			params.put("transportPro", "UDP/TLS/RTP/SAVPF");
	            		params.put("sdp", sdp);
	            		params.put("callId", callId);
	            		params.put("fromTag", fromTag);
	            		params.put("toTag", toTag);
//	            		params.put("receivedFrom", ext.getRemoteIpAddress());
	            		LOGGER.debug("---------: Answer requeset = " + params);

		        		Map<String, Object> result = TestRTPEngineNG.manage(params, null);
	            		sdp = result.get("sdp").toString();
		        		sipResponse.setContent(sdp.getBytes(), headerFactory.createContentTypeHeader("application", "sdp"));

		        		Map<String, String> sdpMap = sdps.get(callId);
	            		sdpMap.put("answer", sdp);
	            		sdpMap.put("toTag", toTag);

		        		proxyRespManage(isFromBack, responseEvent);
		        	} else {
		        		LOGGER.debug("From WebRTC");
						LOGGER.debug(sipResponse.getCallId().getCallId());

						//---:
	    	            String fromTag = sipResponse.getFromTag();
	    	            String toTag = sipResponse.getToTag();
						String callId = sipResponse.getCallId().getCallId();
						String sdp = new String((byte[])response.getContent());

	    	            Map<String, String> params = new HashMap<>();
						params.put("command", "answer");
						params.put("transportPro", "RTP/AVP");
						params.put("sdp", sdp);
						params.put("callId", callId);
						params.put("fromTag", fromTag);
						params.put("toTag", toTag);
//						params.put("receivedFrom", "192.168.43.238");
						LOGGER.debug("---------: answer requeset = " + params);
						Map<String, Object> result = TestRTPEngineNG.manage(params, null);
						sdp = result.get("sdp").toString();
						//---:

//		        		Map<String, String> sdpMap = sdps.get(sipResponse.getCallId().getCallId());
//			            sipResponse.setContent(sdpTmp_2.getBytes(), headerFactory.createContentTypeHeader("application", "sdp"));
//			            sipResponse.setContent(sdpMap.get("offer").getBytes(), headerFactory.createContentTypeHeader("application", "sdp"));

			            ClientTransaction ct = responseEvent.getClientTransaction();
			        	LOGGER.debug("---------: TRX = " + ct);

			        	if (ct != null) {
			        		ListeningPoint lp = sipProvider.getListeningPoint(gatewayTransport);
			                String host = lp.getIPAddress();
			                int port = lp.getPort();

			        		ServerTransaction st = (ServerTransaction)ct.getApplicationData();
			        		SIPResponse newResponse = (SIPResponse) response.clone();
			        		newResponse.removeFirst(ViaHeader.NAME);
			        		SipURI contactUri = (SipURI)newResponse.getContactHeader().getAddress().getURI();
			        		contactUri.setTransportParam(gatewayTransport);
			        		contactUri.setHost(host);
			        		contactUri.setPort(port);

			        		newResponse.setContent(sdp.getBytes(), headerFactory.createContentTypeHeader("application", "sdp"));
			        		st.sendResponse(newResponse);
			        	} else {
			        		ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();
				            topmostViaHeader.setTransport(gatewayTransport);
			        		sipProvider.sendResponse(response);
			        	}
		        	}
		        } else if (cseqHeader.getMethod().equals(Request.BYE)) {
		        	ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();
		            topmostViaHeader.setTransport(gatewayTransport);

		            SIPTransaction trx = sipStackImpl.findTransaction(topmostViaHeader.getBranch().toLowerCase(), true);
		            if(trx == null) {
		            	sipProvider.sendResponse(sipResponse);
		            } else {
		            	LOGGER.debug("---------: TRX = " + trx);
		            	LOGGER.debug("---------: TRX ID = " + trx.getTransactionId());
		            	trx.sendMessage(sipResponse);
		            }
		        } else {
		        	LOGGER.debug("1 Unknow processResponse ************ " + response.getStatusCode());
		        }
            } else if (response.getStatusCode() == Response.TRYING) {
            	LOGGER.debug("TRYING");
            	if(!isFromBack) {
//		        	ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();
//		            topmostViaHeader.setTransport(gatewayTransport);
//		            sipProvider.sendResponse(response);

		            ClientTransaction ct = responseEvent.getClientTransaction();
		            if (ct != null) {
		        		ServerTransaction st = (ServerTransaction)ct.getApplicationData();
		        		Response newResponse = (Response) response.clone();
		        		newResponse.removeFirst(ViaHeader.NAME);
		        		st.sendResponse(newResponse);
		        	} else {
		        		ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();
			            topmostViaHeader.setTransport(gatewayTransport);
		        		sipProvider.sendResponse(response);
		        	}
            	} else {
            		proxyRespManage(isFromBack, responseEvent);
            	}
            } else if (response.getStatusCode() == Response.RINGING) {
            	LOGGER.debug("RINGING");

            	if(!isFromBack) {
            		ClientTransaction ct = responseEvent.getClientTransaction();
		            if (ct != null) {
		        		ServerTransaction st = (ServerTransaction)ct.getApplicationData();
		        		Response newResponse = (Response) response.clone();
		        		newResponse.removeFirst(ViaHeader.NAME);
		        		st.sendResponse(newResponse);
		        	} else {
		        		ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();
			            topmostViaHeader.setTransport(gatewayTransport);
		        		sipProvider.sendResponse(response);
		        	}
            	} else {
            		proxyRespManage(isFromBack, responseEvent);
            	}
            } else if (response.getStatusCode() == Response.BUSY_HERE ||
            		   response.getStatusCode() == Response.SERVICE_UNAVAILABLE ||
            		   response.getStatusCode() == Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST) {

            	if (cseqHeader.getMethod().equals(Request.INVITE) ||
            		cseqHeader.getMethod().equals(Request.BYE)) {

            		//---: Delete media 1st round.
    	            Map<String, String> params = new HashMap<>();
        			params.put("command", "delete");
            		params.put("callId", sipResponse.getCallId().getCallId());
            		params.put("fromTag", sipResponse.getFromTag());
            		LOGGER.debug("---------: Delete requeset = " + params);
            		Map<String, Object> result = TestRTPEngineNG.manage(params, null);
            		LOGGER.debug("---------: Delete result = " + result);

            		LOGGER.debug("Remove sdps");
             		sdps.remove(sipResponse.getCallId().getCallId());
             		LOGGER.debug("sdps : " + sdps.size());
            	} else {
            		LOGGER.debug("Unknow Method ************ " + cseqHeader.getMethod());
            	}

            	ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();
            	SIPTransaction trx = sipStackImpl.findTransaction(topmostViaHeader.getBranch().toLowerCase(), true);
	            if(trx == null) {
	            	sipProvider.sendResponse(sipResponse);
	            } else {
	            	LOGGER.debug("---------: TRX = " + trx);
	            	LOGGER.debug("---------: TRX ID = " + trx.getTransactionId());
	            	topmostViaHeader.setTransport(gatewayTransport);
	            	trx.sendMessage(sipResponse);
	            }
            } else {
            	LOGGER.error("2 Unknow processResponse ************ " + response.getStatusCode());

            	ViaHeader topmostViaHeader = sipResponse.getTopmostViaHeader();
            	SIPTransaction trx = sipStackImpl.findTransaction(topmostViaHeader.getBranch().toLowerCase(), true);
	            if(trx == null) {
	            	sipProvider.sendResponse(sipResponse);
	            } else {
	            	LOGGER.debug("---------: TRX = " + trx);
	            	LOGGER.debug("---------: TRX ID = " + trx.getTransactionId());
	            	sipResponse.removeFirst(ViaHeader.NAME);
	            	trx.sendMessage(sipResponse);
	            }
            }
        } catch (Exception ex) {
        	LOGGER.error(ex.toString(), ex);
        	try {
        		Dialog dialog = responseEvent.getDialog();
        		Request request = dialog.createRequest(Request.BYE);
        		request.removeFirst(ViaHeader.NAME);
        		ClientTransaction clientTrx = sipProvider.getNewClientTransaction(request);
        		dialog.sendRequest(clientTrx);
			} catch (Exception e) {
				LOGGER.error(e.toString(), e);
			}
        }
        LOGGER.debug("#####----: End processResponse \n");
	}

	private void proxyReqManage(RequestEvent requestEvent) throws Exception {
		try {
			Request request = requestEvent.getRequest();
			ListeningPoint lp = sipProvider.getListeningPoint(gatewayTransport);
            String host = lp.getIPAddress();
            int port = lp.getPort();

            ServerTransaction st = requestEvent.getServerTransaction();
            ClientTransaction ct1 = null;
            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
                LOGGER.debug("Create Server Transaction " + st);
            } else {
            	ct1 = (ClientTransaction)st.getApplicationData();
            }

            ContactHeader contactHeader = (ContactHeader) request.getHeader(ContactHeader.NAME);
            SipURI contactUri = (SipURI)contactHeader.getAddress().getURI();

            Request newRequest = (Request) request.clone();

            //---: Update Via Header
            ViaHeader viaHeader = (ViaHeader)request.getHeader(ViaHeader.NAME);
            viaHeader = headerFactory.createViaHeader(host, port, gatewayTransport, viaHeader.getBranch());
//          ViaHeader viaHeader = headerFactory.createViaHeader(host, port, gatewayTransport, null);
            newRequest.addFirst(viaHeader);

            //---: Update Contact Header
            contactHeader = (ContactHeader) newRequest.getHeader(ContactHeader.NAME);
            contactUri = (SipURI)contactHeader.getAddress().getURI();
            contactUri.setHost(host);
            contactUri.setPort(port);
            contactUri.setTransportParam(gatewayTransport);

            if(ct1 == null) {
            	ct1 = sipProvider.getNewClientTransaction(newRequest);
            	LOGGER.debug("Create Client Transaction " + ct1);
            }

            ct1.setApplicationData(st);
            ct1.sendRequest();
		} catch (Exception e) {
			throw e;
		}
	}

	private void proxyRespManage(boolean isFromBack, ResponseEvent responseEvent) throws Exception {
		try {
			Response response = responseEvent.getResponse();
			CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
			LOGGER.debug("ClientTxID = " + responseEvent.getClientTransaction()
                    + " client tx id " + ((ViaHeader) response.getHeader(ViaHeader.NAME)).getBranch()
                    + " CSeq header = " + response.getHeader(CSeqHeader.NAME)
                    + " status code = " + response.getStatusCode());

        	ClientTransaction ct = responseEvent.getClientTransaction();
        	LOGGER.debug("---------: TRX = " + ct);
        	if (ct != null) {
        		ServerTransaction st = (ServerTransaction)ct.getApplicationData();
        		Response newResponse = (Response) response.clone();
        		newResponse.removeFirst(ViaHeader.NAME);

//        		st.setApplicationData(ct);
        		st.sendResponse(newResponse);
        		LOGGER.debug("---------: TRX = " + st);

//        		ct.terminate();
        	} else {
        		Response newResponse = (Response) response.clone();
        		newResponse.removeFirst(ViaHeader.NAME);
        		// Send the retransmission statelessly
        		sipProvider.sendResponse(newResponse);
        	}
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void processDialogTerminated(DialogTerminatedEvent arg0) {
		LOGGER.debug("processDialogTerminated");
	}

	@Override
	public void processIOException(IOExceptionEvent arg0) {
		LOGGER.debug("processIOException");
	}

	@Override
	public void processTimeout(TimeoutEvent arg0) {
		LOGGER.debug("processTimeout");
	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        if (transactionTerminatedEvent.isServerTransaction()) {
        	ServerTransaction st = transactionTerminatedEvent.getServerTransaction();
        	LOGGER.debug("Server tx terminated! " + st);
        } else {
        	ClientTransaction ct = transactionTerminatedEvent.getClientTransaction();
        	LOGGER.debug("Client tx terminated! " + ct);
        }

        SipStackImpl sipStackImpl = (SipStackImpl)sipStack;
		LOGGER.debug("ClientTrxSize : " + sipStackImpl.getClientTransactionTableSize() +
				     ", ServerTrxSizeSize : " + sipStackImpl.getServerTransactionTableSize());
	}

	public void init() {
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");

		//---: https://javadoc.io/doc/javax.sip/jain-sip-ri/latest/index.html
		//---: On class : gov.nist.javax.sip.SipStackImpl
		//---: Online Documents

		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "DMS_WEBRTC_GATEWAY");

		//---: Logger Setting
//		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");
//		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "shootmedebug.txt");
//		properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "shootmelog.txt");

		//---: Thread Setting
//		properties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "30");
//		properties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");

		//---: Connection Timeout Setting
//		properties.setProperty("gov.nist.javax.sip.READ_TIMEOUT ", "-1"); //---: Default value is -1 milliseconds. The client can be as slow as it wants to be.
//		properties.setProperty("gov.nist.javax.sip.CONNECTION_TIMEOUT", "5000"); //---: Default value is 10000 milliseconds.
//		properties.setProperty("gov.nist.javax.sip.NIO_MAX_SOCKET_IDLE_TIME", "30000");   //---: milliseconds
//		properties.setProperty("gov.nist.javax.sip.NIO_BLOCKING_MODE", "NONBLOCKING");

		//---: Connection Processor Setting
		properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());

		try {
			// Create SipStack object
			sipStack = sipFactory.createSipStack(properties);
			LOGGER.debug("sipStack = " + sipStack);
		} catch (PeerUnavailableException e) {
			// could not find
			// gov.nist.jain.protocol.ip.sip.SipStackImpl
			// in the classpath
			e.printStackTrace();
			LOGGER.debug(e.getMessage());
			if (e.getCause() != null)
				e.getCause().printStackTrace();
		}

		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			this.listeningPoint = sipStack.createListeningPoint(wsHost, wsPort, wsTransport);
			this.listeningPointProxy = sipStack.createListeningPoint(gatewayHost, gatewayPort, gatewayTransport);

			sipProvider = sipStack.createSipProvider(listeningPoint);
			sipProvider.addListeningPoint(listeningPointProxy);

			LOGGER.debug("ws provider " + sipProvider);
			sipProvider.addSipListener(this);

			//----:
			/*Iterator listeningPoints = sipStack.getListeningPoints();
			while(listeningPoints.hasNext()) {
				ListeningPointImpl obj = (ListeningPointImpl)listeningPoints.next();
				System.out.println();
			}*/
			//----:

		} catch (Exception ex) {
			LOGGER.debug(ex.getMessage());
			ex.printStackTrace();
		}
	}

	private boolean isRespFromBack(ResponseEventExt ext) {
		return ext.getRemoteIpAddress().equals(sipHost);
	}
	private boolean isReqFromBack(RequestEventExt ext) {
		return ext.getRemoteIpAddress().equals(sipHost);
	}

	public static void main(String args[]) {
		new WebRTCGateway().init();
	}

}
