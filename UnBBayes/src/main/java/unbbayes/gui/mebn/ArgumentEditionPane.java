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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;

/**
 * A panel to be used by the user in order to edit the arguments of a resident
 * node. This panel is divided into 2 trees: a tree containing the ordinary
 * variables from a MFrag containing that resident node, and a tree containing
 * ordinary variables which are arguments of that resident node. Those trees
 * are realized by clicking a node twice:
 * 	- by clicking twice at a node from the MFrag tree, that ordinary variable 
 *    will be added as that node's argument.
 *  - by clicking twice at a node from the Resident tree, that ordinary variable
 *    will be deleted from the node's argument.
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com) 
 * @version 1.0 (11/15/2006)
 * 
 */

public class ArgumentEditionPane extends JPanel{
	
	OrdinaryVariable ordinaryVariableSelected; 
	boolean treeResidentActive = true; 
	
	OVariableTreeForArgumentEdition treeMFrag; 
	ResidentOVariableTree treeResident; 
	JToolBar jtbInformation; 
	
	JScrollPane jspTreeMFrag; 
	JScrollPane jspTreeResident;
	
	JToolBar jtbDown; 
	JToolBar jtbOptions; 
	
	JToolBar jtbMFrag; 
	JToolBar jtbResident; 
	
	MEBNController mebnController; 
	MFrag mFrag; 
	ResidentNode residentNode; 
	
	JButton btnNew; 
	JButton btnDel; 	
	JButton btnDown; 
	
	JLabel labelName;
	JTextField txtName; 
	
	JLabel labelType; 
	JTextField txtType; 
	
    private final IconController iconController = IconController.getInstance();
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
    
    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
    private Matcher matcher;
    
	/**
	 * @param _controller network's controller
	 * @param resident The node which the arguments we are editing. 
	 */
	public ArgumentEditionPane(MEBNController _controller, ResidentNode resident){
		
		super(); 
		
		this.setBorder(MebnToolkit.getBorderForTabPanel(resource.getString("ArgumentTitle"))); 
		
		mebnController = _controller; 
	    mFrag = mebnController.getCurrentMFrag(); 
	    residentNode = resident;
	    
	    treeMFrag = new OVariableTreeForArgumentEdition(_controller);
	    jspTreeMFrag = new JScrollPane(treeMFrag);
	    
	    treeResident = new ResidentOVariableTree(_controller, resident); 	    
	    jspTreeResident = new JScrollPane(treeResident); 
	    
	    jtbDown = new JToolBar(); 
	    jtbDown.setLayout(new GridLayout(1,0)); 
	    
	    btnDown = new JButton(iconController.getDownIcon());
	    btnDown.setToolTipText(resource.getString("downArgumentToolTip")); 
	    
	    jtbDown.add(btnDown); 
	    jtbDown.setFloatable(false); 
	    
	    jtbInformation = new JToolBar(); 
	    jtbInformation.setLayout(new GridLayout(4,0)); 
	    
	    labelName = new JLabel(resource.getString("nameLabel"));
	    txtName = new JTextField(10); 
	    labelType = new JLabel(resource.getString("typeLabel"));
	    txtType = new JTextField(10); 
	    
	    jtbInformation.add(labelName); 
	    jtbInformation.add(txtName); 
	    jtbInformation.add(labelType); 
	    jtbInformation.add(txtType); 
	    txtType.setEditable(false); 
	    jtbInformation.setFloatable(false); 

	    jtbOptions = new JToolBar(); 
	    jtbOptions.setLayout(new GridLayout(0, 2)); 
	     
	    btnNew = new JButton(iconController.getMoreIcon()); 
	    btnNew.setToolTipText(resource.getString("newArgumentToolTip")); 
	    btnDel = new JButton(iconController.getLessIcon()); 
	    btnDel.setToolTipText(resource.getString("delArgumentToolTip")); 	    
	    jtbOptions.add(btnNew);
	    jtbOptions.add(btnDel); 
	    jtbOptions.setFloatable(false);
	    
	    addListenersOptions(); 
	    
	    JPanel panelCenter = new JPanel();
		GridBagLayout gridbag = new GridBagLayout(); 
		GridBagConstraints constraints = new GridBagConstraints(); 
		panelCenter.setLayout(gridbag);
	    
	    constraints.gridx = 0; 
	    constraints.gridy = 0; 
	    constraints.gridwidth = 1; 
	    constraints.gridheight = 1; 
	    constraints.weightx = 100; 
	    constraints.weighty = 30; 
	    constraints.fill = GridBagConstraints.BOTH; 
	    constraints.anchor = GridBagConstraints.NORTH; 
	    gridbag.setConstraints(jspTreeMFrag, constraints); 
	    panelCenter.add(jspTreeMFrag);

	    constraints.gridx = 0;
	    constraints.gridy = 1;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    constraints.weightx = 0;
	    constraints.weighty = 0;
	    constraints.fill = GridBagConstraints.BOTH;
	    constraints.anchor = GridBagConstraints.NORTH;
	    gridbag.setConstraints(jtbDown, constraints);
	    panelCenter.add(jtbDown);
	    
	    constraints.gridx = 0;
	    constraints.gridy = 2;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    constraints.weightx = 0;
	    constraints.weighty = 30;
	    constraints.fill = GridBagConstraints.BOTH;
	    constraints.anchor = GridBagConstraints.NORTH;
	    gridbag.setConstraints(jspTreeResident, constraints);
	    panelCenter.add(jspTreeResident);
	    
	    constraints.gridx = 0;
	    constraints.gridy = 3;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    constraints.weightx = 0;
	    constraints.weighty = 0;
	    constraints.fill = GridBagConstraints.BOTH;
	    constraints.anchor = GridBagConstraints.CENTER;
	    gridbag.setConstraints(jtbOptions, constraints);
	    panelCenter.add(jtbOptions);	

	    constraints.gridx = 0;
	    constraints.gridy = 4;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    constraints.weightx = 0;
	    constraints.weighty = 0;
	    constraints.fill = GridBagConstraints.BOTH;
	    constraints.anchor = GridBagConstraints.NORTH;
	    gridbag.setConstraints(jtbInformation, constraints);  
	    panelCenter.add(jtbInformation);
	 
	    ResidentPaneOptions options = new ResidentPaneOptions(_controller); 
	    this.setLayout(new BorderLayout()); 
	    this.add(options, BorderLayout.NORTH); 
	    this.add(panelCenter, BorderLayout.CENTER); 
	}

