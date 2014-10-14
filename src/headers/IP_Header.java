package headers;

import java.util.Random;

/********************************************************************
 * IP_Header.java
 *
 * @version Oct 14, 2014
 *******************************************************************/
public class IP_Header {

	/* Length in bytes */
	private final int header_length = 20;
	
	/** Bit string representing this header. */
	private String bits;
	
	public IP_Header() {
		
		bits = new String(new char[header_length * 8]).replace("\0", "0");
		setupHeader();
	}
	
	private void setupHeader() {
		bits = insertData(0, 4, "0100");
		
		String ihl = Integer.toString((header_length / 4));
		bits =  insertData(4, 8, ihl);
		
		Random rand = new Random();
		int id = rand.nextInt();
		bits = insertData(32, 48, Integer.toString(id, 2));
		
		int ttl = 172800;
		insertData(64, 72, Integer.toString(ttl, 2));
		
		int protocol = 17;
		insertData(72, 80, Integer.toString(protocol, 2));
	}
	
	private String insertData(int start, int end, String data) {
		data = String.format("%0" + (end - start) + "s", data);
		return bits.substring(0, start) + data + bits.substring(end);
	}
	
	private void calculateChecksum() {
		// TODO: Do this.
	}
	
	public void setDataSize(int length) {
		length += 20;
		insertData(16, 31, Integer.toString(length, 2));
	}
	
	public void setSource() {
		
	}
	
	public void setAddress() {
		
	}
}
