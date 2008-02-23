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
package unbbayes.gui.mebn.finding;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.gui.ParcialStateException;
import unbbayes.gui.mebn.auxiliary.ListCellRenderer;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.util.designpatterns.Visitor;

/**
 * Pane for edition of instances of the random variables (resident nodes). 
 * 
 * The panel is divided in two parts: 
 * 1) Panel for selection of node / Pane for edition of instance 
 * 2) List of instances of the node selected
 * 
 * @author Laecio Lima dos Santos
 * @version 1.0 (09/09/07)
 */

public class RandonVariableFindingEdtitionPane extends JPanel implements Visitor{

	private RandonVariableListPane randonVariableListPane; 
	private RandonVariableInstanceEditionPane randonVariableInstanceEditionPane; 
	private RandonVariableInstanceListPane randonVariableInstanceListPane; 
	
	private MEBNController mebnController; 
	
	private IconController iconController = IconController.getInstance(); 
  	private static ResourceBundle resource = 
  		ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

  	private static final String SELECTION_PANE = "SelectionPane"; 
  	private static final String EDITION_PANE = "EditionPane"; 
  	
  	private JPanel upperPanel; 
  	private JPanel downPanel; 
  	
	public RandonVariableFindingEdtitionPane(){
		//empty
	}
  	
	public RandonVariableFindingEdtitionPane(MEBNController mebnController){
		
		super(new BorderLayout()); 
		
		this.mebnController = mebnController; 
		
		upperPanel = new JPanel(new BorderLayout());
		upperPanel.add(new RandonVariableListPane(), BorderLayout.CENTER); 
		
		downPanel = new JPanel(new BorderLayout()); 
		downPanel.add(new RandonVariableInstanceListPane(), BorderLayout.CENTER); 
		
		this.add(upperPanel, BorderLayout.CENTER); 
		this.add(downPanel, BorderLayout.PAGE_END); 
	}
	
	public void showRandonVariableInstanceListPane(DomainResidentNode node){
		downPanel.removeAll(); 
		randonVariableInstanceListPane = new RandonVariableInstanceListPane(node); 
		downPanel.add(randonVariableInstanceListPane, BorderLayout.CENTER); 
		downPanel.validate(); 
	}
	
	public void showRandonVariableEditionPane(DomainResidentNode node){
		upperPanel.removeAll(); 
		upperPanel.add(new RandonVariableInstanceEditionPane(node), BorderLayout.CENTER); 
		upperPanel.validate(); 
	}
	
	public void showRandonVariableListPane(){
		upperPanel.removeAll(); 
		upperPanel.add(new RandonVariableListPane(), BorderLayout.CENTER); 
		upperPanel.validate(); 
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.util.designpatterns.Visitor#visit()
	 */
	public void visit() {
		// TODO Auto-generated method stub
		
	}
	
	private class RandonVariableListPane extends JPanel{
		
		private JList jlistResident; 
		private JScrollPane scrollListObjectEntity; 
		private DefaultListModel listModel; 
		
		private JButton btnEditNode; 
		private JToolBar jtbOptions; 
		
		public RandonVariableListPane(){
			
			super(new BorderLayout()); 
			
			listModel = new DefaultListModel(); 
			for(MFrag mfrag: mebnController.getMultiEntityBayesianNetwork().getMFragList()){
				for(ResidentNode node: mfrag.getResidentNodeList()){
					listModel.addElement(node); 
				}
			}
			
			jlistResident = new JList(listModel); 
			scrollListObjectEntity = new JScrollPane(jlistResident); 
			
			jlistResident.setCellRenderer(new ListCellRenderer(iconController.getYellowNodeIcon())); 
			
			jlistResident.addListSelectionListener(
		            new ListSelectionListener(){
		                public void valueChanged(ListSelectionEvent e) {
		                	if(jlistResident.getSelectedValue() != null){
		                	   showRandonVariableInstanceListPane((DomainResidentNode)(jlistResident.getSelectedValue()));
		                	}
		                }
		            }  	
			 );
			
			jtbOptions = new JToolBar();
			jtbOptions.setLayout(new GridLayout(1,3)); 
			jtbOptions.setFloatable(false); 
			btnEditNode = new JButton(iconController.getEdit()); 
			btnEditNode.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					if(jlistResident.getSelectedValue() != null){
					   showRandonVariableEditionPane((DomainResidentNode)(jlistResident.getSelectedValue())); 
					}
				}
				
			}); 
			
			jtbOptions.add(new JPanel()); 
			jtbOptions.add(btnEditNode); 
			jtbOptions.add(new JPanel());
			
