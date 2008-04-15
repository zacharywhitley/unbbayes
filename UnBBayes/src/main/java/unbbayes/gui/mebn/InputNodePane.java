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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import unbbayes.controller.MEBNController;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.prs.mebn.InputNode;

/**
 * Pane for edition of a input node. 
 * Contains: 
 * -> A tree for selection of the property "is input of" 
 * -> A pane for selection of the arguments of the input node. 
 * 
 * @author Laecio Lima dos Santos
 * @version 1.0 05/28/07
 *
 */
public class InputNodePane extends JPanel{
	
	private InputNode inputNode; 
	private InputInstanceOfTree inputInstanceOfTree; 
	private JScrollPane inputInstanceOfTreeScroll; 
	private JPanel argumentsPane; 
	private MEBNController controller; 
	
	public InputNodePane(){
		
	}
	
	public InputNodePane(MEBNController _controller, InputNode _inputNode){
		
		JToolBar jtbInputInstanceOf; 
		JLabel jlInputInstanceOf; 
		JButton btnInputOfResident; 
		JButton btnInputOfBuiltIn; 
		controller = _controller; 
		
		inputNode = _inputNode; 
		
		this.setBorder(MebnToolkit.getBorderForTabPanel("Input Node")); 
		
		jtbInputInstanceOf = new JToolBar(); 
		
		jlInputInstanceOf = new JLabel("Input of: ");
		jtbInputInstanceOf.setFloatable(false); 
		
		jtbInputInstanceOf.add(jlInputInstanceOf); 
		
		/* 
		 * No artigo original da Dr. Laskey, um n� de input pode ser instancia
		 * de um n� residente ou de uma built-in... Simplificado nesta vers�o
		 * apenas para residente.  
		 */
        //btnInputOfResident = new JButton("RES"); 
        //btnInputOfBuiltIn = new JButton("BUI"); 
		//jtbInputInstanceOf.add(btnInputOfResident); 
		//jtbInputInstanceOf.add(btnInputOfBuiltIn); 
		
		inputInstanceOfTree = new InputInstanceOfTree(controller); 
		inputInstanceOfTreeScroll = new JScrollPane(inputInstanceOfTree);
		inputInstanceOfTreeScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Resident List")); 
		
		if(_inputNode.getResidentNodePointer() != null){
		    argumentsPane = new ArgumentsTypedPane(inputNode,  _inputNode.getResidentNodePointer(), controller); 
		}
		else{
			argumentsPane = new JPanel(); 
		}
		
		setLayout(new BorderLayout());
		
		this.add(jtbInputInstanceOf, BorderLayout.NORTH); 
		this.add(argumentsPane, BorderLayout.SOUTH); 
		this.add(inputInstanceOfTreeScroll, BorderLayout.CENTER);
		
		this.setVisible(true);
		
	}
	
    
	public void updateArgumentPane(){
		this.remove(argumentsPane); 
		argumentsPane = new ArgumentsTypedPane(inputNode, inputNode.getResidentNodePointer(), controller);
		this.add(argumentsPane, BorderLayout.SOUTH); 
		validate(); 
		repaint(); 
	}

}
