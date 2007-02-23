package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import unbbayes.controller.FormulaTreeController;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;

public class EntityListForReplaceInFormula extends JPanel{

	private List<ObjectEntity> listEntity; 
	
	private JList jlEntities; 
	private DefaultListModel listModel;
	private ObjectEntity selected;
	
	private FormulaTreeController formulaTreeController; 
	
	public EntityListForReplaceInFormula(FormulaTreeController _formulaTreeController){
		
		super(); 
		formulaTreeController = _formulaTreeController; 
		
		listModel = new DefaultListModel(); 
		
		jlEntities = new JList(listModel); 
		jlEntities.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jlEntities.setLayoutOrientation(JList.VERTICAL);
		jlEntities.setVisibleRowCount(-1);
		
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
		
		listModel.clear(); 
		
		listEntity = ObjectEntity.getListEntity(); 
		
		listModel = new DefaultListModel(); 
		for(Entity entity: listEntity){
			listModel.addElement(entity); 
		}
		
		jlEntities.setModel(listModel); 
	}
	
	private void addListListener(){

	    jlEntities.addListSelectionListener(
            new ListSelectionListener(){
                public void valueChanged(ListSelectionEvent e) {
                	
                	selected = (ObjectEntity)jlEntities.getSelectedValue(); 
                	formulaTreeController.addEntity(selected); 
                    
                }
            }  	
	    );
	   
	}	
	
}
