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
import java.awt.Component;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import unbbayes.controller.IconController;
import unbbayes.controller.mebn.FormulaTreeController;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;

/**
 * Panel with the entities for use in formula as operando
 * 
 * @author Laecio
 *
 */
public class EntityListForReplaceInFormula extends JPanel{

    private final IconController iconController = IconController.getInstance();
    
	private List<ObjectEntity> listEntity; 

	private JList jlEntities; 
	private DefaultListModel listModel;
	private ObjectEntity selected;
	
	private FormulaTreeController formulaTreeController; 
	private MEBNController mebnController; 
	
	public EntityListForReplaceInFormula(MEBNController _controller, FormulaTreeController _formulaTreeController){
		
		super(); 
		formulaTreeController = _formulaTreeController; 
		mebnController = _controller; 
		
		listModel = new DefaultListModel(); 
		
		jlEntities = new JList(listModel); 
		jlEntities.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jlEntities.setLayoutOrientation(JList.VERTICAL);
		jlEntities.setVisibleRowCount(-1);
	    jlEntities.setCellRenderer(new ListEntitiesCellRenderer()); 
		
		selected = null;     
		update(); 
		addListListener();
		
		this.setLayout(new BorderLayout()); 
		this.add(jlEntities, BorderLayout.CENTER); 
	}
    
	/**
	 *  update the list of entities 
	 **/
	
	public void update(){
		
		ObjectEntity antSelected = selected; 
		
		listModel.clear(); 
		
		listEntity = mebnController.getMultiEntityBayesianNetwork().getObjectEntityContainer().getListEntity(); 
		
		listModel = new DefaultListModel(); 
		for(Entity entity: listEntity){
			listModel.addElement(entity); 
		}
		
		jlEntities.setModel(listModel); 
		
		/* 
		 * Warning: Por algum motivo estranho a mim a referencia feita por
		 * selected estava sendo perdida quando se adicionava seguidamente
		 * nos entidades... Esta jogadinha solucionou o problema... 
		 */
		selected = antSelected; 
		
	}
	
	private void addListListener(){

	    jlEntities.addListSelectionListener(
            new ListSelectionListener(){
                public void valueChanged(ListSelectionEvent e) {
                	
                	selected = (ObjectEntity)jlEntities.getSelectedValue(); 
                	formulaTreeController.addEntity(selected); 
                	mebnController.updateFormulaActiveContextNode(); 
				
                }
            }  	
	    );
	   
	}	
	
	class ListEntitiesCellRenderer extends DefaultListCellRenderer{
		
		private ImageIcon entityIcon = iconController.getEntityInstanceIcon(); 
		
		public ListEntitiesCellRenderer(){
			
		}
		
		public Component getListCellRendererComponent(JList list, Object value, int index, 
				                                      boolean isSelected, boolean cellHasFocus){
			
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);  
			
			super.setIcon(entityIcon); 
			
			if(isSelected){
			   super.setBorder(BorderFactory.createEtchedBorder()); 
			}
			
			return this; 
		}
		
	}
}
