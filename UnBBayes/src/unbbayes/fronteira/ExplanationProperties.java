/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package unbbayes.fronteira;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import unbbayes.jprs.jbn.*;
import java.awt.event.*;

public class ExplanationProperties extends JDialog
{
  private JPanel jPanel1 = new JPanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JTabbedPane jTabbedPane1 = new JTabbedPane();
  private JPanel jPanel2 = new JPanel();
  private JPanel descriptionPanel = new JPanel();
  private JPanel explanationPanel = new JPanel();
  private JButton jButton1 = new JButton();
  private Border border1;
  private JPanel jPanel5 = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JPanel jPanel7 = new JPanel();
  private BorderLayout borderLayout3 = new BorderLayout();
  private Border border2;
  private JPanel jPanel6 = new JPanel();
  private JPanel jPanel8 = new JPanel();
  private BorderLayout borderLayout4 = new BorderLayout();
  private GridLayout gridLayout1 = new GridLayout();
  private JPanel jPanel9 = new JPanel();
  private JPanel jPanel10 = new JPanel();
  private JPanel jPanel11 = new JPanel();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private BorderLayout borderLayout5 = new BorderLayout();
  private JTextArea jTextArea1 = new JTextArea();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private BorderLayout borderLayout6 = new BorderLayout();
  private BorderLayout borderLayout7 = new BorderLayout();
  private BorderLayout borderLayout8 = new BorderLayout();
  private JPanel jPanel3 = new JPanel();
  private GridLayout gridLayout2 = new GridLayout();
  private JPanel jPanel4 = new JPanel();
  private JPanel jPanel12 = new JPanel();
  private BorderLayout borderLayout9 = new BorderLayout();
  private BorderLayout borderLayout10 = new BorderLayout();
  private JPanel jPanel13 = new JPanel();
  private JPanel jPanel14 = new JPanel();
  private JPanel jPanel15 = new JPanel();
  private JLabel jLabel3 = new JLabel();
  private JComboBox jComboBox1 = new JComboBox();
  private BorderLayout borderLayout11 = new BorderLayout();
  private Border border3;
  private TitledBorder titledBorder1;
  private GridLayout gridLayout3 = new GridLayout();
  private JPanel jPanel17 = new JPanel();
  private JPanel jPanel18 = new JPanel();
  private JPanel jPanel19 = new JPanel();
  private JPanel jPanel20 = new JPanel();
  private JPanel jPanel21 = new JPanel();
  private JPanel jPanel22 = new JPanel();
  private JRadioButton jRadioButton1 = new JRadioButton();
  private JRadioButton jRadioButton2 = new JRadioButton();
  private JRadioButton jRadioButton3 = new JRadioButton();
  private JRadioButton jRadioButton4 = new JRadioButton();
  private JRadioButton jRadioButton5 = new JRadioButton();
  private BorderLayout borderLayout12 = new BorderLayout();
  private BorderLayout borderLayout13 = new BorderLayout();
  private BorderLayout borderLayout14 = new BorderLayout();
  private BorderLayout borderLayout15 = new BorderLayout();
  private BorderLayout borderLayout16 = new BorderLayout();
  private BorderLayout borderLayout17 = new BorderLayout();
  private ButtonGroup buttonGroup1 = new ButtonGroup();
  private Border border4;
  private JPanel jPanel24 = new JPanel();
  private JLabel jLabel4 = new JLabel();
  private BorderLayout borderLayout18 = new BorderLayout();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private BorderLayout borderLayout19 = new BorderLayout();
  private JTextArea jTextArea2 = new JTextArea();
  private NetWindow netWindow;

