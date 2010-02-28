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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.util.ResourceController;

public class CPTTextPane extends JTextPane{

	private final CPTEditionPane cptEditionPane_; 
	private final StyleTable styleTable; 
	
	private ColloringUtils colloringUtils; 
	
	private StyledDocument doc; 
	private int positionCaret = 0; 
	
	private JPopupMenu popupOptions; 
	
	private static ResourceBundle resource = ResourceController.newInstance().getBundle(
			unbbayes.gui.mebn.resources.Resources.class.getName());
	
	CPTTextPane(CPTEditionPane cptEditionPane){
		super();
		this.cptEditionPane_ = cptEditionPane; 
		styleTable = new StyleTableImpl(this.getStyledDocument()); 
		doc = this.getStyledDocument(); 
		
		setCaretPosition(0); 
		addCaretListener( new CaretListener(){
			public void caretUpdate(CaretEvent e) {
				positionCaret = e.getDot();
				cptEditionPane_.atualizeCaretPosition(positionCaret); 
			}
		}); 
		
		setMinimumSize(new Dimension(100,100)); 
		setMaximumSize(new Dimension(100,100)); 
		setPreferredSize(new Dimension(100,100)); 	
		
		colloringUtils = new ColloringUtils(cptEditionPane.getResidentNode(), styleTable); 
		if(cptEditionPane.getResidentNode().getTableFunction() != null){
			colloringUtils.turnTextColor(cptEditionPane.getResidentNode().getTableFunction(), 0, doc);
		}
		
		popupOptions = createPopupMenu(); 
		
		addKeyListener(new TextPaneKeyListener()); 
		addMouseListener(new TextPaneMouseListener()); 
	}
	
