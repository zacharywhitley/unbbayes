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

package unbbayes.gui.mebn.cpt;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.prs.mebn.IMultiEntityNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.compiler.ICompiler;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.util.ResourceController;

public class CPTEditionPane extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MEBNController mebnController; 
	private ResidentNode residentNode; 

	private CPTTextPane cptTextPane; 
	private JpFather jpFather; 
	private JpArguments jpArguments; 
	private JpStates jpStates; 
	private JPanel jpButtonsEdition; 
	private JpTurnOptionsPane jpOptionsPane; 
	
	private JTextField txtPosition; 
	
	private JFrame fatherDialog; 

	private static ResourceBundle resource = ResourceController.newInstance().getBundle(
			unbbayes.gui.mebn.resources.Resources.class.getName());

	public CPTEditionPane(MEBNController mebnController, ResidentNode residentNode){
		super(); 
		this.mebnController = mebnController; 
		this.residentNode = residentNode; 

		fatherDialog = (JFrame)this.getParent(); 
		
		cptTextPane = new CPTTextPane(this); 

		jpFather = new JpFather(); 
		jpArguments = new JpArguments(); 
		jpStates = new JpStates(); 

		jpButtonsEdition = new JpButtons(); 

		jpOptionsPane = new JpTurnOptionsPane(); 
		
		setLayout(new BorderLayout());
		 
		
		JPanel centerPane = new JPanel(new BorderLayout());
		centerPane.add(jpButtonsEdition, BorderLayout.NORTH); 
		
		JScrollPane jspCptTextPane = new JScrollPane(cptTextPane); 
		centerPane.add(jspCptTextPane, BorderLayout.CENTER);
		centerPane.add(new JpMainButtons(), BorderLayout.SOUTH); 
		
		add(centerPane, BorderLayout.CENTER); 
		
		add(jpOptionsPane, BorderLayout.WEST); 
		
		this.setMinimumSize(new Dimension(750, 300)); 
		this.setPreferredSize(new Dimension(750, 300)); 
	}
	
	public ResidentNode getResidentNode(){
		return this.residentNode; 
	}

	/**
	 * uses 
	 * - jpOptions
	 * - jpFather
	 * - jpArguments
	 * - jpStates
	 */
	private class JpTurnOptionsPane extends JPanel{
		
		private JButton btnSelectFatherTab; 
//		private JButton btnSelectArgumentTab; 
		private JButton btnSelectStatesTab; 
		private JPanel jpOptions; 
		private CardLayout cardLayout;   
		
		private static final String TAB_FATHER = "FatherTab"; 
		private static final String TAB_ARGUMENTS = "ArgumentsTab"; 
		private static final String TAB_STATES = "StatesTab"; 
		
		private JpTurnOptionsPane(){
			super();    
			
			//Tabs...
			cardLayout = new CardLayout(); 
			jpOptions = new JPanel(cardLayout);
			jpOptions.add(TAB_FATHER, jpFather); 
			jpOptions.add(TAB_ARGUMENTS, jpArguments); 
			jpOptions.add(TAB_STATES, jpStates); 

			cardLayout.show(jpOptions, TAB_FATHER);
			
			//Buttons
			JPanel jpButtons = new JPanel(new GridLayout(1,2));
			
			btnSelectFatherTab = new JButton(resource.getString("fatherCPT")); 
			btnSelectFatherTab.setToolTipText(resource.getString("fatherCPTTip")); 
			
			btnSelectStatesTab = new JButton(resource.getString("statesCPT")); 
			btnSelectStatesTab.setToolTipText(resource.getString("statesCPTTip")); 
			
			btnSelectStatesTab.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					cardLayout.show(jpOptions, TAB_STATES); 
				}
			}); 
			
			btnSelectFatherTab.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					cardLayout.show(jpOptions, TAB_FATHER); 
				}
			}); 
			
//			btnSelectArgumentTab.addActionListener( new ActionListener(){
//				public void actionPerformed(ActionEvent e){
//					cardLayout.show(jpOptions, TAB_ARGUMENTS); 
//				}
//			}); 
			
			jpButtons.add(btnSelectFatherTab); 
			jpButtons.add(btnSelectStatesTab); 
			
			//All
			this.setLayout(new BorderLayout()); 
