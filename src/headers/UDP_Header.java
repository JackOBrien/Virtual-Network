package headers;

public class UDP_Header {

	private final int header_length = 8;
	
	String bin;
	
	public UDP_Header() {
		bin = new String(new byte[header_length * 8]).replace("\0", "0");
	}
	
	public void setSrcPort() {
		
	}
	
	public void setDstPort() {
		
	}
	
}
