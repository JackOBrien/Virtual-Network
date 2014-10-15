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
import java.text.ParseException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JInternalFrame.JDesktopIcon;
import javax.swing.text.MaskFormatter;

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
	
	private JDialog destDialog;
	
	/****************************************************************
	 * Default constructor for the GUI.
	 ***************************************************************/
	public GUI() {
		
		monospaced = new Font("monospaced", Font.PLAIN, 12);
		
		setupMenu();
		setupFrame();
		
		setDestination(true);
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
		frame.setLocationRelativeTo(null);
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
		textField.setActionCommand("mainField");
		textField.setBackground(new Color(30, 31, 31));
		textField.setForeground(new Color(145, 245, 245));
		textField.setFont(monospaced);
	}
	
	private void setDestination(boolean firstTime) {
		destDialog = new DestinationDialog(frame, firstTime);
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
			
			/* Checks for cancel button */
			else if (source.equals("Close")) {
				
			}
			
			/* Checks for exit button */
			else if (source.equals("Exit")) {
				System.exit(0);
			}
			
			/* Checks or input from the set destination text field */
			else if (source.equals("destinationField")) {
				destDialog.dispose();
			}
			
			/* Checks or input from the main text field */
			else if (source.equals("mainField")) {
				
				// Ignores blank lines
				if (textField.getText().isEmpty()) return;

				// Appends text inputed by used to text area.
				textArea.append(textField.getText() + "\n");
				textField.setText("");
			}
		}
	};
	
	public static void main(String[] args) {
		new GUI();
	}
	
	/* Inner class to create the set destination window */
	private class DestinationDialog extends JDialog {
		
		private DestinationDialog(JFrame topWindow, boolean first) {
			setTitle("Set Destinaton IP");
			
			JPanel panel = new JPanel(new BorderLayout(5, 5));
			panel.setPreferredSize(new Dimension(200, 40));
			
			JTextField field = new JTextField(15);
			field.addActionListener(al);
			field.setActionCommand("destinationField");
			field.setFont(monospaced);
						
			JButton b = new JButton();
			b.addActionListener(al);
			b.setPreferredSize(new Dimension(0, 18));
			b.setFocusable(false);
			
			String name = "Cancle";
			if (first) {
				name = "Exit";
			}
			
			b.setText(name);
			b.setActionCommand(name);
			
			panel.add(field, BorderLayout.CENTER);
			panel.add(b, BorderLayout.SOUTH);
			add(panel);
			
			requestFocus();
			
			pack();
			setLocationRelativeTo(topWindow);
			setModal(true);
			setVisible(true);
		}
	}
}


