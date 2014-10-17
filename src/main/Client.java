package main;

import headers.IP_Header;
import headers.UDP_Header;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {

	private final int PORT = 1618;
	
	private DatagramSocket clientSocket;
	
	public Client() throws SocketException {
		clientSocket = new DatagramSocket(PORT);
	}
	
	public void sendMessage(String message, InetAddress dstAddr) {
		byte[] data = buildPacket(message, dstAddr);	
		
		DatagramPacket packet = 
				new DatagramPacket(data, data.length, dstAddr, PORT);
		
		try {
			clientSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private byte[] buildPacket(String message, InetAddress dstAddr) {
		IP_Header ipH = new IP_Header();
		UDP_Header udpH = new UDP_Header();
		
		InetAddress localAddress = null;
		
		/* Gets the IP address of the local host. */
		try {
			localAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		// Set the IP addresses
		ipH.setSource(localAddress);
		ipH.setDestination(dstAddr);
		
		// Set the IP header data length field
		ipH.setDataSize(message.length());
		
		// Gets the bit string from the IP header
		String ipBits = ipH.getBitString();
		
		// Set the ports
		udpH.setSrcPort(PORT);
		udpH.setDstPort(PORT);
		
		// Set the UDP header data length field
		udpH.setDataLength(message.length());
		
		// Gets the bit string from the UDP header
		String udpBits = udpH.getBitString();
		
		String packetBits = ipBits + udpBits + message;
		int packetLength = packetBits.length() / 8;
		
		byte[] bytes = new byte[packetLength];
		
		for (int i = 0; i < bytes.length; i++) {
			int start = i * 8;
			int end = start + 8;
			
			String octet = packetBits.substring(start, end);
			
			bytes[i] = (byte) Integer.parseInt(octet, 2);
		}
		
		return bytes;
	}
}
