package unbbayes.datamining.gui.neuralmodel;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class OptionsPanel extends JPanel{
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
      resource = ResourceBundle.getBundle("unbbayes.datamining.gui.neuralmodel.resources.NeuralModelResource");
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    labelMaxOrder.setText(resource.getString("maximumOrder"));
    this.setLayout(borderLayout1);
    labelConfidence.setText(resource.getString("minimumConfidence"));
    jPanel1.setLayout(gridLayout1);
    gridLayout1.setColumns(6);
    gridLayout1.setHgap(15);
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

  public void enableCombos(boolean enable){
    jComboBoxMaxOrder.setEnabled(enable);
    jComboBoxSupport.setEnabled(enable);
    jComboBoxConfidence.setEnabled(enable);
  }

  public int getMaxOrder(){
    return ((Integer)jComboBoxMaxOrder.getSelectedItem()).intValue();
  }

  public int getConfidence(){
    return jComboBoxConfidence.getSelectedIndex();
  }

  public int getSupport(){
    return jComboBoxSupport.getSelectedIndex();
  }
}