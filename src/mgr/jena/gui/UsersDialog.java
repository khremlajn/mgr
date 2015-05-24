package mgr.jena.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import mgr.jena.Utils.MapSorter;
import mgr.jena.osm.OSMUser;

public class UsersDialog extends JDialog {
	
	private String result = "";
	private JTable table;
	
	/**
	 * Create the dialog.
	 */
	public UsersDialog(Map<String, OSMUser> users, OSMUser currentUser) {
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		users = MapSorter.sortByKey(users);
		setBounds(100, 100, 500, 500);
		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		//contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			JLabel tf = new JLabel("Choose user: ");
			contentPanel.add(tf);
		    CustomTableModel model = new CustomTableModel();
		    table = new JTable(model);
		    table.getTableHeader().setReorderingAllowed(false);
		    table.getTableHeader().setResizingAllowed(false);
		    model.addColumn("Username");
		    model.addColumn("Number of reviews");
		    Iterator<Entry<String, OSMUser>> it = users.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<String, OSMUser> pair = it.next();
		        model.addRow(new Object[] { pair.getKey() , pair.getValue().getReviewsSize() });
		    }
		    System.out.println("Users count: " + model.getRowCount());
		    JScrollPane scrollPane = new JScrollPane(table);
			scrollPane.setPreferredSize(new Dimension(400,350));
		    contentPanel.add(scrollPane);
		    final JTextField newUser = new JTextField();
			newUser.setPreferredSize(new Dimension(350,25));
			newUser.setToolTipText("Enter user name");
			JButton createUser = new JButton("Create user");
			createUser.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent event) {
	            	result = newUser.getText();
	            	UsersDialog.this.setVisible(false);
	            }
	        });
			contentPanel.add(newUser);
			contentPanel.add(createUser);
		    JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			contentPanel.add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
		            @Override
		            public void actionPerformed(ActionEvent event) {
		            	result = (String)table.getValueAt(table.getSelectedRow(), 0);
		            	UsersDialog.this.setVisible(false);
		            }
		        });
				buttonPane.add(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
		            @Override
		            public void actionPerformed(ActionEvent event) {
		            	UsersDialog.this.setVisible(false);
		            }
		        });
				buttonPane.add(cancelButton);
			}
		getContentPane().add(contentPanel);
		setCurrentUser(table, users, currentUser);
		
		this.setVisible(true);
	}
	
	public String getResult()
	{
		return result;
	}
	
	private void setCurrentUser(JTable table, Map<String, OSMUser> users, OSMUser currentUser)
	{
		if(currentUser == null || !users.containsKey(currentUser.getUserID()))
		{
			table.setRowSelectionInterval(0, 0);
		}
		else
		{
			for(int i=0;i<table.getRowCount();i++)
			{
				String data = (String)table.getValueAt(i, 0);
				if(currentUser.getUserID().equalsIgnoreCase(data))
				{
					table.setRowSelectionInterval(i, i);
					return;
				}
			}
		}
	}
	
	private class CustomTableModel extends DefaultTableModel
	{
		public boolean isCellEditable(int rowIndex, int mColIndex) {
	          return false;
	        }
	}

}
