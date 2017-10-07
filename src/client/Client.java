package client;

import headers.IP_Header;
import headers.UDP_Header;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
		
	private InetAddress realDst;
	
	private InetAddress srcIP;
	
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
	
	public String[] receiveMessage() {
		byte[] buf = new byte[1024];

		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		
		try {
			clientSocket.receive(packet);
		} catch (Exception e) {
			return null;
		}
		
		int protocol = buf[9] & 0xFF;
		
		if (protocol == 1) {
			String[] toReturn = new String[3];
			
			toReturn[0] = Integer.toString((int) buf[20]);
			toReturn[1] = translateIP(buf, 12);
			toReturn[2] = translateIP(buf, 44);
			
			return toReturn;
		}
		
		int data_length = (int) (((buf[2] << 8) | (buf[3] & 0xFF)) & 0xFFFF);
		
		String message = "";
		
		for (int i = 28; i < data_length; i++) {
			message += (char) buf[i];
		}
		
		String[] srcAndMessage = new String[2];
		
		srcAndMessage[0] = translateIP(buf, 12);
		srcAndMessage[1] = message;
		
		return srcAndMessage;
	}
	
	/****************************************************************
	 * Converts the 4 bytes from the given starting index into
	 * IPv4 format as a String.
	 * 
	 * @param data the byte array containing the IPv4 address.
	 * @param start the index of the first byte of the address.
	 * @return String representation of the address.
	 ***************************************************************/
	private String translateIP(byte[] data, int start) {
		String ip = "";
		
		for (int i = 0; i < 4; i++) {
			ip += "." + Integer.toString((int) (data[i + start] & 0xFF));
		}
		
		return ip.substring(1);
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
		
		// Set the IP addresses
		ipH.setSource(srcIP);
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
		
		srcIP = ipArr.get(0);
		realDst = ipArr.get(1);	
	}
	
	public String getTitle() {
		return host_number + ": " + srcIP.getHostAddress();
	}
}