  public ExplanationProperties(NetWindow netWindow)
  { this.netWindow = netWindow;
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception
  { this.setSize(550, 470);
    border1 = BorderFactory.createEmptyBorder(0,10,0,10);
    border2 = BorderFactory.createEmptyBorder(20,20,20,20);
    border3 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Tipo de Evidência:");
    border4 = BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Tipo de Evidência:"),BorderFactory.createEmptyBorder(0,10,0,0));
    this.setResizable(false);
    this.setTitle("Propriedades da Variável de Explicação");
    jPanel1.setLayout(borderLayout1);
    jButton1.setText("OK");
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton1_actionPerformed(e);
      }
    });
    jTabbedPane1.setBorder(border1);
    descriptionPanel.setLayout(borderLayout2);
    jPanel5.setLayout(borderLayout3);
    jPanel5.setBorder(border2);
    jPanel7.setLayout(borderLayout4);
    jPanel8.setLayout(gridLayout1);
    gridLayout1.setRows(3);
    jPanel6.setLayout(borderLayout5);
    jLabel1.setText("Variável de explicação : ");
    jLabel2.setText("Descrição:");
    jPanel9.setLayout(borderLayout6);
    jPanel10.setLayout(borderLayout7);
    borderLayout4.setVgap(5);
    explanationPanel.setLayout(borderLayout8);
    jPanel3.setBorder(border2);
    jPanel3.setLayout(gridLayout2);
    gridLayout2.setRows(2);
    gridLayout2.setVgap(10);
    jPanel12.setLayout(borderLayout9);
    jPanel4.setLayout(borderLayout10);
    jLabel3.setText("Evidencia : ");
    jPanel14.setLayout(borderLayout11);
    jPanel13.setBorder(border4);
    jPanel13.setLayout(gridLayout3);
    gridLayout3.setColumns(3);
    gridLayout3.setHgap(10);
    gridLayout3.setRows(2);
    jRadioButton1.setText("Trigger");
    jRadioButton2.setText("Complementar");
    jRadioButton3.setText("N/A");
    jRadioButton4.setText("Essencial");
    jRadioButton5.setText("Excludente");
    jPanel22.setLayout(borderLayout12);
    jPanel21.setLayout(borderLayout13);
    jPanel20.setLayout(borderLayout14);
    jPanel19.setLayout(borderLayout15);
    jPanel18.setLayout(borderLayout16);
    jPanel17.setLayout(borderLayout17);
    jLabel4.setText("Texto para Explanação :");
    jPanel24.setLayout(borderLayout18);
    jPanel15.setLayout(borderLayout19);
    borderLayout10.setVgap(5);
    borderLayout9.setVgap(10);
    this.getContentPane().add(jPanel1,  BorderLayout.CENTER);
    jPanel1.add(jTabbedPane1,  BorderLayout.CENTER);
    jTabbedPane1.add(descriptionPanel, "Descrição");
    descriptionPanel.add(jPanel5, BorderLayout.CENTER);
    jPanel5.add(jPanel7,  BorderLayout.CENTER);
    jPanel7.add(jPanel8, BorderLayout.NORTH);
    jPanel8.add(jPanel9, null);
    jPanel9.add(jLabel1, BorderLayout.CENTER);
    jPanel8.add(jPanel11, null);
    jPanel8.add(jPanel10, null);
    jPanel10.add(jLabel2, BorderLayout.CENTER);
    jPanel7.add(jPanel6, BorderLayout.CENTER);
    jPanel6.add(jScrollPane1,  BorderLayout.CENTER);
    jScrollPane1.getViewport().add(jTextArea1, null);
    jTabbedPane1.add(explanationPanel, "Explanação");
    jPanel1.add(jPanel2,  BorderLayout.SOUTH);
    jPanel2.add(jButton1, null);
    explanationPanel.add(jPanel3,  BorderLayout.CENTER);
    jPanel3.add(jPanel12, null);
    jPanel12.add(jPanel13, BorderLayout.CENTER);
    jPanel13.add(jPanel22, null);
    jPanel22.add(jRadioButton1, BorderLayout.CENTER);
    jPanel13.add(jPanel21, null);
    jPanel21.add(jRadioButton2, BorderLayout.CENTER);
    jPanel13.add(jPanel20, null);
    jPanel20.add(jRadioButton3, BorderLayout.CENTER);
    jPanel13.add(jPanel19, null);
    jPanel19.add(jRadioButton4, BorderLayout.CENTER);
    jPanel13.add(jPanel18, null);
    jPanel18.add(jRadioButton5, BorderLayout.CENTER);
    jPanel13.add(jPanel17, null);
    jPanel12.add(jPanel14, BorderLayout.NORTH);
    jPanel14.add(jLabel3, BorderLayout.WEST);
    jPanel14.add(jComboBox1, BorderLayout.CENTER);
    jPanel3.add(jPanel4, null);
    jPanel4.add(jPanel15, BorderLayout.CENTER);
    jPanel15.add(jScrollPane2,  BorderLayout.CENTER);
    jScrollPane2.getViewport().add(jTextArea2, null);
    jPanel4.add(jPanel24, BorderLayout.NORTH);
    jPanel24.add(jLabel4, BorderLayout.CENTER);
    buttonGroup1.add(jRadioButton1);
    buttonGroup1.add(jRadioButton2);
    buttonGroup1.add(jRadioButton3);
    buttonGroup1.add(jRadioButton4);
    buttonGroup1.add(jRadioButton5);
  }

  public void setProbabilisticNode(ProbabilisticNode node)
  {
  }

  void jButton1_actionPerformed(ActionEvent e)
  {   dispose();
  }
}