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

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

/**
 * Renderer for a JList where each element have the same icon
 * (the icon is show before the element) and the selected element
 * have a border. 
 * 
 * @author Laecio Lima dos Santos
 * @version 1.0 (03/23/07)
 */

public class ListCellRenderer extends DefaultListCellRenderer{
	
		private ImageIcon icon;
		
		public ListCellRenderer(ImageIcon _icon){
			super(); 
			icon = _icon; 
		}
		
		public Component getListCellRendererComponent(JList list, Object value, int index, 
				                                      boolean isSelected, boolean cellHasFocus){
			
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);  
			
			super.setIcon(icon); 
			
			if(isSelected){
			   super.setBorder(BorderFactory.createEtchedBorder()); 
			}
			
			return this; 
		}
		
}
