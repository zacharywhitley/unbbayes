package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.Entity;

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
public class TableEdition extends JFrame{

	JPanel contentPane;

	
	JTextPane txtPane;
	JScrollPane jsTxtPane; 
	StyledDocument doc; 
	
	/* posicao onde se enconra o prompt */
	int positionCaret = 0; 
	
	/* buttons */
	JPanel jpOptions; 
	JPanel jpButtons; 
	final JPanel jpFather; 
	
	JButton btnIfAnyClause; 
	JButton btnIfAllClause; 
	JButton btnElseClause; 
	JButton btnStates; 
	
	JButton btnAdd; 
	JButton btnSub; 
	JButton btnMult; 
	JButton btnDiv; 
	
	JButton btnEqual; 
	JButton btnAnd; 
	JButton btnOr; 
	JButton btnNot; 
	
	JButton btnNumber; 
	JButton btnCardinality; 
	JButton btnMax; 
	JButton btnMin; 
	
	DomainResidentNode residentNode; 
	
	String[] oVariableArray; 
	String[] fatherNodeArray; 
	String[] statesArray; 
	
    private JList jlStates; 
    private DefaultListModel listModel;
    
	JPanel jpStates; 
	
	public TableEdition(DomainResidentNode _residentNode){
		
		super("Table");
		
		residentNode = _residentNode; 
		
		txtPane = new JTextPane(); 
		txtPane.setCaretPosition(0); 
		txtPane.addCaretListener(new CaretListenerLs()); 
        
        doc = txtPane.getStyledDocument();
        addStylesToDocument(doc); 
        
        jpButtons = new JPanel(new GridLayout(4, 4));
        buildJpButtons(); 
        
        
        jpOptions = new JPanel(new BorderLayout()); 
        jpOptions.add("North", jpButtons); 
            
        jpFather = new JPanel(new GridLayout(3, 0)); 
        buildJpFather(doc); 
        
    	jpOptions.add("Center", jpFather); 
        
        addButtonsListeners();    
        
        jsTxtPane = new JScrollPane(txtPane); 
		jsTxtPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); 
		jsTxtPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 
		
		GridBagLayout gridbag = new GridBagLayout(); 
		GridBagConstraints constraints = new GridBagConstraints(); 
		
		contentPane = new JPanel(gridbag); 
	    
		constraints.gridx = 0; 
	    constraints.gridy = 0; 
	    constraints.gridwidth = 1; 
	    constraints.gridheight = 1; 
	    constraints.weightx = 100;
	    constraints.weighty = 80; 
	    constraints.fill = GridBagConstraints.BOTH; 
	    constraints.anchor = GridBagConstraints.NORTH; 
	    gridbag.setConstraints(jsTxtPane, constraints); 
	    contentPane.add(jsTxtPane);
	    
		constraints.gridx = 1; 
	    constraints.gridy = 0; 
	    constraints.gridwidth = 1; 
	    constraints.gridheight = 1; 
	    constraints.weightx = 0; 
	    constraints.weighty = 15; 
	    constraints.fill = GridBagConstraints.BOTH; 
	    constraints.anchor = GridBagConstraints.NORTH; 
	    gridbag.setConstraints(jpOptions, constraints); 
	    contentPane.add(jpOptions);
		
