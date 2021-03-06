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
package unbbayes.metaphor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class MetaphorFrame extends JFrame{
	
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;	
	
  private MetaphorMainPanel mainPanel = new MetaphorMainPanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  public MetaphorFrame() {
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
    this.setTitle("Metáfora Médica");

    this.getContentPane().setLayout(borderLayout1);
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public static void main(String[] args)
  {
    new MetaphorFrame().setVisible(true);
  }
/**
 * @return the mainPanel
 */
public MetaphorMainPanel getMainPanel() {
	return mainPanel;
}
/**
 * @param mainPanel the mainPanel to set
 */
public void setMainPanel(MetaphorMainPanel mainPanel) {
	this.mainPanel = mainPanel;
}
}