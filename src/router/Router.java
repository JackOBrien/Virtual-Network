package router;

import headers.IP_Header;
import headers.UDP_Header;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/********************************************************************
 * Router.java
 *
 *
 * @author Jack O'Brien
 * @author Megan Maher
 * @author Tyler McCarthy
 * @version Oct 24, 2014
 *******************************************************************/
public class Router {
	
	private final int PORT = 1618;
	
	/** Path to the configuration files. */
	private final String PATH = "config/";
	
	private int router_number;
	
	private DatagramSocket routerSocket;
	
	public Router() throws SocketException {
		routerSocket = new DatagramSocket(PORT);
	}
	
	private DatagramPacket receivePacket() throws IOException {
		byte[] buffer = new byte[65535];
		
		DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
		routerSocket.receive(recvPacket);
				
		return recvPacket;
	}
	
	public void handlePacket() throws IOException {
		byte[] data = receivePacket().getData();
		
		/* Validates the checksums */
		validateChecksumIP(data, 0, 20);
		validateChecksumUDP(data, 20, 28);
		validateChecksumIP(data, 28, 48);
		validateChecksumUDP(data, 28, data.length);
	}
	
	private void validateChecksumIP(byte[] data, int start, int end) 
			throws IOException {
		
		int checksumIndex = start + 10;
		
		int storedChecksum = (data[checksumIndex] << 8) | 
							 (data[checksumIndex + 1] & 0xFF);
		int calculatedChecksum = IP_Header.calculateChecksum(data, start, end);
		
		if (storedChecksum != calculatedChecksum) {
			throw new IOException("Bad IP Checksum: Got " + storedChecksum + 
					" Expected " + calculatedChecksum);
		}
	}
	
	private void validateChecksumUDP(byte[] data, int start, int end) 
			throws IOException {
		
		int checksumIndex = start + 6;
		
		int storedChecksum = (data[checksumIndex] << 8) | 
							 (data[checksumIndex + 1] & 0xFF);
		int calculatedChecksum = UDP_Header.calculateChecksum(data, start, end);
		
		if (storedChecksum != calculatedChecksum) {
			throw new IOException("Bad UDP Checksum: Got " + storedChecksum + 
					" Expected " + calculatedChecksum);
		}
	}
}
