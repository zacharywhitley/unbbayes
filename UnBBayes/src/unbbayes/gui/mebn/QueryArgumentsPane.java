package unbbayes.gui.mebn;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import unbbayes.controller.MEBNController;
import unbbayes.gui.ParcialStateException;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;

/**
 * Painel para selecionar os argumentos da query. 
 * 
 * S�o disponiveis como argumentos as object entities previamente cadastradas
 * no sistema e h� a op��o para o usu�rio entrar com uma entidade n�o existente. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 06/28/07
 *
 */

public class QueryArgumentsPane extends JPanel{
	
	private MEBNController mebnController; 
	private ResidentNode node; 
	
	private JComboBox argument[]; 
	
	
	public QueryArgumentsPane(ResidentNode node, MEBNController mebnController){
		
		super(); 
		
		this.node = node; 
		this.mebnController = mebnController; 
		
		setLayout(new GridLayout(node.getOrdinaryVariableList().size(), 1)); 
		
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
			argument[i].setEditable(true); 
			
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
	}
	
	public ObjectEntityInstance[] getArguments() throws ParcialStateException{
		
		ObjectEntityInstance[] argumentVector = new ObjectEntityInstance[argument.length]; 
		
		for(int i = 0; i < argument.length; i++){
			if(argument[i].getSelectedItem() != null){
				if(argument[i].getSelectedItem() instanceof ObjectEntityInstance){
				     argumentVector[i] = (ObjectEntityInstance)argument[i].getSelectedItem(); 
				}else{
					if(argument[i].getSelectedItem() instanceof String){
						if(isNameValid((String)argument[i].getSelectedItem())){
						 String nameInstance = (String)argument[i].getSelectedItem(); 
						 ObjectEntity objectEntity = mebnController.getMultiEntityBayesianNetwork().getObjectEntityContainer().getObjectEntityByType(node.getOrdinaryVariableList().get(i).getValueType()); 
						 argumentVector[i] = new ObjectEntityInstance(nameInstance, objectEntity);
						}else{
							throw new ParcialStateException(); 
						}
					}
				}
			}
			else{
				throw new ParcialStateException(); 
			}
		}
		
		return argumentVector; 
	}
	
	private boolean isNameValid(String name){
		return true;
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
