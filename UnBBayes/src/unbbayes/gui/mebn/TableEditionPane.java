package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import unbbayes.controller.MEBNController;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.exception.EntityNotPossibleValueOfNodeException;
import unbbayes.prs.mebn.exception.NodeNotPresentInMTheoryException;
import unbbayes.prs.mebn.table.TableParser;
import unbbayes.prs.mebn.table.exception.InvalidProbabilityFunctionOperandException;
import unbbayes.prs.mebn.table.exception.TableFunctionMalformedException;

/**
 * Tabela de distribuição de probabilidade de um nodo Resident.
 * Contem um editor de texto e um menu com opções para facilitar a 
 * edição (botões de auto texto e botões para abrir lista de 
 * escolhas de variaveis da ontologia. 
 * 
 * @author Laecio Lima dos Santos
 * @version 1.0 (14/12/06)
 *
 */

public class TableEditionPane extends JPanel{
	
	private JTextPane txtPane;
	private JScrollPane jsTxtPane; 
	
	private StyledDocument doc; 
	
	/* posicao onde se enconra o prompt */
	private int positionCaret = 0; 
	
	/* buttons */
	private JPanel jpButtonsEdition; 
	
	private JPanel jpOptions; 
	private JPanel jpFather; 
	private JPanel jpArguments; 
	private JPanel jpStates;
	
	private JButton btnIfAnyClause; 
	private JButton btnIfAllClause; 
	private JButton btnElseClause; 
	private JButton btnEraseAll; 
	
	private JButton btnEqual; 
	private JButton btnAnd; 
	private JButton btnOr; 
	private JButton btnNot; 
	
	private JButton btnNumber; 
	private JButton btnCardinality; 
	private JButton btnMax; 
	private JButton btnMin; 
	
	private JButton btnStates; 
	private JButton btnFathers; 
	private JButton btnArguments; 
	
	private JButton btnCompile; 
	
	private JButton btnExit; 
	
	
	private DomainResidentNode residentNode; 
	private String[] oVariableArray; 
	private String[] fatherNodeArray; 
	private String[] statesArray; 
	private ToolKitForTableEdition toolKit; 
	
	private CardLayout cardLayout; 
	
	private JList jlStates; 
	private DefaultListModel listModel;
	
	private MEBNController mebnController; 
	
	private TableEdition tableEdition; 
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
	public TableEditionPane(DomainResidentNode _residentNode, MEBNController _mebnController){
		
		super();
		
		mebnController = _mebnController; 
		residentNode = _residentNode; 
		
		txtPane = new JTextPane(); 
		txtPane.setCaretPosition(0); 
		txtPane.addCaretListener( new CaretListener(){
			public void caretUpdate(CaretEvent e) {
				positionCaret = e.getDot();
			}
		}); 
		
		txtPane.addKeyListener(new TextPaneKeyListener()); 
		
		doc = txtPane.getStyledDocument();
		toolKit = new ToolKitForTableEdition(doc); 
		tableEdition = new TableEdition(residentNode, toolKit); 	
		this.buildTxtEdition(residentNode.getTableFunction(), doc); 

		buildJpButtons(); 
		addButtonsListeners(); 
		
		jsTxtPane = new JScrollPane(txtPane);
		jsTxtPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		jsTxtPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		cardLayout = new CardLayout();         
		jpOptions = new JPanel(cardLayout);
		
		jpFather = buildJpFather(); 
		jpArguments = buildJpArguments(); 
		jpStates = buildJpStates(); 
		
		jpOptions.add("FatherTab", jpFather); 
		jpOptions.add("ArgumentsTab", jpArguments); 
		jpOptions.add("StatesTab", jpStates); 
		
		cardLayout.show(jpOptions, "ArgumentsTab"); 
		
		GridBagLayout gridbag = new GridBagLayout(); 
		GridBagConstraints constraints = new GridBagConstraints(); 
		
		this.setLayout(gridbag); 
		
		constraints.gridx = 0; 
		constraints.gridy = 0; 
		constraints.gridwidth = 1; 
		constraints.gridheight = 1; 
		constraints.weightx = 0;
		constraints.weighty = 0; 
		constraints.fill = GridBagConstraints.BOTH; 
		constraints.anchor = GridBagConstraints.NORTH; 
		gridbag.setConstraints(jpButtonsEdition, constraints); 
		this.add(jpButtonsEdition);
		
		constraints.gridx = 1; 
		constraints.gridy = 0; 
		constraints.gridwidth = 1; 
		constraints.gridheight = 1; 
		constraints.weightx = 100;
		constraints.weighty = 0; 
		constraints.fill = GridBagConstraints.BOTH; 
		constraints.anchor = GridBagConstraints.NORTH; 
		gridbag.setConstraints(jsTxtPane, constraints); 
		this.add(jsTxtPane);
		
		constraints.gridx = 2; 
		constraints.gridy = 0; 
		constraints.gridwidth = 1; 
		constraints.gridheight = 1; 
		constraints.weightx = 40;
		constraints.weighty = 0; 
		constraints.fill = GridBagConstraints.BOTH; 
		constraints.anchor = GridBagConstraints.NORTH; 
		gridbag.setConstraints(jpOptions, constraints); 
		this.add(jpOptions);	
		
	}
	
