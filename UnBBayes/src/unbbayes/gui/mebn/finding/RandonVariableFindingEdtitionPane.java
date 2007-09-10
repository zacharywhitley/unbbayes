package unbbayes.gui.mebn.finding;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.gui.mebn.auxiliary.ListCellRenderer;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.RandonVariableFinding;
import unbbayes.prs.mebn.ResidentNode;

/**
 * Pane for edition of instances of the randon variables (resident nodes). 
 * 
 * The painel is divided in two parts: 
 * 1) Paine for selection of node / Pane for edition of instance 
 * 2) List os instances of the node selected
 * 
 * @author Laecio Lima dos Santos
 * @version 1.0 (09/09/07)
 */

public class RandonVariableFindingEdtitionPane extends JPanel{

	private RandonVariableListPane randonVariableListPane; 
	private RandonVariableInstanceEditionPane randonVariableInstanceEditionPane; 
	private RandonVariableInstanceListPane randonVariableInstanceListPane; 
	
	private MEBNController mebnController; 
	
	private IconController iconController = IconController.getInstance(); 
  	private static ResourceBundle resource = 
  		ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

	
	public RandonVariableFindingEdtitionPane(MEBNController mebnController){
		
		super(new BorderLayout()); 
		this.mebnController = mebnController; 
		
		randonVariableListPane = new RandonVariableListPane(); 
		randonVariableInstanceListPane = new RandonVariableInstanceListPane(); 
		
		this.add(randonVariableListPane, BorderLayout.PAGE_START); 
		this.add(randonVariableInstanceListPane, BorderLayout.CENTER); 
		
		
	}
	
	public void showRandonVariableInstanceListPane(ResidentNode node){
		
	}
	
	public void showRandonVariableEditionPane(ResidentNode node){
		
	}
	
	private class RandonVariableListPane extends JPanel{
		
		private JList jlistResident; 
		private JScrollPane scrollListObjectEntity; 
		private List<ResidentNode> listNodes; 
		private DefaultListModel listModel; 
		
		private JButton btnEditNode; 
		private JToolBar jtbOptions; 
		
		public RandonVariableListPane(){
			
			super(new BorderLayout()); 
			
			listNodes = new ArrayList<ResidentNode>(); 
			listModel = new DefaultListModel(); 
			for(MFrag mfrag: mebnController.getMultiEntityBayesianNetwork().getMFragList()){
				for(ResidentNode node: mfrag.getResidentNodeList()){
					listNodes.add(node); 
					listModel.addElement(node); 
				}
			}
			
			jlistResident = new JList(listModel); 
			scrollListObjectEntity = new JScrollPane(jlistResident); 
			
			jlistResident.setCellRenderer(new ListCellRenderer(iconController.getResidentNodeIcon())); 
			
			jlistResident.addListSelectionListener(
		            new ListSelectionListener(){
		                public void valueChanged(ListSelectionEvent e) {
		                	showRandonVariableInstanceListPane((ResidentNode)(jlistResident.getSelectedValue())); 
		                }
		            }  	
			 );
			
			jtbOptions = new JToolBar();
			btnEditNode = new JButton("Editar"); 
			jtbOptions.add(btnEditNode); 
			
			this.add(scrollListObjectEntity, BorderLayout.CENTER);
			this.add(jtbOptions, BorderLayout.PAGE_END); 
			
		}		
	}
	
	/**
	 * Pane contains: 
	 * - Name of ResidentNode 
	 * - List of arguments 
	 * - Selection of state
	 * - buttons for actions
	 * 
	 * @author Laecio Lima dos Santos (laecio@gmail.com)
	 * @version 1.0 (09/09/07)
	 */
	private class RandonVariableInstanceEditionPane extends JPanel{
		
		private ResidentNode residentNode; 
		
		private JLabel nodeName; 
		
		private JComboBox comboState; 
		private JPanel paneArguments; 
		
		private JButton btnInsert; 
		private JButton btnClear; 
		private JButton btnBack; 
		
		private JToolBar jtbOptions; 
		
		public RandonVariableInstanceEditionPane(ResidentNode residentNode){
			
			super(new BorderLayout()); 
			this.residentNode = residentNode; 
			
			btnBack = new JButton("Back"); 
			btnClear = new JButton("Clear"); 
			btnInsert = new JButton("Insert"); 
			
			jtbOptions = new JToolBar(); 
			jtbOptions.add(btnBack); 
			jtbOptions.add(btnClear); 
			jtbOptions.add(btnInsert); 
			
			this.add(jtbOptions, BorderLayout.LINE_END); 
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
		
		private ResidentNode residentNode; 
		
		private JList jlistFindings; 
		private JScrollPane scrollListObjectEntity; 
		private List<RandonVariableFinding> listInstances; 
		private DefaultListModel listModel; 
		
        public RandonVariableInstanceListPane(){
			super(); 
		}
		
        public RandonVariableInstanceListPane(ResidentNode residentNode){
        	
        	super(new BorderLayout()); 
        	
        	this.residentNode = residentNode; 
        	
        	listInstances = new ArrayList<RandonVariableFinding>(); 
        	listModel = new DefaultListModel(); 
        	for(RandonVariableFinding finding: ((DomainResidentNode)(residentNode)).getRandonVariableFindingList()){
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
