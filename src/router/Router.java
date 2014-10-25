package router;

import headers.IP_Header;
import headers.UDP_Header;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

/********************************************************************
 * Router.java
 *
 *
 * @author Jack O'Brien
 * @author Megan Maher
 * @author Tyler McCarthy
 * @version Oct 25, 2014
 *******************************************************************/
public class Router {
	
	private final int PORT = 1618;
	
	/** Path to the configuration files. */
	private final String PATH = "config/";
	
	private int router_number;
	
	private HashMap<String, InetAddress> prefixes;
	
	private DatagramSocket routerSocket;
	
	public Router() throws Exception {
		routerSocket = new DatagramSocket(PORT);
		
		readPrefixes();
	}
	
	public void setRouterNumber(int router_number) {
		this.router_number = router_number;
	}
	
	private DatagramPacket receivePacket() throws IOException {
		byte[] buffer = new byte[65535];
		
		DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
		routerSocket.receive(recvPacket);
				
		return recvPacket;
	}
	
	public void handlePacket() throws IOException {
		byte[] data = receivePacket().getData();
		
		// Validates the checksums
		validateChecksumIP(data, 0, 20);
		validateChecksumUDP(data, 20, 28);
		validateChecksumIP(data, 28, 48);
		validateChecksumUDP(data, 28, data.length);
		
		int virtualIP = 28;
		
		int TTL = (int) (data[virtualIP + 8] & 0xFF);
		
		/* Check the TTL */
		if (TTL <= 0) return;
		
		// Decrements the TTL
		TTL--;
		data[virtualIP + 8] = (byte) TTL;
		
		String dest = translateIP(data, virtualIP + 16);
		InetAddress realDstIP = null;
		int prefixLength = 0;
		
		for (String prefix : prefixes.keySet()) {
			if (dest.startsWith(prefix) & prefix.length() > prefixLength) {
				realDstIP = prefixes.get(prefix);
				prefixLength = prefix.length();
			}
		}
		
		if (realDstIP == null) {
			// TODO: Send ICMP message
			return;
		}
		
		
		DatagramPacket sendPkt = new DatagramPacket(data, data.length, 
				realDstIP, PORT);
		
		/* Forwards the packet */
		routerSocket.send(sendPkt);
	}	
	
	private void validateChecksumIP(byte[] data, int start, int end) 
			throws IOException {
		
		int checksumIndex = start + 10;
		
		int storedChecksum = (data[checksumIndex] << 8) | 
							 (data[checksumIndex + 1] & 0xFF);
		int calculatedChecksum = IP_Header.calculateChecksum(data, start, end);
		
		if (storedChecksum != calculatedChecksum) {
			throw new IOException("Bad IP Checksum: Got " + storedChecksum + 
					" Expected " + calculatedChecksum);
		}
	}
	
	private void validateChecksumUDP(byte[] data, int start, int end) 
			throws IOException {
		
		int checksumIndex = start + 6;
		
		int storedChecksum = (data[checksumIndex] << 8) | 
							 (data[checksumIndex + 1] & 0xFF);
		int calculatedChecksum = UDP_Header.calculateChecksum(data, start, end);
		
		if (storedChecksum != calculatedChecksum) {
			throw new IOException("Bad UDP Checksum: Got " + storedChecksum + 
					" Expected " + calculatedChecksum);
		}
	}
	
	private void readPrefixes() throws Exception {
		String path = PATH + "router-" + router_number + ".txt";
		
		ArrayList<InetAddress> ipArr = new ArrayList<InetAddress>();
		
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line;
		
		while ((line = br.readLine()) != null) {
			
			if (!line.startsWith("prefix ")) continue;
			
			String[] splitStr = line.split(" ");
			
			if (splitStr.length != 3) continue;
			
			String key = splitStr[1];
			String[] keySplit = key.split("/");
			
			if (keySplit.length != 2) continue;
			
			int prefixLength = Integer.parseInt(keySplit[1]);
			prefixLength /= 8; // in bytes
			
			String[] ipSplit = keySplit[0].split("\\.");
			
			if (ipSplit.length != 4) continue;
			key = "";
			
			for (int i = 0; i < prefixLength; i++) {
				key += "." + ipSplit[i];
			}
			
			InetAddress value = InetAddress.getByName(splitStr[2]);
			
			prefixes.put(key.substring(1), value);
		}
		
		br.close();
	}
	
	private String translateIP(byte[] data, int start) {
		String ip = "";
		
		for (int i = start; i < 4; i++) {
			ip += "." + Integer.toString((int) (data[i] & 0xFF));
		}
		
		return ip.substring(1);
	}
}
