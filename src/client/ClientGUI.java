package client;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/********************************************************************
 * GUI.java
 *
 * GUI to accept user input and print results.
 *
 * @author Jack O'Brien
 * @author Megan Maher
 * @author Tyler McCarthy
 * 
 * @version Oct 15, 2014
 *******************************************************************/
public class ClientGUI {
	
	/** Image of a globe to be applied to the frame. */
	private Image globe;
	
	/** The frame containing the GUI. */
	private JFrame frame;
	
	/** Text area to print results to screen. */
	private JTextArea textArea;
	
	/** Text field to accept user input. */
	private JTextField textField;
	
	/** Bar which holds the button to change dst and label displaying the
	 * current dst. */
	private JMenuBar menuBar;
	
	/** Label displaying the IPv4 address of the current destination. */
	private JLabel ipLabel;
	
	/** Monospaced size 12 font. */
	private Font monospaced;
	
	private int host_number;
	
	private boolean gotValidIP;
	
	private InetAddress dstAddr;
	
	private Client client;
	
	/****************************************************************
	 * Default constructor for the GUI.
	 ***************************************************************/
	public ClientGUI(Client client) {
		this.client = client;
		
		monospaced = new Font("monospaced", Font.PLAIN, 12);
		
		gotValidIP = false;
		host_number = 1;
		
		setupMenu();
		setupFrame();
		
		setDestination(true);
	}
	
	/****************************************************************
	 * Sets up the menu and its components.
	 ***************************************************************/
	private void setupMenu() {
		
		// Creates and configures the menu bar
		menuBar = new JMenuBar();
		menuBar.setBackground(Color.lightGray);
		menuBar.setBorderPainted(false);
		menuBar.setLayout(new BorderLayout());
		menuBar.setPreferredSize(new Dimension(0, 18));

		// Creates and configures the change dst button
		JButton b = new JButton("Change Destination");
		b.setBackground(Color.lightGray);
		b.setBorderPainted(false);
		b.setFocusable(false);
		b.addActionListener(al);
		b.setActionCommand("changeIP");
		
		// Creates and configures the IP dst label
		ipLabel = new JLabel();
		ipLabel.setForeground(new Color(0, 122, 8));
		ipLabel.setFont(monospaced);
		
		// Adds the two components to the menu bar
		menuBar.add(b, BorderLayout.WEST);
		menuBar.add(ipLabel, BorderLayout.EAST);
	}
	
