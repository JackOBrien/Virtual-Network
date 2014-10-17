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
		insertData(32, 40, length);
	}
	
	private void calculateChecksum() {
		
		// Creates the array of octets for the first three
		// fields (ignores the checksum field)
		String[] octets = new String[header_length - 2];
		
		for (int i = 0; i < octets.length; i++) {
			int start = i * 8;
			int end = start + 8;
			
			octets[i] = bits.substring(start, end);
		}
		
		int sum = 0;
		
		for (String octet : octets) {
			sum += Integer.parseInt(octet, 2);
		}
	
		insertData(40, 56, sum);
	}
	
	private String insertData(int start, int end, int data) {
		String dataStr = Integer.toString(data, 2);
		int length = end - start;
		int bufferLength = length - dataStr.length();
		String binary = new String(new char[length]).replace("\0", "0");
		binary = binary.substring(0, bufferLength) + dataStr;
		return bits.substring(0, start) + binary + bits.substring(end);
	}

	public String getBitString() {
		calculateChecksum();
		return bits;
	}
	
}
