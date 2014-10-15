package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuBar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JInternalFrame.JDesktopIcon;

/********************************************************************
 * GUI.java
 *
 * GUI to accept user input and print results.
 *
 * @author Jack O'Brien
 * @authon Megan Maher
 * @author Tyler McCarthy
 * 
 * @version Oct 14, 2014
 *******************************************************************/
public class GUI {
	
	/** Image of a globe to be applied to the frame. */
	private Image globe;
	
	/** The frame containing the GUI. */
	private JFrame frame;
	
	/** Text area to print results to screen. */
	private JTextArea textArea;
	
	/** Text field to accept user input. */
	private JTextField textField;
	
	private JMenuBar menuBar;
	
	/** Monospaced size 12 font. */
	private Font monospaced;
	
	/****************************************************************
	 * Default constructor for the GUI.
	 ***************************************************************/
	public GUI() {
		
		monospaced = new Font("monospaced", Font.PLAIN, 12);
		
		setupMenu();
		setupFrame();
		
		setDestination();
	}
	
	private void setupMenu() {
		menuBar = new JMenuBar();
		
		menuBar.setBackground(Color.lightGray);
		menuBar.setBorderPainted(false);
		
		JButton b = new JButton("Change Destination");
		b.setBackground(Color.lightGray);
		b.setBorderPainted(false);
		b.setFocusable(false);
		b.setPreferredSize(new Dimension(0, 18));
		b.setActionCommand("changeIP");
		
		menuBar.add(b);
	}
	
	/****************************************************************
	 * Instantiates and sets up the JFrame. Sets the icon and adds 
	 * all components.
	 ***************************************************************/
	private void setupFrame() {
		frame = new JFrame("Virtual-Network Client");
		frame.setLayout(new BorderLayout());
		frame.setLocationRelativeTo(null);

		/* Attempts to read the image file. */
		try {
			globe = ImageIO.read(new File("src/main/globe.png"));
		} catch (IOException e) {
			throw new RuntimeException("Icon resource not found.");
		}
		
		if (globe != null) {
			frame.setIconImage(globe);
		}
		
		setupTextArea();
		setupTextField();	
		
		// Adds components
		frame.setJMenuBar(menuBar);
		frame.add(new JScrollPane(textArea), BorderLayout.PAGE_START);
		frame.add(textField, BorderLayout.PAGE_END);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
	
	/****************************************************************
	 * Sets up and instantiates the text area.
	 ***************************************************************/
	private void setupTextArea() {
		textArea = new JTextArea(15, 72);
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
		textField.setBackground(new Color(30, 31, 31));
		textField.setForeground(new Color(145, 245, 245));
		textField.setFont(monospaced);
	}
	
	private void setDestination() {
		new DestinationDialog(frame);
	}
	
	/* Action listener to handle user input from text field. */
	private ActionListener al = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			/* Checks for change destination button */
			if (e.getActionCommand().equals("changeIP")){
				
			}
			
			// Ignores blank lines
			if (textField.getText().isEmpty()) return;
			
			// Appends text inputed by used to text area.
			textArea.append(textField.getText() + "\n");
			textField.setText("");
		}
	};
	
	public static void main(String[] args) {
		new GUI();
	}
	
	/* Inner class to create the set destination window */
	private class DestinationDialog extends JDialog {
		
		private DestinationDialog(JFrame topWindow) {
			setTitle("Set Destinaton IP");
			setUndecorated(true);
			setLocation(topWindow.getLocation());
			
			JPanel panel = new JPanel();
			
			/* Adds 4 text fields to the panel */
			for (int i = 1; i <= 4; i++) {
				JTextField field = new JTextField(3);
				field.addActionListener(al);
				field.setActionCommand("field" + i);
				panel.add(field);
			}
			
			add(panel);
			
			pack();
			setVisible(true);
			setModal(true);
		}
	}
}


