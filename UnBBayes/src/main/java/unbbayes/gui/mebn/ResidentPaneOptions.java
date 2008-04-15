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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;

/**
 * JToolBar for resident editions pane with buttons for change the active pane. 
 * 
 * @author Laecio
 *
 */
public class ResidentPaneOptions extends JToolBar{

	private final IconController iconController = IconController.getInstance(); 
	private final MEBNController mebnController; 
	
		ResidentPaneOptions(MEBNController mebnController){
			super(); 
			
			this.mebnController = mebnController; 
			
			setLayout(new GridLayout(1,4)); 
			
	  		JButton btnStateEdition = new JButton(iconController.getStateIcon());
	  	    btnStateEdition.setBackground(MebnToolkit.getColor1()); 
	  		JButton btnEditTable = new JButton(iconController.getGridIcon());
	  		btnEditTable.setBackground(MebnToolkit.getColor1()); 
	  		JButton btnEditArguments = new JButton(iconController.getArgumentsIcon());
	  		btnEditArguments.setBackground(MebnToolkit.getColor1()); 
	  		
	  	JButton btnTriangle = new JButton(iconController.getTriangleIcon());
	  btnTriangle.setBackground(MebnToolkit.getColor1()); 
	  btnTriangle.setEnabled(false); 
	  		
			btnStateEdition.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					ResidentPaneOptions.this.mebnController.getMebnEditionPane().setResidentNodeTabActive();
				}
			});

			btnEditTable.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					ResidentPaneOptions.this.mebnController.setEnableTableEditionView();
				}
			});

			btnEditArguments.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent ae){
					ResidentPaneOptions.this.mebnController.getMebnEditionPane().setEditArgumentsTabActive();
				}

			});
			
  		add(btnTriangle);
  		add(btnStateEdition);
  		add(btnEditArguments);
  		add(btnEditTable);
  		setFloatable(false); 
		}
}