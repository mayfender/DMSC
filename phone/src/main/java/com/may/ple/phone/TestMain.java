package com.may.ple.phone;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class TestMain {

	public static void main(String args[]) {
		try {

			String host = "192.168.43.100";
			byte[] bytes = { (byte) 0xd1, 0x35, (byte) 0x39, (byte) 0xea, (byte) 0xa2, (byte) 0xd8 };
			DatagramSocket datagramSocket = new DatagramSocket();
			final InetAddress inetAddress = InetAddress.getByName(host);
			final DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, inetAddress, 5060);
			datagramSocket.send(sendPacket);

			datagramSocket.close();

			System.out.println("finish");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
