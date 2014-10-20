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
	
	/****************************************************************
	 * @param udpBits bit string containing the UDP header data
	 * @param message String of ASCII text containing the message
	 ***************************************************************/
	public void calculateChecksum() {
		String[] octets = new String[header_length];
		
		/* Adds the IP header to the octets array */
		for (int i = 0; i < header_length; i++) {
			int start = i * 8;
			int end = start + 8;
			
			octets[i] = bits.substring(start, end);
		}
				
		// Set checksum to 0
		octets[10] = "00000000";
		octets[11] = "00000000";
		
		String sum = new String(new char[16]).replace("\0", "0");
		
		/* Loops through every pair of octets and adds them to the sum
		 * as a 16 bit value */
		for (int i = 0; i < octets.length; i += 2) {
			String value = "";
			
			// Converts two octets into 16 bit value
			value = octets[i] + octets[i + 1];
						
			// Adds value to the current sum
			int s = Integer.parseInt(sum, 2) + Integer.parseInt(value, 2);
			sum = Integer.toBinaryString(s);
			sum = String.format("%16s", sum).replace(' ', '0');
			
			// Handles 16-bit carry correction
			if (sum.length() > 16) {
				String carryStr = sum.substring(0, sum.length() - 16);
				int carry = Integer.parseInt(carryStr, 2);
				
				s = Integer.parseInt(sum.substring(carryStr.length()), 2);
				s += carry;
				
				sum = Integer.toBinaryString(s);
				sum = String.format("%16s", sum).replace(' ', '0');
			}
		}
		
		sum = sum.replace('1', '2');
		sum = sum.replace('0', '1');
		sum = sum.replace('2', '0');
						
		System.out.println("IP Checksum: " + Integer.toHexString(Integer.parseInt(sum, 2)));
		
		insertData(80, 96, Integer.parseInt(sum, 2));
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
		calculateChecksum();
		return bits;
	}
}