//			this.add(jpButtons, BorderLayout.NORTH); 
			this.add(jpOptions, BorderLayout.CENTER);  
			
			this.setPreferredSize(new Dimension(300, 300)); 
			this.setMinimumSize(new Dimension(300, 300)); 
			this.setMaximumSize(new Dimension(300, 300)); 
		}
	}
	
	protected void atualizeCaretPosition(int caretPosition){
          if(txtPosition != null){
        	  txtPosition.setText(resource.getString("position") + ": " + caretPosition ); 
          }
	}
	
	private class JpFather extends JPanel{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private List<InputNode> inputNodeList; 
		
		private final List<ResidentNode> residentNodeAuxList; 
		
		private JList jlStates; 
		private JList jlArguments; 
		
		private DefaultListModel listStatesModel;
		private DefaultListModel listArgumentsModel; 
		private String[] fatherNodeArray; 
		
		//Contains the element that corresponds to each index of residentNodeAuxList
		//(the InputNode or the ResidentNode)
		private List<MultiEntityNode> fatherNodeList; 	

		JpFather(){
			super(); 
			setMinimumSize(new Dimension(100,100));			

			residentNodeAuxList = new ArrayList<ResidentNode>(); 
			fatherNodeList = new ArrayList<MultiEntityNode>(); 
			
			//THE NODE
			residentNodeAuxList.add(residentNode); 
			fatherNodeList.add(residentNode); 
			
			//INPUT NODE FATHERS
			inputNodeList = residentNode.getParentInputNodesList(); 
			for(InputNode inputNode: inputNodeList){
				Object father = inputNode.getInputInstanceOf();
				if (father instanceof ResidentNode){
					residentNodeAuxList.add((ResidentNode)father); 
					fatherNodeList.add(inputNode); 
				}
			}
			
			//RESIDENT NODE FATHERS
			for(ResidentNode resident: residentNode.getResidentNodeFatherList()){
				residentNodeAuxList.add(resident); 
				fatherNodeList.add(resident); 
			}			

			fatherNodeArray = new String[residentNodeAuxList.size()]; 

			int i = 0; 

			for(ResidentNode node: residentNodeAuxList){
				fatherNodeArray[i] = node.getName(); 
				i++; 
			}

			final JList jlFathers = new JList(fatherNodeArray);
			JScrollPane jscJlOFathers = new JScrollPane(jlFathers); 

			jlFathers.addMouseListener(new MouseAdapter() {

				public void mousePressed(MouseEvent e) {

					if ((e.getModifiers() == MouseEvent.BUTTON1_MASK) && (e.getClickCount() == 2)){
						int selectedIndex = jlFathers.getSelectedIndex(); 
						cptTextPane.insertNode(fatherNodeArray[selectedIndex]); 
					}
					else{
						if ((e.getModifiers() == MouseEvent.BUTTON1_MASK) && (e.getClickCount() == 1)){
							jlArguments.setEnabled(true); 
							int selectedIndex = jlFathers.getSelectedIndex(); 
							ResidentNode residentNode = residentNodeAuxList.get(selectedIndex); 
							updateStatesList(residentNode);
							
							IMultiEntityNode node = fatherNodeList.get(selectedIndex); 
						    // let's assume node != null.
							if(node instanceof InputNode){
						    	List<OrdinaryVariable> listOrdinaryVariable = ((InputNode)node).getOrdinaryVariableList(); 
						    	updateArgumentsList(listOrdinaryVariable);	
						    }else{
						    	List<OrdinaryVariable> listOrdinaryVariable = ((ResidentNode)node).getOrdinaryVariableList(); 
						    	updateArgumentsList(listOrdinaryVariable);
						    	if (node.equals(getResidentNode())){
						    		jlArguments.setEnabled(false); 
						    	}
						    }
						}					
					}

				}

			});

			/* Lista com os estados do nodo pai selecionado */
			listStatesModel = new DefaultListModel();
			jlStates = new JList(listStatesModel); 
			JScrollPane jspStates = new JScrollPane(jlStates); 

			jlStates.addMouseListener(new MouseAdapter() {

				public void mousePressed(MouseEvent e) {

					if ((e.getModifiers() == MouseEvent.BUTTON1_MASK) && (e.getClickCount() == 2)){
						String selectedIndex = (String)jlStates.getSelectedValue(); 
						cptTextPane.insertStateFather( selectedIndex); 
					}	
				}

			});

			/* Lista com os argumentos do nodo selecionado */
			listArgumentsModel = new DefaultListModel();
			jlArguments = new JList(listArgumentsModel); 
			JScrollPane jspArguments = new JScrollPane(jlArguments); 

			jlArguments.addMouseListener(new MouseAdapter() {

				public void mousePressed(MouseEvent e) {

					if ((e.getModifiers() == MouseEvent.BUTTON1_MASK) && (e.getClickCount() == 2)){
						String selectedIndex = (String)jlArguments.getSelectedValue(); 
						cptTextPane.insertParamSet(selectedIndex); 
					}	
				}

			});
			
			setLayout(new GridLayout(2,0)); 

			jscJlOFathers.setBorder(MebnToolkit.getBorderForTabPanel(resource.getString("NodesTitle"))); 	
			jspStates.setBorder(MebnToolkit.getBorderForTabPanel(resource.getString("StatesTitle"))); 
			jspArguments.setBorder(MebnToolkit.getBorderForTabPanel(resource.getString("ArgumentTitle"))); 
			
			add(jscJlOFathers); 
			
			JPanel jpBottom = new JPanel(new GridLayout(0,2)); 
			jpBottom.add(jspStates); 
			jpBottom.add(jspArguments); 
			
			add(jpBottom); 
			
		}

		private void updateStatesList(ResidentNode resident){

			List<Entity> listStates = resident.getPossibleValueList(); 

			listStatesModel.removeAllElements(); 
			listStatesModel = new DefaultListModel(); 

			for(Entity entity: listStates){
				listStatesModel.addElement(entity.getName()); 
			}

			jlStates.setModel(listStatesModel); 
		}

		private void updateArgumentsList(List<OrdinaryVariable> ovList){

			listArgumentsModel.removeAllElements(); 
			listArgumentsModel = new DefaultListModel(); 

			for(OrdinaryVariable entity: ovList){
				listArgumentsModel.addElement(entity.getName()); 
			}

			jlArguments.setModel(listArgumentsModel); 
		}
		
	}

	private class JpArguments extends JPanel{

		private String[] oVariableArray; 

		JpArguments(){

			setLayout(new BorderLayout()); 
			int i; 

			/* Lista com as variavies ordinarias */

			setBorder(MebnToolkit.getBorderForTabPanel(resource.getString("ArgumentTitle"))); 

			List<OrdinaryVariable> oVariableList = residentNode.getOrdinaryVariableList(); 
			oVariableArray = new String[oVariableList.size()]; 
			i = 0; 

			for(OrdinaryVariable ov: oVariableList){
				oVariableArray[i] = ov.getName(); 
				i++; 
			}

			final JList jlOVariable = new JList(oVariableArray);
			JScrollPane jscJlOVariable = new JScrollPane(jlOVariable); 

			add(jscJlOVariable, BorderLayout.CENTER); 

			jlOVariable.addMouseListener(new MouseAdapter() {

				public void mousePressed(MouseEvent e) {

					if ((e.getModifiers() == MouseEvent.BUTTON1_MASK) && (e.getClickCount() == 2)){
						int selectedIndex = jlOVariable.getSelectedIndex(); 
						cptTextPane.insertParamSet(oVariableArray[selectedIndex]); 
					}

				}
			});   	

		}


	}

	private class JpStates extends JPanel{

		private String[] statesArray; 

		private JpStates(){
			final JList jlStates; 
			JScrollPane jscJlStates; 
			int i; 

			setLayout(new BorderLayout()); 
			/* lista with the states */

			setBorder(MebnToolkit.getBorderForTabPanel(resource.getString("StatesTitle"))); 

			List<Entity> statesList = residentNode.getPossibleValueList(); 
			statesArray = new String[statesList.size()]; 
			i = 0; 

			for(Entity state: statesList){
				statesArray[i] = state.getName(); 
				i++; 
			}

			jlStates = new JList(statesArray);
			jscJlStates = new JScrollPane(jlStates); 

			add(jscJlStates, BorderLayout.CENTER); 

			jlStates.addMouseListener(new MouseAdapter() {

				public void mousePressed(MouseEvent e) {

					if ((e.getModifiers() == MouseEvent.BUTTON1_MASK) && (e.getClickCount() == 2)){
						int selectedIndex = jlStates.getSelectedIndex(); 
						cptTextPane.insertState(statesArray[selectedIndex]); 
					}

				}
			}); 

		}

	}

	private class JpButtons extends JPanel{

		//Buttons
		private JButton btnIfAnyClause; 
		private JButton btnIfAllClause; 
		private JButton btnElseClause; 
		private JButton btnEraseAll; 
		private JButton btnDefaultClause; 

		private JButton btnEqual; 
		private JButton btnAnd; 
		private JButton btnOr; 
		private JButton btnNot; 

		private JButton btnCardinality; 
		private JButton btnMax; 
		private JButton btnMin; 
		
		JpButtons(){
			super(); 

			Font font = new Font("Serif", Font.BOLD, 10); 

			btnEraseAll = new JButton(resource.getString("clear"));
			btnEraseAll.setFont(font); 
			btnEraseAll.setToolTipText(resource.getString("deleteTip")); 

			btnIfAnyClause = new JButton(resource.getString("ifAny"));
			btnIfAnyClause.setFont(font);
			btnIfAnyClause.setToolTipText(resource.getString("anyTip")); 

			btnIfAllClause = new JButton(resource.getString("ifAll"));
			btnIfAllClause.setFont(font);
			btnIfAllClause.setToolTipText(resource.getString("allTip")); 

			btnElseClause = new JButton(resource.getString("else")); 
			btnElseClause.setFont(font);
			btnElseClause.setToolTipText(resource.getString("elseTip")); 

			btnDefaultClause = new JButton(resource.getString("default"));
			btnDefaultClause.setFont(font);
			btnDefaultClause.setToolTipText(resource.getString("defaultTip")); 
			
			btnEqual= new JButton(resource.getString("equal")); 
			btnEqual.setFont(font);
			btnEqual.setToolTipText(resource.getString("equalTip")); 

			btnAnd= new JButton(resource.getString("and")); 
			btnAnd.setFont(font);
			btnAnd.setToolTipText(resource.getString("andTip")); 

			btnOr= new JButton(resource.getString("or"));
			btnOr.setFont(font);
			btnOr.setToolTipText(resource.getString("orTip")); 

			btnNot= new JButton(resource.getString("not"));     	
			btnNot.setFont(font);
			btnNot.setToolTipText(resource.getString("notTip")); 

			btnCardinality= new JButton(resource.getString("card"));
			btnCardinality.setFont(font);
			btnCardinality.setToolTipText(resource.getString("cadinalityTip"));

			btnMax= new JButton(resource.getString("max")); 
			btnMax.setFont(font);
			btnMax.setToolTipText(resource.getString("maxTip")); 

			btnMin= new JButton(resource.getString("min"));     
			btnMin.setFont(font);
			btnMin.setToolTipText(resource.getString("minTip")); 

			setLayout(new GridLayout(2,6)); 

			//FIRST LINE
			add(btnIfAnyClause); 
			add(btnIfAllClause);
			add(btnElseClause);
			add(btnDefaultClause); 
			add(btnEqual); 
			add(btnEraseAll); 
			
			//SECOND LINE
			add(btnAnd);
			add(btnOr); 
			add(btnNot); 
			add(btnMax); 
			add(btnMin); 
			add(btnCardinality);
			
			addButtonsListeners();

		}
		

		private void addButtonsListeners(){
			
			btnIfAnyClause.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					cptTextPane.insertIfClause(0); 
				}
			}); 
			
			btnIfAllClause.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					cptTextPane.insertIfClause(1); 
				}
			});  
			
			btnElseClause.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					cptTextPane.insertElseClause(); 
				}
			}); 
			
			
			btnEqual.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					cptTextPane.insertEqualOperator(); 
				}
			}); 
			
			btnAnd.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					cptTextPane.insertAndOperator(); 
				}
			}); 
			
			btnOr.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					cptTextPane.insertOrOperator();
				}
			});  
			
			btnNot.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					cptTextPane.insertNotOperator();
				}
			});
			
			btnCardinality.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					cptTextPane.insertCardinalityClause(); 
				}
			}); 
			
			btnMax.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					cptTextPane.insertMaxClause(); 
				}
			}); 
			
			btnMin.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					cptTextPane.insertMinClause(); 
				}
			});   
			
			btnEraseAll.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					cptTextPane.clearTable(); 
				}
			}); 
			
			btnDefaultClause.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					cptTextPane.insertDefaultClause(); 
				}
			}); 
		}

	}
	
	private class JpMainButtons extends JPanel{

		
		private JButton btnCompile; 
		private JButton btnExit; 
		private JButton btnSave; 
		
		
		JpMainButtons(){
			super(); 

			Font font = new Font("Serif", Font.PLAIN, 12); 

			btnCompile = new JButton(resource.getString("compileCPT"));
			btnCompile.setFont(font); 
			btnCompile.setToolTipText(resource.getString("compileCPTTip")); 

			btnSave = new JButton(resource.getString("saveCPT"));
			btnSave.setFont(font);
			btnSave.setToolTipText(resource.getString("saveCPTTip")); 

			btnExit = new JButton(resource.getString("exitCPT"));
			btnExit.setFont(font);
			btnExit.setToolTipText(resource.getString("exitCPTTip")); 
			
			txtPosition = new JTextField(); 
			txtPosition.setEditable(false); 
			txtPosition.setForeground(Color.black); 
			txtPosition.setBackground(Color.WHITE); 
			txtPosition.setAlignmentX(JTextField.CENTER_ALIGNMENT); 
			
			setLayout(new GridLayout(0,4)); 
			
			//Fourth row
			add(txtPosition); 
			add(btnSave); 
			add(btnCompile); 
			add(btnExit); 

			addListeners(); 
		}
		
		private void addListeners(){
			
			btnSave.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					mebnController.saveCPT(residentNode, cptTextPane.getTableTxt()); 
					JOptionPane.showMessageDialog(mebnController.getCPTEditionFrame(residentNode), 
							resource.getString("CptSaveOK"), resource.getString("sucess"), 
							JOptionPane.INFORMATION_MESSAGE);
					mebnController.openCPTDialog(residentNode); 
				}
				
			}); 
			
			btnCompile.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					ICompiler compiler = getResidentNode().getCompiler();
					
					try {
						compiler.init(cptTextPane.getTableTxt());
						compiler.parse(); 
						JOptionPane.showMessageDialog(mebnController.getCPTEditionFrame(residentNode), 
								resource.getString("CptCompileOK"), resource.getString("sucess"), 
								JOptionPane.INFORMATION_MESSAGE);
					} catch (MEBNException e1) {
						JOptionPane.showMessageDialog(mebnController.getCPTDialog(residentNode), 
								e1.getMessage() + " > " + compiler.getIndex() + " <", resource.getString("error"), 
								JOptionPane.ERROR_MESSAGE);
					} catch (Exception exc) {
						// this is an unknown exception...
						exc.printStackTrace();
						JOptionPane.showMessageDialog(mebnController.getCPTDialog(residentNode), 
								exc.getMessage(), resource.getString("error"), 
								JOptionPane.ERROR_MESSAGE);
					}
					
					mebnController.openCPTDialog(residentNode); 
				}
				
			}); 
			
			btnExit.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					mebnController.closeCPTDialog(residentNode); 
				}
				
			}); 
			
		}

	}

}
