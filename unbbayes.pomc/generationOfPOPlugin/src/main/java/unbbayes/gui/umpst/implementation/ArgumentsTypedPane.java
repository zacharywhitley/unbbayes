package unbbayes.gui.umpst.implementation;

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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import unbbayes.controller.umpst.FormulaTreeControllerUMP;
import unbbayes.model.umpst.implementation.CauseVariableModel;
import unbbayes.model.umpst.implementation.EffectVariableModel;
import unbbayes.model.umpst.implementation.EventNCPointer;
import unbbayes.model.umpst.implementation.EventVariableObjectModel;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.rule.RuleModel;

public class ArgumentsTypedPane extends JPanel {
	
	private RuleModel rule;
	private EventVariableObjectModel eventVariable = null;
	private EventNCPointer eventNCPointer;
	private CauseVariableModel causeVariable;
	private EffectVariableModel effectVariable;
	private OrdinaryVariableModel ovSelected[];
	
	private FormulaEditionPane formulaEditionPane;
	private FormulaTreeControllerUMP formulaTreeController;
	
//	private EventNCPointer eventVariable;
	private JPanel argPane;
	
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.gui.umpst.resources.Resources.class.getName());

	public ArgumentsTypedPane(FormulaEditionPane formulaEditonPane, RuleModel rule, EventNCPointer eventNCPointer) {
		super();
		this.formulaEditionPane = formulaEditionPane;
		this.formulaTreeController = formulaEditonPane.getFormulaTreeController();
		this.rule = rule;
		this.eventNCPointer = eventNCPointer;
		this.eventVariable = eventNCPointer.getEventVariable();
		
		ovSelected = new OrdinaryVariableModel[eventVariable.getRelationshipModel().getEntityList().size()];
		createArgumentsTypedPane();
	}
	
	public void createArgumentsTypedPane() {
		final JFrame frame = new JFrame("Adding argument");
		argPane = new JPanel(new BorderLayout());
		
		int numberArguments = eventVariable.getRelationshipModel().getEntityList().size();
		
		JPanel listPane =  new JPanel(new BorderLayout());
		listPane.setLayout(new GridLayout(numberArguments + 1, 1));
		
		JComboBox argument[] = new JComboBox[numberArguments];
		
		JLabel variableName = new JLabel(eventVariable.getRelationship()); 
		variableName.setOpaque(true); 
		variableName.setHorizontalAlignment(JLabel.CENTER); 
		variableName.setBorder(BorderFactory.createLineBorder(Color.BLACK)); 
		
		JPanel titlePanel = new JPanel(new BorderLayout()); 
		titlePanel.add(variableName, BorderLayout.CENTER); 
//		panel.add(btnOpenResidentNode, BorderLayout.LINE_END);		
		listPane.add(titlePanel);
		
		JToolBar tbArgX;
		JButton btnArgXNumber; 
		JButton btnArgXType; 
		
		// Set if ordinaryVariableList is empty
		boolean listEmpty = (rule.getOrdinaryVariableList().size() == 0) ; 
			  
		//Build ComboBox for each argument
		ArrayList<OrdinaryVariableModel> ovList = (ArrayList<OrdinaryVariableModel>)rule.getOrdinaryVariableList();		
		for(int i = 0; i < numberArguments; i++){			
			tbArgX = new JToolBar();			
			Vector<OrdinaryVariableModel> list = new Vector<OrdinaryVariableModel>(); 
			Vector<String> argList = new Vector<String>();
			list.add(null); // null element
			argList.add(null);

			// List OVs to argument box
			for(OrdinaryVariableModel ov: ovList){				
				if (eventVariable.getRelationshipModel().getEntityList().get(i).getName()
						.equals(ov.getTypeEntity())) {
					list.add(ov);
					argList.add(ov.getVariable());
				}				
			}
			
			argument[i] = new JComboBox(argList); 
			argument[i].addItemListener(new ComboListener(i, list)); 
			
			//Adding components to panel
			btnArgXNumber = new JButton("" + (i+1));
			btnArgXNumber.setBackground(new Color(193, 207, 180)); 
			btnArgXType = new JButton(eventVariable.getRelationshipModel().getEntityList().get(i).getName()); 
			btnArgXType.setBackground(new Color(193, 210, 205)); 
			
			tbArgX.add(btnArgXNumber); 
			tbArgX.add(btnArgXType); 
			tbArgX.add(argument[i]); 
			tbArgX.setFloatable(false);
			
			listPane.add(tbArgX); 
			
		}
		argPane.add(listPane);
		
		JButton btnClose = new JButton("Close"); 
		btnClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				frame.dispose();
			}
		});
		
		JButton btnOk = new JButton("Confirm"); 
		btnOk.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				
				// Check if there are null arguments
				if (getOvSelected() != null) {
					boolean approved = true;
					OrdinaryVariableModel[] ovConfirmed = getOvSelected();
					for (int i = 0; i < getOvSelected().length; i++) {
						if (ovConfirmed[i] == null) {
							System.err.println("Error. Null argument");
							approved = false; 
							break;
						}
					}
					if (approved) {
						if (eventNCPointer.getOvArgumentList().size() > 0) {						
							eventNCPointer.removeOVArgumentList();
						}						
						for (int i = 0; i < getOvSelected().length; i++) {
							eventNCPointer.getOvArgumentList().add(ovConfirmed[i]);
						}
						formulaTreeController.updateArgumentsOfObject(eventNCPointer);
						formulaTreeController.updateFormulaActiveContextNode();
						frame.dispose();
					}
				}
			}
		});
		
		tbArgX = new JToolBar();		
		tbArgX.setFloatable(false);
