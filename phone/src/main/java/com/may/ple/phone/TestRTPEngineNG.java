package com.may.ple.phone;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;

public class TestRTPEngineNG {
	private static final Logger LOGGER = Logger.getLogger(TestRTPEngineNG.class.getName());

	public static void main(String[] args) throws Exception {
		String callId = "6cj7fn2bbadrmh50h1pm";
		String fromTag = "914766023544";
		String toTag = "o8o22o9e7s";
		String command = "offer";
		String sdp = "v=0\r\n" +
				"o=- 6424352541988584888 2 IN IP4 127.0.0.1\r\n" +
				"s=-\r\n" +
				"t=0 0\r\n" +
				"a=group:BUNDLE 0\r\n" +
				"a=extmap-allow-mixed\r\n" +
				"a=msid-semantic: WMS tMGxh5RJBSZ9xBzxAxvmG4F2yzDUADmKnQQT\r\n" +
				"m=audio 49105 UDP/TLS/RTP/SAVPF 111 63 103 104 9 0 8 106 105 13 110 112 113 126\r\n" +
				"c=IN IP4 49.237.14.228\r\n" +
				"a=rtcp:4869 IN IP4 49.237.14.228\r\n" +
				"a=candidate:413598511 1 udp 2113937151 579d70c1-2da2-4816-a42e-71af2203b79c.local 57459 typ host generation 0 network-cost 999\r\n" +
				"a=candidate:413598511 2 udp 2113937150 579d70c1-2da2-4816-a42e-71af2203b79c.local 57461 typ host generation 0 network-cost 999\r\n" +
				"a=candidate:3895772373 1 udp 1677729535 49.237.14.228 49105 typ srflx raddr 0.0.0.0 rport 0 generation 0 network-cost 999\r\n" +
				"a=candidate:3895772373 2 udp 1677729534 49.237.14.228 4869 typ srflx raddr 0.0.0.0 rport 0 generation 0 network-cost 999\r\n" +
				"a=ice-ufrag:efWP\r\n" +
				"a=ice-pwd:CGTdnvQB+hcDojJf7O5vA3UE\r\n" +
				"a=ice-options:trickle\r\n" +
				"a=fingerprint:sha-256 E7:5C:B1:49:24:7D:38:35:C2:4A:8E:59:CD:49:9C:DF:73:0D:ED:7B:09:58:97:65:28:EC:ED:36:7B:FA:9D:FC\r\n" +
				"a=setup:actpass\r\n" +
				"a=mid:0\r\n" +
				"a=extmap:1 urn:ietf:params:rtp-hdrext:ssrc-audio-level\r\n" +
				"a=extmap:2 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time\r\n" +
				"a=extmap:3 http://www.ietf.org/id/draft-holmer-rmcat-transport-wide-cc-extensions-01\r\n" +
				"a=extmap:4 urn:ietf:params:rtp-hdrext:sdes:mid\r\n" +
				"a=sendrecv\r\n" +
				"a=msid:tMGxh5RJBSZ9xBzxAxvmG4F2yzDUADmKnQQT 7020dc6a-50fa-4cf8-93df-42b5c6dd6076\r\n" +
				"a=rtcp-mux\r\n" +
				"a=rtpmap:111 opus/48000/2\r\n" +
				"a=rtcp-fb:111 transport-cc\r\n" +
				"a=fmtp:111 minptime=10;useinbandfec=1\r\n" +
				"a=rtpmap:63 red/48000/2\r\n" +
				"a=fmtp:63 111/111\r\n" +
				"a=rtpmap:103 ISAC/16000\r\n" +
				"a=rtpmap:104 ISAC/32000\r\n" +
				"a=rtpmap:9 G722/8000\r\n" +
				"a=rtpmap:0 PCMU/8000\r\n" +
				"a=rtpmap:8 PCMA/8000\r\n" +
				"a=rtpmap:106 CN/32000\r\n" +
				"a=rtpmap:105 CN/16000\r\n" +
				"a=rtpmap:13 CN/8000\r\n" +
				"a=rtpmap:110 telephone-event/48000\r\n" +
				"a=rtpmap:112 telephone-event/32000\r\n" +
				"a=rtpmap:113 telephone-event/16000\r\n" +
				"a=rtpmap:126 telephone-event/8000\r\n" +
				"a=ssrc:3701702615 cname:guvIjxXzJVh2PTNC\r\n" +
				"a=ssrc:3701702615 msid:tMGxh5RJBSZ9xBzxAxvmG4F2yzDUADmKnQQT 7020dc6a-50fa-4cf8-93df-42b5c6dd6076";

		/*String sdp = "v=0\r\n" +
				"o=100 0 0 IN IP4 192.168.43.238\r\n" +
				"s=-\r\n" +
				"c=IN IP4 192.168.43.238\r\n" +
				"t=0 0\r\n" +
				"m=audio 4070 RTP/AVP 0 8\r\n" +
				"a=rtpmap:0 PCMU/8000\r\n" +
				"a=rtpmap:8 PCMA/8000";*/

		Map<String, String> params = new HashMap<>();
		params.put("command", "offer");
		params.put("sdp", sdp);
		params.put("callId", callId);
		params.put("fromTag", fromTag);
		params.put("toTag", "");

//		Map<String, Object> result = manage(params);
//		System.out.println(result);
	}

