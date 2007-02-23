package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.text.StyledDocument;

import unbbayes.controller.MEBNController;
import unbbayes.controller.NetworkController;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.exception.EntityNotPossibleValueOfNodeException;
import unbbayes.prs.mebn.exception.NodeNotPresentInMTheoryException;
import unbbayes.prs.mebn.table.TableParser;
import unbbayes.prs.mebn.table.exception.InvalidProbabilityFunctionOperandException;
import unbbayes.prs.mebn.table.exception.TableFunctionMalformedException;


/** 
 * Pane that show the probabilistic table of the selected resident node
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */ 

public class TableViewPane extends JPanel{

	private DomainResidentNode residentNode; 
	private JButton btnEditTable; 
	private JButton btnCompileTable; 
	private JToolBar jtbTable;
	private JTextPane txtPane; 
	private int positionCaret = 0; 
	private JScrollPane jsTxtPane; 
	private StyledDocument doc; 
	private ToolKitForTableEdition toolKit; 
	
	private TableEdition tableEdition; 
	
	private MEBNController mebnController; 
	
	TableViewPane(NetworkController _controller, DomainResidentNode _residentNode){
		
		super(); 
		this.setLayout(new BorderLayout()); 
		
		mebnController = _controller.getMebnController(); 
		
		residentNode = _residentNode; 
		
		
		txtPane = new JTextPane(); 
		txtPane.setCaretPosition(0); 
		txtPane.setBackground(new Color(245,245,255)); 
		txtPane.setEditable(false); 
		
		/*
		txtPane.addCaretListener( new CaretListener(){
			public void caretUpdate(CaretEvent e) {
				positionCaret = e.getDot();
			}
		});
		*/ 
		
		doc = txtPane.getStyledDocument();
		toolKit = new ToolKitForTableEdition(doc); 
		buildTxtEdition(residentNode.getTableFunction(), doc); 
		
		jsTxtPane = new JScrollPane(txtPane);
		jsTxtPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		jsTxtPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		btnEditTable = new JButton("EDIT");
		btnEditTable.setBackground(ToolKitForGuiMebn.getBorderColor()); 
		btnEditTable.setForeground(Color.WHITE);

		btnCompileTable = new JButton("COMP"); 
		btnCompileTable.setBackground(new Color(102, 169, 1)); //green 
		btnCompileTable.setForeground(Color.WHITE); 
		
		addListeners(); 
		
		jtbTable = new JToolBar();		
		jtbTable.setLayout(new GridLayout(1,2)); 
		jtbTable.add(btnEditTable); 
		jtbTable.add(btnCompileTable); 
		jtbTable.setFloatable(false); 
		
		
		this.add(jtbTable, BorderLayout.SOUTH);
		this.add(jsTxtPane, BorderLayout.CENTER); 
	}
	
	private void addListeners(){
		
		btnEditTable.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
    			mebnController.setEnableTableEditionView(); 
        	}
		}); 
		
		btnCompileTable.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				TableParser tableParser = new TableParser(residentNode.getMFrag().getMultiEntityBayesianNetwork(), residentNode);  
				
				try{
				   if(residentNode.getTableFunction() != null)
				        tableParser.parseTable(residentNode.getTableFunction());
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
	}
	
	private void buildTxtEdition(String textTable, StyledDocument doc){

		if(textTable != null){
		tableEdition = new TableEdition(residentNode, toolKit); 	
		tableEdition.turnTextColor(textTable, positionCaret, doc);
		}
	
	}	
	
}
