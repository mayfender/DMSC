package com.may.ple.phone;

public class เราไม่ทิ้งกัน {
	private static String dateFormat = "%1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS";

	public static void main(String[] args) throws Exception {
		String sdp = "v=0\r\n" +
				"o=root 846439391 846439391 IN IP4 192.168.43.100\r\n" +
				"s=Asterisk PBX 11.25.3\r\n" +
				"c=IN IP4 192.168.43.100\r\n" +
				"t=0 0\r\n" +
				"m=audio 17898 UDP/TLS/RTP/SAVPF 0 8 111 126\r\n" +
				"a=rtpmap:0 PCMU/8000\r\n" +
				"a=rtpmap:8 PCMA/8000\r\n" +
				"a=rtpmap:111 opus/48000/2\r\n" +
				"a=maxptime:60\r\n" +
				"a=fmtp:111 maxplaybackrate=16000; stereo=0; sprop-stereo=0; useinbandfec=0\r\n" +
				"a=rtpmap:126 telephone-event/8000\r\n" +
				"a=fmtp:126 0-16\r\n" +
				"a=ptime:20\r\n" +
				"a=ice-ufrag:34becf1956b3c9fb3bfe5736004669a8\r\n" +
				"a=ice-pwd:4a968dc8372f24c826e9463942ad80fa\r\n" +
				"a=candidate:Hc0a82b64 1 UDP 2130706431 192.168.43.100 17898 typ host\r\n" +
				"a=candidate:Hc0a82bfa 1 UDP 2130706431 192.168.43.250 17898 typ host\r\n" +
				"a=candidate:Hc0a82b64 2 UDP 2130706430 192.168.43.100 17899 typ host\r\n" +
				"a=candidate:Hc0a82bfa 2 UDP 2130706430 192.168.43.250 17899 typ host\r\n" +
				"a=connection:new\r\n" +
				"a=setup:active\r\n" +
				"a=fingerprint:SHA-256 06:5E:84:54:C7:E1:DB:51:4F:92:89:F4:B6:50:37:F3:68:C4:03:ED:0A:D7:F4:36:81:A5:9E:D5:B3:9C:3E:6A\r\n" +
				"a=sendrecv\r\n" +
				"a=rtcp-mux\r\n" +
				"a=rtcp:61795 IN IP4 127.0.0.1";

		int startIndex = sdp.indexOf("m=audio");
		startIndex = startIndex + "m=audio".length();

		String sdp2 = sdp.substring(startIndex).trim();
		int endIndex = sdp2.indexOf(" ");

		System.out.println(sdp2.substring(0, endIndex));

//		System.out.println(sdp.substring(s, s + 10));
	}
}