	private JPopupMenu createPopupMenu(){
		
		JMenuItem itemIfAnyClause = new JMenuItem(resource.getString("ifAny"));
		JMenuItem itemIfAllClause = new JMenuItem(resource.getString("ifAll"));
		JMenuItem itemElseClause = new JMenuItem(resource.getString("else")); 
		JMenuItem itemEraseAll = new JMenuItem(resource.getString("clear"));
		JMenuItem itemDefaultClause = new JMenuItem(resource.getString("default"));

		JMenuItem itemEqual = new JMenuItem(resource.getString("equal")); 
		JMenuItem itemAnd = new JMenuItem(resource.getString("and"));
		JMenuItem itemOr = new JMenuItem(resource.getString("or")); 
		JMenuItem itemNot = new JMenuItem(resource.getString("not"));  

		JMenuItem itemCardinality = new JMenuItem(resource.getString("card")); 
		JMenuItem itemMax = new JMenuItem(resource.getString("max")); 
		JMenuItem itemMin = new JMenuItem(resource.getString("min")); 

		itemIfAnyClause.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertIfClause(0); 
			}
		}); 
		
		itemIfAllClause.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertIfClause(1); 
			}
		});  
		
		itemElseClause.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertElseClause(); 
			}
		}); 
		
		
		itemEqual.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertEqualOperator(); 
			}
		}); 
		
		itemAnd.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertAndOperator(); 
			}
		}); 
		
		itemOr.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertOrOperator();
			}
		});  
		
		itemNot.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertNotOperator();
			}
		});
		
		itemCardinality.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertCardinalityClause(); 
			}
		}); 
		
		itemMax.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertMaxClause(); 
			}
		}); 
		
		itemMin.addActionListener( new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertMinClause(); 
			}
		});   
		
		itemEraseAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				clearTable(); 
			}
		}); 
		
		itemDefaultClause.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				insertDefaultClause(); 
			}
		}); 
		
		JPopupMenu popup = new JPopupMenu(); 
		
		popup.add(itemIfAnyClause); 
		popup.add(itemIfAllClause); 
		popup.add(itemElseClause); 
		popup.add(itemDefaultClause); 
		
		popup.addSeparator(); 
		
		popup.add(itemEqual); 
		popup.add(itemAnd); 
		popup.add(itemOr); 
		popup.add(itemNot); 
		
		popup.addSeparator(); 
		
		popup.add(itemMax); 
		popup.add(itemMin); 
		popup.add(itemCardinality); 
		
		popup.addSeparator(); 
		
		popup.add(itemEraseAll); 
		
		return popup; 
	}
	
	/**
	 * choice: 
	 * 0 -> any Clause
	 * 1 -> all Clause
	 * @param doc
	 * @param choice
	 */
	public void insertIfClause(int choice){
		
		Position pos = doc.getEndPosition(); 
		int start = pos.getOffset(); 
		
		try {
			replaceSelection(""); 
			doc.insertString(positionCaret, "if", styleTable.getIfStyle());
			doc.insertString(positionCaret, " ", styleTable.getDefaultStyle());
			
			if(choice == 0){
				doc.insertString(positionCaret, "any", styleTable.getAnyStyle());
				doc.insertString(positionCaret, " ", styleTable.getDefaultStyle());				
				doc.insertString(positionCaret, "paramSubSet", styleTable.getDescriptionStyle()); 
				doc.insertString(positionCaret, " ", styleTable.getDefaultStyle());
			}
			else{
				doc.insertString(positionCaret, "all", styleTable.getAnyStyle()); 	
				doc.insertString(positionCaret, " ", styleTable.getDefaultStyle());
				doc.insertString(positionCaret, "paramSet", styleTable.getDescriptionStyle());
				doc.insertString(positionCaret, " ", styleTable.getDefaultStyle());
			}
	
			doc.insertString(positionCaret, "have", styleTable.getAnyStyle());
			
			doc.insertString(positionCaret, " ( ", styleTable.getDefaultStyle()); 
			doc.insertString(positionCaret, "booleanFunction", styleTable.getDescriptionStyle()); 
			doc.insertString(positionCaret, " ) ", styleTable.getDefaultStyle()); 
			
			//doc.insertString(positionCaret, " then ", styleTable.getIfStyle());
			insertStatesDistribution();
			
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		
	}

	/*
	 * Insert the distribution of the states in the format: 
	 * 
	 * [
	 *   state1 = formula,
	 *   state2 = formula,
	 *   state3 = formula
	 * ]
	 * 
	 * @throws BadLocationException
	 */
	private void insertStatesDistribution() throws BadLocationException {
		doc.insertString(positionCaret, "[\n", styleTable.getDefaultStyle()); 
		
		List<Entity> statesList = cptEditionPane_.getResidentNode().getPossibleValueList(); 
		
		int sizeStatesList = statesList.size(); //used for controller the commas 
		
		for(Entity entity :statesList){
			
			sizeStatesList--; 
			
			doc.insertString(positionCaret, "   " + entity.getName() + " = ", styleTable.getStateNodeStyle());          	
			doc.insertString(positionCaret, "formula", styleTable.getDescriptionStyle());
			
			if(sizeStatesList != 0){ //more itens: put comma
				doc.insertString(positionCaret, ",", styleTable.getDefaultStyle()); 
			}
			
			doc.insertString(positionCaret, "\n", styleTable.getDefaultStyle()); 
		}
		
		doc.insertString(positionCaret, "]\n", styleTable.getDefaultStyle());
	}
	
	public void insertDefaultClause(){
		try {
			insertStatesDistribution();
		} catch (BadLocationException e) {
			System.err.println("Couldn't insert initial text into text pane.");
		} 
	}
	
	public void clearTable(){
		int end = doc.getLength();
		try {
			doc.remove(0, end);
			positionCaret = 0; 
		} catch (BadLocationException e) {
			System.err.println("Couldn't insert initial text into text pane.");
		} 
	}
	
	public void insertElseClause(){
		try {
			replaceSelection(""); 
			doc.insertString(positionCaret, "else ", styleTable.getIfStyle());
			insertStatesDistribution(); 
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}		
	}
	
	public void insertAndOperator(){
		try{
			replaceSelection(""); 
			insertEqualOperator(); 
			doc.insertString(positionCaret, " & ", styleTable.getBooleanStyle()); 		
			insertEqualOperator(); 
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}		
	}
	
	public void insertOrOperator(){
		try{
			replaceSelection(""); 
			insertEqualOperator(); 
			doc.insertString(positionCaret, " | ", styleTable.getBooleanStyle()); 		
			insertEqualOperator(); 
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}		
	}
	
	public void insertNotOperator(){
		try{
			replaceSelection(""); 
			doc.insertString(positionCaret, " ~ ", styleTable.getBooleanStyle()); 		
			doc.insertString(positionCaret, "Node", styleTable.getDescriptionStyle()); 
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}			
	}
	
	public void insertEqualOperator(){
		try {
			replaceSelection(""); 
			doc.insertString(positionCaret, "Node", styleTable.getDescriptionStyle()); 
			doc.insertString(positionCaret, " = ", styleTable.getDefaultStyle()); 		
			doc.insertString(positionCaret, "NodeState", styleTable.getDescriptionStyle()); 
		
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		
	}
	
	public void insertCardinalityClause(){
		try {
			replaceSelection(""); 
			doc.insertString(positionCaret, "cardinality", styleTable.getFunctionStyle());			
			doc.insertString(positionCaret, "(", styleTable.getDefaultStyle());
			doc.insertString(positionCaret, " op ", styleTable.getDescriptionStyle());
		    doc.insertString(positionCaret, ")", styleTable.getDefaultStyle());
				
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		
	}
	
	public void insertParamSet(String param){
		try {
			replaceSelection(""); 
			doc.insertString(positionCaret, param , styleTable.getArgumentStyle());
			
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
	}	
	
	public void insertState(String state){
		try {
			replaceSelection(""); 
			doc.insertString(positionCaret, state , styleTable.getStateNodeStyle());
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
	}	
	
	public void insertStateFather(String state){
		try {
			replaceSelection(""); 
			doc.insertString(positionCaret, state , styleTable.getStateFatherStyle());
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
	}		
	
	public void insertNode(String param){
		try {
			replaceSelection(""); 
			doc.insertString(positionCaret, param , styleTable.getFatherStyle());
			
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		
	}	
	
	public void insertMaxClause(){
		try {
			replaceSelection(""); 
			doc.insertString(positionCaret, "max", styleTable.getFunctionStyle());			
			doc.insertString(positionCaret, "(", styleTable.getDefaultStyle());
			doc.insertString(positionCaret, " op1 ", styleTable.getDescriptionStyle());
			doc.insertString(positionCaret, ",", styleTable.getDefaultStyle());
			doc.insertString(positionCaret, " op2 ", styleTable.getDescriptionStyle());
			doc.insertString(positionCaret, ")", styleTable.getDefaultStyle());
				
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		
	}	
	
	public void insertMinClause(){
		try {
			
			replaceSelection(""); 
			doc.insertString(positionCaret, "min", styleTable.getFunctionStyle());			
			doc.insertString(positionCaret, "(", styleTable.getDefaultStyle());
			doc.insertString(positionCaret, " op1 ", styleTable.getDescriptionStyle());
			doc.insertString(positionCaret, ",", styleTable.getDefaultStyle());
			doc.insertString(positionCaret, " op2 ", styleTable.getDescriptionStyle());
			doc.insertString(positionCaret, ")", styleTable.getDefaultStyle());
				
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		
	}	
	
	public String getTableTxt(){
		
		String text; 
		
		try{
		    text = doc.getText(0, doc.getLength());
		}
		catch(BadLocationException e){
			text = null; 
		}
	    
		return text; 
		
	}
	
	/*
	 * Open a context menu when the user click with the rigth button
	 */
	private class TextPaneMouseListener extends MouseAdapter{

		public void mousePressed(MouseEvent e) {
			if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
				popupOptions.setEnabled(true);
				popupOptions.show(e.getComponent(),e.getX(),e.getY());
			}
		}		
		
	}
	
	private class TextPaneKeyListener implements KeyListener {
		
		public void keyTyped(KeyEvent e){
			//apenas para fazer com que o texto digitado pelo usuario seja preto. 
			try{
			   doc.insertString(positionCaret, "", styleTable.getDefaultStyle()); 
			}
			catch(Exception exception){
				exception.printStackTrace(); 
			}
		}
		
		public void keyReleased(KeyEvent e){
			
		}
		
		/**
		 * Turn the color of the text
		 */
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
					colloringUtils.turnTextColor(text, i, doc);
					
					//texto posterior ao ultimo espaco
					if(endPosition != txtTable.length()){
						
						i = endPosition; 
						while((i < txtTable.length()) && (txtTable.charAt(i) != ' ')){
							i++; 
						}
						
						if(i == txtTable.length()) i--; 
						text = doc.getText(endPosition, i - endPosition); 					
						doc.remove(endPosition, i - endPosition); 
						colloringUtils.turnTextColor(text, endPosition, doc); 
					}
					
					positionCaret = endPosition; 
					setCaretPosition(positionCaret); 
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
						colloringUtils.turnTextColor(text, i, doc); 
						
						positionCaret = endPosition; 
						setCaretPosition(positionCaret); 
					}
					catch(Exception ex){
						ex.printStackTrace(); 
					}
				}/* if*/				
			} /* else */
		}
	}	
	
	public void setCursorPosition(int position){
		setCaretPosition(position);
	}
	
}
