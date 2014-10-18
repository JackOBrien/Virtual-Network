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
	
	private void calculateChecksum() {
		String[] octets = new String[header_length];
		
		for (int i = 0; i < octets.length; i++) {
			int start = i * 8;
			int end = start + 8;
			
			octets[i] = bits.substring(start, end);
		}
				
		// Set checksum to 0
		octets[10] = "00000000";
		octets[11] = "00000000";
				
		int sum = 0;
		
		for (String octet : octets) {
			sum += Integer.parseInt(octet, 2);
		}
	
		String afterAdding = Integer.toBinaryString(sum);
		
		String highOrder = afterAdding.substring(0, afterAdding.length() - 8);
		afterAdding = afterAdding.substring(highOrder.length());
		
		sum = Integer.parseInt(afterAdding, 2) 
				+ Integer.parseInt(highOrder, 2);
		
		afterAdding = Integer.toBinaryString(sum);
		
		afterAdding = afterAdding.replace('1', '2');
		afterAdding = afterAdding.replace('0', '1');
		afterAdding = afterAdding.replace('2', '0');
						
		insertData(80, 96, Integer.parseInt(afterAdding, 2));
	}
	
	public void setDataSize(int length) {
		length += header_length;
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
	
	public String getBitString() {
		calculateChecksum();
		return bits;
	}
}
