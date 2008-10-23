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
package unbbayes.monteCarlo.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 
 * Class which constructs a screen for user interaction, related to sample generation of montecarlo method
 * 
 * @author Danilo
 */
public class MCParametersPane extends JFrame {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
	private JPanel painelPrincipal; 
	private JPanel painelNCasos;
	private JLabel lNCasos;
	private JTextField txtNCasos;
	private JButton btnOK;
	
	
	public MCParametersPane(){
		super("Monte Carlo Simulation");
		Container c = getContentPane();		
		c.add(criarTela());		
		pack();
		setVisible(true);
	}
	
	private JPanel criarTela(){
		painelPrincipal = new JPanel(new BorderLayout());
		painelPrincipal.add(getPainelNCasos());
		return painelPrincipal;		
	}
	
	private JPanel getPainelNCasos(){
		painelNCasos = new JPanel(new GridLayout(1,3,5,5));
		lNCasos = new JLabel("Number of cases : ");
		txtNCasos = new JTextField();		
		btnOK = new JButton("OK");
		painelNCasos.add(lNCasos);
		painelNCasos.add(txtNCasos);
		painelNCasos.add(btnOK);
		return painelNCasos;
	}	
	
	public String getNumeroCasos(){
		return txtNCasos.getText();		
	}
	
	public void adicionaOKListener(ActionListener al){
		btnOK.addActionListener(al);		
	}	
}
