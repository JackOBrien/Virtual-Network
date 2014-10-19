package headers;

public class UDP_Header {

	private final int header_length = 8;
	
	String bits;
	
	public UDP_Header() {
		bits = new String(new byte[header_length * 8]).replace("\0", "0");
	}
	
	public void setSrcPort(int srcPort) {
		insertData(0, 16, srcPort);
	}
	
	public void setDstPort(int dstPort) {
		insertData(16, 32, dstPort);
	}
	
	public void setDataLength(int length) {
		insertData(32, 48, (length + header_length));
	}
	
	public void calculateChecksum(String ipv4Bits) {
		
		int lenIP = (ipv4Bits.length() / 8); 	// #IPv4 octets
		int lenUDP = (header_length - 2);		// Length of UDP - checksum
		
		// Creates the array of octets for the first three
		// fields (ignores the checksum field)
		String[] octets = new String[lenIP + lenUDP];
		
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
			
			// Handles 16-bit carry correction
			if (sum.length() > 16) {
				String carryStr = sum.substring(0, sum.length() - 16);
				int carry = Integer.parseInt(carryStr, 2);
				
				s = Integer.parseInt(sum.substring(carryStr.length()), 2);
				s += carry;
				
				sum = Integer.toBinaryString(s);
			}
		}
		
		sum = sum.replace('1', '2');
		sum = sum.replace('0', '1');
		sum = sum.replace('2', '0');
						
		System.out.println("UDP Checksum: " + Integer.toHexString(Integer.parseInt(sum, 2)));
		
		insertData(48, 64, Integer.parseInt(sum, 2));
	}
	
	private void insertData(int start, int end, int data) {
		String dataStr = Integer.toString(data, 2);
		int length = end - start;
		int bufferLength = length - dataStr.length();
		String binary = new String(new char[length]).replace("\0", "0");
		binary = binary.substring(0, bufferLength) + dataStr;

		bits = bits.substring(0, start) + binary + bits.substring(end);
	}

	public int getLength() {
		return header_length;
	}
	
	public String getBitString() {
		return bits;
	}
	
}
