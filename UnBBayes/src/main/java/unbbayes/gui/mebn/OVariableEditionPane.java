/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import unbbayes.controller.MEBNController;
import unbbayes.gui.mebn.auxiliary.ToolKitForGuiMebn;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.Type;

/** 
 * Pane for edition of the ordinary variables of one MFrag. 
 * Show a tree with the ordinary variables, buttons for add, 
 * remove and text panes for rename and set the type of 
 * one ordinary variable. 
 * 
 * @author Laecio Lima dos Santos
 * @version 1.0 02/22/07
 */
public class OVariableEditionPane extends JPanel {

	private OVariableTreeForOVariableEdition treeMFrag; 
	private JScrollPane jspTreeMFrag; 
	
	private JPanel jpInformation; 
	
	private JLabel name; 
	private JTextField txtName; 
	private JLabel type; 
	
	
	private JComboBox jcbType; 
	private Type[] types; 	
	
	private JButton jbNew; 
	private JButton jbDelete; 	
	
	private JToolBar jtbOptions; 	
	
	private MEBNController mebnController; 
	private MFrag mFrag; 
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
    private Matcher matcher;	
	
	public OVariableEditionPane(MEBNController _controller){
		
		super(); 
		
		this.setBorder(ToolKitForGuiMebn.getBorderForTabPanel(resource.getString("OVariableTitle"))); 
		
		setLayout(new BorderLayout()); 
		
		mebnController = _controller; 
	    mFrag = mebnController.getCurrentMFrag(); 
	    
	    treeMFrag = new OVariableTreeForOVariableEdition(_controller);
	    jspTreeMFrag = new JScrollPane(treeMFrag);
	    
	    /* painel of information about the OVariable */
	    jpInformation = new JPanel(new GridLayout(5, 0)); 
	    
	    name = new JLabel(resource.getString("nameLabel")); 
	    txtName = new JTextField(10);
	    type = new JLabel(resource.getString("typeLabel")); 
	    
	    //Fill the combo box with the possible labels 
	    types = mebnController.getMultiEntityBayesianNetwork().getTypeContainer().getListOfTypes().toArray( new Type[0] ); 
	    jcbType = new JComboBox(types); 
	    jcbType.setSelectedIndex(0); 
	    
	    jtbOptions = new JToolBar(); 
	    jtbOptions.setLayout(new GridLayout(0, 2)); 
	    
	    jbNew = new JButton("+"); 
	    jbNew.setToolTipText(resource.getString("newOVariableToolTip")); 
	    jbDelete = new JButton("-"); 
	    jbDelete.setToolTipText(resource.getString("delOVariableToolTip")); 
	    //jtbOptions.add(jbNew);
	    //jtbOptions.add(jbDelete); 
	    jtbOptions.setFloatable(false);	    

	    jpInformation.add(jtbOptions); 
	    jpInformation.add(name); 
	    jpInformation.add(txtName); 
	    jpInformation.add(type); 
	    jpInformation.add(jcbType); 
	    
	    addListeners(); 
	    
	    this.add("Center", jspTreeMFrag);	 
	    //TODO refazer esta classe retirando informa��es desnecess�rias... 
	    //this.add("South", jpInformation);	    
	}

	/**
	 *  Create a empty painel 
	 **/
	
	public OVariableEditionPane(){
		
	}
	
	/**
	 * update the tree of ordinary variables of the MFrag active
	 */
	public void update(){
		if(treeMFrag != null){
			treeMFrag.updateTree();
		}
	}	
	
	public void setNameOVariableSelected(String name){
		txtName.setText(name);; 
	}
	
	/**
	 * Set the type of the variable show in the window. 
	 * @param nameType The name o the type
	 * @return true if success or false if the type doesn't exist in the 
	 * list of types of this panel. 
	 */
	public boolean setTypeOVariableSelected(Type nameType){
		
		int index = 0;
		boolean sucess = false; 
		
		for(index = 0; index < types.length; index++){
			if(types[index].equals(nameType)){
				jcbType.setSelectedIndex(index);
				sucess = true; 
				break; 
			}
		}
		
		return sucess; 
	}
	
	/**
	 * Get the name of ovariable show in the pane. 
	 */
	public String getNameOVariable(){
		return txtName.getText(); 
	}
	
	
	public void addListeners(){
		
		jbNew.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				
  				try{
  				   OrdinaryVariable ov = mebnController.addNewOrdinaryVariableInMFrag(); 
  				   treeMFrag.updateTree(); 
  				   treeMFrag.setOVariableActive(ov);
  				   txtName.setText(ov.getName()); 
  				   txtName.selectAll(); 
  				   txtName.requestFocus(); 
  				}
  				catch(Exception e){
  					e.printStackTrace();  
  				}
  				
  			}
  		});
		
		jbDelete.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent ae) {
  				
				if(treeMFrag.getOVariableActive() != null){
	  				mebnController.removeOrdinaryVariableOfMFrag(treeMFrag.getOVariableActive());						   
					treeMFrag.updateTree();
				}				
  				
  			}
  		});
		
		txtName.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtName.getText().length()>0)) {
  					try {
  						String name = txtName.getText(0,txtName.getText().length());
  						matcher = wordPattern.matcher(name);
  						if (matcher.matches()) {
  							if(treeMFrag.getOVariableActive() != null){
  							   treeMFrag.getOVariableActive().setName(name); 
  							   treeMFrag.updateTree();
  							}
  						}  else {
  							JOptionPane.showMessageDialog(null, resource.getString("nameError"), resource.getString("nameException"), JOptionPane.ERROR_MESSAGE);
  							txtName.selectAll();
  						}
  					}
  					catch (javax.swing.text.BadLocationException ble) {
  						System.out.println(ble.getMessage());
  					}
  				}
  			}
  		});
		
	    jcbType.addActionListener(
           new ActionListener(){
        	   public void actionPerformed(ActionEvent e){
        		   
	    	        Type typeName = (Type)jcbType.getSelectedItem();
					System.out.println("item selected = " + typeName); 
	    	        
					if(treeMFrag.getOVariableActive() != null){
						treeMFrag.getOVariableActive().setValueType(typeName); 
					}
					
					treeMFrag.updateTree(); 
					
					
        	   }
           }
	    ); 
		
		
	}
	
	
	
}
