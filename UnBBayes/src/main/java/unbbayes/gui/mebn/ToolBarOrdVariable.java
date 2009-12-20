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

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.gui.mebn.auxiliary.ButtonLabel;
import unbbayes.gui.mebn.auxiliary.FocusListenerTextField;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.exception.DuplicatedNameException;
import unbbayes.prs.mebn.exception.ReservedWordException;

/**
 * Bar of edition for one ordinary variable. 
 * 
 * Structure: 
 * Icon of OV - Name of OV - Type of OV
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 *
 */
public class ToolBarOrdVariable extends JToolBar{

	private MEBNController mebnController; 
	
	private JLabel name; 
	private JTextField txtName; 
	private JLabel type; 
	private OrdinaryVariable ov; 
		
	private JComboBox jcbType = new JComboBox(); 
	private Type[] types; 	
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.gui.resources.GuiResources.class.getName());
	
    private final Pattern wordPattern = Pattern.compile("[a-zA-Z_0-9]*");
    private Matcher matcher;	
	
	public ToolBarOrdVariable(MEBNController _mebnController){
		
		super(); 
		setLayout(new GridLayout(1, 5)); 
		
		mebnController = _mebnController; 
		this.setFloatable(false); 
  		ButtonLabel btnOrdVariableActive = new ButtonLabel(resource.getString("OrdVariableButton"), IconController.getInstance().getOVariableNodeIcon());  
  		
	    name = new JLabel(resource.getString("nameLabel")); 
	    txtName = new JTextField(10);
	    type = new JLabel(resource.getString("typeLabel")); 
	    
	    //Fill the combo box with the possible labels 
	    types = mebnController.getMultiEntityBayesianNetwork().getTypeContainer().getListOfTypes().toArray( new Type[0] ); 
        buildJComboBoxTypes(types); 
	    
        txtName.addFocusListener(new FocusListenerTextField()); 
		txtName.addKeyListener(new KeyAdapter() {
  			public void keyPressed(KeyEvent e) {
  				
  				if ((e.getKeyCode() == KeyEvent.VK_ENTER) && (txtName.getText().length()>0)) {
  					try {
  						String name = txtName.getText(0,txtName.getText().length());
  						matcher = wordPattern.matcher(name);
  						if (matcher.matches()) {
  							if(ov != null){
  								
  								if(mebnController.getCurrentMFrag().getOrdinaryVariableByName(name)!= null){
  									JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), 
  	  	  									resource.getString("nameAlreadyExists"), 
  	  	  									resource.getString("nameError"), 
  	  	  									JOptionPane.ERROR_MESSAGE);
  	  							   	
  								}else{
  									try {
										mebnController.renameOrdinaryVariable(ov, name);
									} catch (DuplicatedNameException e1) {
										JOptionPane.showMessageDialog(null, 
												resource.getString("nameDuplicated"), 
												resource.getString("nameError"), 
												JOptionPane.ERROR_MESSAGE);
									} catch (ReservedWordException e2) {
		  	  							JOptionPane.showMessageDialog(mebnController.getScreen(),
		  	  									resource.getString("nameReserved"),
		  	  									resource.getString("nameError"),
		  	  									JOptionPane.ERROR_MESSAGE);
									}
  								}
  							}
  						}  else {
  							txtName.setBackground(MebnToolkit.getColorTextFieldError()); 
  							txtName.setForeground(Color.WHITE); 
  							txtName.selectAll();
  							JOptionPane.showMessageDialog(null, 
  									resource.getString("nameDuplicated"), 
  									resource.getString("nameError"), 
  									JOptionPane.ERROR_MESSAGE);
  						}
  					}
  					catch (javax.swing.text.BadLocationException ble) {
  						System.out.println(ble.getMessage());
  					}
  				}
  			}
  			

  			public void keyReleased(KeyEvent e){
  				try{
                    String name = txtName.getText(0,txtName.getText().length());
						matcher = wordPattern.matcher(name);
						if (!matcher.matches()) {
							txtName.setBackground(MebnToolkit.getColorTextFieldError()); 
							txtName.setForeground(Color.WHITE); 
						}
						else{
							txtName.setBackground(MebnToolkit.getColorTextFieldSelected());
							txtName.setForeground(Color.BLACK); 
						}
  				}
  				catch(Exception efd){
  					
  				}
  				
  			}
  		});  
	    
	    add(btnOrdVariableActive); 
	    
	    JToolBar barName = new JToolBar(); 
	    barName.setFloatable(false); 
//	    barName.add(name); 
	    barName.add(txtName); 
	    add(barName);
	    
	    JToolBar barType = new JToolBar(); 
	    barType.setFloatable(false); 
//	    barType.add(type); 
	    barType.add(jcbType); 
	    add(barType); 
	    
	    add(new EmptyPanel()); 
	    add(new EmptyPanel()); 
	     
	}
	
  	
  	/**
  	 * A simple panel for fill the empty spaces in the toolbar of the 
  	 * active object. 
  	 * 
  	 * @author Laecio
  	 */
  	private class EmptyPanel extends JButton{
  		
  		public EmptyPanel(){
  			super(); 
  			setEnabled(false); 
  		}
  		
  	}
	
	/**
	 * Set the ordinary variable that this tool bar use. 
	 * @param _ov
	 */
	public void setOrdVariable(OrdinaryVariable _ov){
		
		ov = _ov; 
		txtName.setText(ov.getName()); 
		
		//select the type of the OV in the combo box
		for(int i = 0; i < types.length; i++){
			if(types[i].equals(ov.getValueType())){
				jcbType.setSelectedIndex(i);
				break; 
			}
		}
		
		repaint(); 
	}
	
	/**
	 * Update the types of the list of types
	 */
	
	public void updateListOfTypes(){
		types = mebnController.getMultiEntityBayesianNetwork().getTypeContainer().getListOfTypes().toArray( new Type[0] );
		buildJComboBoxTypes(types); 
		validate(); 
	}
	
	private JComboBox buildJComboBoxTypes(Type[] types){
	    
	    jcbType.setModel(new DefaultComboBoxModel(types)); 
	    
	    if(ov!=null){
	    	jcbType.setSelectedItem(ov.getType()); 
	    }
	    
	    jcbType.addActionListener(
           new ActionListener(){
        	   public void actionPerformed(ActionEvent e){
	    	        Type typeName = (Type)jcbType.getSelectedItem();
	    	        //by young2
	    	        if(ov != null && !typeName.equals(ov.getValueType()) ){
	    	        	mebnController.setOrdinaryVariableType(ov, typeName); 
	    	        }
        	   }
           }
	    );   
	    
	    return jcbType; 
	}
	
}
