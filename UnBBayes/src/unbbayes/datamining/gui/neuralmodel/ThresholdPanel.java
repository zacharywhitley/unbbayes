package unbbayes.datamining.gui.neuralmodel;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class ThresholdPanel extends JPanel{
  private JLabel labelMaxOrder = new JLabel();
  private JLabel labelConfidence = new JLabel();
  private Border border1;
  private TitledBorder titledBorder1;
  private Border border2;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel1 = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();
  private JLabel labelSupport = new JLabel();
  private JComboBox jComboBoxMaxOrder = new JComboBox();
  private JComboBox jComboBoxSupport = new JComboBox();
  private JComboBox jComboBoxConfidence = new JComboBox();

  public ThresholdPanel() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    border1 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Opções");
    border2 = BorderFactory.createCompoundBorder(titledBorder1,BorderFactory.createEmptyBorder(5,5,5,5));
    labelMaxOrder.setText("Ordem Máxima:");
    this.setLayout(borderLayout1);
    labelConfidence.setText("Confiança mínima:");
    jPanel1.setLayout(gridLayout1);
    gridLayout1.setColumns(2);
    gridLayout1.setRows(3);
    jPanel1.setBorder(border2);
    labelSupport.setText("Suporte mínimo:");
    this.add(jPanel1,  BorderLayout.CENTER);
    jPanel1.add(labelMaxOrder, null);
    jPanel1.add(jComboBoxMaxOrder, null);
    jPanel1.add(labelSupport, null);
    jPanel1.add(jComboBoxSupport, null);
    jPanel1.add(labelConfidence, null);
    jPanel1.add(jComboBoxConfidence, null);

    for(int i=1; i<12; i++){
      jComboBoxMaxOrder.addItem(new Integer(i));
    }

    for(int i=0; i<101; i++){
      jComboBoxSupport.addItem(new String(i + "%"));
      jComboBoxConfidence.addItem(new String(i + "%"));
    }

    jComboBoxMaxOrder.setSelectedIndex(5);
    jComboBoxSupport.setSelectedIndex(7);
    jComboBoxConfidence.setSelectedIndex(7);
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