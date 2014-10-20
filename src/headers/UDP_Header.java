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
	
	public void calculateChecksum(String ipv4Bits, String message) {
		
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
			
//			TODO REMOVE THESE
//			System.out.print("V: " + Integer.parseInt(octets[i], 2) + " " + Integer.parseInt(octets[i+1], 2));
//			System.out.println(" : " + Integer.parseInt((octets[i] + octets[i+1]), 2));
//			System.out.println("Value:\t" + value);

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
			System.out.println("\t\tSum:\t" + sum + " : " + Integer.parseInt(sum, 2));
		}

		sum = sum.replace('1', '2');
		sum = sum.replace('0', '1');
		sum = sum.replace('2', '0');

		int iSum = Integer.parseInt(sum, 2);
		iSum += 20;
		sum = Integer.toBinaryString(iSum);
		
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
