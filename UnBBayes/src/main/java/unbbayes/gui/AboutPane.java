package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

public class AboutPane extends JPanel{

	public AboutPane(){
		super(); 
		BorderLayout borderLayout = new BorderLayout(); 
		setLayout(borderLayout);
		add(new MainPane(), BorderLayout.CENTER); 
		add(new LogoPane(), BorderLayout.LINE_START); 
		
		setMinimumSize(new Dimension(400, 300)); 
		
	}
	
	public static void main(String... args){
		JFrame testFrame = new JFrame(); 
		testFrame.setContentPane(new AboutPane()); 
		testFrame.setVisible(true); 
		testFrame.setPreferredSize(new Dimension(600, 300)); 
		testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		testFrame.pack(); 
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
					" (at your option) any later version.");
			textLicence.setEditable(false); 
			textLicence.setForeground(new Color(60, 129, 121)); 
//			textLicence.setBackground(new Color(155, 210, 204)); 
			textLicence.setBorder(BorderFactory.createLoweredBevelBorder()); 
			
			helpPanel.add(textLicence, BorderLayout.NORTH); 
			helpPanel.add(new CollaboratorPane(), BorderLayout.CENTER); 
			
           JTextArea links = new JTextArea(4,9);
			
           links.setText(
					"Licença GNU\n" +
					"Fórum sugestões\n " +
					"Lista de Features\n" +
					"Histórico das versões\n"
		    );
			
           links.setEditable(false); 
//           links.setBackground(new Color(155, 210, 204)); 
           links.setForeground(new Color(60, 129, 121)); 
		   links.setBorder(BorderFactory.createLoweredBevelBorder()); 
           
			helpPanel.add(links, BorderLayout.SOUTH); 
			
			
			return helpPanel; 
		}
		
	}
	
	class CollaboratorPane extends JPanel{
		
		public CollaboratorPane(){
			
			super(); 
			this.setLayout(new BorderLayout()); 
			
			JTextArea collaboratorsPanel = new JTextArea();

			collaboratorsPanel.setText(
					"Danilo Custódio\n" +
					"Laécio Lima\n"+
					"Marcelo Ladeira\n"+
					"Michael Onishi\n"+
					"Paulo C. Costa\n"+
					"Rommel Carvalho\n"+
					"Shou Matsumoto"
			);
            collaboratorsPanel.setBackground(new Color(155, 210, 204)); 
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
		
		String name = "UnBBayes 3 (MEBN)"; 
		String version = "3.0.1"; 
		String buildID = "02212008-01L"; 
		
		public LogoPane(){
			super(); 
			setPreferredSize(new Dimension(200, 300)); 
		}
		
		public void paintComponent(Graphics comp){
			Graphics2D g2D = (Graphics2D)comp; 
			
			Image imgLogo = null;
			Toolkit tk = Toolkit.getDefaultToolkit(); 
			imgLogo = tk.getImage("img/logo.jpg"); 
			
			g2D.drawImage(imgLogo, 25, 25, 150, 150, this); 
			
			g2D.drawRect(20, 180, 160, 80); 
			g2D.drawString(name, 25, 200); 
			g2D.drawString("Version: " + version, 25, 220); 
			g2D.drawString("Build ID: " + buildID, 25, 235); 
		}
		
	}
}
