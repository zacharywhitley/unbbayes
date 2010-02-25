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
package unbbayes.datamining.gui.neuralmodel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ResourceBundle;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


/**
 *  Class that implements a panel with configuration options of the CNM.
 *
 *  @author Rafael Moraes Noivo
 *  @version $1.0 $ (02/16/2003)
 */
public class OptionsPanel extends JPanel{
	
  /** Serialization runtime version number */
  private static final long serialVersionUID = 0;	
	
  private ResourceBundle resource;
  private JLabel labelMaxOrder = new JLabel();
  private JLabel labelConfidence = new JLabel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel1 = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();
  private JLabel labelSupport = new JLabel();
  private JComboBox jComboBoxMaxOrder = new JComboBox();
  private JComboBox jComboBoxSupport = new JComboBox();
  private JComboBox jComboBoxConfidence = new JComboBox();
  private JPanel jPanel2 = new JPanel();
  private JPanel jPanel3 = new JPanel();
  private GridLayout gridLayout3 = new GridLayout();
  private JPanel jPanel4 = new JPanel();
  private GridLayout gridLayout4 = new GridLayout();
  private GridLayout gridLayout2 = new GridLayout();

  public OptionsPanel() {
    try {
      resource = unbbayes.util.ResourceController.newInstance().getBundle(
    		  unbbayes.datamining.gui.neuralmodel.resources.NeuralModelResource.class.getName());
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    labelMaxOrder.setHorizontalAlignment(SwingConstants.RIGHT);
    labelMaxOrder.setHorizontalTextPosition(SwingConstants.TRAILING);
    labelMaxOrder.setText(resource.getString("maximumOrder"));
    this.setLayout(borderLayout1);
    labelConfidence.setHorizontalAlignment(SwingConstants.RIGHT);
    labelConfidence.setText(resource.getString("minimumConfidence"));
    jPanel1.setLayout(gridLayout1);
    gridLayout1.setColumns(6);
    gridLayout1.setHgap(15);
    labelSupport.setHorizontalAlignment(SwingConstants.RIGHT);
    labelSupport.setHorizontalTextPosition(SwingConstants.TRAILING);
    labelSupport.setText(resource.getString("minimumSupport"));
    jPanel2.setLayout(gridLayout2);
    jPanel3.setLayout(gridLayout3);
    gridLayout3.setColumns(2);
    jPanel4.setLayout(gridLayout4);
    gridLayout4.setColumns(2);
    this.add(jPanel1,  BorderLayout.CENTER);
    jPanel1.add(jPanel2, null);
    jPanel2.add(labelMaxOrder, null);
    jPanel2.add(jComboBoxMaxOrder, null);
    jPanel1.add(jPanel3, null);
    jPanel3.add(labelSupport, null);
    jPanel3.add(jComboBoxSupport, null);
    jPanel1.add(jPanel4, null);
    jPanel4.add(labelConfidence, null);
    jPanel4.add(jComboBoxConfidence, null);

    for(int i=1; i<12; i++){
      jComboBoxMaxOrder.addItem(new Integer(i));
    }

    for(int i=0; i<101; i++){
      jComboBoxSupport.addItem(new String(i + "%"));
      jComboBoxConfidence.addItem(new String(i + "%"));
    }

    jComboBoxMaxOrder.setSelectedIndex(2);
    jComboBoxSupport.setSelectedIndex(7);
    jComboBoxConfidence.setSelectedIndex(60);

    jComboBoxMaxOrder.setEnabled(false);
    jComboBoxSupport.setEnabled(false);
    jComboBoxConfidence.setEnabled(false);
  }

  /**
   * Used to enable or disable the combo boxes.
   *
   * @param enable <code>true</code> to enable the combo box;
   *               <code>false</code> otherwise.
   * @see {@link jComboBox}
   */
  public void enableCombos(boolean enable){
    jComboBoxMaxOrder.setEnabled(enable);
    jComboBoxSupport.setEnabled(enable);
    jComboBoxConfidence.setEnabled(enable);
  }

  /**
   * Return the maximum order of combinations entered by the user.
   *
   * @return the maximum order of combinations.
   */
  public int getMaxOrder(){
    return ((Integer)jComboBoxMaxOrder.getSelectedItem()).intValue();
  }

  /**
   * Return the minimum confidence entered by the user.
   *
   * @return the minimum confidence
   */
  public int getConfidence(){
    return jComboBoxConfidence.getSelectedIndex();
  }

  /**
   * Return the minimum support entered by the user.
   *
   * @return the minimum support
   */
  public int getSupport(){
    return jComboBoxSupport.getSelectedIndex();
  }
}