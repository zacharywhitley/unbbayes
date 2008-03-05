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
import unbbayes.gui.mebn.auxiliary.ToolKitForTableEdition;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.compiler.Compiler;
import unbbayes.prs.mebn.compiler.exception.InconsistentTableSemanticsException;
import unbbayes.prs.mebn.compiler.exception.TableFunctionMalformedException;
import unbbayes.prs.mebn.exception.EntityNotPossibleValueOfNodeException;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.exception.NodeNotPresentInMTheoryException;

/** 
 * Pane that show the probabilistic table of the selected resident node
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */ 

public class TablePreviewPane extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ResidentNode residentNode; 
	private JButton btnEditTable; 
	private JButton btnCompileTable; 
	private JToolBar jtbTable;
	private JTextPane txtPane; 
	private int positionCaret = 0; 
	private JScrollPane jsTxtPane; 
	private StyledDocument doc; 
	private ToolKitForTableEdition toolKit; 
	
	private TableEditionUtils tableEdition; 
	
	private MEBNController mebnController; 
	
	TablePreviewPane(MEBNController _controller, ResidentNode _residentNode){
		
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
		if(residentNode.getTableFunction() != null){
			tableEdition = new TableEditionUtils(residentNode, toolKit); 	
			tableEdition.turnTextColor(residentNode.getTableFunction(), positionCaret, doc);
		}
		
		jsTxtPane = new JScrollPane(txtPane);
		jsTxtPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		jsTxtPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		Font font = new Font("Serif", Font.ITALIC, 12); 
		
		btnEditTable = new JButton("edit");
		btnEditTable.setBackground(Color.LIGHT_GRAY); 
		btnEditTable.setForeground(Color.BLUE); 
		btnEditTable.setFont(font);
		btnEditTable.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
    			mebnController.setEnableTableEditionView(); 
        	}
		}); 

		btnCompileTable = new JButton("comp"); 
		btnCompileTable.setBackground(Color.LIGHT_GRAY); 
		btnCompileTable.setForeground(Color.BLUE); 
		btnCompileTable.setFont(font);
		btnCompileTable.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Compiler tableParser = new Compiler(residentNode);  
				
				try{
				   if(residentNode.getTableFunction() != null)
				        tableParser.parse(residentNode.getTableFunction());
				}
				catch(TableFunctionMalformedException e1){
					JOptionPane.showMessageDialog(null, e1.getMessage()+ " : " + tableParser.getIndex(), "TableFunctionMalformedException", JOptionPane.ERROR_MESSAGE);		
				}
				catch(NodeNotPresentInMTheoryException e2){
					JOptionPane.showMessageDialog(null, e2.getMessage()+ " : " + tableParser.getIndex(), "NodeNotPresentInMTheoryException", JOptionPane.ERROR_MESSAGE);		
									
				}
				catch(EntityNotPossibleValueOfNodeException e3 ){
					JOptionPane.showMessageDialog(null, e3.getMessage()+ " : " + tableParser.getIndex(), "EntityNotPossibleValueOfNodeException", JOptionPane.ERROR_MESSAGE);		
									
				}
				catch(InconsistentTableSemanticsException ex){
					JOptionPane.showMessageDialog(null, ex.getMessage()+ " : " + tableParser.getIndex(), "InconsistentTableSemanticsException", JOptionPane.ERROR_MESSAGE);		
									
				}catch (MEBNException exc) {
					JOptionPane.showMessageDialog(null, exc.getMessage()+ " : " + tableParser.getIndex(), "MEBNException", JOptionPane.ERROR_MESSAGE);		
				}
			}
		}); 
		
		jtbTable = new JToolBar();		
		jtbTable.setLayout(new GridLayout(1,2)); 
		jtbTable.add(btnEditTable); 
		jtbTable.add(btnCompileTable); 
		jtbTable.setFloatable(false); 
		
		
		this.add(jtbTable, BorderLayout.SOUTH);
		this.add(jsTxtPane, BorderLayout.CENTER); 
	}
}
