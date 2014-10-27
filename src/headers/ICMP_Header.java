package headers;

import java.util.Random;

/********************************************************************
 * ICMP_Header.java
 * 
 * @author Jack O'Brien
 * @author Megan Maher
 * @author mccartty
 *
 * @version Oct 27, 2014
 *******************************************************************/
public class ICMP_Header {

	/** Length in bytes */
	private final int header_length = 8;
	
	/** Bit string representing this header */
	private String bits;
	
	/** Fields */
	int type, code;
	
	/****************************************************************
	 * Default constructor 
	 ***************************************************************/
	public ICMP_Header(int t, int c) {
		
		type = t;
		code = c;
		
		bits = new String(new char[header_length * 8]).replace("\0", "0");
		setupHeader();
		
	}
	
	/****************************************************************
	 * 
	 ***************************************************************/
	private void setupHeader() {
		
		//set type & code fields
		insertData(0, 1, type);
		insertData(1, 2, code); 
		
        Random rand = new Random();
        int id = rand.nextInt(65536);
        insertData(4, 6, id);

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
