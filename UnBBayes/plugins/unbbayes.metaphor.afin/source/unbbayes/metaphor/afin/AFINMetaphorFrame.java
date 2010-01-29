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
package unbbayes.metaphor.afin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import unbbayes.controller.IconController;
import unbbayes.gui.AboutPane;

public class AFINMetaphorFrame extends JFrame{
	
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;	
	
  
  private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
		  unbbayes.metaphor.afin.resources.AFINMetaphorResources.class.getName());

  
  AFINMetaphorMainPanel mainPanel = new AFINMetaphorMainPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  public AFINMetaphorFrame() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
	  
	IconController iconController = IconController.getInstance();
	  
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    this.setSize(screenSize);
    this.setTitle(resource.getString("UnBBayesMetaphor"));

    this.getContentPane().setLayout(borderLayout1);
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    
    JMenuBar menu = new JMenuBar();
    JMenu file = new JMenu(resource.getString("File"));
    file.setMnemonic(resource.getString("File").charAt(0));
    //JMenu view = new JMenu("View");
    //file.setMnemonic(Character.getNumericValue('v'));
    JMenu help = new JMenu(resource.getString("Help"));
    file.setMnemonic(resource.getString("Help").charAt(0));
    
    JMenuItem openItem = new JMenuItem(resource.getString("OpenItem"),
			iconController.getOpenIcon());
    openItem.addActionListener(new ActionListener() {;
		public void actionPerformed(ActionEvent e) {
			mainPanel.openButton_actionPerformed(e);
		};
    });
    file.add(openItem);
    
    JMenuItem exitItem = new JMenuItem(resource.getString("ExitItem"), 'X');
    exitItem.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		setVisible(false);
			dispose();
			System.exit(0);
		};
    });
    file.add(exitItem);
    
    JMenuItem aboutItem = new JMenuItem(resource.getString("AboutItem"));
    aboutItem.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
        	AboutPane abourPane = new AboutPane();
    		abourPane.pack();
    		abourPane.setVisible(true);
		};
    });
    help.add(aboutItem);
    
    menu.add(file);
    //menu.add(view);
    menu.add(help);
    this.setJMenuBar(menu);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }


  
  
}