	/**
	 *  Create a empty panel  
	 * */	
	public ArgumentEditionPane(){
		
	}
		
	public void addListenersOptions(){
		
  		txtName.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtName.getText().length()>0)) {
  					try {
  						String name = txtName.getText(0,txtName.getText().length());
  						matcher = wordPattern.matcher(name);
  						if (matcher.matches()) {
  							mebnController.renameOVariableInArgumentEditionPane(name); 
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
		
	    btnDown.addActionListener(
	    		new ActionListener(){
	    			public void actionPerformed(ActionEvent ae){
	    				OrdinaryVariable ov = treeMFrag.getOVariableSelected(); 
	    				if (ov != null){
	    					try{
	    					   mebnController.addOrdinaryVariableInResident(ov);
	    					}
	    					catch(OVariableAlreadyExistsInArgumentList e1){
	  							JOptionPane.showMessageDialog(null, resource.getString("oVariableAlreadyIsArgumentError"), resource.getString("operationError"), JOptionPane.ERROR_MESSAGE);
	    					}
	    					catch(ArgumentNodeAlreadySetException e2){
	    						e2.printStackTrace(); 
	    					}
	    				}
	    			}
	    		}
	    ); 
	    
		btnNew.addActionListener(
		    new ActionListener(){
		    	public void actionPerformed(ActionEvent ae){
		    		
		    		try{
		    		   OrdinaryVariable ov = mebnController.addNewOrdinaryVariableInResident();
			    		treeResident.setOVariableSelected(ov);
			    		txtName.setText(ov.getName()); 
			    		txtName.selectAll(); 
			    		txtName.requestFocus(); 
		    		}
					catch(OVariableAlreadyExistsInArgumentList e1){
							JOptionPane.showMessageDialog(null, resource.getString("oVariableAlreadyIsArgumentError"), resource.getString("operationError"), JOptionPane.ERROR_MESSAGE);
					}
					catch(ArgumentNodeAlreadySetException e2){
						e2.printStackTrace(); 
					}
		    	}
		    }
		); 
		
		btnDel.addActionListener(
			    new ActionListener(){
			    	public void actionPerformed(ActionEvent ae){
			    		OrdinaryVariable ov = treeResident.getOVariableSelected(); 
			    		if (ov != null){
			    		    mebnController.removeOrdinaryVariableInResident(ov); 
			    		}
			    	}
			    }
		); 		
		
	}
	
	public void setTxtName(String newName){
		txtName.setText(newName); 
	}
	
	public ResidentOVariableTree getResidentOVariableTree(){
		return this.treeResident; 
	}

	public OVariableTreeForArgumentEdition getMFragOVariableTree(){
		return this.treeMFrag; 
	}	
	
	/**
	 * Set the Tree of ordinary variables of the resident node 
	 * how the active tree (the tree of the mfrag is inactive)
	 */
	public void setTreeResidentActive(){
		treeResidentActive = true; 
	}

	/**
	 * Set the Tree of ordinary variables of the mfrag 
	 * how the active tree (the tree of the resident is inactive)
	 */
	public void setTreeMFragActive(){
		treeResidentActive = false; 
	}
	
	/**
	 * Return true if the Resident OVariable Tree is active and else if 
	 * the MFrag OVariable tree is the active. 
	 * @return
	 */
	public boolean isTreeResidentActive(){
		return treeResidentActive; 
	}
		
	/**
	 * Update the trees of resident nodes of the MFrag active and
	 * the Resident node active. 
	 */
	public void update(){
		
	  	treeMFrag.updateTree(); 
	  	treeResident.updateTree(); 
	  	
	}
}