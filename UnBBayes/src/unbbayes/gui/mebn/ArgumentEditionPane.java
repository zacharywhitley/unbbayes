package unbbayes.gui.mebn;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
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
	
	OVariableTreeForArgumentEdition treeMFrag; 
	ResidentOVariableTree treeResident; 
	JToolBar jtbInformation; 
	
	JScrollPane jspTreeMFrag; 
	JScrollPane jspTreeResident;
	JToolBar jtbOptions; 
	
	MEBNController mebnController; 
	MFrag mFrag; 
	ResidentNode residentNode; 
	
	JButton jbDown; 
	JButton jbUp; 	
	
	JLabel labelType; 
	JTextField textType; 
	
    private final IconController iconController = IconController.getInstance();
	
	/**
	 * @param _controller o controlador da rede
	 * @param resident O nodo ao qual se esta editando os argumentos. 
	 */
	public ArgumentEditionPane(NetworkController _controller, ResidentNode resident){
		
		super(); 
		
		GridBagLayout gridbag = new GridBagLayout(); 
		GridBagConstraints constraints = new GridBagConstraints(); 
		
		setLayout(gridbag);
		
		mebnController = _controller.getMebnController(); 
	    mFrag = mebnController.getCurrentMFrag(); 
	    residentNode = resident;
	    
	    treeMFrag = new OVariableTreeForArgumentEdition(_controller);
	    jspTreeMFrag = new JScrollPane(treeMFrag);
	    jspTreeMFrag.setBackground(Color.green); 
	    
	    treeResident = new ResidentOVariableTree(_controller, resident); 	    
	    jspTreeResident = new JScrollPane(treeResident); 
	    
	    jtbInformation = new JToolBar(); 

	    
	    constraints.gridx = 0; 
	    constraints.gridy = 0; 
	    constraints.gridwidth = 1; 
	    constraints.gridheight = 1; 
	    constraints.weightx = 100; 
	    constraints.weighty = 50; 
	    constraints.fill = GridBagConstraints.BOTH; 
	    constraints.anchor = GridBagConstraints.NORTH; 
	    gridbag.setConstraints(jspTreeMFrag, constraints); 
	    this.add(jspTreeMFrag);
	    
	    jtbOptions = new JToolBar(); 
	    jtbOptions.setLayout(new GridLayout(0, 2)); 
	     
	    jbDown = new JButton("ADD"); 
	    jbUp = new JButton("DEL"); 
	    jtbOptions.add(jbDown);
	    jtbOptions.add(jbUp); 
	    jtbOptions.setFloatable(false);

	    constraints.gridx = 0;
	    constraints.gridy = 1;
	    constraints.gridwidth = 1;
	    constraints.gridheight = 1;
	    constraints.weightx = 0;
	    constraints.weighty = 5;
	    constraints.fill = GridBagConstraints.BOTH;
	    constraints.anchor = GridBagConstraints.CENTER;
	    gridbag.setConstraints(jtbOptions, constraints);
	    this.add(jtbOptions);	
	    
	    addListenersOptions(); 
	    
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
	    constraints.weighty = 5;
	    constraints.fill = GridBagConstraints.BOTH;
	    constraints.anchor = GridBagConstraints.NORTH;
	    gridbag.setConstraints(jtbInformation, constraints);  
	    
	    jtbInformation.setLayout(new GridLayout(2,0)); 
	    labelType = new JLabel("Type: ");
	    textType = new JTextField(10); 
	    jtbInformation.add(labelType); 
	    jtbInformation.add(textType); 
	    textType.setEditable(false); 
	    jtbInformation.setFloatable(false); 
	    this.add(jtbInformation);
	    
	}

	/**
	 *  Create a empty painel 
	 *  
	 *  */
	
	public ArgumentEditionPane(){
		
		
		
		
		
	}
		
	public void addListenersOptions(){
		
		jbDown.addActionListener(
		    new ActionListener(){
		    	public void actionPerformed(ActionEvent ae){
		    		OrdinaryVariable ov = treeMFrag.getOVariableSelected(); 
		    		if (ov != null){
		    		    mebnController.addOrdinaryVariableInResident(ov); 
		    		}
		    	}
		    }
		); 
		
		jbUp.addActionListener(
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
	
	public void update(){
		
	  	treeMFrag.updateTree(); 
	  	treeResident.updateTree(); 
	  	
	}
}