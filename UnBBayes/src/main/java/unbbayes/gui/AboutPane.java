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
 * @author Rommel Novaes Carvalho (rommel.carvalho@gmail.com)
 */
public class AboutPane extends JFrame{

	private Color backgroundColor; 
	
	private String name = "UnBBayes 3 (MEBN)"; 
	private String version = "3.0.1"; 
	private String buildID = "20080229-01L"; 
	
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
			setResizable(false);
		}
		
		private JPanel buildHelpPanel(){
			JPanel helpPanel = new JPanel(); 
			helpPanel.setLayout(new BorderLayout()); 
			
			JTextArea textLicence = new JTextArea(4,9);
			textLicence.setText(
					" UnBBayes is free software: you can redistribute it and/or modify it\n" +
					" under the terms of the GNU General Public License as published by\n" +
					" the Free Software Foundation, either version 3 of the License, or\n" +
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
					
					"\nDanilo Custódio da Silva (danilocustodio@gmail.com)" +
					"\n\t- Learning Bayesian Network: K2, B, and V" +
					"\n\t- Incremental Learning\n" +
					
					"\nEduardo Andrade Rodrigues " +
					"\n\t- GUI in Delphi 5.0 for initial version\n" + 
					
					"\nGabriel M. N. Guimarães " +
					"\n\t- TAN and BAN algorithms\n" +
					
					"\nFrancisco José Fiuza Lima Jr " +
					"\n\t- Medical Metaphor GUI\n" + 
					
					"\nLaécio Lima dos Santos (laecio@gmail.com)" +
					"\n\t- MEBN/PR-OWL implementation\n"+
					
					"\nProf. Dr. Marcelo Ladeira (mladeira@unb.br)" +
					"\n\t- Coordination of the project\n"+
					
					"\nMário Henrique Paes Vieira" +
					"\n\t- Medical Metaphor" +
					"\n\t- Coordination of the integration of Java code \n" +
					
					"\nMichael Shigeki Onishi (mso@gmail.com)" +
					"\n\t- Bayesians Networks" +
					"\n\t- Influence Diagrams" +
					"\n\t- Multiple Sectioned Bayesian Network" +
					"\n\t- XML format and UnBBayes Server (J2EE)\n" +
					
					"\nProf. Dr. Paulo C. Costa (pcosta@gmu.edu)" +
					"\n\t- Consultant in MEBN and PR-OWL \n" +
					
					"\nRommel Novaes Carvalho (rommel.carvalho@gmail.com)" +
					"\n\t- Bayesians Networks" +
					"\n\t- Influence Diagrams" +
					"\n\t- Multiple Sectioned Bayesian Network" +
					"\n\t- XML format and UnBBayes Server (J2EE)" +
					"\n\t- MEBN/PR-OWL implementation\n"+
					
					"\nShou Matsumoto (cardialfly@gmail.com)" +
					"\n\t- MEBN/PR-OWL implementation\n" + 
					
					"\nProf. Dr. Wagner Teixeira da Silva" +
					"\n\t- Consultant in bayesian network learning"
			);
			
			collaboratorsPanel.setBackground(backgroundColor); 
			collaboratorsPanel.setEditable(false);
			collaboratorsPanel.setCaretPosition(0);
			
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
		Image imgLogo = tk.getImage(getClass().getResource(
				"/img/logo.png")); 
		
		public LogoPane(){
			super(); 
			setPreferredSize(new Dimension(200, 300)); 
		}
		
		public void paintComponent(Graphics comp){
			Graphics2D g2D = (Graphics2D)comp; 
			
			g2D.drawImage(imgLogo, 15, 20, 175, 150, this); 
			
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
