package headers;

import java.net.InetAddress;
import java.util.Random;

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
	
	/** Bit string representing this header. */
	private String bits;
	
	public IP_Header() {
		bits = new String(new char[header_length * 8]).replace("\0", "0");
		setupHeader();
	}
	
	private void setupHeader() {
		int version = 4;
		bits = insertData(0, 4, version);
		
		bits =  insertData(4, 8, (header_length / 4));
		
		Random rand = new Random();
		int id = rand.nextInt(65536);
		bits = insertData(32, 48, id);
		
		int ttl = 64;
		insertData(64, 72, ttl);
		
		int protocol = 17;
		insertData(72, 80, protocol);
	}
	
	private String insertData(int start, int end, int data) {
		String dataStr = Integer.toString(data, 2);
		int length = end - start;
		int bufferLength = (length - dataStr.length());
		String binary = new String(new char[length]).replace("\0", "0");
		binary = binary.substring(0, bufferLength) + dataStr;
		return bits.substring(0, start) + binary + bits.substring(end);
	}
	
	private void calculateChecksum() {
		String[] octets = new String[header_length];
		
		for (int i = 0; i < octets.length; i++) {
			int start = i * 8;
			int end = start + 8;
			
			octets[i] = bits.substring(start, end);
		}
				
		// Set checksum to 0
		octets[6] = "00000000";
		octets[7] = "00000000";
		
		int sum = 0;
		
		for (String octet : octets) {
			sum += Integer.parseInt(octet, 2);
		}
	
		insertData(79, 95, sum);
	}
	
	public void setDataSize(int length) {
		length += header_length;
		insertData(16, 31, length);
	}
	
	public void setSource(InetAddress srcIP) {
		String[] ipStrAtt = srcIP.getHostAddress().split(".");
		
		// First index of the source IP field
		int srcIndex = 96;
		
		/* Adds all elements of the IP to the bit string */
		for (int i = 0; i < ipStrAtt.length; i++) {
			int start = srcIndex + (i * 8);
			int end = start + 8;
			
			insertData(start, end, Integer.parseInt(ipStrAtt[i], 2));
		}
	}
	
	public void setDestination(InetAddress dstIP) {
		String[] ipStrAtt = dstIP.getHostAddress().split(".");

		// First index of the destination IP field
		int srcIndex = 128;

		/* Adds all elements of the IP to the bit string */
		for (int i = 0; i < ipStrAtt.length; i++) {
			int start = srcIndex + (i * 8);
			int end = start + 8;

			insertData(start, end, Integer.parseInt(ipStrAtt[i], 2));
		}
	}
	
	public String getBitString() {
		calculateChecksum();
		return bits;
	}
}
