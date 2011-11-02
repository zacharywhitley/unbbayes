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
import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.ParcialStateException;
import unbbayes.gui.mebn.auxiliary.ListCellRenderer;
import unbbayes.gui.mebn.util.OrganizerUtils;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;

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

public class RandomVariableFindingEdtitionPane extends JPanel {

	private RandomVariableListPane randomVariableListPane; 
	private RandomVariableInstanceEditionPane randomVariableInstanceEditionPane; 
	private RandomVariableInstanceListPane randomVariableInstanceListPane; 
	
	private MEBNController mebnController; 
	
	private IconController iconController = IconController.getInstance(); 
  	private static ResourceBundle resource = 
  		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.gui.mebn.resources.Resources.class.getName());

  	private static final String SELECTION_PANE = "SelectionPane"; 
  	private static final String EDITION_PANE = "EditionPane"; 
  	
  	private RandomVariableFinding instanceSelected = null; 
  	private IResidentNode residentSelected = null; 
	private boolean editingInstance = false; //user adding a new instance or only editing a instance previous created. 
	
  	private JPanel upperPanel; 
  	private JPanel downPanel; 
  	
	public RandomVariableFindingEdtitionPane(){
		//empty
	}
  	
	public RandomVariableFindingEdtitionPane(MEBNController mebnController){
		
		super(new BorderLayout()); 
		
		this.mebnController = mebnController; 
		
		upperPanel = new JPanel(new BorderLayout());
		randomVariableListPane = new RandomVariableListPane(); 
		upperPanel.add(randomVariableListPane, BorderLayout.CENTER); 
		
		downPanel = new JPanel(new BorderLayout()); 
		downPanel.add(new RandomVariableInstanceListPane(), BorderLayout.CENTER); 
		
		this.add(upperPanel, BorderLayout.CENTER); 
		this.add(downPanel, BorderLayout.PAGE_END); 
	}
	
	public void showRandomVariableInstanceListPane(IResidentNode node){
		downPanel.removeAll(); 
		randomVariableInstanceListPane = new RandomVariableInstanceListPane(node); 
		downPanel.add(randomVariableInstanceListPane, BorderLayout.CENTER); 
		downPanel.validate(); 
	}
	
	public void showRandomVariableEditionPane(ResidentNode node){
		upperPanel.removeAll(); 
		upperPanel.add(new RandomVariableInstanceEditionPane(node), BorderLayout.CENTER); 
		upperPanel.validate(); 
	}
	
	public void showRandomVariableListPane(){
		upperPanel.removeAll(); 
		randomVariableListPane = new RandomVariableListPane(); 
		upperPanel.add(randomVariableListPane, BorderLayout.CENTER); 
		upperPanel.validate(); 
	}
	
	
	private class RandomVariableListPane extends JPanel{
		
		private JList jlistResident; 
		private JScrollPane scrollListObjectEntity; 
		private DefaultListModel listModel; 

		private JButton btnAddInstance; 
		private JButton btnRemoveInstance; 
		
		private JToolBar jtbOptions; 
		
		public RandomVariableListPane(){
			
			super(new BorderLayout()); 
			
			listModel = new DefaultListModel(); 
			
			List<ResidentNode> listResident = 
				OrganizerUtils.createOrderedResidentNodeList(
						mebnController.getMultiEntityBayesianNetwork()); 
			
			for(ResidentNode node: listResident){
				System.out.println(node);
				listModel.addElement(new ResidentNodeJacket(node));
			}
			
			jlistResident = new JList(listModel); 
			scrollListObjectEntity = new JScrollPane(jlistResident); 
			
			jlistResident.setCellRenderer(new ListCellRenderer(iconController.getYellowNodeIcon())); 
			
			jlistResident.addListSelectionListener(
		            new ListSelectionListener(){
		                public void valueChanged(ListSelectionEvent e) {
		                	if(jlistResident.getSelectedValue() != null){
		                		editingInstance = false; 
		                		btnRemoveInstance.setEnabled(false); 
		                    	residentSelected = (IResidentNode)(
		                    			((ResidentNodeJacket)jlistResident.getSelectedValue()).getResidentNode());  
		                    	showRandomVariableInstanceListPane((IResidentNode)(
		                    			((ResidentNodeJacket)jlistResident.getSelectedValue()).getResidentNode()));
		                	}
		                }
		            }  	
			 );
			
			jtbOptions = new JToolBar();
			jtbOptions.setLayout(new GridLayout(1,4)); 
			jtbOptions.setFloatable(false); 
			
			btnAddInstance = new JButton(iconController.getEdit()); 
			btnRemoveInstance = new JButton(iconController.getLessIcon()); 
			
			btnAddInstance.setToolTipText(resource.getString("editNodeFindingTip")); 
			btnRemoveInstance.setToolTipText(resource.getString("removeFindingTip")); 
			
			btnAddInstance.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(jlistResident.getSelectedValue() != null){
					   showRandomVariableEditionPane((ResidentNode)(
                   			((ResidentNodeJacket)jlistResident.getSelectedValue()).getResidentNode())); 
					}
				}
			}); 
			
			btnRemoveInstance.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(residentSelected != null){
						if(instanceSelected!=null){
							residentSelected.removeRandomVariableFinding(instanceSelected); 
							instanceSelected = null; 
							showRandomVariableInstanceListPane(residentSelected); 
						}
					}
				}
			}); 
			
			
			jtbOptions.add(new JPanel()); 
			jtbOptions.add(btnAddInstance);
			jtbOptions.add(btnRemoveInstance);
			jtbOptions.add(new JPanel());
			
			this.add(scrollListObjectEntity, BorderLayout.CENTER);
			this.add(jtbOptions, BorderLayout.PAGE_END); 
		}		
		
		public void enableBtnRemoveInstance(){
			btnRemoveInstance.setEnabled(true); 
		}
		
		public void unableBtnRemoveInstance(){
			btnRemoveInstance.setEnabled(false); 
		}
		
        
        class ResidentNodeJacket{
        	
        	private IResidentNode resident; 
        	
        	public ResidentNodeJacket(IResidentNode resident){
        		this.resident = resident; 
        	}
        	
        	public String toString(){
        		return "<html>" + resident.toString() + "<font color=blue>" + 
        		" (" + resident.getRandomVariableFindingList().size() + ")" + "</font></style>"; 
        	}
        	
        	public IResidentNode getResidentNode(){
        		return resident; 
        	}
        	
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
	private class RandomVariableInstanceEditionPane extends JPanel{
		
		private final ResidentNode residentNode; 
		
		private JLabel nodeName; 
		
		private JComboBox comboState; 
		private JPanel paneArguments; 
		
		private FindingArgumentPane findingArgumentPane;  
		
		private JButton btnInsert; 
		private JButton btnBack; 
		
		private JToolBar jtbOptions; 
		
		private JToolBar jtbName; 
		
		public RandomVariableInstanceEditionPane(ResidentNode _residentNode){
			
			super(new BorderLayout()); 
			this.residentNode = _residentNode; 
			
			nodeName = new JLabel(residentNode.getName()); 
			nodeName.setAlignmentX(JLabel.CENTER_ALIGNMENT); 
			nodeName.setBackground(Color.YELLOW); 
			
			findingArgumentPane = new FindingArgumentPane(residentNode, mebnController); 
			
			btnBack = new JButton(iconController.getEditUndo()); 
			btnInsert = new JButton(iconController.getMoreIcon()); 
			
			btnBack.setToolTipText(resource.getString("backToNodeSelectionTip")); 
			btnInsert.setToolTipText(resource.getString("addFindingTip")); 
			
			
			jtbOptions = new JToolBar(); 
			jtbOptions.setLayout(new GridLayout(1,4));
			jtbOptions.add(new JPanel()); 
			jtbOptions.add(btnBack); 
			jtbOptions.add(btnInsert); 
			jtbOptions.add(new JPanel()); 
			jtbOptions.setFloatable(false); 
			
			btnBack.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					showRandomVariableListPane(); 
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
                            mebnController.createRandomVariableFinding(residentNode, arguments, state); 
							showRandomVariableInstanceListPane(residentNode);
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
	private class RandomVariableInstanceListPane extends JPanel{
		
		private IResidentNode residentNode; 
		
		private JList jlistFindings; 
		private JScrollPane scrollListObjectEntity; 
		private List<RandomVariableFinding> listInstances; 
		private DefaultListModel listModel; 
		
        public RandomVariableInstanceListPane(){
        	super(new BorderLayout()); 
        	
        	listInstances = new ArrayList<RandomVariableFinding>(); 
        	listModel = new DefaultListModel(); 
        	        	
        	jlistFindings = new JList(listModel); 
        	scrollListObjectEntity = new JScrollPane(jlistFindings); 
        	        	
        	this.add(scrollListObjectEntity, BorderLayout.CENTER);
        
		}
		
        public RandomVariableInstanceListPane(IResidentNode residentNode){
        	
        	super(new BorderLayout()); 
        	
        	this.residentNode = residentNode; 
        	
        	listInstances = new ArrayList<RandomVariableFinding>(); 
        	listModel = new DefaultListModel(); 
        	for(RandomVariableFinding finding: residentNode.getRandomVariableFindingList()){
        		listInstances.add(finding); 
        		listModel.addElement(finding); 
        	}
        	
        	jlistFindings = new JList(listModel); 
        	scrollListObjectEntity = new JScrollPane(jlistFindings); 
        	
        	jlistFindings.setCellRenderer(new ListCellRenderer(iconController.getEntityInstanceIcon())); 
        	
        	jlistFindings.addListSelectionListener(
        			new ListSelectionListener(){
        				public void valueChanged(ListSelectionEvent e) {
        				     editingInstance = true; 
        				     instanceSelected = (RandomVariableFinding)jlistFindings.getSelectedValue();  
        				     
        				     if(randomVariableListPane != null){
        				    	 randomVariableListPane.enableBtnRemoveInstance(); 
        				     }
        				}
        			}  	
        	);
        	
        	this.add(scrollListObjectEntity, BorderLayout.CENTER);
        
        }

		
	}
	
}
