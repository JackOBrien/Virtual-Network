package headers;

import java.net.InetAddress;
import java.util.Random;

import com.sun.org.apache.xpath.internal.compiler.PsuedoNames;

/********************************************************************
 * IP_Header.java
 *
 * @author Jack O'Brien
 * @author Megan Maher
 * @author Tyler McCarthy
 *
 * @version Oct 14, 2014
 *******************************************************************/
public class IP_Header {

	/** Length in bytes */
	private final int header_length = 20;
	
	private int totalLength;
	
	/** Bit string representing this header. */
	private String bits;
	
	public IP_Header() {
		bits = new String(new char[header_length * 8]).replace("\0", "0");
		totalLength = header_length;
		setupHeader();
	}
	
	private void setupHeader() {
		int version = 4;
		insertData(0, 4, version);
		
		insertData(4, 8, (header_length / 4));
		
		Random rand = new Random();
		int id = rand.nextInt(65536);
		insertData(32, 48, id);
		
		int ttl = 64;
		insertData(64, 72, ttl);
		
		int protocol = 17;
		insertData(72, 80, protocol);
	}
	
	private void insertData(int start, int end, int data) {
		String dataStr = Integer.toString(data, 2);
		int length = end - start;
		int bufferLength = (length - dataStr.length());
		String binary = new String(new char[length]).replace("\0", "0");
		binary = binary.substring(0, bufferLength) + dataStr;
		bits = bits.substring(0, start) + binary + bits.substring(end);
	}
	
	/****************************************************************
	 * @param udpBits bit string containing the UDP header data
	 * @param message String of ASCII text containing the message
	 ***************************************************************/
	public void calculateChecksum(String udpBits, String message) {
		String[] octets = new String[totalLength];
		
		/* Adds the IP header to the octets array */
		for (int i = 0; i < header_length; i++) {
			int start = i * 8;
			int end = start + 8;
			
			octets[i] = bits.substring(start, end);
		}
				
		// Set checksum to 0
		octets[10] = "00000000";
		octets[11] = "00000000";
				
		int udpLength = (udpBits.length() / 8); // length in bytes
		
		/* Adds the UDP header to the octets array */
		for (int i = 0; i < udpLength; i++) {
			int start = i * 8;
			int end = start + 8;
			
			octets[i + header_length] = udpBits.substring(start, end);
		}
		
		String msgBits = "";
		
		/* Convert message string to bit string */
		for (int i = 0; i < message.length(); i++) {
			int ascii = (int) message.charAt(i);
			String binary = Integer.toBinaryString(ascii);
			binary = String.format("%8s", binary).replace(' ', '0');
			msgBits += binary;
		}
				
		int currentIndex = (header_length + udpLength);
		
		/* Adds the message data to the octets array */
		for (int i = currentIndex; i < octets.length; i++) {
			int start = (i - currentIndex) * 8;
			int end = start + 8;
			
			octets[i] = msgBits.substring(start, end);
		}
		
		int sum = 0;
		
		/* Loops through every pair of octets and adds them to the sum
		 * as a 16 bit value */
		for (int i = 0; i < octets.length; i += 2) {
			String value = "";
			
			/* Checks for odd number of octets */
			if ((i + 1) < octets.length) {
				
				// Converts two octets into 16 bit value
				value = octets[i] + octets[i + 1];
			} else {
				value = octets[i];
			}
			
			System.out.println(value);
			
			// Adds value to the current sum
			sum += Integer.parseInt(value, 2);
		}
	
		String afterAdding = Integer.toBinaryString(sum);
		
		if (afterAdding.length() > 16) {
			String highOrder = 
					afterAdding.substring(0, afterAdding.length() - 16);
			afterAdding = afterAdding.substring(highOrder.length());
			
			sum = Integer.parseInt(afterAdding, 2) 
					+ Integer.parseInt(highOrder, 2);
			
			afterAdding = Integer.toBinaryString(sum);
		}
		
		afterAdding = afterAdding.replace('1', '2');
		afterAdding = afterAdding.replace('0', '1');
		afterAdding = afterAdding.replace('2', '0');
						
		System.out.println("IP Checksum: " + Integer.toHexString(Integer.parseInt(afterAdding, 2)));
		
		insertData(80, 96, Integer.parseInt(afterAdding, 2));
	}
	
	public void setDataSize(int length) {
		length += header_length;
		totalLength = length;
		insertData(16, 32, length);
	}
	
	public void setSource(InetAddress srcIP) {
		String[] ipStrAtt = srcIP.getHostAddress().split("\\.");
		
		// First index of the source IP field
		int srcIndex = 96;
		
		/* Adds all elements of the IP to the bit string */
		for (int i = 0; i < ipStrAtt.length; i++) {
			int start = srcIndex + (i * 8);
			int end = start + 8;
			
			insertData(start, end, Integer.parseInt(ipStrAtt[i]));
		}
		
	}
	
	public void setDestination(InetAddress dstIP) {
		String[] ipStrAtt = dstIP.getHostAddress().split("\\.");

		// First index of the destination IP field
		int srcIndex = 128;

		/* Adds all elements of the IP to the bit string */
		for (int i = 0; i < ipStrAtt.length; i++) {
			int start = srcIndex + (i * 8);
			int end = start + 8;

			insertData(start, end, Integer.parseInt(ipStrAtt[i]));
		}
	}
	
	public String getPseudoHeader() {
		
		// Adds the source then destination addresses
		String pseudoBits = bits.substring(96, 160);
		
		// Adds protocol padded by zeros
		pseudoBits += "00000000" + bits.substring(72, 80);
		
		// Adds the length
		pseudoBits += bits.substring(16, 32);
		
		return pseudoBits;
	}
	
	public String getBitString() {
		return bits;
	}
}
