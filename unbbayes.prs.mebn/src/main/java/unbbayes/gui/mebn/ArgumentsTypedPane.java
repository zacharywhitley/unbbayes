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
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.exception.OVDontIsOfTypeExpected;

/**
 * Painel utilizado para se selecionar quais variaveis ordinarias
 * irao preencher cada um dos argumentos. E criada uma combo box
 * referente a cada argumento a ser preenchido, e nesta sao listadas
 * todas as variaveis ordinarias que sao do tipo esperado. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 06/28/07
 *
 */
public class ArgumentsTypedPane extends JPanel{
	
	private ResidentNodePointer pointer; 
	private MEBNController mebnController; 
	private Object node; 
	
	private IconController iconController = IconController.getInstance(); 
	
  	private static ResourceBundle resource =
  		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.gui.mebn.resources.Resources.class.getName());
	
	/**
	 * 
	 * Notas: o painel e criado baseando-se na estrutura atual do no residente
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
		
		JLabel residentNodeName = new JLabel(resource.getString("nodeLabel")+ " = " + pointer.getResidentNode().getName()); 
		residentNodeName.setOpaque(true); 
		residentNodeName.setHorizontalAlignment(JLabel.CENTER); 
		//residentNodeName.setBackground(new Color(78, 201, 249)); 
		residentNodeName.setBorder(BorderFactory.createLineBorder(Color.BLACK)); 
		
		JButton btnOpenResidentNode = new JButton(iconController.getResidentNodeIcon()); 
		btnOpenResidentNode.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				mebnController.openPanel(pointer.getResidentNode()); 
			}
		}); 
		btnOpenResidentNode.setToolTipText(resource.getString("openTip")); 
		
		JPanel panel = new JPanel(new BorderLayout()); 
		panel.add(residentNodeName, BorderLayout.CENTER); 
		panel.add(btnOpenResidentNode, BorderLayout.LINE_END); 
		
		add(panel); 
		
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
				
				// FIXME: This if should also verify if the OV is a subclass of the expected class.
				// For now I will remove this condition to verify if the rest works as expected.
//				if(ov.getValueType().equals(pointer.getTypeOfArgument(i))){
					list.add(ov); 
//				}
				
			}
			
			
			argument[i] = new JComboBox(list); 
			argument[i].addItemListener(new ComboListener(i)); 
			
            //Selecionar a ov ativa da JComboBox (a que atualmente preenche o argumento)
			
			int indexSelected = 0; 
						
			if(pointer.getArgument(i) != null){
				int j = 1; 
				for(OrdinaryVariable ov: list){
					if(ov != null){
						if(ov.equals(pointer.getArgument(i))){
							indexSelected = j;
							break; 
						}else{
							j++; 
						}
					}
				}
			}
			else{
				indexSelected = 0; //null
			}
			
			//by young2
			//	argument[i].setSelectedIndex(indexSelected); 
			
			//Adicionando componentes ao painel. 
			btnArgXNumber = new JButton("" + i);
			btnArgXNumber.setBackground(new Color(193, 207, 180)); 
			btnArgXType = new JButton(pointer.getTypeOfArgument(i).getName()); 
			btnArgXType.setBackground(new Color(193, 210, 205)); 
			
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
