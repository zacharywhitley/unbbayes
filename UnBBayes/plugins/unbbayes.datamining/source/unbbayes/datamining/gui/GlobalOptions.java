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
package unbbayes.datamining.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import unbbayes.datamining.datamanipulation.Options;

public class GlobalOptions extends JInternalFrame
{
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;		
	
  private JPanel contentPane;
  private JPanel jPanel1 = new JPanel();
  private GridLayout gridLayout2 = new GridLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel7 = new JPanel();
  private BorderLayout borderLayout5 = new BorderLayout();
  private BorderLayout borderLayout3 = new BorderLayout();
  private BorderLayout borderLayout7 = new BorderLayout();
  private JPanel jPanel6 = new JPanel();
  private JLabel defaultLanguageLabel = new JLabel();
  private JPanel defaultLookNFeelLabelPanel = new JPanel();
  private JLabel defaultLookNFeelLabel = new JLabel();
  private JPanel jPanel4 = new JPanel();
  private JLabel jLabel1 = new JLabel();
  private JPanel jPanel3 = new JPanel();
  private JPanel jPanel2 = new JPanel();
  private JPanel jPanel8 = new JPanel();
  private JButton jButton1 = new JButton();
  private JButton jButton2 = new JButton();
  private GridLayout gridLayout1 = new GridLayout();
  private GridLayout gridLayout3 = new GridLayout();
  private JPanel jPanel12 = new JPanel();
  private JPanel jPanel13 = new JPanel();
  private JPanel lookNFeelComboBoxPanel = new JPanel();
  private JComboBox lookNFeelComboBox = new JComboBox();
  private BorderLayout borderLayout4 = new BorderLayout();
  private JPanel jPanel15 = new JPanel();
  private JPanel jPanel16 = new JPanel();
  private JPanel jPanel17 = new JPanel();
  private JComboBox defaultLanguageComboBox = new JComboBox();
  private GridLayout gridLayout4 = new GridLayout();
  private BorderLayout borderLayout6 = new BorderLayout();
  private JPanel jPanel18 = new JPanel();
  private JPanel jPanel19 = new JPanel();
  private JLabel jLabel4 = new JLabel();
  private BorderLayout borderLayout8 = new BorderLayout();
  private GridLayout gridLayout5 = new GridLayout();
  private JTextField jTextField1 = new JTextField();
  private JPanel jPanel9 = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JPanel jPanel11 = new JPanel();
  private JPanel jPanel10 = new JPanel();
  private JPanel jPanel20 = new JPanel();
  private JPanel jPanel21 = new JPanel();
  private JPanel jPanel22 = new JPanel();
  private BorderLayout borderLayout9 = new BorderLayout();
  private JTextField jTextField2 = new JTextField();

  public GlobalOptions()
  {   super("Global Options",false,true,true,true);
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      try
      {   jbInit();
      }
      catch(Exception e)
      {   e.printStackTrace();
      }
  }

