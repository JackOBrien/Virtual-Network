package headers;

/********************************************************************
 * UDP_Header.java
 *
 * @author Jack O'Brien
 * @author Megan Maher
 * @author Tyler McCarthy
 *
 * @version Oct 20, 2014
 *******************************************************************/
public class UDP_Header {

	/** The UDP header length is 8 bytes. */
	private final int header_length = 8;
	
	/** Bit string representing this header. */
	String bits;
	
	/****************************************************************
	 * Constructor for this UDP_Header. Instantates the bit string
	 * to be 8 bytes worth of zeros.
	 ***************************************************************/
	public UDP_Header() {
		bits = new String(new byte[header_length * 8]).replace("\0", "0");
	}
	
	/****************************************************************
	 * Sets the source port.
	 * 
	 * @param srcPort the source port number to be inserted.
	 ***************************************************************/
	public void setSrcPort(int srcPort) {
		insertData(0, 16, srcPort);
	}
	
	/****************************************************************
	 * Sets the destination port.
	 * 
	 * @param dstPort the destination port number to be inserted.
	 ***************************************************************/
	public void setDstPort(int dstPort) {
		insertData(16, 32, dstPort);
	}
	
	/****************************************************************
	 * Sets the UDP length field.
	 * 
	 * @param length number of bytes in the data field.
	 ***************************************************************/
	public void setDataLength(int length) {
		insertData(32, 48, (length + header_length));
	}
	
	/****************************************************************
	 * Calculates and inserts the UDP checksum.
	 * 
	 * @param ipv4Bits the bit string representing the IPv4 pseudo
	 * header not including the length field.
	 * @param message the plain text message.
	 ***************************************************************/
	public void calculateChecksum(String ipv4Bits, String message) {
		
		// Add length to IPv4 Pseudo Header
		ipv4Bits += bits.substring(32, 48);
		
		int lenIP = (ipv4Bits.length() / 8); 	// #IPv4 octets
		int lenUDP = (header_length - 2);		// Length of UDP - checksum
		int lenMsg = message.length();
		
		/* Checks for a message of odd length */
		if (message.length() % 2 != 0) {
			lenMsg++;
		}
		
		// Creates the array of octets for the first three
		// fields (ignores the checksum field)
		String[] octets = new String[lenIP + lenUDP + lenMsg];
		
		/* Adds IPv4 pseudo header bits to octet array */
		for (int i = 0; i < lenIP; i++) {
			int start = i * 8;
			int end = start + 8;
			
			octets[i] = ipv4Bits.substring(start, end);
		}
		
		/* Adds UDP bits to octet array */
		for (int i = 0; i < lenUDP; i++) {
			int start = i * 8;
			int end = start + 8;
						
			octets[i + lenIP] = bits.substring(start, end);
		}
		
		String messageBits = "";
		
		/* Convert message into bit string */
		for (int i = 0; i < message.length(); i++) {
			int ascii = (int) message.charAt(i);
			String binary = Integer.toBinaryString(ascii);
			binary = String.format("%8s", binary).replace(' ', '0');
			messageBits += binary;
		}		
		
		/* Pads end of message with 0's if odd */
		if (message.length() != lenMsg) {
			messageBits += "00000000";
		}
		
		/* Adds message bits into octet array */
		for (int i = 0; i < lenMsg; i++) {
			int start = i * 8;
			int end = start + 8;
			
			octets[i + lenIP + lenUDP] = messageBits.substring(start, end);
		}
		
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
		
		insertData(48, 64, Integer.parseInt(sum, 2));
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
	 * @return the length in bytes of this header.
	 ***************************************************************/
	public int getLength() {
		return header_length;
	}
	
	/****************************************************************
	 * @return the bit string representation of this header.
	 ***************************************************************/
	public String getBitString() {
		return bits;
	}
	
}
