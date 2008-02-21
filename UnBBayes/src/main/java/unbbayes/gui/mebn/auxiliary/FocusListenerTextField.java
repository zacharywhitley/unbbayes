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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JTextField;


/**
 * Altera a cor do campo de texto quando o usuario est� o 
 * editando. 
 */
public class FocusListenerTextField implements FocusListener{
	
	public void focusGained(FocusEvent e){
		JTextField field = (JTextField)e.getSource(); 
		field.setBorder(BorderFactory.createEtchedBorder()); 
		field.setBackground(ToolKitForGuiMebn.getColorTextFieldSelected()); //verde 
	}
	
	public void focusLost(FocusEvent e){
		JTextField field = (JTextField)e.getSource(); 
		field.setBorder(null); 
		field.setBackground(ToolKitForGuiMebn.getColorTextFieldUnselected()); 
	}
}