package unbbayes.datamining.gui.neuralmodel;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import unbbayes.datamining.datamanipulation.*;

public class InferencePanel extends JPanel {
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanel1 = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();
  private JPanel jPanel2 = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JButton buttonClassify = new JButton();
  private JPanel jPanel3 = new JPanel();
  private JPanel jPanel4 = new JPanel();
  private NeuralModelMain mainController;

  public InferencePanel() {
    try {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  void jbInit() throws Exception {
    this.setLayout(borderLayout1);
    jPanel1.setLayout(gridLayout1);
    gridLayout1.setColumns(2);
    gridLayout1.setHgap(5);
    jPanel2.setLayout(borderLayout2);
    buttonClassify.setText("Classificar");
    buttonClassify.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttonClassify_actionPerformed(e);
      }
    });
    this.add(jPanel1,  BorderLayout.CENTER);
    jPanel1.add(jPanel2, null);
    jPanel2.add(buttonClassify,  BorderLayout.SOUTH);
    jPanel2.add(jPanel3,  BorderLayout.CENTER);
    jPanel1.add(jPanel4, null);
  }

  void buttonClassify_actionPerformed(ActionEvent e) {
    short[] s = {0,0,1,0,0};
    Instance inst = new Instance(s);
    float[] res;
    res = mainController.classify(inst);

    for(int i=0; i<res.length;i++){
      System.out.println(res[i] + " ");
    }

    Utils.normalize(res);
    System.out.println("\n normalizado:");

    for(int i=0; i<res.length;i++){
      System.out.println(res[i] + " ");
    }


  }

  public void setMainController(NeuralModelMain mainController){
    this.mainController = mainController;
  }
}