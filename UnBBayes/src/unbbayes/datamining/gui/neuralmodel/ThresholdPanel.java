package unbbayes.datamining.gui.neuralmodel;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ThresholdPanel extends JPanel{
  private JLabel labelThreshold = new JLabel();
  private JLabel labelReliability = new JLabel();
  private JTextField jTextFieldReliability = new JTextField();
  private JTextField jTextFieldThreshold = new JTextField();
  private Border border1;
  private TitledBorder titledBorder1;
  private Border border2;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel1 = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();

  public ThresholdPanel(/*int numAtt /*, int maxAcc, int minAcc*/) {
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
    labelThreshold.setText("Ordem Máxima:");
    this.setLayout(borderLayout1);
    labelReliability.setText("Confiança:");
    jTextFieldReliability.setText("60");
    jTextFieldReliability.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        jTextField_keyPressed(e);
      }
    });
    jTextFieldThreshold.setText("3");
    jTextFieldThreshold.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        jTextField_keyPressed(e);
      }
    });
    jPanel1.setLayout(gridLayout1);
    gridLayout1.setColumns(2);
    gridLayout1.setRows(2);
    jPanel1.setBorder(border2);
    this.add(jPanel1,  BorderLayout.CENTER);
    jPanel1.add(labelThreshold, null);
    jPanel1.add(jTextFieldThreshold, null);
    jPanel1.add(labelReliability, null);
    jPanel1.add(jTextFieldReliability, null);
  }

  void jTextField_keyPressed(KeyEvent e) {
    int valor;

    int codigoTecla = e.getKeyCode();
    if (codigoTecla == KeyEvent.VK_UP || codigoTecla == KeyEvent.VK_DOWN){
      valor = Integer.parseInt(((JTextField)e.getSource()).getText());

      if (codigoTecla == KeyEvent.VK_UP){
        valor++;
        ((JTextField)e.getSource()).setText("" + valor);
      } else if (codigoTecla == KeyEvent.VK_DOWN){
        valor--;
        ((JTextField)e.getSource()).setText("" + valor);
      }
    }
  }

  public int getThreshold(){
    return Integer.parseInt(jTextFieldThreshold.getText());
  }

  public int getReliability(){
    return Integer.parseInt(jTextFieldReliability.getText());
  }
}