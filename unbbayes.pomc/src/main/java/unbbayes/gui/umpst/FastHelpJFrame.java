package unbbayes.gui.umpst;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.JHelp;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class FastHelpJFrame {

	private final JFrame frame = new JFrame(); 
	private final Component father; 
	
	public FastHelpJFrame(String title, Component _father){
		
		father = _father; 
		
		try {
			HelpSet set = new HelpSet(null, getClass().getResource("/help/UMPHelp/ump.hs"));
			JHelp help = new JHelp(set);
			help.setCurrentID(title); 
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
			JPanel panelHelp = new JPanel(new BorderLayout()); 
			Border border = BorderFactory.createEtchedBorder(Color.RED, Color.black); 
//			panelHelp.setBorder(BorderFactory.createTitledBorder("Help")); 
			panelHelp.setBorder(border); 
			panelHelp.add(help.getContentViewer(),BorderLayout.CENTER); 
			
			JButton buttonClose = new JButton("CLOSE"); 
			buttonClose.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					frame.dispose(); 
				}
			}); 
			
			panelHelp.add(buttonClose, BorderLayout.PAGE_END); 
			
			frame.setContentPane(panelHelp);
			
//			frame.setUndecorated(true); 
			frame.pack();
			frame.setLocationRelativeTo(father);
			frame.setTitle("Help"); 
			frame.setVisible(true);
			frame.setAlwaysOnTop(true);
			
		} catch (HelpSetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}
	
	public void showHelp(){
//		frame.setUndecorated(true); 
		frame.pack();
		frame.setLocationRelativeTo(father);
		frame.setTitle("Help"); 
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
	}

	
}