			this.add(scrollListObjectEntity, BorderLayout.CENTER);
			this.add(jtbOptions, BorderLayout.PAGE_END); 
			
		}		
	}
	
	/**
	 * Pane contains: 
	 * - Name of DomainResidentNode 
	 * - List of arguments 
	 * - Selection of state
	 * - buttons for actions
	 * 
	 * @author Laecio Lima dos Santos (laecio@gmail.com)
	 * @version 1.0 (09/09/07)
	 */
	private class RandonVariableInstanceEditionPane extends JPanel{
		
		private final DomainResidentNode residentNode; 
		
		private JLabel nodeName; 
		
		private JComboBox comboState; 
		private JPanel paneArguments; 
		
		private FindingArgumentPane findingArgumentPane;  
		
		private JButton btnInsert; 
		private JButton btnClear; 
		private JButton btnBack; 
		
		private JToolBar jtbOptions; 
		
		private JToolBar jtbName; 
		
		public RandonVariableInstanceEditionPane(DomainResidentNode _residentNode){
			
			super(new BorderLayout()); 
			this.residentNode = _residentNode; 
			
			nodeName = new JLabel(residentNode.getName()); 
			nodeName.setAlignmentX(JLabel.CENTER_ALIGNMENT); 
			nodeName.setBackground(Color.YELLOW); 
			
			findingArgumentPane = new FindingArgumentPane((DomainResidentNode)residentNode, mebnController); 
			
			btnBack = new JButton(iconController.getEditUndo()); 
			btnClear = new JButton(iconController.getEditClear()); 
			btnInsert = new JButton(iconController.getMoreIcon()); 
			
			jtbOptions = new JToolBar(); 
			jtbOptions.setLayout(new GridLayout(1,3)); 
			jtbOptions.add(btnBack); 
			jtbOptions.add(btnClear); 
			jtbOptions.add(btnInsert); 
			jtbOptions.setFloatable(false); 
			
			btnBack.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					showRandonVariableListPane(); 
				}
			}); 
			
			btnClear.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					findingArgumentPane.clear(); 
				}
			}); 
			
			btnInsert.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					try {
						ObjectEntityInstance[] arguments = findingArgumentPane.getArguments();
						Entity state = findingArgumentPane.getState(); 
						if(state == null){
							JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), 
									resource.getString("stateUnmarked"), 
									resource.getString("error"), 
									JOptionPane.ERROR_MESSAGE);							
						}else{
                            mebnController.createRandonVariableFinding(residentNode, arguments, state); 
							showRandonVariableInstanceListPane(residentNode);
						}
					} catch (ParcialStateException e1) {
						JOptionPane.showMessageDialog(mebnController.getMebnEditionPane(), 
									resource.getString("argumentMissing"), 
									resource.getString("error"), 
									JOptionPane.ERROR_MESSAGE);
					} 
				}
			}); 			
			
			this.add(new JScrollPane(findingArgumentPane), BorderLayout.CENTER); 
			this.add(jtbOptions, BorderLayout.PAGE_END); 
			
			TitledBorder titledBorder; 
			
			titledBorder = BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(Color.BLUE), 
					resource.getString("AddFinding") + ": " +residentNode.getName()); 
			titledBorder.setTitleColor(Color.BLUE); 
			titledBorder.setTitleJustification(TitledBorder.CENTER); 
			
			this.setBorder(titledBorder); 
			
		}
		
	}
	
	/**
	 * List of Instances of a resident node. 
	 * 
	 * @author Laecio Lima dos Santos
	 * @version 1.0 (09/09/07)
	 *
	 */
	private class RandonVariableInstanceListPane extends JPanel{
		
		private DomainResidentNode residentNode; 
		
		private JList jlistFindings; 
		private JScrollPane scrollListObjectEntity; 
		private List<RandomVariableFinding> listInstances; 
		private DefaultListModel listModel; 
		
        public RandonVariableInstanceListPane(){
        	super(new BorderLayout()); 
        	
        	listInstances = new ArrayList<RandomVariableFinding>(); 
        	listModel = new DefaultListModel(); 
        	        	
        	jlistFindings = new JList(listModel); 
        	scrollListObjectEntity = new JScrollPane(jlistFindings); 
        	        	
        	this.add(scrollListObjectEntity, BorderLayout.CENTER);
        
		}
		
        public RandonVariableInstanceListPane(DomainResidentNode residentNode){
        	
        	super(new BorderLayout()); 
        	
        	this.residentNode = residentNode; 
        	
        	listInstances = new ArrayList<RandomVariableFinding>(); 
        	listModel = new DefaultListModel(); 
        	for(RandomVariableFinding finding: ((DomainResidentNode)(residentNode)).getRandonVariableFindingList()){
        		listInstances.add(finding); 
        		listModel.addElement(finding); 
        	}
        	
        	jlistFindings = new JList(listModel); 
        	scrollListObjectEntity = new JScrollPane(jlistFindings); 
        	
        	jlistFindings.setCellRenderer(new ListCellRenderer(iconController.getEntityInstanceIcon())); 
        	
        	jlistFindings.addListSelectionListener(
        			new ListSelectionListener(){
        				public void valueChanged(ListSelectionEvent e) {
        				
        				}
        			}  	
        	);
        	
        	this.add(scrollListObjectEntity, BorderLayout.CENTER);
        
        }
		
	}
	
}