	/****************************************************************
	 * Instantiates and sets up the JFrame. Sets the icon and adds 
	 * all components.
	 ***************************************************************/
	private void setupFrame() {
		frame = new JFrame("Virtual-Network Client");
		frame.setLayout(new BorderLayout());

		/* Attempts to read the image file. */
		try {
			globe = ImageIO.read(new File("/src/client/globe.png"));
		} catch (IOException e) {
			throw new RuntimeException("Icon resource not found.");
		}
		
		/* Sets the frame's icon the the globe image */
		if (globe != null) {
			frame.setIconImage(globe);
		}
		
		setupTextArea();
		setupTextField();	
		
		// Adds components
		frame.setJMenuBar(menuBar);
		frame.add(new JScrollPane(textArea,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
		frame.add(textField, BorderLayout.PAGE_END);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null); // Centers the frame on the screen.
		frame.setVisible(true);
	}
	
	/****************************************************************
	 * Sets up and instantiates the text area.
	 ***************************************************************/
	private void setupTextArea() {
		textArea = new JTextArea(15, 72);
		
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		textArea.setBackground(new Color(9, 10, 10));
		textArea.setForeground(new Color(142, 230, 242));
		
		textArea.setFont(monospaced);
		textArea.setEditable(false);
	}
	
	/****************************************************************
	 * Sets up and instantiates the text field.
	 ***************************************************************/
	private void setupTextField() {
		textField = new JTextField();
		
		textField.addActionListener(al);
		textField.setActionCommand("mainField");
		
		textField.setBackground(new Color(30, 31, 31));
		textField.setForeground(new Color(145, 245, 245));
		
		textField.setFont(monospaced);
	}
	
	/****************************************************************
	 * Creates a new DestinationDialog.
	 * 
	 * @param firstTime true if this is the dialog being displayed.
	 * at the start of the program.
	 ***************************************************************/
	private void setDestination(boolean firstTime) {
		new DestinationDialog(frame, firstTime);
	}
	
	/****************************************************************
	 * Creates a new DestinationDialog with a specified title.
	 * 
	 * @param firstTime true if this is the dialog being displayed.
	 * @param title the title to display on the dialog.
	 ***************************************************************/
	private void setDestination(boolean firstTime, String title) {
		new DestinationDialog(frame, firstTime, title);
	}
	
	/* Action listener to handle user input from text field. */
	private ActionListener al = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String source = e.getActionCommand();
			
			/* Checks for change destination button */
			if (source.equals("changeIP")){
				setDestination(false);
			}
			
			/* Checks or input from the set destination text field */
			else if (source.equals("destinationField")) {
				JTextField tf = (JTextField) e.getSource();
				
				String enteredText = tf.getText();
				InetAddress ip = null;
								
				String currentLabel = ipLabel.getText();
				
				/* Validates input */
				try {
					ip = InetAddress.getByName(enteredText);
				} catch (UnknownHostException e1) {
					
					if (currentLabel.isEmpty()) {
						ipLabel.setText("-- Invalid IP --");
					} else {
						ipLabel.setText(currentLabel);
					}
					
					String title = "Invalid IPv4 Address";
					
					// Re prompts the user based on whether or not we
					// already have an IP.
					setDestination(!gotValidIP, title);
					return;
				}
				
				dstAddr = ip;
				
				if (!gotValidIP) {
					try {
						client.setHostNumber(host_number);
					} catch (Exception e1) {
						e1.printStackTrace();
						String title = "Invalid config file for Host " +
								host_number;
						setDestination(true, title);
					}
				}
				
				gotValidIP = true;
				
				// Updates label text to the new IP address
				ipLabel.setText("Dst IP: " + dstAddr.getHostAddress() + "   ");
			}
			
			/* Checks for exit button */
			else if (source.equals("Exit")) {
				System.exit(0);
			}
			
			/* Checks or input from the main text field */
			else if (source.equals("mainField")) {
				
				// Ignores blank lines
				if (textField.getText().isEmpty()) return;

				String message = textField.getText();
				message = "- Sent to <" + dstAddr.getHostAddress() +
						"> - " + message;
				
				
				// Appends text inputed by used to text area.
				textArea.append(message + "\n");
				textField.setText("");
				
				client.sendMessage(message, dstAddr);
			}
		}
	};
	
	public static void main(String[] args) {
		Client client = null;
		try {
			client = new Client();
		} catch (SocketException e) {
			// TODO: Alert User then exit
			System.err.println("Port in use");
			return;
		}
		
		new ClientGUI(client);
	}
	
	/* Inner class to create the set destination window */
	private class DestinationDialog extends JDialog implements ActionListener
		, ChangeListener {
		
		/** Default serial version UID to satisfy eclipse */
		private static final long serialVersionUID = 1L;
		
		private JSpinner spin;

		/****************************************************************
		 * Constructor which allows for a user specified title.
		 * 
		 * @param frame the frame to center the dialog over.
		 * @param first tells if the button should be exit (T) or cancel (F).
		 * @param text the title of the dialog.
		 ***************************************************************/
		private DestinationDialog(JFrame frame, boolean first, String text) {
			setupDialog(frame, first, text);
		}
		
		/****************************************************************
		 * Constructor which sets a default title.
		 * 
		 * @param frame the frame to center the dialog over.
		 * @param first tells if the button should be exit (T) or cancel (F).
		 ***************************************************************/
		private DestinationDialog(JFrame topWindow, boolean first) {
			setupDialog(topWindow, first, "Set Destiniation IP");
		}
		
		/****************************************************************
		 * Sets up the entire dialog window.
		 * 
		 * @param frame the frame to center the dialog over.
		 * @param first tells if the button should be exit (T) or cancel (F).
		 * @param text the title of the dialog.
		 ***************************************************************/
		private void setupDialog(JFrame frame, boolean first, String text) {
			setUndecorated(true);
			
			Dimension preferred = new Dimension(0, 18);
			
			// Sets up panel which will contain all components
			JPanel panel = new JPanel(new BorderLayout(5, 5));
			panel.setPreferredSize(new Dimension(205, 70));
			panel.setBorder(
					BorderFactory.createLineBorder(new Color(0, 122, 8), 2));
			
			// Sets up title label
			JLabel title = new JLabel(text);
			title.setOpaque(true);
			title.setBackground(Color.LIGHT_GRAY);
			title.setPreferredSize(preferred);
			
			// Sets up text field
			JTextField field = new JTextField(15);
			field.addActionListener(al);
			field.addActionListener(this);
			field.setActionCommand("destinationField");
			field.setFont(monospaced);
			field.setPreferredSize(preferred);
					
			// Sets up exit/cancel button
			JButton b = new JButton();
			b.setOpaque(true);
			b.setBackground(Color.LIGHT_GRAY);
			b.addActionListener(al);
			b.addActionListener(this);
			b.setPreferredSize(preferred);
			b.setFocusable(false);
			
			/* Switches the name of the button based on 
			 * the "first" parameter. Also adds host_number spinner. */
			String name = "Cancel";
			if (first) {
				SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 4, 1);
				spin = new JSpinner(model);
				
				spin.addChangeListener(this);
				
				panel.add(spin, BorderLayout.EAST);
				
				if (!title.getText().startsWith("Invalid"))
					title.setText("Set Destination IP and Host Number");
				panel.setPreferredSize(new Dimension(215, 70));
				name = "Exit";
			}
			
			b.setText(name);
			b.setActionCommand(name);
			
			// Adds the components to the panel, then to the dialog
			panel.add(title, BorderLayout.NORTH);
			panel.add(field, BorderLayout.CENTER);
			panel.add(b, BorderLayout.SOUTH);
			add(panel);
			
			requestFocus();
			
			pack();
			setLocationRelativeTo(frame);
			setModal(true);
			setVisible(true);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			textField.requestFocus();
			dispose();
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			host_number = (int) spin.getValue();
		}
	}
}


