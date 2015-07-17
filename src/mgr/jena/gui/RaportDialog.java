package mgr.jena.gui;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.*;

import mgr.jena.Utils.MapSorter;
 
/**
 * A Java class to demonstrate how to put a scrolling text area
 * in a JOptionPane showMessageDialog dialog. 
 * 
 * Steps are simple - Just create a JTextArea, wrap it in a 
 * JScrollPane, and then add the JScrollPane to the showMessageDialog.
 */
public class RaportDialog extends JDialog
{

  public RaportDialog()
  {
	  this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setBounds(100, 100, 500, 500);
		this.setResizable(false);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		JTextArea textArea = new JTextArea(20, 25);
		String raport = "";
		
		try {
			FileReader f = new FileReader("RecommendationRaport.txt");
			BufferedReader r = new BufferedReader(f);
			String line = "";
			try {
				while((line = r.readLine()) != null)
				{
					raport += line + "\n";
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		textArea.setEditable(false); 
		textArea.setText(raport);
	    // wrap a scrollpane around it
	    JScrollPane scrollPane = new JScrollPane(textArea);
	    this.getContentPane().add(scrollPane);
	    this.setVisible(true);
  }
  
  
}
