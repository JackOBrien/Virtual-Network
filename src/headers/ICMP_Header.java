package headers;

/********************************************************************
 * ICMP_Header.java
 * 
 * @author Jack O'Brien
 * @author Megan Maher
 * @author Tyler McCarthy
 *
 * @version Oct 27, 2014
 *******************************************************************/
public class ICMP_Header extends Header {

	/** Length in bytes */
	private final int header_length = 8;
	
	/** Bit string representing this header */
	private String bits;
	
	private int type;
		
	public static final int UNREACHABLE = 3;
	
	public static final int TIME_EXCEEDED = 11;
	
	
	/****************************************************************
	 * Default constructor for ICMP_Header
	 * 
	 * @param t
	 ***************************************************************/
	public ICMP_Header(int t) {
		type = t;
		
		bits = new String(new char[header_length * 8]).replace("\0", "0");
		
		setupHeader();
	}
	
	private void setupHeader() {
		
		// Set type and code fields
		insertData(0, 8, type);
		
        if (type == UNREACHABLE) {
        	setupUnreachableHeader();
        }
        
        if (type == TIME_EXCEEDED) {
        	setupTimeHeader();
        }

//        insertData(16, 32, calculateChecksum(bits));
	}
	
	private void setupUnreachableHeader() {
		int code = 7;
		
		insertData(8, 16, code); 

	}
	
	private void setupTimeHeader() {
		int code = 0;
		
		insertData(8, 16, code); 

	}
	
	public byte[] getBytes(byte[] ipAndUDP) {
		int length = bits.length() / 8;
		
		byte[] bytes = new byte[length + ipAndUDP.length];
		
		/* Converts the bit strings into bytes and adds them
		 * to the byte array. */
		for (int i = 0; i < length; i++) {
			int start = i * 8;
			int end = start + 8;
			
			String octet = bits.substring(start, end);
			
			bytes[i] = (byte) Integer.parseInt(octet, 2);
		}
		
		for (int i = length; i < bytes.length; i++) {
			bytes[i] = ipAndUDP[i - length];
		}
		
		insertData(16, 32, calculateChecksum(bytes, 0, bytes.length));
		
		return bytes;
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
}
