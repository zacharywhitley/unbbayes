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
