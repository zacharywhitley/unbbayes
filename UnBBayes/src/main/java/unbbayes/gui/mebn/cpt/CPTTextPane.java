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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import unbbayes.prs.mebn.entity.Entity;

public class CPTTextPane extends JTextPane{

	private final CPTEditionPane cptEditionPane_; 
	private final StyleTable styleTable; 
	
	private ColloringUtils colloringUtils; 
	
	private StyledDocument doc; 
	private int positionCaret = 0; 
	
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
		addKeyListener(new TextPaneKeyListener()); 
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
			doc.insertString(positionCaret, " ", styleTable.getDefaultStype());
			
			if(choice == 0){
				doc.insertString(positionCaret, "any", styleTable.getAnyStyle());
				doc.insertString(positionCaret, " ", styleTable.getDefaultStype());				
				doc.insertString(positionCaret, "paramSubSet", styleTable.getDescriptionStyle()); 
				doc.insertString(positionCaret, " ", styleTable.getDefaultStype());
			}
			else{
				doc.insertString(positionCaret, "all", styleTable.getAnyStyle()); 	
				doc.insertString(positionCaret, " ", styleTable.getDefaultStype());
				doc.insertString(positionCaret, "paramSet", styleTable.getDescriptionStyle());
				doc.insertString(positionCaret, " ", styleTable.getDefaultStype());
			}
	
			doc.insertString(positionCaret, "have", styleTable.getAnyStyle());
			
			doc.insertString(positionCaret, " ( ", styleTable.getDefaultStype()); 
			doc.insertString(positionCaret, "booleanFunction", styleTable.getDescriptionStyle()); 
			doc.insertString(positionCaret, " ) ", styleTable.getDefaultStype()); 
			
			//doc.insertString(positionCaret, " then ", styleTable.getIfStyle());
			doc.insertString(positionCaret, "[\n", styleTable.getDefaultStype()); 
			
			List<Entity> statesList = cptEditionPane_.getResidentNode().getPossibleValueList(); 
			
			for(Entity entity :statesList){
				
				doc.insertString(positionCaret, "   " + entity.getName() + " = ", styleTable.getStateNodeStyle());          	
				doc.insertString(positionCaret, "formula", styleTable.getDescriptionStyle()); 
				doc.insertString(positionCaret, "\n", styleTable.getDefaultStype()); 
			}
			
			doc.insertString(positionCaret, "]\n", styleTable.getDefaultStype());
			
			
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		
	}
	
	public void insertElseClause(){
		try {
			replaceSelection(""); 
			doc.insertString(positionCaret, "else ", styleTable.getIfStyle());
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
			doc.insertString(positionCaret, " = ", styleTable.getDefaultStype()); 		
			doc.insertString(positionCaret, "NodeState", styleTable.getDescriptionStyle()); 
		
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		
	}
	
	public void insertCardinalityClause(){
		try {
			replaceSelection(""); 
			doc.insertString(positionCaret, "cardinality", styleTable.getFunctionStyle());			
			doc.insertString(positionCaret, "(", styleTable.getDefaultStype());
			doc.insertString(positionCaret, " op ", styleTable.getDescriptionStyle());
		    doc.insertString(positionCaret, ")", styleTable.getDefaultStype());
				
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
			doc.insertString(positionCaret, "(", styleTable.getDefaultStype());
			doc.insertString(positionCaret, " op1 ", styleTable.getDescriptionStyle());
			doc.insertString(positionCaret, ",", styleTable.getDefaultStype());
			doc.insertString(positionCaret, " op2 ", styleTable.getDescriptionStyle());
			doc.insertString(positionCaret, ")", styleTable.getDefaultStype());
				
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}
		
	}	
	
	public void insertMinClause(){
		try {
			
			replaceSelection(""); 
			doc.insertString(positionCaret, "min", styleTable.getFunctionStyle());			
			doc.insertString(positionCaret, "(", styleTable.getDefaultStype());
			doc.insertString(positionCaret, " op1 ", styleTable.getDescriptionStyle());
			doc.insertString(positionCaret, ",", styleTable.getDefaultStype());
			doc.insertString(positionCaret, " op2 ", styleTable.getDescriptionStyle());
			doc.insertString(positionCaret, ")", styleTable.getDefaultStype());
				
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
	
	private class TextPaneKeyListener implements KeyListener {
		
		public void keyTyped(KeyEvent e){
			//apenas para fazer com que o texto digitado pelo usuario seja preto. 
			try{
			   doc.insertString(positionCaret, "", styleTable.getDefaultStype()); 
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
