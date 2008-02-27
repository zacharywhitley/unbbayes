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
package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.Border;

/**
 * About pane with informations of the program. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class AboutPane extends JFrame{

	private Color backgroundColor; 
	
	private String name = "UnBBayes 3 (MEBN)"; 
	private String version = "3.0.1"; 
	private String buildID = "02212008-01L"; 
	
	public AboutPane(){
		super("About"); 
		
		this.setLocation(GUIUtils.getCenterPositionForComponent(400,300));		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		BorderLayout borderLayout = new BorderLayout(); 
		setLayout(borderLayout);
		add(new MainPane(), BorderLayout.CENTER); 
		add(new LogoPane(), BorderLayout.LINE_START); 
		
		setMinimumSize(new Dimension(400, 300)); 
		setMaximumSize(new Dimension(400, 300));
		pack(); 
		backgroundColor = getBackground(); 
	}
	
	class MainPane extends JPanel{
		
		public MainPane(){
			super();
			setLayout(new BorderLayout()); 
			add(buildHelpPanel(), BorderLayout.NORTH); 
			setPreferredSize(new Dimension(400, 300)); 
		}
		
		private JPanel buildHelpPanel(){
			JPanel helpPanel = new JPanel(); 
			helpPanel.setLayout(new BorderLayout()); 
			
			JTextArea textLicence = new JTextArea(4,9);
			textLicence.setText(
					" UnbBayes is free software; you can redistribute it and/or modify it \n" +
					" under the terms of the GNU General Public License as published by\n" +
					" the Free Software Foundation; either version 2 of the License, or \n" +
					" (at your option) any later version."
					);
			textLicence.setEditable(false); 
			textLicence.setBackground(backgroundColor); 
			textLicence.setForeground(Color.black); 
			
			Border etched = BorderFactory.createLoweredBevelBorder(); 
			Border empty = BorderFactory.createEmptyBorder(5, 5, 5, 5); 
			Border compound = BorderFactory.createCompoundBorder(empty, etched); 
			textLicence.setBorder(compound); 
			
			helpPanel.add(textLicence, BorderLayout.NORTH); 
			helpPanel.add(new CollaboratorPane(), BorderLayout.CENTER); 
			
			helpPanel.add(new InformationPane(), BorderLayout.SOUTH); 
			
			
			return helpPanel; 
		}
		
	}
	
	class InformationPane extends JPanel{
		
		JButton btnLicence; 
		JButton btnFeatures; 
		JButton btnHistoric; 
		
		public InformationPane(){
			
			setLayout(new BorderLayout()); 
			this.setPreferredSize(new Dimension(100, 50)); 
			
			btnLicence = new JButton("Read Licence"); 
			btnLicence.setEnabled(false); 
			btnFeatures = new JButton("Features");
			btnFeatures.setEnabled(false); 
			btnHistoric = new JButton("Version History"); 
			btnHistoric.setEnabled(false); 
			
			JToolBar jtb = new JToolBar();
			jtb.setLayout(new FlowLayout(FlowLayout.TRAILING)); 
			jtb.add(btnLicence); 
			jtb.add(btnFeatures); 
			jtb.add(btnHistoric); 
			jtb.setFloatable(false); 
			
			add(jtb, BorderLayout.NORTH); 
			
			
		}
		
	}
	
	class CollaboratorPane extends JPanel{
		
		public CollaboratorPane(){
			
			super(); 
			this.setPreferredSize(new Dimension(100, 150)); 
			this.setLayout(new BorderLayout()); 
			
			JTextArea collaboratorsPanel = new JTextArea();

			collaboratorsPanel.setText(
					"Alberto Magno Muniz Soares " +
					"\n\t- Initial version in Delphi 5.0\n" + 
					
					"\nBruno Gonçalves Domingues " +
					"\n\t- Project of the XML format of probabilistic networks\n" +
					
					"\nCleuber Moreira Fernandes " +
					"\n\t- Implementation of XPC algorithm\n" +
					
					"\nDanilo Custódio da Silva " +
					"\n\t- Learning Bayesian Network: K2, B, V" +
					"\n\t- Incremental Learning\n" +
					
					"\nEduardo Andrade Rodrigues " +
					"\n\t- GUI in Delphi 5.0 for Initial version\n" + 
					
					"\nGabriel M. N. Guimarães " +
					"\n\t- TAN and BAN algorithms\n" +
					
					"\nFrancisco José Fiuza Lima Jr " +
					"\n\t- GUI for Metaphor Medical\n" + 
					
					"\nLaécio Lima dos Santos " +
					"\n\t- Implementation of MEBN/Pr-OWL support\n"+
					
					"\nProf. Marcelo Ladeira " +
					"\n\t- Coordination of the project\n"+
					
					"\nMário Henrique Paes Vieira" +
					"\n\t- Metaphor Medical" +
					"\n\t- Coordination of the integration of Java code \n" +
					
					"\nMichael Shigeki Onishi " +
					"\n\t- Bayesians Networks" +
					"\n\t- Diagrams of Influences and MSBN\n"+
					
					"\nProf. Paulo C. Costa" +
					"\n\t- Consultant in MEBN and Pr-OWL \n"+
					
					"\nRommel Novaes Carvalho" +
					"\n\t- Bayesianas Networks, Diagrams of Influences" +
					"\n\t- XML format and UnBBayes Server (J2EE)" +
					"\n\t- Implementation of MEBN/Pr-OWL support\n"+
					
					"\nShou Matsumoto" +
					"\n\t- Implementation of MEBN/Pr-OWL support\n" + 
					
					"\nProf. Wagner Teixeira da Silva" +
					"\n\t- Consultant in bayesian network learning"
			);
			
			collaboratorsPanel.setBackground(backgroundColor); 
			collaboratorsPanel.setEditable(false); 
			
  	        JScrollPane scrollPane =
  	            new JScrollPane(collaboratorsPanel,
  	                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
  	                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
  	        scrollPane.setBorder(BorderFactory.createTitledBorder("Collaborators")); 
			this.add(scrollPane, BorderLayout.CENTER); 

		}
	}
	
	class LogoPane extends JPanel{

		Toolkit tk = Toolkit.getDefaultToolkit(); 
		Image imgLogo = imgLogo = tk.getImage("resources/img/logo.jpg"); 
		
		public LogoPane(){
			super(); 
			setPreferredSize(new Dimension(200, 300)); 
		}
		
		public void paintComponent(Graphics comp){
			Graphics2D g2D = (Graphics2D)comp; 
			
			g2D.drawImage(imgLogo, 25, 20, 150, 150, this); 
			
			g2D.drawRect(20, 175, 160, 80); 
			g2D.drawString(name, 25, 200); 
			g2D.drawString("Version: " + version + " (alpha)", 25, 220); 
			g2D.drawString("Build ID: " + buildID, 25, 235); 
		}
		
	}

	//tests. 
	public static void main(String... args){
		JFrame testFrame = new AboutPane(); 
		testFrame.setVisible(true); 
		testFrame.setPreferredSize(new Dimension(600, 300)); 
		testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		testFrame.pack(); 
	}
	
}
