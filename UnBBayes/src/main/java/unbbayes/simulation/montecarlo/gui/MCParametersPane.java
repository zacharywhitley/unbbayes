/*
 *  UnBBayes
 *  Copyright (C) 2002, 2009 Universidade de Brasilia - http://www.unb.br
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
package unbbayes.simulation.montecarlo.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 
 * Class which constructs a GUI for user interaction, related to sample generation of Monte Carlo methods.
 * 
 * @author Danilo Custódio (danilocustodio@gmail.com)
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 */
public class MCParametersPane extends JFrame {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.simulation.montecarlo.resources.MCResources");
	
	private JPanel mainPane; 
	private JPanel sampleSizePane;
	private JLabel sampleSizeLbl;
	private JTextField sampleSizeTxt;
	private JButton okBtn;
	
	
	public MCParametersPane(){
		super(resource.getString("mcTitle"));
		Container c = getContentPane();		
		c.add(createPane());		
		pack();
		setVisible(true);
	}
	
	private JPanel createPane(){
		mainPane = new JPanel(new BorderLayout());
		mainPane.add(getSampleSizePane());
		return mainPane;		
	}
	
	private JPanel getSampleSizePane(){
		sampleSizePane = new JPanel(new GridLayout(1,3,5,5));
		sampleSizeLbl = new JLabel(resource.getString("sampleSizeLbl"));
		sampleSizeTxt = new JTextField();		
		okBtn = new JButton(resource.getString("ok"));
		sampleSizePane.add(sampleSizeLbl);
		sampleSizePane.add(sampleSizeTxt);
		sampleSizePane.add(okBtn);
		return sampleSizePane;
	}	
	
	public String getSampleSize(){
		return sampleSizeTxt.getText();		
	}
	
	public void addOKListener(ActionListener al){
		okBtn.addActionListener(al);		
	}	
}
