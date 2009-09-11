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

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class ButtonLabel extends JButton{
	
	private int SIZE_LABEL = 15; 
	
	public ButtonLabel(String label, ImageIcon icon){
		super(); 
		int sizeLabel = label.length();
		if(sizeLabel < SIZE_LABEL){
			for(int i = sizeLabel; i < SIZE_LABEL; i++){
				label+=" "; 
			}
		}
		setIcon(icon); 
		setText(label); 
    	setBackground(MebnToolkit.getColor1()); //Verde quase cinza new Color(193, 210, 205)
    	setForeground(Color.BLACK);
    }
	
}
