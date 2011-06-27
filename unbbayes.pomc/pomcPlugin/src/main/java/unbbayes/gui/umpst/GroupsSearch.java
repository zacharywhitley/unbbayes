package unbbayes.gui.umpst;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import unbbayes.model.umpst.groups.GroupsModel;
import unbbayes.model.umpst.project.UMPSTProject;

public class GroupsSearch extends IUMPSTPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JLabel labelGroup;
	
	private JButton buttonSearch;
	private JButton buttonAddGroup,buttonCancel;

	private JTextField textGroup;
	

	
	
	
	public GroupsSearch(UmpstModule janelaPai){
		super(janelaPai);
		
		this.setLayout(new BorderLayout());
		//GridBagConstraints constraints = new  GridBagConstraints();
		
		JPanel panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBackground(new Color(0x4169AA));
		
		panel.add(getLabelGroup());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.add(getTextGroup());
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		JPanel buttonPane = new JPanel ();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(getButtonSearch());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(getButtonCancel());
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(getButtonAddGroup());


		
		this.add(panel, BorderLayout.CENTER);
		this.add(buttonPane, BorderLayout.PAGE_END);
		
		
		
		}

	/**
	 * @return the buttonAddGroup
	 */
	public JButton getButtonAddGroup() {
		
		if (buttonAddGroup == null){
			buttonAddGroup = new JButton ("add new group");
			buttonAddGroup.setForeground(Color.blue);
			buttonAddGroup.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					changePanel(getGroupsAdd(null));
				}
			});
		}
		
		return buttonAddGroup;
	} 
	

	/**
	 * @return the buttonCancel
	 */
	public JButton getButtonCancel() {
		
		if (buttonCancel == null){
			buttonCancel = new JButton ("cancel search");
			buttonCancel.setForeground(Color.blue);
			buttonCancel.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					textGroup.setText("");
					returnTableGroups();
				}
			});
		}
		
		return buttonCancel;
	}
	
	
	public GroupsAdd getGroupsAdd(GroupsModel group){
		
		GroupsAdd ret = new GroupsAdd(getFatherPanel(),group);
		
		return ret;
		
	}
	
	
	/**
	 * @return the labelGroup
	 */
	public JLabel getLabelGroup() {
		
		if(labelGroup == null){
			labelGroup = new JLabel("Search for a group: ");
			labelGroup.setForeground(Color.white);
		}
		
		return labelGroup;
	}


	/**
	 * @return the buttonSearch
	 */
	public JButton getButtonSearch() {
		
		if(buttonSearch == null){
			buttonSearch = new JButton("Search: ");
			buttonSearch.setForeground(Color.blue);
		}
	
			
		buttonSearch.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(!textGroup.getText().equals("")){
					updateTableGroups();
				}
				else{
					JOptionPane.showMessageDialog(null, "Seach is empty!");
				}

			}
		});
		
		return buttonSearch;
	}


	/**
	 * @return the textGroup
	 */
	public JTextField getTextGroup() {
		
		if (textGroup == null){
			textGroup = new JTextField(10);
		}
		
		return textGroup;
	}
	
	public void updateTableGroups(){
    	String[] columnNames = {"ID","Group","",""};
    	
    	
    	
		Set<GroupsModel> aux = UMPSTProject.getInstance().getMapSearchGroups().get(textGroup.getText()).getRelatedGroups();
		GroupsModel group;
		Object[][] data = new Object[UMPSTProject.getInstance().getMapSearchGroups().get(textGroup.getText()).getRelatedGroups().size()][4];

		Integer i=0;
		
	   
    	for (Iterator<GroupsModel> it = aux.iterator(); it.hasNext(); ) {
    	     group = it.next();  // No downcasting required.
    	     
    	 	data[i][0] = group.getId();
			data[i][1] = group.getGroupName();			
			data[i][2] = "";
			data[i][3] = "";
			i++;
    	}
    	
	    
   
	    UmpstModule pai = getFatherPanel();
	    changePanel(pai.getMenuPanel());
	    
	    TableGroups groupsTable = pai.getMenuPanel().getGroupsPane().getGroupsTable();
	    JTable table = groupsTable.createTable(columnNames,data);
	    
	    groupsTable.getScrollPanePergunta().setViewportView(table);
	    groupsTable.getScrollPanePergunta().updateUI();
	    groupsTable.getScrollPanePergunta().repaint();
	    groupsTable.updateUI();
	    groupsTable.repaint();
    }
	
	
	 
    public void returnTableGroups(){
    	String[] columnNames = {"ID","Group","",""};	    
	    
		Object[][] data = new Object[UMPSTProject.getInstance().getMapGroups().size()][4];
		Integer i=0;
	    
		Set<String> keys = UMPSTProject.getInstance().getMapGroups().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			data[i][0] = UMPSTProject.getInstance().getMapGroups().get(key).getId();
			data[i][1] = UMPSTProject.getInstance().getMapGroups().get(key).getGroupName();			
			data[i][2] = "";
			data[i][3] = "";
			i++;
		}
   
	    UmpstModule pai = getFatherPanel();
	    changePanel(pai.getMenuPanel());
	    
	    TableGroups groupTable = pai.getMenuPanel().getGroupsPane().getGroupsTable();
	    JTable table = groupTable.createTable(columnNames,data);
	    
	    groupTable.getScrollPanePergunta().setViewportView(table);
	    groupTable.getScrollPanePergunta().updateUI();
	    groupTable.getScrollPanePergunta().repaint();
	    groupTable.updateUI();
	    groupTable.repaint();
    }	

}