//		tbArgX.setLayout(new GridLayout());	
		tbArgX.add(new JPanel());
		tbArgX.add(btnOk);
		tbArgX.add(btnClose);
		
		argPane.add(tbArgX, BorderLayout.PAGE_END);
//		argPane.add(toolBar);
		
		frame.add(argPane);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
//		frame.setLocationRelativeTo(buttonBackAtributes);
		frame.setSize(300,150);
		frame.setVisible(true);
	}
	
	class ComboListener implements ItemListener {		
		int indice;
		Vector<OrdinaryVariableModel> ovList;		
		
		public ComboListener(int i, Vector<OrdinaryVariableModel> list){
			indice = i;
			ovList = list;
		}
		
		public void itemStateChanged(ItemEvent e) {			
			JComboBox combo = (JComboBox)e.getSource(); 
			if(combo.getSelectedItem() != null){
				Object ov = combo.getSelectedItem(); // The object is string argument
				for (int i = 1; i < ovList.size(); i++) { // The first element is null
					if (ov.toString().equals(ovList.get(i).getVariable())) {
						ovSelected[indice] = ovList.get(i);
						break;
					}
				}		
//				OrdinaryVariableModel ov = (OrdinaryVariableModel)combo.getSelectedItem(); 
//				try{
//					eventVariable.addOrdinaryVariable(ov, indice);
//					mebnController.updateArgumentsOfObject(node); 
//				}
//				catch(OVDontIsOfTypeExpected ex){
//					ex.printStackTrace(); 
//				}
			}
		}		
	}

	/**
	 * @return the eventVariable
	 */
	public EventVariableObjectModel getEventVariable() {
		return eventVariable;
	}

	/**
	 * @param eventVariable the eventVariable to set
	 */
	public void setEventVariable(EventVariableObjectModel eventVariable) {
		this.eventVariable = eventVariable;
	}

	/**
	 * @return the ovSelected
	 */
	public OrdinaryVariableModel[] getOvSelected() {
		return ovSelected;
	}

	/**
	 * @param ovSelected the ovSelected to set
	 */
	public void setOvSelected(OrdinaryVariableModel ovSelected[]) {
		this.ovSelected = ovSelected;
	}
}
