package unbbayes.gui.mebn;

import java.awt.Color;
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
import unbbayes.controller.NetworkController;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;

/**
 * Painel para que o usuario edite quais são os argumentos presentes em 
 * um resident node. O painel é dividido em duas arvores: 
 * a arvore contendo as variaveis ordinarias da MFrag a qual o resident
 * pertence, e uma arvore contendo as variaveis ordinarias que são argumentos
 * no residente. As ações são realizadas clicando-se duas vezes em um nodo: 
 * - ao clicar duas vezes em um nodo da arvore da MFrag, esta variavel ordinaria
 * é adicionada como argumento no nodo
 * - ao clicar duas vezes em um nodo da arvore do Resident, esta variavel 
 * ordinaria é excluida como argumento do nodo. 
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
	 * @param _controller o controlador da rede
	 * @param resident O nodo ao qual se esta editando os argumentos. 
	 */
	public ArgumentEditionPane(NetworkController _controller, ResidentNode resident){
		
		super(); 
		
		this.setBorder(ToolKitForGuiMebn.getBorderForTabPanel(resource.getString("ArgumentTitle"))); 
		
		GridBagLayout gridbag = new GridBagLayout(); 
		GridBagConstraints constraints = new GridBagConstraints(); 
		
		setLayout(gridbag);
		
		mebnController = _controller.getMebnController(); 
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
	     
	    btnNew = new JButton("+"); 
	    btnNew.setToolTipText(resource.getString("newArgumentToolTip")); 
	    btnDel = new JButton("-"); 
	    btnDel.setToolTipText(resource.getString("delArgumentToolTip")); 	    
	    jtbOptions.add(btnNew);
	    jtbOptions.add(btnDel); 
	    jtbOptions.setFloatable(false);
	    
	    addListenersOptions(); 
	    
	    constraints.gridx = 0; 
	    constraints.gridy = 0; 
	    constraints.gridwidth = 1; 
	    constraints.gridheight = 1; 
	    constraints.weightx = 100; 
	    constraints.weighty = 30; 
	    constraints.fill = GridBagConstraints.BOTH; 
	    constraints.anchor = GridBagConstraints.NORTH; 
	    gridbag.setConstraints(jspTreeMFrag, constraints); 
	    this.add(jspTreeMFrag);

	    constraints.gridx = 0;
	    constraints.gridy = 1;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    constraints.weightx = 0;
	    constraints.weighty = 0;
	    constraints.fill = GridBagConstraints.BOTH;
	    constraints.anchor = GridBagConstraints.NORTH;
	    gridbag.setConstraints(jtbDown, constraints);
	    this.add(jtbDown);
	    
	    constraints.gridx = 0;
	    constraints.gridy = 2;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    constraints.weightx = 0;
	    constraints.weighty = 30;
	    constraints.fill = GridBagConstraints.BOTH;
	    constraints.anchor = GridBagConstraints.NORTH;
	    gridbag.setConstraints(jspTreeResident, constraints);
	    this.add(jspTreeResident);
	    
	    constraints.gridx = 0;
	    constraints.gridy = 3;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    constraints.weightx = 0;
	    constraints.weighty = 0;
	    constraints.fill = GridBagConstraints.BOTH;
	    constraints.anchor = GridBagConstraints.CENTER;
	    gridbag.setConstraints(jtbOptions, constraints);
	    this.add(jtbOptions);	

	    
	    constraints.gridx = 0;
	    constraints.gridy = 4;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    constraints.weightx = 0;
	    constraints.weighty = 0;
	    constraints.fill = GridBagConstraints.BOTH;
	    constraints.anchor = GridBagConstraints.NORTH;
	    gridbag.setConstraints(jtbInformation, constraints);  
	    this.add(jtbInformation);
	    
	}

	/**
	 *  Create a empty painel  
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
	    					mebnController.addOrdinaryVariableInResident(ov); 
	    				}
	    			}
	    		}
	    ); 
	    
		btnNew.addActionListener(
		    new ActionListener(){
		    	public void actionPerformed(ActionEvent ae){
		    		OrdinaryVariable ov = mebnController.addNewOrdinaryVariableInResident(); 
		    		treeResident.setOVariableSelected(ov);
		    		txtName.setText(ov.getName()); 
		    		txtName.selectAll(); 
		    		txtName.requestFocus(); 
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