  /**Component initialization*/
  private void jbInit() throws Exception
  { contentPane = (JPanel) this.getContentPane();
    contentPane.setLayout(borderLayout1);
    this.setSize(400,300);
    jPanel1.setLayout(gridLayout2);
    gridLayout2.setColumns(2);
    gridLayout2.setHgap(5);
    gridLayout2.setRows(4);
    gridLayout2.setVgap(5);
    jPanel7.setLayout(borderLayout7);
    jPanel6.setLayout(gridLayout1);
    defaultLanguageLabel.setText("Default Language");
    defaultLookNFeelLabelPanel.setLayout(borderLayout3);
    defaultLookNFeelLabel.setText("Default Look and Feel");
    jPanel4.setLayout(gridLayout3);
    jLabel1.setText("Maximum number of states allowed");
    jPanel3.setLayout(borderLayout5);
    jPanel2.setLayout(gridLayout4);
    jButton1.setMaximumSize(new Dimension(80, 27));
    jButton1.setMinimumSize(new Dimension(80, 27));
    jButton1.setPreferredSize(new Dimension(80, 27));
    jButton1.setMnemonic('C');
    jButton1.setText("Cancel");
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton1_actionPerformed(e);
      }
    });
    jButton2.setMaximumSize(new Dimension(80, 27));
    jButton2.setMinimumSize(new Dimension(80, 27));
    jButton2.setPreferredSize(new Dimension(80, 27));
    jButton2.setMnemonic('O');
    jButton2.setText("OK");
    jButton2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton2_actionPerformed(e);
      }
    });
    gridLayout1.setRows(3);
    gridLayout3.setRows(3);
    lookNFeelComboBoxPanel.setLayout(borderLayout4);
    gridLayout4.setRows(3);
    jPanel16.setLayout(borderLayout6);
    jLabel4.setText("Minimum amount of instances to show confidence intervals");
    jPanel18.setLayout(borderLayout8);
    jPanel19.setLayout(gridLayout5);
    gridLayout5.setRows(3);
    jTextField1.setText("40");
    jTextField1.setHorizontalAlignment(SwingConstants.RIGHT);
    jPanel11.setLayout(borderLayout2);
    jPanel22.setLayout(borderLayout9);
    jTextField2.setText("100");
    jTextField2.setHorizontalAlignment(SwingConstants.RIGHT);
    contentPane.add(jPanel1, BorderLayout.CENTER);
    jPanel1.add(jPanel7, null);
    jPanel7.add(jLabel1, BorderLayout.CENTER);
    jPanel1.add(jPanel19, null);
    jPanel19.add(jPanel9, null);
    jPanel19.add(jPanel11, null);
    jPanel11.add(jTextField1, BorderLayout.CENTER);
    jPanel19.add(jPanel10, null);
    jPanel1.add(jPanel18, null);
    jPanel18.add(jLabel4, BorderLayout.CENTER);
    jPanel1.add(jPanel6, null);
    jPanel6.add(jPanel20, null);
    jPanel6.add(jPanel22, null);
    jPanel22.add(jTextField2,  BorderLayout.CENTER);
    jPanel6.add(jPanel21, null);
    jPanel1.add(defaultLookNFeelLabelPanel, null);
    defaultLookNFeelLabelPanel.add(defaultLookNFeelLabel, BorderLayout.CENTER);
    jPanel1.add(jPanel4, null);
    jPanel4.add(jPanel13, null);
    jPanel4.add(lookNFeelComboBoxPanel, null);
    lookNFeelComboBoxPanel.add(lookNFeelComboBox, BorderLayout.CENTER);
    jPanel4.add(jPanel12, null);
    jPanel1.add(jPanel3, null);
    jPanel3.add(defaultLanguageLabel, BorderLayout.CENTER);
    jPanel1.add(jPanel2, null);
    jPanel2.add(jPanel17, null);
    jPanel2.add(jPanel16, null);
    jPanel16.add(defaultLanguageComboBox, BorderLayout.CENTER);
    jPanel2.add(jPanel15, null);
    contentPane.add(jPanel8,  BorderLayout.SOUTH);
    jPanel8.add(jButton2, null);
    jPanel8.add(jButton1, null);
    lookNFeelComboBox.addItem("Metal");
    lookNFeelComboBox.addItem("Motif");
    lookNFeelComboBox.addItem("Windows");
    defaultLanguageComboBox.addItem("English");
    defaultLanguageComboBox.addItem("Portuguese");
  }

  public void setDefaultOptions(int states,int confidence,String language,String laf)
  { jTextField1.setText(""+states);
    jTextField2.setText(""+confidence);
    lookNFeelComboBox.setSelectedItem(laf);
    defaultLanguageComboBox.setSelectedItem(language);
  }

  void jButton2_actionPerformed(ActionEvent e)
  {   try
      {   int numStates = Integer.parseInt(jTextField1.getText());
          int confidenceLimit = Integer.parseInt(jTextField2.getText());
          Options.getInstance().setNumberStatesAllowed(numStates);
          Options.getInstance().setConfidenceLimit(confidenceLimit);
          PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("DataMining.ini")),true);
          pw.println("[data mining]");
          pw.println("Maximum states = "+numStates);
          pw.println("Confidence limit = "+confidenceLimit);
          pw.println("Language = "+defaultLanguageComboBox.getSelectedItem().toString());
          pw.println("Look and Feel = "+lookNFeelComboBox.getSelectedItem().toString());
          dispose();
      }
      catch (IOException ioe)
      {   JOptionPane.showConfirmDialog(this,"Error writing file = "+ioe.getMessage(),"Error",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
      }
      catch (Exception ex)
      {   JOptionPane.showConfirmDialog(this,"Error = "+ex.getMessage(),"Error",JOptionPane.CLOSED_OPTION,JOptionPane.ERROR_MESSAGE);
      }
  }

  void jButton1_actionPerformed(ActionEvent e)
  {   dispose();
  }

/**
 * @return the defaultLookNFeelLabelPanel
 */
public JPanel getDefaultLookNFeelLabelPanel() {
	return defaultLookNFeelLabelPanel;
}

/**
 * @param defaultLookNFeelLabelPanel the defaultLookNFeelLabelPanel to set
 */
public void setDefaultLookNFeelLabelPanel(JPanel defaultLookNFeelLabelPanel) {
	this.defaultLookNFeelLabelPanel = defaultLookNFeelLabelPanel;
}

/**
 * @return the lookNFeelComboBoxPanel
 */
public JPanel getLookNFeelComboBoxPanel() {
	return lookNFeelComboBoxPanel;
}

/**
 * @param lookNFeelComboBoxPanel the lookNFeelComboBoxPanel to set
 */
public void setLookNFeelComboBoxPanel(JPanel lookNFeelComboBoxPanel) {
	this.lookNFeelComboBoxPanel = lookNFeelComboBoxPanel;
}

/**
 * @return the defaultLanguageLabel
 */
public JLabel getDefaultLanguageLabel() {
	return defaultLanguageLabel;
}

/**
 * @param defaultLanguageLabel the defaultLanguageLabel to set
 */
public void setDefaultLanguageLabel(JLabel defaultLanguageLabel) {
	this.defaultLanguageLabel = defaultLanguageLabel;
}

/**
 * @return the defaultLanguageComboBox
 */
public JComboBox getDefaultLanguageComboBox() {
	return defaultLanguageComboBox;
}

/**
 * @param defaultLanguageComboBox the defaultLanguageComboBox to set
 */
public void setDefaultLanguageComboBox(JComboBox defaultLanguageComboBox) {
	this.defaultLanguageComboBox = defaultLanguageComboBox;
}

/**
 * @return the lookNFeelComboBox
 */
public JComboBox getLookNFeelComboBox() {
	return lookNFeelComboBox;
}

/**
 * @param lookNFeelComboBox the lookNFeelComboBox to set
 */
public void setLookNFeelComboBox(JComboBox lookNFeelComboBox) {
	this.lookNFeelComboBox = lookNFeelComboBox;
}

/**
 * @return the defaultLookNFeelLabel
 */
public JLabel getDefaultLookNFeelLabel() {
	return defaultLookNFeelLabel;
}

/**
 * @param defaultLookNFeelLabel the defaultLookNFeelLabel to set
 */
public void setDefaultLookNFeelLabel(JLabel defaultLookNFeelLabel) {
	this.defaultLookNFeelLabel = defaultLookNFeelLabel;
}
}