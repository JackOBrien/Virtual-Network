package router;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
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
	
	public DatagramPacket receivePacket() throws IOException {
		byte[] buffer = new byte[65535];
		
		DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
		routerSocket.receive(recvPacket);
				
		return recvPacket;
	}
}