	private void buildJpButtons(){
		
		jpButtonsEdition = new JPanel(new GridLayout(5, 4));
		
		btnEraseAll = new JButton("DEL");
		btnEraseAll.setBackground(Color.RED); 
		btnEraseAll.setForeground(Color.WHITE); 
		
		btnIfAnyClause = new JButton("ANY");
		btnIfAllClause = new JButton("ALL");
		btnElseClause = new JButton("ELS"); 
		
		btnEqual= new JButton("== "); 
		btnAnd= new JButton("AND"); 
		btnOr= new JButton("OR "); 
		btnNot= new JButton("NOT");     	
		
		btnNumber= new JButton("NUM"); 
		btnCardinality= new JButton("CAR"); 
		btnMax= new JButton("MAX"); 
		btnMin= new JButton("MIN");     
		
		btnStates= new JButton("STT"); 
		btnFathers= new JButton("FAT");  
		btnArguments= new JButton("ARG"); 
		
		btnCompile = new JButton("C&S"); 
		btnCompile.setBackground(new Color(102, 169, 1)); //green 
		btnCompile.setForeground(Color.WHITE); 
		
		btnExit = new JButton("EXI");   
		btnExit.setBackground(Color.RED); 
		btnExit.setForeground(Color.WHITE); 
		
		jpButtonsEdition.add(btnEraseAll);
		jpButtonsEdition.add(btnIfAnyClause); 
		jpButtonsEdition.add(btnIfAllClause);
		jpButtonsEdition.add(btnElseClause);
		
		jpButtonsEdition.add(btnEqual); 
		jpButtonsEdition.add(btnAnd);
		jpButtonsEdition.add(btnOr); 
		jpButtonsEdition.add(btnNot); 
		
		jpButtonsEdition.add(btnNumber); 
		jpButtonsEdition.add(btnCardinality);
		jpButtonsEdition.add(btnMax); 
		jpButtonsEdition.add(btnMin); 
		
		//TODO fazer isto de uma forma descente...
		JButton btnPhanton; 
		btnPhanton = new JButton(); 
		btnPhanton.setVisible(false); 
		jpButtonsEdition.add(btnPhanton); 
		btnPhanton = new JButton(); 
		btnPhanton.setVisible(false); 
		jpButtonsEdition.add(btnPhanton);
		btnPhanton = new JButton(); 
		btnPhanton.setVisible(false); 
		jpButtonsEdition.add(btnPhanton);
		
		jpButtonsEdition.add(btnCompile); 
		
		jpButtonsEdition.add(btnStates); 
		jpButtonsEdition.add(btnFathers);
		jpButtonsEdition.add(btnArguments); 
		jpButtonsEdition.add(btnExit); 
		
	}
	
