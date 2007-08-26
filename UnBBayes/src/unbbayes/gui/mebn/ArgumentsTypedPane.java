package unbbayes.gui.mebn;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import unbbayes.controller.MEBNController;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.exception.OVDontIsOfTypeExpected;

/**
 * Painel utilizado para se selecionar quais variáveis ordinárias
 * irao preencher cada um dos argumentos. É criada uma combo box
 * referente a cada argumento a ser preenchido, e nesta são listadas
 * todas as variaveis ordinárias que são do tipo esperado. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 06/28/07
 *
 */
public class ArgumentsTypedPane extends JPanel{
	
	ResidentNodePointer pointer; 
	MEBNController mebnController; 
	Object node; 
	
	/**
	 * 
	 * Notas: o painel é criado baseando-se na estrutura atual do nó residente
	 * ao qual o ResidentNodePointer aponta. 
	 * 
	 * @param _node Node that have this pointer (GenerativeInputNode or ContextNode)
	 * @param _pointer
	 * @param _mebnController
	 */
	public ArgumentsTypedPane(Object _node, ResidentNodePointer _pointer, MEBNController _mebnController){
		super(); 
		pointer = _pointer; 
		mebnController = _mebnController; 
		node = _node; 
		
		if(pointer.getNumberArguments() > 5)
		setLayout(new GridLayout(pointer.getNumberArguments() + 1,1)); 
		else setLayout(new GridLayout(6 ,1 )); 
		
		JComboBox argument[] = new JComboBox[pointer.getNumberArguments()]; 
		
		JLabel residentNodeName = new JLabel("Node = " + pointer.getResidentNode().getName()); 
		residentNodeName.setOpaque(true); 
		residentNodeName.setHorizontalAlignment(JLabel.CENTER); 
		//residentNodeName.setBackground(new Color(78, 201, 249)); 
		residentNodeName.setBorder(BorderFactory.createLineBorder(Color.BLACK)); 
		add(residentNodeName); 
		
		JToolBar tbArgX; 
		JButton btnArgXNumber; 
		JButton btnArgXType; 
		
		/* Indica se a lista de argumentos estava inicialmente vazia... */
		boolean listEmpty = (pointer.getOrdinaryVariableList().size() == 0) ; 
			  
		//Montagem das JComboBox para cada argumento	

		ArrayList<OrdinaryVariable> ovList = (ArrayList<OrdinaryVariable>)mebnController.getCurrentMFrag().getOrdinaryVariableList(); 
		
		for(int i = 0; i < pointer.getNumberArguments(); i++){
			
			tbArgX = new JToolBar(); 
			
			Vector<OrdinaryVariable> list = new Vector<OrdinaryVariable>(); 
			list.add(null); //elemento em branco... 

			//Verificacao de quais ov deverao entrar na JComboBox
			for(OrdinaryVariable ov: ovList){
				
				if(ov.getValueType().equals(pointer.getTypeOfArgument(i))){
					list.add(ov); 
				}
				
			}
			
			
			argument[i] = new JComboBox(list); 
			argument[i].addItemListener(new ComboListener(i)); 
			
            //Selecionar a ov ativa da JComboBox (a que atualmente preenche o argumento)
			
			int indexSelected = 0; 
						
			if(pointer.getArgument(i) != null){
				int j = 0; 
				for(OrdinaryVariable ov: ovList){
					if(ov.equals(pointer.getArgument(i))){
						indexSelected = j;
						break; 
					}else{
						j++; 
					}
				}
			}
			else{
				indexSelected = 0; //null
			}
			
			argument[i].setSelectedIndex(indexSelected); 
			
			//Adicionando componentes ao painel. 
			btnArgXNumber = new JButton("" + i);
			btnArgXNumber.setBackground(new Color(78, 201, 249)); 
			btnArgXType = new JButton(pointer.getTypeOfArgument(i).getName()); 
			btnArgXType.setBackground(Color.LIGHT_GRAY); 
			
			tbArgX.add(btnArgXNumber); 
			tbArgX.add(btnArgXType); 
			tbArgX.add(argument[i]); 
			tbArgX.setFloatable(false); 
			
			add(tbArgX); 
		}
	}
	
	class ComboListener implements ItemListener{
		
		int indice;
		
		public ComboListener(int i){
			indice = i; 
		}
		
		public void itemStateChanged(ItemEvent e){
			
			JComboBox combo = (JComboBox)e.getSource(); 
			if(combo.getSelectedItem() != null){
				OrdinaryVariable ov = (OrdinaryVariable)combo.getSelectedItem(); 
				try{
					pointer.addOrdinaryVariable(ov, indice);
					mebnController.updateArgumentsOfObject(node); 
				}
				catch(OVDontIsOfTypeExpected ex){
					ex.printStackTrace(); 
				}
			}
		}
		
	}
	
}