	//public static Map<String, Object> manage(String command, String sdp, String callId, String fromTag, String toTag, String viaBranch) throws Exception {
	public static Map<String, Object> manage(Map<String, String> params, List<String> flags) throws Exception {
		try {
			//---: Parameters
			String command = params.get("command");
			String sdp = params.get("sdp");
			String callId = params.get("callId");
			String fromTag = params.get("fromTag");
			String toTag = params.get("toTag");
			String viaBranch = params.get("viaBranch");
			String receivedFrom = params.get("receivedFrom");
			String transportPro = params.get("transportPro");

			//---: [1]
			String commStr;
			if(command.equals("offer") || command.equals("answer")) {
				commStr = getCommandOfferAnswer(command, sdp, callId, fromTag, toTag, receivedFrom, transportPro, flags);
			} else if(command.equals("delete")) {
				commStr = getCommandDelete(callId, fromTag, toTag);
			} else {
				throw new Exception("Command not found.");
			}

			//---: [2]
			return sendCommand(commStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private static Map<String, Object> sendCommand(String commStr) throws Exception {
		byte[] cmm = commStr.getBytes();
		int port = 2223;

		try (DatagramSocket socket = new DatagramSocket();){
			InetAddress address = InetAddress.getByName("192.168.43.200");
			DatagramPacket packet = new DatagramPacket(cmm, cmm.length, address, port);
			socket.send(packet);
			socket.setSoTimeout(3000);

			//---: Receive server return back.
			byte[] receiveData = new byte[5120];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			socket.receive(receivePacket);
			String sentence = new String( receivePacket.getData(), 0, receivePacket.getLength());

			sentence = sentence.substring(sentence.indexOf(" ")).trim();
			Map<String, Object> result = new Bencode().decode(sentence.getBytes(), Type.DICTIONARY);
			LOGGER.debug("######### RTPEngine result : " + result);
			if(!result.get("result").toString().equals("ok")) {
				throw new Exception("RTPEngine Error !!!!");
			} else {
				LOGGER.debug("----------: Success.");
			}
			return result;
		} catch (Exception e) {
			throw e;
		}
	}

	private static String getCommandOfferAnswer(String command, String sdp, String callId,
			String fromTag, String toTag, String receivedFrom, String transportPro, List<String> flagParams) {
//		String transportPro;
		String ice;
		List<String> receivedFromVal = new ArrayList<>();
		receivedFromVal.add("IP4");
		receivedFromVal.add(receivedFrom);

//		transportPro = "RTP/SAVPF";
		if(command.equals("offer")) {
//			transportPro = "RTP/AVP";
//			transportPro = "RTP/SAVPF";
//			ice = "remove";
		} else {
//			transportPro = "RTP/AVPF";
//			transportPro = "RTP/SAVPF";
//			ice = "force";
		}

		Bencode bencode = new Bencode();
		byte[] encoded = bencode.encode(new HashMap<Object, Object>() {{
			put("command", command);
			put("call-id", callId);
			put("from-tag", fromTag);
			put("sdp", sdp);
			put("transport protocol", transportPro);

			if(StringUtils.isNotBlank(toTag)) {
				put("to-tag", toTag);
			}
			if(StringUtils.isNotBlank(receivedFrom)) {
				put("received-from", receivedFromVal);
			}

			List<String> replaces = new ArrayList<>();
			List<String> muxs = new ArrayList<>();
			List<String> flags = new ArrayList<>();

			if(flagParams != null) {
				flags.addAll(flagParams);
			}

			if(command.equals("offer")) {
				//---: sip -> webrtc
				put("ICE", "force");
				muxs.add("offer");
				muxs.add("require");
				flags.add("trickle-ICE");
				flags.add("generate-mid");
				flags.add("no-rtcp-attribute");
				flags.add("SDES-off");

				//---: webrtc -> sip
				/*put("ICE", "remove");
				muxs.add("demux");
				flags.add("SDES-off");
				flags.add("no-rtcp-attribute");*/
			} else if(command.equals("answer")) {

			}

			if(muxs.size() > 0) {
				put("rtcp-mux", muxs);
			}
			if(flags.size() > 0) {
				put("flags", flags);
			}
			if(replaces.size() > 0) {
				put("replace", replaces);
			}
		}});

		return new Random().nextInt() + " " + new String(encoded, bencode.getCharset());
	}

	private static String getCommandDelete(String callId, String fromTag, String toTag) {
		Bencode bencode = new Bencode();
		byte[] encoded = bencode.encode(new HashMap<Object, Object>() {{
			put("command", "delete");
			put("call-id", callId);
			put("from-tag", fromTag);
			put("to-tag", toTag == null ? "" : toTag);
		}});

		return new Random().nextInt() + " " + new String(encoded, bencode.getCharset());
	}

}
