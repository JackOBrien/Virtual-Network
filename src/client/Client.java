package client;

import headers.IP_Header;
import headers.UDP_Header;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/********************************************************************
 * CIS 457-10 Project 4
 * 
 * Logic for the client on our virtual network.
 *
 * @author Jack O'Brien
 * @author Megan Maher
 * @author Tyler McCarthy
 *
 * @version Oct 24, 2014
 *******************************************************************/
public class Client {

	/** The port to send the datagram packet on is 1618. */
	private final int PORT = 1618;
	
	/** The port used in the UDP header created by this
	 * client is 4529. */
	private final int VIRTUAL_PORT = 4529;
	
	/** Path to the configuration files. */
	private final String PATH = "config/";
	
	private int host_number;
	
	private InetAddress srcAddr;
	
	private InetAddress realDst;
	
	/** The socket used by this client. */
	private DatagramSocket clientSocket;
	
	/****************************************************************
	 * Constructor for this client. Instantiates the socket.
	 * 
	 * @throws SocketException if the socket is unable to be created.
	 * Most likely the port is already in use.
	 ***************************************************************/
	public Client() throws SocketException {		
		clientSocket = new DatagramSocket(PORT);
	}
	
	public void setHostNumber(int host_number) throws Exception {
		this.host_number = host_number;
		
		readConfigFile();
	}
	
	/****************************************************************
	 * Sends the given message to the IPv4 address given by the
	 * configuration file. The message is encapsulated with an 
	 * IPv4 and UDP layer.
	 * 
	 * @param message plain text message to send.
	 * @param dstAddr destination IPv4 address.
	 ***************************************************************/
	public void sendMessage(String message, InetAddress dstAddr) {
		byte[] data = buildPacket(message, dstAddr);	
		
		DatagramPacket packet = 
				new DatagramPacket(data, data.length, realDst, PORT);
		
		try {
			clientSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/****************************************************************
	 * Encapsulates the data to be sent to the given address.
	 * 
	 * @param message plain text message to send.
	 * @param dstAddr destination IPv4 address.
	 * @return byte array representing the constructed packet.
	 ***************************************************************/
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
		ipH.setDataSize(message.length() + udpH.getLength());
		
		// Set the ports
		udpH.setSrcPort(VIRTUAL_PORT);
		udpH.setDstPort(VIRTUAL_PORT);
		
		// Set the UDP header data length field
		udpH.setDataLength(message.length());
		
		// Calculates and sets the UDP header checksum
		udpH.calculateChecksum(ipH.getPseudoHeader(), message);
		
		// Gets the bit string for the UDP header
		String udpBits = udpH.getBitString();
		
		// Gets the bit string from the IP header
		String ipBits = ipH.getBitString();
		
		byte[] messageData = message.getBytes();
		
		String packetBits = ipBits + udpBits;
		int packetLength = packetBits.length() / 8;
		
		byte[] bytes = new byte[packetLength + messageData.length];
		
		/* Converts the bit strings into bytes and adds them
		 * to the byte array. */
		for (int i = 0; i < packetLength; i++) {
			int start = i * 8;
			int end = start + 8;
			
			String octet = packetBits.substring(start, end);
			
			bytes[i] = (byte) Integer.parseInt(octet, 2);
		}
		
		/* Adds the bytes of the message to the byte array. */
		for (int i = packetLength; i < bytes.length; i++) {
			bytes[i] = messageData[i-packetLength];
		}
		
		return bytes;
	}
	
	private void readConfigFile() throws Exception {
		String path = PATH + "host-" + host_number + ".txt";
		
		ArrayList<InetAddress> ipArr = new ArrayList<InetAddress>();
		
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line;
		
		while ((line = br.readLine()) != null) {
			String regex = "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b";
			
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(line);
			
			/* Checks if an IP has been found. */
			if (matcher.find()) {
				String ipStr = matcher.group(0);
				InetAddress ip = InetAddress.getByName(ipStr);
				ipArr.add(ip);
			}
		}
		
		br.close();
		
		if (ipArr.size() < 2) {
			throw new Exception("Improper configuration file format.");
		}
		
		srcAddr = ipArr.get(0);
		realDst = ipArr.get(1);		
	}
}
