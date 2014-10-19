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
		insertData(32, 40, (length + header_length));
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
		
		int sum = 0;
		
		/* Loops through every pair of octets and adds them to the sum
		 * as a 16 bit value */
		for (int i = 0; i < octets.length; i += 2) {
			
			// Converts two octets into 16 bit value
			String value = octets[i] + octets[i+1];
			
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
						
		insertData(48, 64, Integer.parseInt(afterAdding, 2));
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
