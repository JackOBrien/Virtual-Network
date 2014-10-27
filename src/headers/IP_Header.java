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
 * @version Oct 20, 2014
 *******************************************************************/
public class IP_Header {

	/** Length in bytes */
	private final int header_length = 20;
		
	/** Bit string representing this header. */
	private String bits;
	
	/****************************************************************
	 * Default constructor for IP_Header. Sets all the default values
	 * into the bit string representing this header.
	 ***************************************************************/
	public IP_Header() {
		bits = new String(new char[header_length * 8]).replace("\0", "0");
		setupHeader();
	}
	
	/****************************************************************
	 * Sets all the default values of the IPv4 Header.
	 * Version: 4
	 * IHL: 20
	 * ID: Random int between 0 and 2^16 - 1
	 * TTL: 64
	 * Protocol: 17 (UDP)
	 ***************************************************************/
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
	
	/****************************************************************
	 * Inserts the given data into the bit string at the given location.
	 * 
	 * @param start Starting index to be inserted (inclusive).
	 * @param end Ending index to be inserted (exclusive).
	 * @param data the data to be inserted.
	 ***************************************************************/
	private void insertData(int start, int end, int data) {
		
		// Converts data to binary string
		String dataStr = Integer.toBinaryString(data);
		int length = end - start;
		
		// Pads the binary string with 0's to fit in the given range
		int bufferLength = (length - dataStr.length());
		String binary = new String(new char[length]).replace("\0", "0");
		binary = binary.substring(0, bufferLength) + dataStr;
		
		// Adds the binary string of data into the main bit string at
		// the given location
		bits = bits.substring(0, start) + binary + bits.substring(end);
	}
	
	/****************************************************************
	 * Calculates the IPv4 checksum.
	 ***************************************************************/
	public void calculateChecksum() {
		int checksum = calculateChecksum(bits);
		insertData(80, 96, checksum);
	}
	
	/****************************************************************
	 * Sets the length field of the IPv4 header. Adds the header length
	 * the the given length.
	 * 
	 * @param length Number of bytes that follow the IPv4 header.
	 ***************************************************************/
	public void setDataSize(int length) {
		length += header_length;
		insertData(16, 32, length);
	}
	
	/****************************************************************
	 * Inserts the given IPv4 address into the Source IP field.
	 * 
	 * @param srcIP the source IPv4 address to be inserted.
	 ***************************************************************/
	public void setSource(InetAddress srcIP) {
		String[] ipStrArr = srcIP.getHostAddress().split("\\.");
		
		// First index of the source IP field
		int srcIndex = 96;
		
		/* Adds all elements of the IP to the bit string */
		for (int i = 0; i < ipStrArr.length; i++) {
			int start = srcIndex + (i * 8);
			int end = start + 8;
			
			insertData(start, end, Integer.parseInt(ipStrArr[i]));
		}
		
	}
	
	/****************************************************************
	 * Inserts the given IPv4 address into the Destination IP field.
	 * 
	 * @param dstIP the destination IPv4 address to be inserted.
	 ***************************************************************/
	public void setDestination(InetAddress dstIP) {
		String[] ipStrArr = dstIP.getHostAddress().split("\\.");

		// First index of the destination IP field
		int srcIndex = 128;

		/* Adds all elements of the IP to the bit string */
		for (int i = 0; i < ipStrArr.length; i++) {
			int start = srcIndex + (i * 8);
			int end = start + 8;

			insertData(start, end, Integer.parseInt(ipStrArr[i]));
		}
	}
	
	/****************************************************************
	 * Returns a bit string of most of the IPv4 Pseudo Header for 
	 * the UDP checksum calculation. The length field is NOT included.
	 * 
	 * @return Bit string of the pseudo header with out the length field.
	 ***************************************************************/
	public String getPseudoHeader() {
		
		// Adds the source then destination addresses
		String pseudoBits = bits.substring(96, 160);
		
		// Adds protocol padded by zeros
		pseudoBits += "00000000" + bits.substring(72, 80);
		
		return pseudoBits;
	}
	
	/****************************************************************
	 * Calculates the checksum and returns the bit string which
	 * represents this header.
	 * 
	 * @return the bit string representing this header.
	 ***************************************************************/
	public String getBitString() {
		calculateChecksum();
		return bits;
	}
	
	private static String bytesToBitString(byte[] data, int start, int end) {
		
		String bitStr = "";
		
		for (int i = start; i < end; i++) {
			String s = Integer.toBinaryString(data[i] & 0xFF);
			s = String.format("%8s", s).replace(' ', '0');
			bitStr += s;
		}
		
		return bitStr;
	}
	
	public static int calculateChecksum(byte[] data, int start, int end) {
		String bitStr = bytesToBitString(data, start, end);
		
		return calculateChecksum(bitStr);
	}
	
	public static int calculateChecksum(String bitString) {
		
		// Parses the header length in bytes
		int header_length = 
				Integer.parseInt(bitString.substring(4, 8), 2);
		header_length *= 4;
		
		String[] octets = new String[header_length];
		
		/* Adds the IP header to the octets array */
		for (int i = 0; i < header_length; i++) {
			int start = i * 8;
			int end = start + 8;
			
			octets[i] = bitString.substring(start, end);
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
		
		// Flips the bits 
		sum = sum.replace('1', '2');
		sum = sum.replace('0', '1');
		sum = sum.replace('2', '0');
										
		return Integer.parseInt(sum, 2);
	}
}
