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
package unbbayes.metaphor.mebn;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class MEBNMetaphorFrame extends JFrame{
	
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;	
	
  MEBNMetaphorMainPanel jPanel1 = new MEBNMetaphorMainPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  public MEBNMetaphorFrame() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    this.setSize(screenSize);
    this.setTitle("MEBN Metaphor");

    this.getContentPane().setLayout(borderLayout1);
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    
    JMenuBar menu = new JMenuBar();
    JMenu file = new JMenu("File");
    file.setMnemonic(Character.getNumericValue('f'));
    JMenu view = new JMenu("View");
    file.setMnemonic(Character.getNumericValue('v'));
    JMenu help = new JMenu("Help");
    file.setMnemonic(Character.getNumericValue('h'));
    
    menu.add(file);
    menu.add(view);
    menu.add(help);
    this.setJMenuBar(menu);
  }

  //Overridden so we can exit when window is closed
  protected void processWindowEvent(WindowEvent e)
  {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING)
    {
      System.exit(0);
    }
  }

  public static void main(String[] args)
  {
    new MEBNMetaphorFrame().setVisible(true);
  }
}