package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
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
import unbbayes.gui.mebn.auxiliary.ToolKitForGuiMebn;
import unbbayes.gui.mebn.auxiliary.ToolKitForTableEdition;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.compiler.MEBNTableParser;
import unbbayes.prs.mebn.compiler.exception.InconsistentTableSemanticsException;
import unbbayes.prs.mebn.compiler.exception.TableFunctionMalformedException;
import unbbayes.prs.mebn.exception.EntityNotPossibleValueOfNodeException;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.exception.NodeNotPresentInMTheoryException;
import unbbayes.prs.mebn.table.TableParser;
import unbbayes.prs.mebn.table.exception.InvalidProbabilityFunctionOperandException;

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
	
	TableViewPane(MEBNController _controller, DomainResidentNode _residentNode){
		
		super(); 
		this.setLayout(new BorderLayout()); 
		
		mebnController = _controller; 
		
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
		
		Font font = new Font("Serif", Font.ITALIC, 12); 
		
		btnEditTable = new JButton("edit");
		btnEditTable.setBackground(Color.LIGHT_GRAY); 
		btnEditTable.setForeground(Color.BLUE); 
		btnEditTable.setFont(font);

		btnCompileTable = new JButton("comp"); 
		btnCompileTable.setBackground(Color.LIGHT_GRAY); 
		btnCompileTable.setForeground(Color.BLUE); 
		btnCompileTable.setFont(font);
		
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
				MEBNTableParser tableParser = MEBNTableParser.getInstance(residentNode);  
				
				try{
				   if(residentNode.getTableFunction() != null)
				        tableParser.parse(residentNode.getTableFunction());
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
				catch(InconsistentTableSemanticsException ex){
					JOptionPane.showMessageDialog(null, ex.getMessage(), "InconsistentTableSemanticsException", JOptionPane.ERROR_MESSAGE);		
									
				}catch (MEBNException exc) {
					JOptionPane.showMessageDialog(null, exc.getMessage(), "MEBNException", JOptionPane.ERROR_MESSAGE);		
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