	private JPanel buildJpFather(){
		
		JPanel jpFather; 
		List<MultiEntityNode> fatherNodeList; 		
		List<GenerativeInputNode> inputNodeList; 
		final List<DomainResidentNode> residentNodeAuxList; 
		
		int i; 
		
		/* Lista com os nodos pais */
		jpFather = new JPanel();
		jpFather.setBorder(ToolKitForGuiMebn.getBorderForTabPanel(resource.getString("FathersTitle"))); 				
		
		residentNodeAuxList = residentNode.getResidentNodeFatherList(); 
		
		inputNodeList = residentNode.getInputNodeFatherList(); 
		for(GenerativeInputNode inputNode: inputNodeList){
			Object father = inputNode.getInputInstanceOf();
			//TODO caso BuiltInRV
			if (father instanceof ResidentNode){
				residentNodeAuxList.add((DomainResidentNode)father); 
			}
		}
		
		fatherNodeArray = new String[residentNodeAuxList.size()]; 
		
		i = 0; 
		
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
					insertNode(doc, fatherNodeArray[selectedIndex]); 
					
				}
				else{
					if ((e.getModifiers() == MouseEvent.BUTTON1_MASK) && (e.getClickCount() == 1)){
						int selectedIndex = jlFathers.getSelectedIndex(); 
						List<GenerativeInputNode> inputNodeList = residentNode.getInputNodeFatherList(); 
						//TODO fazer isso de uma forma decente!!!
						ResidentNode residentNode = residentNodeAuxList.get(selectedIndex); 
						updateStatesList(residentNode);
					}					
				}
				
			}
			
		});
		
		/* Lista com os estados do nodo pai selecionado */
		listModel = new DefaultListModel();
		jlStates = new JList(listModel); 
		JScrollPane jspStates = new JScrollPane(jlStates); 
		
		
		jlStates.addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e) {
				
				if ((e.getModifiers() == MouseEvent.BUTTON1_MASK) && (e.getClickCount() == 2)){
					String selectedIndex = (String)jlStates.getSelectedValue(); 
					insertStateFather(doc, selectedIndex); 
				}	
			}
			
		});
		
		
		
		
		jpFather.setLayout(new GridLayout(2,0)); 
		
		jpFather.add(jscJlOFathers); 
		jpFather.add(jspStates); 
		
		return jpFather; 
	}
	
	
	private JPanel buildJpArguments(){
		
		JPanel jpArguments = new JPanel(new BorderLayout()); 
		int i; 
		
		/* Lista com as variavies ordinarias */
		
		jpArguments.setBorder(ToolKitForGuiMebn.getBorderForTabPanel(resource.getString("ArgumentTitle"))); 
		
		List<OrdinaryVariable> oVariableList = residentNode.getOrdinaryVariableList(); 
		oVariableArray = new String[oVariableList.size()]; 
		i = 0; 
		
		for(OrdinaryVariable ov: oVariableList){
			oVariableArray[i] = ov.getName(); 
			i++; 
		}
		
		final JList jlOVariable = new JList(oVariableArray);
		JScrollPane jscJlOVariable = new JScrollPane(jlOVariable); 
		
		jpArguments.add(jscJlOVariable, BorderLayout.CENTER); 
		
		jlOVariable.addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e) {
				
				if ((e.getModifiers() == MouseEvent.BUTTON1_MASK) && (e.getClickCount() == 2)){
					int selectedIndex = jlOVariable.getSelectedIndex(); 
					insertParamSet(doc, oVariableArray[selectedIndex]); 
				}
				
			}
		}); 
		
		return jpArguments;    	
		
	}
	
	private JPanel buildJpStates(){
		
		JPanel jpStates;  
		final JList jlStates; 
		JScrollPane jscJlStates; 
		int i; 
		
		
		jpStates = new JPanel(new BorderLayout());
		/* lista with the states */
		
		jpStates.setBorder(ToolKitForGuiMebn.getBorderForTabPanel(resource.getString("StatesTitle"))); 
		
		List<Entity> statesList = residentNode.getPossibleValueList(); 
		statesArray = new String[statesList.size()]; 
		i = 0; 
		
		for(Entity state: statesList){
			statesArray[i] = state.getName(); 
			i++; 
		}
		
		jlStates = new JList(statesArray);
		jscJlStates = new JScrollPane(jlStates); 
		
		jpStates.add(jscJlStates, BorderLayout.CENTER); 
		
		jlStates.addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e) {
				
				if ((e.getModifiers() == MouseEvent.BUTTON1_MASK) && (e.getClickCount() == 2)){
					int selectedIndex = jlStates.getSelectedIndex(); 
					insertState(doc, statesArray[selectedIndex]); 
				}
				
			}
		}); 
		
		
		return jpStates;  	
		
	}
	
	private void updateStatesList(ResidentNode resident){
		
		List<Entity> listStates = resident.getPossibleValueList(); 
		
		listModel.removeAllElements(); 
		listModel = new DefaultListModel(); 
		
		for(Entity entity: listStates){
			listModel.addElement(entity.getName()); 
		}
		
		jlStates.setModel(listModel); 
	}
	
	private void updateStatesList(){
		
		DomainResidentNode resident = residentNode; 
		List<Entity> listStates = resident.getPossibleValueList(); 
		
		listModel.removeAllElements(); 
		listModel = new DefaultListModel(); 
		
		for(Entity entity: listStates){
			listModel.addElement(entity.getName()); 
		}
		
		jlStates.setModel(listModel); 
	}
	
	
	

	
	/**
	 * choice: 
	 * 0 -> any Clause
	 * 1 -> all Clause
	 * @param doc
	 * @param choice
	 */
	private void insertIfClause(StyledDocument doc, int choice){
		
		Position pos = doc.getEndPosition(); 
		int start = pos.getOffset(); 
		
		try {
			txtPane.replaceSelection(""); 
			doc.insertString(positionCaret, "if", toolKit.getIfStyle());
			doc.insertString(positionCaret, " ", toolKit.getDefaultStype());
			
			if(choice == 0){
				doc.insertString(positionCaret, "any", toolKit.getAnyStyle());
				doc.insertString(positionCaret, " ", toolKit.getDefaultStype());				
				doc.insertString(positionCaret, "paramSubSet", toolKit.getDescriptionStyle()); 
				doc.insertString(positionCaret, " ", toolKit.getDefaultStype());
			}
			else{
				doc.insertString(positionCaret, "all", toolKit.getAnyStyle()); 	
				doc.insertString(positionCaret, " ", toolKit.getDefaultStype());
				doc.insertString(positionCaret, "paramSet", toolKit.getDescriptionStyle());
				doc.insertString(positionCaret, " ", toolKit.getDefaultStype());
			}
	
			doc.insertString(positionCaret, "have", toolKit.getAnyStyle());
			
			doc.insertString(positionCaret, " ( ", toolKit.getDefaultStype()); 
			doc.insertString(positionCaret, "booleanFunction", toolKit.getDescriptionStyle()); 
			doc.insertString(positionCaret, " ) ", toolKit.getDefaultStype()); 
			
			doc.insertString(positionCaret, " then ", toolKit.getIfStyle());
			doc.insertString(positionCaret, "[\n", toolKit.getDefaultStype()); 
			
			List<Entity> statesList = residentNode.getPossibleValueList(); 
			
			for(Entity entity :statesList){
				
				doc.insertString(positionCaret, "   " + entity.getName() + " = ", toolKit.getStateNodeStyle());          	
				doc.insertString(positionCaret, "formula", toolKit.getDescriptionStyle()); 
				doc.insertString(positionCaret, "\n", toolKit.getDefaultStype()); 
			}
			
			doc.insertString(positionCaret, "]\n", toolKit.getDefaultStype());
			
			
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		
	}
	
	private void insertElseClause(StyledDocument doc){
		try {
			txtPane.replaceSelection(""); 
			doc.insertString(positionCaret, "else ", toolKit.getIfStyle());
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}		
	}
	
	private void insertAndOperator(StyledDocument doc){
		try{
			txtPane.replaceSelection(""); 
			insertEqualOperator(doc); 
			doc.insertString(positionCaret, " and ", toolKit.getBooleanStyle()); 		
			insertEqualOperator(doc); 
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}		
	}
	
	private void insertOrOperator(StyledDocument doc){
		try{
			txtPane.replaceSelection(""); 
			insertEqualOperator(doc); 
			doc.insertString(positionCaret, " or ", toolKit.getBooleanStyle()); 		
			insertEqualOperator(doc); 
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}		
	}
	
	private void insertNotOperator(StyledDocument doc){
		try{
			txtPane.replaceSelection(""); 
			doc.insertString(positionCaret, "not ", toolKit.getBooleanStyle()); 		
			doc.insertString(positionCaret, "Node", toolKit.getDescriptionStyle()); 
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}			
	}
	
	private void insertEqualOperator(StyledDocument doc){
		try {
			txtPane.replaceSelection(""); 
			doc.insertString(positionCaret, "Node", toolKit.getDescriptionStyle()); 
			doc.insertString(positionCaret, " == ", toolKit.getDefaultStype()); 		
			doc.insertString(positionCaret, "NodeState", toolKit.getDescriptionStyle()); 
		
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		
	}		
	
	private void insertNumberClause(StyledDocument doc){
		try {
			txtPane.replaceSelection(""); 
			doc.insertString(positionCaret, "number", toolKit.getFunctionStyle());			
			doc.insertString(positionCaret, "(", toolKit.getDefaultStype());
			doc.insertString(positionCaret, " op ", toolKit.getDescriptionStyle());
		    doc.insertString(positionCaret, ")", toolKit.getDefaultStype());
							
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
	}
	
	private void insertCardinalityClause(StyledDocument doc){
		try {
			txtPane.replaceSelection(""); 
			doc.insertString(positionCaret, "cardinality", toolKit.getFunctionStyle());			
			doc.insertString(positionCaret, "(", toolKit.getDefaultStype());
			doc.insertString(positionCaret, " op ", toolKit.getDescriptionStyle());
		    doc.insertString(positionCaret, ")", toolKit.getDefaultStype());
				
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		
	}
	
	private void insertParamSet(StyledDocument doc, String param){
		try {
			txtPane.replaceSelection(""); 
			doc.insertString(positionCaret, param , toolKit.getArgumentStyle());
			
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
	}	
	
	private void insertState(StyledDocument doc, String state){
		try {
			txtPane.replaceSelection(""); 
			doc.insertString(positionCaret, state , toolKit.getStateNodeStyle());
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
	}	
	
	private void insertStateFather(StyledDocument doc, String state){
		try {
			txtPane.replaceSelection(""); 
			doc.insertString(positionCaret, state , toolKit.getStateFatherStyle());
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
	}		
	
	private void insertNode(StyledDocument doc, String param){
		try {
			txtPane.replaceSelection(""); 
			doc.insertString(positionCaret, param , toolKit.getFatherStyle());
			
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		
	}	
	
	private void insertMaxClause(StyledDocument doc){
		try {
			txtPane.replaceSelection(""); 
			doc.insertString(positionCaret, "max", toolKit.getFunctionStyle());			
			doc.insertString(positionCaret, "(", toolKit.getDefaultStype());
			doc.insertString(positionCaret, " op1 ", toolKit.getDescriptionStyle());
			doc.insertString(positionCaret, ",", toolKit.getDefaultStype());
			doc.insertString(positionCaret, " op2 ", toolKit.getDescriptionStyle());
			doc.insertString(positionCaret, ")", toolKit.getDefaultStype());
				
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		
	}	
	
	private void insertMinClause(StyledDocument doc){
		try {
			
			txtPane.replaceSelection(""); 
			doc.insertString(positionCaret, "min", toolKit.getFunctionStyle());			
			doc.insertString(positionCaret, "(", toolKit.getDefaultStype());
			doc.insertString(positionCaret, " op1 ", toolKit.getDescriptionStyle());
			doc.insertString(positionCaret, ",", toolKit.getDefaultStype());
			doc.insertString(positionCaret, " op2 ", toolKit.getDescriptionStyle());
			doc.insertString(positionCaret, ")", toolKit.getDefaultStype());
				
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		
	}	
	
	private void addButtonsListeners(){
		
		btnIfAnyClause.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertIfClause(doc, 0); 
			}
		}); 
		
		btnIfAllClause.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertIfClause(doc, 1); 
			}
		});  
		
		btnElseClause.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertElseClause(doc); 
			}
		}); 
		
		btnStates.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				updateStatesList(); 
			}
		}); 
		
		
		btnEqual.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertEqualOperator(doc); 
			}
		}); 
		
		btnAnd.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
			    insertAndOperator(doc); 
			}
		}); 
		
		btnOr.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertOrOperator(doc);
			}
		});  
		
		btnNot.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertNotOperator(doc);
			}
		});    	
		
		btnNumber.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertNumberClause(doc); 
			}
		}); 
		
		btnCardinality.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertCardinalityClause(doc); 
			}
		}); 
		
		btnMax.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertMaxClause(doc); 
			}
		}); 
		
		btnMin.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertMinClause(doc); 
			}
		});     
		
		btnStates.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				cardLayout.show(jpOptions, "StatesTab"); 
			}
		}); 
		
		btnFathers.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				cardLayout.show(jpOptions, "FatherTab"); 
			}
		}); 
		
		btnArguments.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				cardLayout.show(jpOptions, "ArgumentsTab"); 
			}
		}); 
		
		btnCompile.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				residentNode.setTableFunction(getTableTxt());
				TableParser tableParser = new TableParser(residentNode.getMFrag().getMultiEntityBayesianNetwork(), residentNode);  
				
				try{
				   tableParser.parseTable(getTableTxt());
				}
				catch(TableFunctionMalformedException e1){
					JOptionPane.showMessageDialog(null, e1.getMessage(), "TableFunctionMalformedException", JOptionPane.ERROR_MESSAGE);		
				}
				catch(NodeNotPresentInMTheoryException e2){
					JOptionPane.showMessageDialog(null, e2.getMessage(), "NodeNotPresentInMTheoryException", JOptionPane.ERROR_MESSAGE);		
									
				}
				catch(EntityNotPossibleValueOfNodeException e3 ){
					JOptionPane.showMessageDialog(null, e3.getMessage(), "EntityNotPossibleValueOfNodeException", JOptionPane.ERROR_MESSAGE);		
									
				}
				catch(InvalidProbabilityFunctionOperandException e4){
					JOptionPane.showMessageDialog(null, e4.getMessage(), "InvalidProbabilityFunctionOperandException", JOptionPane.ERROR_MESSAGE);		
									
				}
			}
		}); 
		
		btnExit.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){			
				mebnController.setUnableTableEditionView(); 
			}
		}); 
	}
	
	private String getTableTxt(){
		
		String text; 
		
		try{
		    text = doc.getText(0, doc.getLength());
		}
		catch(BadLocationException e){
			text = null; 
		}
	    
		return text; 
		
	}
	
	private void buildTxtEdition(String textTable, StyledDocument doc){
	    if(textTable != null){
		   tableEdition.turnTextColor(textTable, positionCaret, doc);
	    }
	}
	
	class TextPaneKeyListener implements KeyListener {
		
		public void keyTyped(KeyEvent e){
			//apenas para fazer com que o texto digitado pelo usuario seja preto. 
			try{
			   doc.insertString(positionCaret, "", toolKit.getDefaultStype()); 
			}
			catch(Exception exception){
				exception.printStackTrace(); 
			}
		}
		
		public void keyReleased(KeyEvent e){
			
		}
		
		public void keyPressed(KeyEvent e){
			if ((e.getKeyCode() == KeyEvent.VK_ENTER) 
					|| (e.getKeyCode() == KeyEvent.VK_SPACE)){
				
				try{
					String txtTable = doc.getText(0, doc.getLength()); 
					
					int i;
					int endPosition = positionCaret;
					
					// texto anterior ao ultimo espaco
					i = endPosition - 1; 
					while((txtTable.charAt(i) != ' ') && (i > 0)){
						i--; 
					}
					
					String text = doc.getText(i, endPosition - i);
					doc.remove(i, endPosition - i);
					tableEdition.turnTextColor(text, i, doc);
					
					//texto posterior ao ultimo espaco
					if(endPosition != txtTable.length()){
						
						i = endPosition; 
						while((i < txtTable.length()) && (txtTable.charAt(i) != ' ')){
							i++; 
						}
						
						if(i == txtTable.length()) i--; 
						text = doc.getText(endPosition, i - endPosition); 					
						doc.remove(endPosition, i - endPosition); 
						tableEdition.turnTextColor(text, endPosition, doc); 
					}
					
					positionCaret = endPosition; 
					txtPane.setCaretPosition(positionCaret); 
				}
				catch(Exception ex){
					ex.printStackTrace(); 
				}
			}
			else{

				if ((e.getKeyCode() == KeyEvent.VK_DELETE)
					||(e.getKeyCode() == KeyEvent.VK_DOWN)
					||(e.getKeyCode() == KeyEvent.VK_UP)
					||(e.getKeyCode() == KeyEvent.VK_LEFT)
					||(e.getKeyCode() == KeyEvent.VK_RIGHT)){
					
					try{
						String txtTable = doc.getText(0, doc.getLength()); 
						
						int i, j;
						int endPosition = positionCaret;
						
						// texto anterior
						i = endPosition; 
						while((txtTable.charAt(i) != ' ') && (i > 0)){
							i--; 
						}
						
						j = endPosition; 
						while((j < txtTable.length()) && (txtTable.charAt(j) != ' ')){
							j++; 
						}
							
						if(j == txtTable.length()) j--; 
							
						String text = doc.getText(i, j - i); 	
						doc.remove(i, j - i); 
						tableEdition.turnTextColor(text, i, doc); 
						
						positionCaret = endPosition; 
						txtPane.setCaretPosition(positionCaret); 
					}
					catch(Exception ex){
						ex.printStackTrace(); 
					}
				}/* if*/				
			} /* else */
		
			
		}
		
		
	}	
   
}