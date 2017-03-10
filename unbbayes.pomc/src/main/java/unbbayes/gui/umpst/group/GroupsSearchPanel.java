package unbbayes.gui.umpst.group;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import unbbayes.controller.umpst.IconController;
import unbbayes.gui.umpst.IUMPSTPanel;
import unbbayes.gui.umpst.UmpstModule;
import unbbayes.model.umpst.group.GroupModel;
import unbbayes.model.umpst.project.UMPSTProject;

public class GroupsSearchPanel extends IUMPSTPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JLabel labelGroup;
	private JButton buttonSearch;
	private JButton buttonAddGroup,buttonCancel;

	private JTextField textGroup;
	
	/** Load resource file from this package */
	private static ResourceBundle resource = 
			unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.gui.umpst.resources.Resources.class.getName());
	
	public GroupsSearchPanel(UmpstModule janelaPai,UMPSTProject umpstProject){
		super(janelaPai);
		
		this.setUmpstProject(umpstProject);
		
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
			buttonAddGroup = new JButton (IconController.getInstance().getAddIconP());
			buttonAddGroup.setToolTipText(resource.getString("hpAddGroup"));
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
			buttonCancel = new JButton (IconController.getInstance().getEditClear());
			buttonCancel.setToolTipText(resource.getString("hpCleanSearch"));
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
	
	
	public GroupsEditionPanel getGroupsAdd(GroupModel group){
		
		GroupsEditionPanel ret = new GroupsEditionPanel(getFatherPanel(),getUmpstProject(),group);
		
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
			buttonSearch = new JButton(IconController.getInstance().getSearchIcon());
			buttonSearch.setToolTipText(resource.getString("hpSearchGroup"));
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
		
		textGroup.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(!textGroup.getText().equals("")){
					updateTableGroups();
				}
				else{
					JOptionPane.showMessageDialog(null, "Seach is empty!");
				}

			}
		});
		
		return textGroup;
	}
	
	public void updateTableGroups(){
    	String[] columnNames = {"ID","Group","",""};
    	
		Pattern pattern = Pattern.compile(textGroup.getText()); 
		Matcher m; 
		
		List<GroupModel> result = new ArrayList<GroupModel>(); 
		
		for(GroupModel g: getUmpstProject().getMapGroups().values()){
			m = pattern.matcher(g.getName()); 
			if (m.find()){
				result.add(g); 
			}
		}
		
		Object[][] data = new Object[result.size()][5];
		
		Integer i=0;
		
	   
    	for (GroupModel group: result) {
    	      
    	 	data[i][0] = group.getId();
			data[i][1] = group.getName();			
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
	    
		Object[][] data = new Object[getUmpstProject().getMapGroups().size()][4];
		Integer i=0;
	    
		Set<String> keys = getUmpstProject().getMapGroups().keySet();
		TreeSet<String> sortedKeys = new TreeSet<String>(keys);
		
		for (String key: sortedKeys){
			data[i][0] = getUmpstProject().getMapGroups().get(key).getId();
			data[i][1] = getUmpstProject().getMapGroups().get(key).getName();			
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
