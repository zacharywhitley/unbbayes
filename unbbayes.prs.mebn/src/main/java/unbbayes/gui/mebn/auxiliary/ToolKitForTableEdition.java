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
package unbbayes.gui.mebn.auxiliary;

import java.awt.Color;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class ToolKitForTableEdition {

	
	StyledDocument doc; 
	
	//styles
	Style regular; 
	Style regular_blue; 
	Style regular_red; 
	Style regular_green; 
	Style regular_gray; 
	Style regular_magenta; 
	Style regular_yellow;
	Style regular_darkyellow; 
	Style regular_orange; 
	Style regular_lightgray; 
	Style regular_brown; 
	Style regular_purple; 
	
	public ToolKitForTableEdition(StyledDocument _doc){
		doc = _doc; 
		addStylesToDocument(doc); 
	}
	
	/**
	 * Add the possible styles of the table text 
	 * @param doc
	 */
	private void addStylesToDocument(StyledDocument doc) {
		
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(def, "SansSerif");
		
		regular = doc.addStyle("regular", def);
		
		regular_blue = doc.addStyle("regular_blue", regular); 
		StyleConstants.setForeground(regular_blue, Color.BLUE); 
		
		regular_red = doc.addStyle("regular_red", regular); 
		StyleConstants.setForeground(regular_red, Color.RED);         
		
		regular_green = doc.addStyle("regular_green", regular); 
		StyleConstants.setForeground(regular_green, Color.GREEN);    
		
		regular_gray = doc.addStyle("regular_gray", regular); 
		StyleConstants.setForeground(regular_gray, Color.GRAY);    
		
		regular_magenta = doc.addStyle("regular_magenta", regular); 
		StyleConstants.setForeground(regular_magenta, Color.MAGENTA);    
				
		regular_yellow = doc.addStyle("regular_yellow", regular); 
		StyleConstants.setForeground(regular_yellow, Color.YELLOW);    
			
		regular_darkyellow = doc.addStyle("regular_darkyellow", regular); 
		StyleConstants.setForeground(regular_darkyellow, new Color(174, 179, 4));    
			
		regular_orange = doc.addStyle("regular_orange", regular); 
		StyleConstants.setForeground(regular_orange, Color.ORANGE);    
					
		regular_lightgray = doc.addStyle("regular_lightgray", regular); 
		StyleConstants.setForeground(regular_lightgray, Color.LIGHT_GRAY);    
		
		regular_brown = doc.addStyle("regular_brown", regular); 
		StyleConstants.setForeground(regular_brown, new Color(147, 74, 72));    
		
		regular_purple = doc.addStyle("regular_purple", regular); 
		StyleConstants.setForeground(regular_purple, new Color(155, 99, 163));    
		
		/*
		Style s = doc.addStyle("italic", regular);
		StyleConstants.setItalic(s, true);
		
		s = doc.addStyle("bold", regular);
		StyleConstants.setBold(s, true);
		
		s = doc.addStyle("small", regular);
		StyleConstants.setFontSize(s, 10);
		
		s = doc.addStyle("large", regular);
		StyleConstants.setFontSize(s, 16);
		*/
	}		

	public Style getIfStyle(){
		return regular_blue; 
	}
	
	public Style getAnyStyle(){
		return regular_purple; 
	}
	
	public Style getArgumentStyle(){
		return regular_gray; 
	}
	
	public Style getFunctionStyle(){
		return regular_orange; 
	}
	
	public Style getNumberStyle(){
		return regular_red; 
	}
	
	public Style getBooleanStyle(){
		return regular_blue; 
	}
	
	public Style getFatherStyle(){
		return regular; 
	}
	
	public Style getStateNodeStyle(){
		return regular_brown; 
	}
	
	public Style getStateFatherStyle(){
		return regular_green; 
	}
	
	public Style getDefaultStype(){
		return regular; 
	}
	
	public Style getDescriptionStyle(){
		return regular_darkyellow; 
	}
	
}
