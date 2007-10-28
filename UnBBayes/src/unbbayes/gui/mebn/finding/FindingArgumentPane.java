package unbbayes.gui.mebn.finding;


import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import unbbayes.controller.MEBNController;
import unbbayes.gui.ParcialStateException;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.StateLink;

/**
 * Painel utilizado para se selecionar quais vari�veis ordin�rias
 * irao preencher cada um dos argumentos. É criada uma combo box
 * referente a cada argumento a ser preenchido, e nesta s�o listadas
 * todas as variaveis ordin�rias que s�o do tipo esperado. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 06/28/07
 *
 */

public class FindingArgumentPane extends JPanel{
	
	private MEBNController mebnController; 
	private ResidentNode node; 
	
	private JComboBox states; 
	private JComboBox argument[]; 
	
	private final static int MINIMUM_LINE_SIXE_PANEL = 5; 
	
	public FindingArgumentPane(DomainResidentNode node, MEBNController mebnController){
		
		super(); 
		
		this.node = node; 
		this.mebnController = mebnController; 
		
		if(node.getOrdinaryVariableList().size() > MINIMUM_LINE_SIXE_PANEL){
		    setLayout(new GridLayout(node.getOrdinaryVariableList().size(), 1)); 
		}
		else{
			setLayout(new GridLayout(MINIMUM_LINE_SIXE_PANEL + 1 , 1 )); 
		}
		
		argument = new JComboBox[node.getOrdinaryVariableList().size()]; 
		
		JToolBar tbArgX; 
		JButton btnArgXNumber; 
		JButton btnArgXType; 
		 
		//Montagem das JComboBox para cada argumento	

		List<ObjectEntityInstance> entityList = 
			mebnController.getMultiEntityBayesianNetwork().getObjectEntityContainer().getListEntityInstances(); 
		
		int i = 0; 
		
		for(OrdinaryVariable ov: node.getOrdinaryVariableList()){
			
			tbArgX = new JToolBar(); 
			
			Vector<ObjectEntityInstance> list = new Vector<ObjectEntityInstance>(); 
			list.add(null); //elemento em branco... 

			//Verificacao de quais e deverao entrar na JComboBox
			for(ObjectEntityInstance entity: entityList){
				if(entity.getType().equals(ov.getValueType())){
					list.add(entity);
				}
			}
			
			argument[i] = new JComboBox(list); 
			argument[i].addItemListener(new ComboListener(i)); 
			
			argument[i].setSelectedIndex(0); 
			
			//Adicionando componentes ao painel. 
			btnArgXNumber = new JButton("" + i);
			btnArgXNumber.setBackground(new Color(78, 201, 249)); 
			btnArgXType = new JButton(ov.getValueType().getName()); 
			btnArgXType.setBackground(Color.LIGHT_GRAY); 
			
			tbArgX.add(btnArgXNumber); 
			tbArgX.add(btnArgXType); 
			tbArgX.add(argument[i]); 
			tbArgX.setFloatable(false); 
			
			add(tbArgX); 
			i++; 
		}

		JLabel labelState = new JLabel("State:"); 
		
		JButton btnLabelType = null; 
		switch(node.getTypeOfStates()){
		case ResidentNode.BOOLEAN_RV_STATES:
			btnLabelType = new JButton("Boolean"); 
			break; 
		case ResidentNode.CATEGORY_RV_STATES:
			btnLabelType = new JButton("Categorical"); 
			break; 
		case ResidentNode.OBJECT_ENTITY:
			btnLabelType = new JButton("Object"); 
			break; 
		default:
		    break; 	
		}
		
		states = new JComboBox(node.getPossibleValueLinkList().toArray()); 
		
		JToolBar tbStates = new JToolBar();
		
		tbStates.add(labelState);
		tbStates.add(btnLabelType); 
		tbStates.add(states); 
		tbStates.setFloatable(false); 
		
		add(tbStates); 
	}
	
	public ObjectEntityInstance[] getArguments() throws ParcialStateException{
		
		ObjectEntityInstance[] argumentVector = new ObjectEntityInstance[argument.length]; 
		
		for(int i = 0; i < argument.length; i++){
			if(argument[i].getSelectedItem() != null){
				argumentVector[i] = (ObjectEntityInstance)argument[i].getSelectedItem(); 
			}
			else{
				throw new ParcialStateException(); 
			}
		}
		
		return argumentVector; 
	}
	
	public Entity getState(){
		return ((StateLink)(states.getSelectedItem())).getState(); 
	}
	
	public void clear(){
	
	}
	
	private class ComboListener implements ItemListener{
		
		int indice;
		
		public ComboListener(int i){
			indice = i; 
		}
		
		public void itemStateChanged(ItemEvent e){
			
			JComboBox combo = (JComboBox)e.getSource(); 
			if(combo.getSelectedItem() != null){
				
			}
		}
		
	}
	
}