		this.setContentPane(contentPane); 
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
		this.setSize(600, 400); 
	    this.setVisible(true);  
	    this.setLocationRelativeTo(null); 
	    
	}
	
	private void buildJpButtons(){

        btnIfAnyClause = new JButton("ANY");
        btnIfAllClause = new JButton("ALL");
        btnElseClause = new JButton("ELS"); 
        btnStates = new JButton("STT"); 
        
    	btnAdd = new JButton(" + "); 
    	btnSub = new JButton(" - "); 
    	btnMult = new JButton(" * "); 
    	btnDiv = new JButton(" \\ "); 
        
    	btnEqual= new JButton("== "); 
    	btnAnd= new JButton("AND"); 
    	btnOr= new JButton("OR "); 
    	btnNot= new JButton("NOT");     	
                
    	btnNumber= new JButton("NUM"); 
    	btnCardinality= new JButton("CAR"); 
    	btnMax= new JButton("MAX"); 
    	btnMin= new JButton("MIN");     	

    	jpButtons.add(btnIfAnyClause); 
    	jpButtons.add(btnIfAllClause);
    	jpButtons.add(btnElseClause); 
    	jpButtons.add(btnStates); 
        
    	jpButtons.add(btnAdd); 
    	jpButtons.add(btnSub);
    	jpButtons.add(btnMult); 
    	jpButtons.add(btnDiv); 
        
    	jpButtons.add(btnEqual); 
    	jpButtons.add(btnAnd);
    	jpButtons.add(btnOr); 
    	jpButtons.add(btnNot); 
        
    	jpButtons.add(btnNumber); 
    	jpButtons.add(btnCardinality);
    	jpButtons.add(btnMax); 
    	jpButtons.add(btnMin); 

	}
	
	private void buildJpFather(final StyledDocument doc){
		
		int i; 
		
		/* Lista com as variavies ordinarias */
		
		List<OrdinaryVariable> oVariableList = residentNode.getOrdinaryVariableList(); 
		oVariableArray = new String[oVariableList.size()]; 
		i = 0; 
		
		for(OrdinaryVariable ov: oVariableList){
			oVariableArray[i] = ov.getName(); 
			i++; 
		}
		
		final JList jlOVariable = new JList(oVariableArray);
		JScrollPane jscJlOVariable = new JScrollPane(jlOVariable); 
		jpFather.add(jscJlOVariable); 
		
		
		/* Lista com os nodos pais */
		List<GenerativeInputNode> inputNodeList = residentNode.getInputNodeFatherList(); 
		fatherNodeArray = new String[inputNodeList.size()]; 
		i = 0; 
		for(GenerativeInputNode input: inputNodeList){
			fatherNodeArray[i] = ((DomainResidentNode)input.getInputInstanceOf()).getName(); 
			i++; 
		}
		
		final JList jlFathers = new JList(fatherNodeArray);
		JScrollPane jscJlOFathers = new JScrollPane(jlFathers); 
		jpFather.add(jscJlOFathers); 
		
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
						GenerativeInputNode inputNode = inputNodeList.get(selectedIndex); 
						updateStatesList(inputNode); 
					}					
				}
				
			}
	
		});
				
		
		
		/* Lista com os estados do nodo pai selecionado */
		listModel = new DefaultListModel();
		jlStates = new JList(listModel); 
		JScrollPane jspStates = new JScrollPane(jlStates); 
		jpFather.add(jspStates); 
		
		jlStates.addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e) {
				
				if ((e.getModifiers() == MouseEvent.BUTTON1_MASK) && (e.getClickCount() == 2)){
					String selectedIndex = (String)jlStates.getSelectedValue(); 
					insertNode(doc, selectedIndex); 
				}	
			}
	
		});
		
		
		jlOVariable.addMouseListener(new MouseAdapter() {
				
				public void mousePressed(MouseEvent e) {
					
					if ((e.getModifiers() == MouseEvent.BUTTON1_MASK) && (e.getClickCount() == 2)){
						int selectedIndex = jlOVariable.getSelectedIndex(); 
						insertParamSet(doc, oVariableArray[selectedIndex]); 
					}
					
				}
		});
		
	}
	
	
	private void updateStatesList(GenerativeInputNode inputNode){
	
		DomainResidentNode resident = (DomainResidentNode)(inputNode.getInputInstanceOf()); 
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
	 * Adiciona os estilos possiveis para o texto da tabela. 
	 * @param doc
	 */
	protected void addStylesToDocument(StyledDocument doc) {
		
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);
        
        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");
        
        Style regular_cor = doc.addStyle("regular_blue", regular); 
        StyleConstants.setForeground(regular_cor, Color.BLUE); 

        regular_cor = doc.addStyle("regular_red", regular); 
        StyleConstants.setForeground(regular_cor, Color.RED);         
        
        regular_cor = doc.addStyle("regular_green", regular); 
        StyleConstants.setForeground(regular_cor, Color.GREEN);    
        
        regular_cor = doc.addStyle("regular_gray", regular); 
        StyleConstants.setForeground(regular_cor, Color.GRAY);    
                
        
        Style s = doc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);

        s = doc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);

        s = doc.addStyle("small", regular);
        StyleConstants.setFontSize(s, 10);

        s = doc.addStyle("large", regular);
        StyleConstants.setFontSize(s, 16);
        
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
        	
            doc.insertString(positionCaret, "if ", doc.getStyle("regular_blue"));
            if(choice == 0){
                 doc.insertString(positionCaret, "any   ", doc.getStyle("regular_red"));
                 //doc.insertString(positionCaret, "<param_set> ", doc.getStyle("regular_red")); 
            }
            else{
                doc.insertString(positionCaret, "all ", doc.getStyle("regular_red"));
                //doc.insertString(positionCaret, "<param_sub_set> ", doc.getStyle("regular_red")); 	
            }
            doc.insertString(positionCaret, "have ", doc.getStyle("regular_blue"));
            doc.insertString(positionCaret, " (  ", doc.getStyle("regular")); 
            //doc.insertString(positionCaret, "<boolean_expression> ", doc.getStyle("regular_red"));    
            doc.insertString(positionCaret, " ) ", doc.getStyle("regular")); 
            doc.insertString(positionCaret, " then ", doc.getStyle("regular_blue"));
            doc.insertString(positionCaret, "[\n", doc.getStyle("regular")); 
            
            
            List<Entity> statesList = residentNode.getPossibleValueList(); 
            
            for(Entity entity :statesList){
                
            	doc.insertString(positionCaret, "   "+ entity.getName() + " = " + "\n", doc.getStyle("regular_green"));          	
           
            }
            
            doc.insertString(positionCaret, "]\n", doc.getStyle("regular"));

            
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }
	    	
		
	}
	
	private void insertElseClause(StyledDocument doc){
		try {
        	
            doc.insertString(positionCaret, "else ", doc.getStyle("regular_blue"));
                 
            
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }		
	}
	
	private void insertArithmOperator(StyledDocument doc, String operator){
		try {
        	
            doc.insertString(positionCaret, " " + operator + " ", doc.getStyle("regular_red"));
            
            
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }
		
	}
	
	private void insertBooleanOperator(StyledDocument doc, String operator, int numOperandos){
		try {
        	if(numOperandos == 2){
               doc.insertString(positionCaret, " (   ) " + operator + " (   ) ", doc.getStyle("regular_red"));
        	}
        	else{
        	   doc.insertString(positionCaret, operator + "(   ) ", doc.getStyle("regular_red"));		
        	}
            
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }
		
	}	
	
	private void insertEqualOperator(StyledDocument doc){
		try {
        	
               doc.insertString(positionCaret, "  " + "==" + "  ", doc.getStyle("regular_red"));
        
            
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }
		
	}		

	private void insertNumberClause(StyledDocument doc){
		try {
        	
            doc.insertString(positionCaret, " number" + "(   )" , doc.getStyle("regular"));
            
            
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }
		
	}
	
	private void insertCardinalityClause(StyledDocument doc){
		try {
        	
            doc.insertString(positionCaret, " cardinality" + "(   )" , doc.getStyle("regular"));
            
            
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }
		
	}	
	
	private void insertParamSet(StyledDocument doc){
		try {
        	
            doc.insertString(positionCaret, " " , doc.getStyle("regular_red"));
            
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }
		
	}	
	
	private void insertParamSet(StyledDocument doc, String param){
		try {
        	
            doc.insertString(positionCaret, param + "(i)", doc.getStyle("regular_gray"));
            
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }
	}	
	
	private void insertParamSubSet(StyledDocument doc){
		try {
        	
            doc.insertString(positionCaret, " " , doc.getStyle("regular_red"));
            
            
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }
		
	}	
	
	private void insertNode(StyledDocument doc, String param){
		try {
        	
            doc.insertString(positionCaret, param , doc.getStyle("regular_gray"));
            
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }
		
	}	
	
	private void insertMaxClause(StyledDocument doc){
		try {
        	
            doc.insertString(positionCaret, "max(   ;   ) "  , doc.getStyle("regular"));
            
            
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }
		
	}	
	
	private void insertMinClause(StyledDocument doc){
		try {
        	
            doc.insertString(positionCaret, "min(   ;   ) "  , doc.getStyle("regular"));
            
            
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
        
    	btnAdd.addActionListener( new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		insertArithmOperator(doc, "+"); 
        	}
        }); 
    	
    	btnSub.addActionListener( new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		insertArithmOperator(doc, "-"); 
        	}
        }); 
    	
    	btnMult.addActionListener( new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		insertArithmOperator(doc, "*"); 
        	}
        }); 
    	
    	btnDiv.addActionListener( new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		insertArithmOperator(doc, "\\"); 
        	}
        });  
        
    	btnEqual.addActionListener( new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		insertEqualOperator(doc); 
        	}
        }); 
    	
    	btnAnd.addActionListener( new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		insertBooleanOperator(doc, "AND", 2); 
        	}
        }); 
    	
    	btnOr.addActionListener( new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		insertBooleanOperator(doc, "OR", 2);
        	}
        });  
    	
    	btnNot.addActionListener( new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		insertBooleanOperator(doc, "NOT", 1);
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
    }

	  //This listens for and reports caret movements.
    protected class CaretListenerLs  implements CaretListener {
        
        //Might not be invoked from the event dispatching thread.
        public void caretUpdate(CaretEvent e) {
            positionCaret = e.getDot();
        }

    }
    
    
	
}
